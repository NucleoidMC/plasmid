package xyz.nucleoid.plasmid.game.common.rust.network.connection;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.WriteTimeoutHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import xyz.nucleoid.plasmid.game.common.rust.network.message.RustGameMessage;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class RustSocketConnection extends SimpleChannelInboundHandler<ByteBuf> implements RustGameConnection {
    private static final EventLoopGroup EVENT_LOOP_GROUP = new NioEventLoopGroup(
            1,
            new ThreadFactoryBuilder()
                    .setNameFormat("rewrite-it-in-rust")
                    .setDaemon(true)
                    .build()
    );

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int TIMEOUT_SECONDS = 5;

    private static final int MAX_FRAME_SIZE = 4 * 1024 * 1024;
    private static final int FRAME_HEADER_SIZE = 4;

    private static final Gson GSON = new Gson();

    private final Handler handler;

    private final ConcurrentLinkedQueue<ByteBuf> writeQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean scheduledWrite = new AtomicBoolean(false);

    private Channel channel;

    private RustSocketConnection(Handler handler) {
        this.handler = handler;
    }

    public static CompletableFuture<RustSocketConnection> connect(SocketAddress address, Handler handler) {
        var connection = new RustSocketConnection(handler);

        var bootstrap = new Bootstrap();
        bootstrap.group(EVENT_LOOP_GROUP);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.remoteAddress(address);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel channel) {
                channel.pipeline()
                        .addLast(new WriteTimeoutHandler(TIMEOUT_SECONDS, TimeUnit.SECONDS))
                        .addLast(new LengthFieldBasedFrameDecoder(MAX_FRAME_SIZE, 0, FRAME_HEADER_SIZE, 0, FRAME_HEADER_SIZE))
                        .addLast(new LengthFieldPrepender(FRAME_HEADER_SIZE))
                        .addLast(connection);
            }
        });

        var future = new CompletableFuture<RustSocketConnection>();
        bootstrap.connect().addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                connection.channel = channelFuture.channel();
                future.complete(connection);
                connection.handler.acceptConnection();
            } else {
                Throwable cause = channelFuture.cause();
                future.completeExceptionally(cause);
                connection.handler.acceptError(cause);
            }
        });

        return future;
    }

    @Override
    public boolean send(RustGameMessage message) {
        JsonObject payload = this.buildPayload(message);
        if (payload == null) {
            return false;
        }

        var bytes = GSON.toJson(payload).getBytes(StandardCharsets.UTF_8);
        this.writeQueue.add(Unpooled.wrappedBuffer(bytes));

        if (this.scheduledWrite.compareAndSet(false, true)) {
            EVENT_LOOP_GROUP.execute(this::writeQueued);
        }

        return true;
    }

    @Nullable
    private JsonObject buildPayload(RustGameMessage message) {
        final Identifier id = RustGameMessage.REGISTRY.getIdentifier(message.getCodec());
        if (id == null) {
            return null;
        }

        var body = message.encode(JsonOps.INSTANCE);
        if (body.isEmpty()) {
            return null;
        }

        var payload = new JsonObject();
        payload.addProperty("type", id.toString());
        payload.add("body", body.get());
        return payload;
    }

    private void writeQueued() {
        this.scheduledWrite.set(false);

        var writeQueue = this.writeQueue;
        if (!writeQueue.isEmpty()) {
            var channel = this.channel;

            ByteBuf message;
            while ((message = writeQueue.poll()) != null) {
                var future = channel.write(message);
                future.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            }

            channel.flush();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf message) {
        var json = JsonParser.parseString(message.toString(StandardCharsets.UTF_8)).getAsJsonObject();
        var id = new Identifier(json.get("type").getAsString());
        var body = json.getAsJsonObject("body");

        final Codec<? extends RustGameMessage> messageCodec = RustGameMessage.REGISTRY.get(id);
        if (messageCodec == null) {
            LOGGER.error("Unknown message id received {}", id);
            return;
        }

        final DataResult<? extends RustGameMessage> parse = messageCodec.parse(JsonOps.INSTANCE, body);
        parse.result().ifPresent(this.handler::acceptMessage);
        parse.error().ifPresent(result -> LOGGER.error("Malformed message {}", result.message()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        this.handler.acceptError(cause);
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        this.handler.acceptClosed();
    }
}

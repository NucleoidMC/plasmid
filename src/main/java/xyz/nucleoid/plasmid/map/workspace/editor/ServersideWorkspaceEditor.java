package xyz.nucleoid.plasmid.map.workspace.editor;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.map.workspace.MapWorkspace;
import xyz.nucleoid.plasmid.map.workspace.WorkspaceRegion;
import xyz.nucleoid.plasmid.map.workspace.trace.PartialRegion;
import xyz.nucleoid.plasmid.map.workspace.trace.RegionTraceMode;
import xyz.nucleoid.plasmid.util.BlockBounds;

public final class ServersideWorkspaceEditor implements WorkspaceEditor {
    private static final int PARTICLE_INTERVAL = 10;

    private final ServerPlayerEntity player;
    private final MapWorkspace workspace;

    private RegionTraceMode traceMode = RegionTraceMode.OFFSET;
    private PartialRegion tracing;
    private BlockBounds traced;

    private final ArmorStandEntity markerEntity;
    private final Int2IntMap markerTagIds = new Int2IntOpenHashMap();

    private int nextMarkerId = -1;

    public ServersideWorkspaceEditor(ServerPlayerEntity player, MapWorkspace workspace) {
        this.player = player;
        this.workspace = workspace;

        ArmorStandEntity markerEntity = new ArmorStandEntity(EntityType.ARMOR_STAND, player.world);
        markerEntity.setInvisible(true);
        markerEntity.setInvulnerable(true);
        markerEntity.setNoGravity(true);
        markerEntity.setMarker(true);
        markerEntity.setCustomNameVisible(true);
        this.markerEntity = markerEntity;

        this.markerTagIds.defaultReturnValue(Integer.MAX_VALUE);
    }

    @Override
    public void tick() {
        if (this.player.age % PARTICLE_INTERVAL == 0) {
            this.renderWorkspaceBounds();
            this.renderTracingBounds();
        }

        if (this.tracing != null && this.player.age % 5 == 0) {
            BlockPos pos = this.traceMode.tryTrace(this.player);
            if (pos != null) {
                this.tracing.setTarget(pos);
            }
        }
    }

    @Override
    public boolean useRegionItem() {
        if (!this.player.isSneaking()) {
            this.updateTrace();
        } else {
            this.changeTraceMode();
        }
        return true;
    }

    @Override
    @Nullable
    public BlockBounds takeTracedRegion() {
        BlockBounds traced = this.traced;
        this.traced = null;
        return traced;
    }

    private void updateTrace() {
        BlockPos pos = this.traceMode.tryTrace(this.player);
        if (pos != null) {
            PartialRegion tracing = this.tracing;
            if (tracing != null) {
                tracing.setTarget(pos);
                this.traced = tracing.asComplete();
                this.tracing = null;
                this.player.sendMessage(new LiteralText("Use /map region commit <name> to add this region"), true);
            } else {
                this.tracing = new PartialRegion(pos);
            }
        }
    }

    private void changeTraceMode() {
        RegionTraceMode nextMode = this.traceMode.next();
        this.traceMode = nextMode;
        this.player.sendMessage(new LiteralText("Changed trace mode to: ").append(nextMode.getName()), true);
    }

    @Override
    public void addRegion(WorkspaceRegion region) {
        int markerEntityId = this.nextMarkerId();

        Vec3d markerPos = region.bounds.getCenter();

        ArmorStandEntity markerEntity = this.markerEntity;
        markerEntity.setEntityId(markerEntityId);
        markerEntity.setPos(markerPos.x, markerPos.y, markerPos.z);
        markerEntity.setCustomName(new LiteralText(region.marker));

        ServerPlayNetworkHandler networkHandler = this.player.networkHandler;
        networkHandler.sendPacket(markerEntity.createSpawnPacket());
        networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(markerEntityId, markerEntity.getDataTracker(), true));

        this.markerTagIds.put(region.runtimeId, markerEntityId);
    }

    @Override
    public void removeRegion(WorkspaceRegion region) {
        int markerEntityId = this.markerTagIds.remove(region.runtimeId);
        if (markerEntityId != Integer.MAX_VALUE) {
            this.player.networkHandler.sendPacket(new EntitiesDestroyS2CPacket(markerEntityId));
        }
    }

    @Override
    public void updateRegion(WorkspaceRegion lastRegion, WorkspaceRegion newRegion) {
        int markerEntityId = this.markerTagIds.get(newRegion.runtimeId);
        if (markerEntityId == Integer.MAX_VALUE) {
            return;
        }

        ArmorStandEntity markerEntity = this.markerEntity;
        markerEntity.setEntityId(markerEntityId);
        markerEntity.setCustomName(new LiteralText(newRegion.marker));

        this.player.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(markerEntityId, markerEntity.getDataTracker(), true));
    }

    private int nextMarkerId() {
        return this.nextMarkerId--;
    }

    private void renderWorkspaceBounds() {
        MapWorkspace workspace = this.workspace;
        BlockBounds bounds = workspace.getBounds();
        ParticleOutlineRenderer.render(this.player, bounds.getMin(), bounds.getMax(), 1.0F, 0.0F, 0.0F);

        for (WorkspaceRegion region : workspace.getRegions()) {
            BlockBounds regionBounds = region.bounds;
            BlockPos min = regionBounds.getMin();
            BlockPos max = regionBounds.getMax();
            double distance = this.player.squaredDistanceTo(
                    (min.getX() + max.getX()) / 2.0,
                    (min.getY() + max.getY()) / 2.0,
                    (min.getZ() + max.getZ()) / 2.0
            );

            if (distance < 32 * 32) {
                int color = colorForRegionMarker(region.marker);
                float red = (color >> 16 & 0xFF) / 255.0F;
                float green = (color >> 8 & 0xFF) / 255.0F;
                float blue = (color & 0xFF) / 255.0F;

                ParticleOutlineRenderer.render(this.player, min, max, red, green, blue);
            }
        }
    }

    private void renderTracingBounds() {
        PartialRegion tracing = this.tracing;
        BlockBounds traced = this.traced;
        if (tracing != null) {
            ParticleOutlineRenderer.render(this.player, tracing.getMin(), tracing.getMax(), 0.0F, 0.8F, 0.0F);
        } else if (traced != null) {
            ParticleOutlineRenderer.render(this.player, traced.getMin(), traced.getMax(), 0.1F, 1.0F, 0.1F);
        }
    }

    private static int colorForRegionMarker(String marker) {
        return HashCommon.mix(marker.hashCode()) & 0xFFFFFF;
    }
}

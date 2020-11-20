package xyz.nucleoid.plasmid.game.map.template;

import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.plasmid.game.map.template.trace.PartialRegion;
import xyz.nucleoid.plasmid.game.map.template.trace.RegionTraceMode;
import xyz.nucleoid.plasmid.game.map.template.trace.RegionTracer;
import xyz.nucleoid.plasmid.util.BlockBounds;

public final class WorkspaceBoundRenderer {
    public static void onTick(MinecraftServer server) {
        if (server.getTicks() % 10 != 0) {
            return;
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(server);
            MapWorkspace workspace = workspaceManager.byDimension(player.world.getRegistryKey());

            if (workspace != null) {
                WorkspaceBoundRenderer.renderMap(player, workspace);

                if (player instanceof RegionTracer) {
                    RegionTracer regionTracer = (RegionTracer) player;
                    WorkspaceBoundRenderer.renderTracer(player, regionTracer);
                }
            }
        }
    }

    private static void renderMap(ServerPlayerEntity player, MapWorkspace viewing) {
        BlockBounds bounds = viewing.getBounds();
        renderOutline(player, bounds.getMin(), bounds.getMax(), 1.0F, 0.0F, 0.0F);

        for (TemplateRegion region : viewing.getRegions()) {
            BlockBounds regionBounds = region.getBounds();
            BlockPos min = regionBounds.getMin();
            BlockPos max = regionBounds.getMax();
            double distance = player.squaredDistanceTo(
                    (min.getX() + max.getX()) / 2.0,
                    (min.getY() + max.getY()) / 2.0,
                    (min.getZ() + max.getZ()) / 2.0
            );
            if (distance < 32 * 32) {
                renderOutline(player, min, max, 0.0F, 0.0F, 1.0F);
            }
        }
    }

    private static void renderTracer(ServerPlayerEntity player, RegionTracer regionTracer) {
        if (regionTracer.isTracing()) {
            RegionTraceMode traceMode = regionTracer.getMode();

            BlockPos pos = traceMode.tryTrace(player);
            if (pos != null) {
                regionTracer.trace(pos);
            }
        }

        PartialRegion tracing = regionTracer.getTracing();
        if (tracing != null) {
            renderOutline(player, tracing.getMin(), tracing.getMax(), 0.0F, 1.0F, 0.0F);
        }
    }

    private static void renderOutline(ServerPlayerEntity player, BlockPos min, BlockPos max, float red, float green, float blue) {
        DustParticleEffect effect = new DustParticleEffect(red, green, blue, 2.0F);

        Edge[] edges = edges(min, max);

        int maxInterval = 5;
        int maxCount = 20;

        for (Edge edge : edges) {
            int length = edge.length();

            int interval = 1;
            if (length > 0) {
                interval = MathHelper.clamp(length / Math.min(maxCount, length), 1, maxInterval);
            }

            int steps = (length + interval - 1) / interval;
            for (int i = 0; i <= steps; i++) {
                double m = (double) (i * interval) / length;
                spawnParticleIfVisible(
                        player, effect,
                        edge.projX(m), edge.projY(m), edge.projZ(m)
                );
            }
        }
    }

    private static void spawnParticleIfVisible(ServerPlayerEntity player, ParticleEffect effect, double x, double y, double z) {
        ServerWorld world = player.getServerWorld();

        Vec3d delta = player.getPos().subtract(x, y, z);
        double length2 = delta.lengthSquared();
        if (length2 > 256 * 256) {
            return;
        }

        Vec3d rotation = player.getRotationVec(1.0F);
        double dot = (delta.multiply(1.0 / Math.sqrt(length2))).dotProduct(rotation);
        if (dot > 0.0) {
            return;
        }

        world.spawnParticles(
                player, effect, true,
                x, y, z,
                1,
                0.0, 0.0, 0.0,
                0.0
        );
    }

    private static Edge[] edges(BlockPos min, BlockPos max) {
        int minX = min.getX();
        int minY = min.getY();
        int minZ = min.getZ();
        int maxX = max.getX() + 1;
        int maxY = max.getY() + 1;
        int maxZ = max.getZ() + 1;

        return new Edge[] {
                // edges
                new Edge(minX, minY, minZ, minX, minY, maxZ),
                new Edge(minX, maxY, minZ, minX, maxY, maxZ),
                new Edge(maxX, minY, minZ, maxX, minY, maxZ),
                new Edge(maxX, maxY, minZ, maxX, maxY, maxZ),

                // front
                new Edge(minX, minY, minZ, minX, maxY, minZ),
                new Edge(maxX, minY, minZ, maxX, maxY, minZ),
                new Edge(minX, minY, minZ, maxX, minY, minZ),
                new Edge(minX, maxY, minZ, maxX, maxY, minZ),

                // back
                new Edge(minX, minY, maxZ, minX, maxY, maxZ),
                new Edge(maxX, minY, maxZ, maxX, maxY, maxZ),
                new Edge(minX, minY, maxZ, maxX, minY, maxZ),
                new Edge(minX, maxY, maxZ, maxX, maxY, maxZ),
        };
    }

    private static class Edge {
        final int startX, startY, startZ;
        final int endX, endY, endZ;

        Edge(int startX, int startY, int startZ, int endX, int endY, int endZ) {
            this.startX = startX;
            this.startY = startY;
            this.startZ = startZ;
            this.endX = endX;
            this.endY = endY;
            this.endZ = endZ;
        }

        double projX(double m) {
            return this.startX + (this.endX - this.startX) * m;
        }

        double projY(double m) {
            return this.startY + (this.endY - this.startY) * m;
        }

        double projZ(double m) {
            return this.startZ + (this.endZ - this.startZ) * m;
        }

        int length() {
            int dx = this.endX - this.startX;
            int dy = this.endY - this.startY;
            int dz = this.endZ - this.startZ;
            return MathHelper.ceil(Math.sqrt(dx * dx + dy * dy + dz * dz));
        }
    }
}

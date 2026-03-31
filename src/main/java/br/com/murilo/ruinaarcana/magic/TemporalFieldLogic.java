package br.com.murilo.ruinaarcana.magic;

import br.com.murilo.ruinaarcana.config.RuinaArcanaConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class TemporalFieldLogic {

    private TemporalFieldLogic() {
    }

    public static void pulse(ServerLevel level, Player sourcePlayer) {
        BlockPos center = sourcePlayer.blockPosition();
        int radius = RuinaArcanaConfig.VALUES.runeRadius.get();

        accelerateBlocks(level, center, radius);
        pullCreatures(level, sourcePlayer, center, radius);
        spawnRuneParticles(level, center, radius);
    }

    private static void accelerateBlocks(ServerLevel level, BlockPos center, int radius) {
        int extraRandomTicks = Math.max(0, RuinaArcanaConfig.VALUES.runeExtraRandomTicks.get());

        if (extraRandomTicks <= 0) {
            return;
        }

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius))) {

            BlockState state = level.getBlockState(pos);

            if (state.isAir()) {
                continue;
            }

            for (int i = 0; i < extraRandomTicks; i++) {
                state = level.getBlockState(pos);
                if (!state.isRandomlyTicking()) {
                    break;
                }
                state.randomTick(level, pos, level.random);
            }
        }
    }

    private static void pullCreatures(ServerLevel level, Player sourcePlayer, BlockPos center, int radius) {
        AABB area = new AABB(center).inflate(radius + 0.5D);
        Vec3 centerVec = Vec3.atCenterOf(center);
        double baseStrength = RuinaArcanaConfig.VALUES.runePullStrength.get();

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area, entity -> entity.isAlive() && entity != sourcePlayer)) {
            Vec3 direction = centerVec.subtract(target.position());
            double distance = Math.max(0.6D, direction.length());
            Vec3 pull = direction.normalize().scale(baseStrength / distance);

            target.setDeltaMovement(target.getDeltaMovement().add(pull));
            target.hasImpulse = true;
            target.hurtMarked = true;
        }
    }

    private static void spawnRuneParticles(ServerLevel level, BlockPos center, int radius) {
        double cx = center.getX() + 0.5D;
        double cy = center.getY() + 0.15D;
        double cz = center.getZ() + 0.5D;
        int points = Math.max(24, radius * 20);

        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2D * i) / points;
            double x = cx + Math.cos(angle) * radius;
            double z = cz + Math.sin(angle) * radius;

            level.sendParticles(ParticleTypes.ENCHANT, x, cy, z, 1, 0.0D, 0.02D, 0.0D, 0.0D);
            level.sendParticles(ParticleTypes.WITCH, x, cy + 0.02D, z, 1, 0.0D, 0.01D, 0.0D, 0.0D);
        }

        level.sendParticles(ParticleTypes.PORTAL, cx, cy + 0.3D, cz, 18, radius * 0.2D, 0.2D, radius * 0.2D, 0.03D);
    }
}

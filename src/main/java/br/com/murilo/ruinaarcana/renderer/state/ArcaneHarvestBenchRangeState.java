package br.com.murilo.ruinaarcana.renderer.state;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public final class ArcaneHarvestBenchRangeState {

    private static boolean enabled;
    @Nullable
    private static BlockPos benchPos;
    @Nullable
    private static ResourceKey<Level> dimension;
    private static int range;

    private ArcaneHarvestBenchRangeState() {
    }

    public static void enable(BlockPos pos, int newRange, ResourceKey<Level> newDimension) {
        enabled = true;
        benchPos = pos.immutable();
        dimension = newDimension;
        range = Math.max(1, newRange);
    }

    public static void disable() {
        enabled = false;
        benchPos = null;
        dimension = null;
        range = 0;
    }

    public static boolean isEnabled() {
        return enabled && benchPos != null && dimension != null;
    }

    public static boolean isEnabledFor(BlockPos pos, ResourceKey<Level> currentDimension) {
        return isEnabled()
                && benchPos.equals(pos)
                && dimension.equals(currentDimension);
    }

    public static boolean isEnabledForCurrentLevel(@Nullable Level level) {
        return isEnabled()
                && level != null
                && dimension != null
                && level.dimension().equals(dimension);
    }

    public static void updateRange(int newRange) {
        if (!isEnabled()) {
            return;
        }

        range = Math.max(1, newRange);
    }

    @Nullable
    public static BlockPos getBenchPos() {
        return benchPos;
    }

    public static int getRange() {
        return range;
    }
}

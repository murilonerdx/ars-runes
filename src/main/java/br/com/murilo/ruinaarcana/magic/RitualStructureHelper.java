package br.com.murilo.ruinaarcana.magic;

import br.com.murilo.ruinaarcana.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class RitualStructureHelper {

    private RitualStructureHelper() {
    }

    public static boolean hasCatalystPattern(Level level, BlockPos altarPos) {
        return level.getBlockState(altarPos.north()).is(ModBlocks.SIGILO_CELESTE.get())
                && level.getBlockState(altarPos.south()).is(ModBlocks.SIGILO_CELESTE.get())
                && level.getBlockState(altarPos.east()).is(ModBlocks.SIGILO_CRESCIMENTO.get())
                && level.getBlockState(altarPos.west()).is(ModBlocks.SIGILO_CRESCIMENTO.get())
                && level.getBlockState(altarPos.north().east()).is(ModBlocks.SIGILO_GRAVITACIONAL.get())
                && level.getBlockState(altarPos.north().west()).is(ModBlocks.SIGILO_GRAVITACIONAL.get())
                && level.getBlockState(altarPos.south().east()).is(ModBlocks.SIGILO_GRAVITACIONAL.get())
                && level.getBlockState(altarPos.south().west()).is(ModBlocks.SIGILO_GRAVITACIONAL.get());
    }

    public static void clearCatalystPattern(ServerLevel level, BlockPos altarPos) {
        clearIfSigil(level, altarPos.north());
        clearIfSigil(level, altarPos.south());
        clearIfSigil(level, altarPos.east());
        clearIfSigil(level, altarPos.west());
        clearIfSigil(level, altarPos.north().east());
        clearIfSigil(level, altarPos.north().west());
        clearIfSigil(level, altarPos.south().east());
        clearIfSigil(level, altarPos.south().west());
    }

    private static void clearIfSigil(ServerLevel level, BlockPos pos) {
        if (level.getBlockState(pos).is(ModBlocks.SIGILO_CELESTE.get())
                || level.getBlockState(pos).is(ModBlocks.SIGILO_CRESCIMENTO.get())
                || level.getBlockState(pos).is(ModBlocks.SIGILO_GRAVITACIONAL.get())) {
            level.destroyBlock(pos, false);
        }
    }
}

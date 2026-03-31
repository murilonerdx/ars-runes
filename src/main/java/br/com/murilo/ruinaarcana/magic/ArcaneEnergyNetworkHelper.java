package br.com.murilo.ruinaarcana.magic;

import br.com.murilo.ruinaarcana.block.entity.ArcaneBatteryBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class ArcaneEnergyNetworkHelper {

    private ArcaneEnergyNetworkHelper() {
    }

    public static int pullFromNearbyBatteries(ServerLevel level, BlockPos center, int radius, int maxAmount) {
        if (maxAmount <= 0) {
            return 0;
        }

        int remaining = maxAmount;
        int moved = 0;

        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            if (remaining <= 0) {
                break;
            }

            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof ArcaneBatteryBlockEntity battery) || pos.equals(center)) {
                continue;
            }

            int extracted = battery.extractCharge(remaining);
            if (extracted > 0) {
                moved += extracted;
                remaining -= extracted;
            }
        }

        return moved;
    }
}

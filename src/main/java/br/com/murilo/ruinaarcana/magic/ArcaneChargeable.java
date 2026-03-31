package br.com.murilo.ruinaarcana.magic;

import net.minecraft.world.item.ItemStack;

public interface ArcaneChargeable {
    int getMaxCharge(ItemStack stack);
}

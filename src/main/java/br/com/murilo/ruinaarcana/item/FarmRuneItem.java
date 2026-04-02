package br.com.murilo.ruinaarcana.item;

import br.com.murilo.ruinaarcana.config.RuinaArcanaConfig;
import br.com.murilo.ruinaarcana.magic.ArcaneChargeHelper;
import br.com.murilo.ruinaarcana.magic.ArcaneChargeable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class FarmRuneItem extends Item implements ArcaneChargeable {

    private final String tooltipLine1Key;
    private final String tooltipLine2Key;

    public FarmRuneItem(Properties properties, String tooltipLine1Key, String tooltipLine2Key) {
        super(properties);
        this.tooltipLine1Key = tooltipLine1Key;
        this.tooltipLine2Key = tooltipLine2Key;
    }

    @Override
    public int getMaxCharge(ItemStack stack) {
        return RuinaArcanaConfig.VALUES.runeMaxCharge.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(tooltipLine1Key).withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable(tooltipLine2Key).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(
                "tooltip.ruinaarcana.arcane_charge",
                ArcaneChargeHelper.getCharge(stack),
                ArcaneChargeHelper.getMaxCharge(stack)
        ).withStyle(ChatFormatting.AQUA));
    }
}

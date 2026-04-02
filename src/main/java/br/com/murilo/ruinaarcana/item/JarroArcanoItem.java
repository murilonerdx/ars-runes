package br.com.murilo.ruinaarcana.item;

import br.com.murilo.ruinaarcana.magic.ArcaneChargeHelper;
import br.com.murilo.ruinaarcana.magic.ArcaneChargeable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class JarroArcanoItem extends Item implements ArcaneChargeable {

    public JarroArcanoItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getMaxCharge(ItemStack stack) {
        return 10000;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ruinaarcana.jarro_arcano.line1").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("tooltip.ruinaarcana.jarro_arcano.line2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.ruinaarcana.arcane_charge", ArcaneChargeHelper.getCharge(stack), ArcaneChargeHelper.getMaxCharge(stack))
                .withStyle(ChatFormatting.AQUA));
    }
}

package br.com.murilo.ruinaarcana.item;

import br.com.murilo.ruinaarcana.config.RuinaArcanaConfig;
import br.com.murilo.ruinaarcana.magic.ArcaneChargeHelper;
import br.com.murilo.ruinaarcana.magic.ArcaneChargeable;
import br.com.murilo.ruinaarcana.magic.TemporalFieldLogic;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class EntropyRuneItem extends Item implements ArcaneChargeable {

    private static final String ACTIVE_UNTIL_KEY = "ActiveUntil";

    public EntropyRuneItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getMaxCharge(ItemStack stack) {
        return RuinaArcanaConfig.VALUES.runeMaxCharge.get();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        if (isActive(stack, level)) {
            deactivate(stack);
            player.displayClientMessage(Component.translatable("message.ruinaarcana.rune_disabled"), true);
            return InteractionResultHolder.consume(stack);
        }

        int activationCost = RuinaArcanaConfig.VALUES.runeActivationCost.get();
        if (ArcaneChargeHelper.getCharge(stack) < activationCost) {
            player.displayClientMessage(
                    Component.translatable("message.ruinaarcana.rune_not_enough_charge", activationCost),
                    true
            );
            return InteractionResultHolder.fail(stack);
        }

        ArcaneChargeHelper.removeCharge(stack, activationCost);

        long activeUntil = level.getGameTime() + (RuinaArcanaConfig.VALUES.runeActiveSeconds.get() * 20L);
        stack.getOrCreateTag().putLong(ACTIVE_UNTIL_KEY, activeUntil);

        player.displayClientMessage(Component.translatable("message.ruinaarcana.rune_enabled"), true);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (!(entity instanceof Player player) || level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!isSelected && player.getOffhandItem() != stack) {
            return;
        }

        if (!isActive(stack, level)) {
            return;
        }

        int interval = Math.max(1, RuinaArcanaConfig.VALUES.runePulseIntervalTicks.get());
        if (level.getGameTime() % interval != 0L) {
            return;
        }

        int pulseCost = Math.max(1, RuinaArcanaConfig.VALUES.runePulseEnergyCost.get());

        if (ArcaneChargeHelper.getCharge(stack) < pulseCost) {
            deactivate(stack);
            player.displayClientMessage(Component.translatable("message.ruinaarcana.rune_faded"), true);
            return;
        }

        ArcaneChargeHelper.removeCharge(stack, pulseCost);
        TemporalFieldLogic.pulse(serverLevel, player);

        if (ArcaneChargeHelper.getCharge(stack) <= 0 || level.getGameTime() + interval >= getActiveUntil(stack)) {
            deactivate(stack);
            player.displayClientMessage(Component.translatable("message.ruinaarcana.rune_faded"), true);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        int charge = ArcaneChargeHelper.getCharge(stack);
        int maxCharge = ArcaneChargeHelper.getMaxCharge(stack);

        tooltip.add(Component.translatable("tooltip.ruinaarcana.runa_da_ruina.line1").withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.add(Component.translatable("tooltip.ruinaarcana.runa_da_ruina.line2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.ruinaarcana.arcane_charge", charge, maxCharge).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable(isActive(stack, level)
                        ? "tooltip.ruinaarcana.runa_da_ruina.active"
                        : "tooltip.ruinaarcana.runa_da_ruina.idle")
                .withStyle(isActive(stack, level) ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
    }

    private static boolean isActive(ItemStack stack, Level level) {
        if (level == null) {
            return false;
        }
        return getActiveUntil(stack) > level.getGameTime();
    }

    private static long getActiveUntil(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(ACTIVE_UNTIL_KEY)) {
            return 0L;
        }
        return tag.getLong(ACTIVE_UNTIL_KEY);
    }

    private static void deactivate(ItemStack stack) {
        stack.removeTagKey(ACTIVE_UNTIL_KEY);
    }
}
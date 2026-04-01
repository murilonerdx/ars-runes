package br.com.murilo.ruinaarcana.item;

import br.com.murilo.ruinaarcana.block.entity.ArcaneBatteryBlockEntity;
import br.com.murilo.ruinaarcana.block.entity.ArcaneHarvestBenchBlockEntity;
import br.com.murilo.ruinaarcana.config.RuinaArcanaConfig;
import br.com.murilo.ruinaarcana.magic.ArcaneChargeHelper;
import br.com.murilo.ruinaarcana.magic.ArcaneChargeable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public class MagicCatalystItem extends Item implements ArcaneChargeable {

    public MagicCatalystItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getMaxCharge(ItemStack stack) {
        return RuinaArcanaConfig.VALUES.catalystMaxCharge.get();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        BlockEntity blockEntity = level.getBlockEntity(context.getClickedPos());

        if (blockEntity instanceof ArcaneBatteryBlockEntity battery) {
            return tryLinkBattery(player, stack, battery);
        }

        if (blockEntity instanceof ArcaneHarvestBenchBlockEntity bench) {
            return tryLinkHarvestBench(player, stack, bench);
        }

        return InteractionResult.PASS;
    }

    private InteractionResult tryLinkBattery(Player player, ItemStack stack, ArcaneBatteryBlockEntity battery) {
        boolean linked = battery.installCatalystLink(stack);

        if (!linked) {
            if (player != null) {
                player.displayClientMessage(
                        Component.translatable("message.ruinaarcana.catalisador_magico.already_linked"),
                        true
                );
            }
            return InteractionResult.CONSUME;
        }

        consumeCatalyst(player, stack);

        if (player != null) {
            player.displayClientMessage(
                    Component.translatable("message.ruinaarcana.catalisador_magico.linked"),
                    true
            );
        }

        return InteractionResult.CONSUME;
    }

    private InteractionResult tryLinkHarvestBench(Player player, ItemStack stack, ArcaneHarvestBenchBlockEntity bench) {
        boolean linked = bench.installCatalystLink(stack);

        if (!linked) {
            if (player != null) {
                player.displayClientMessage(
                        Component.translatable("message.ruinaarcana.catalisador_magico.already_linked"),
                        true
                );
            }
            return InteractionResult.CONSUME;
        }

        consumeCatalyst(player, stack);

        if (player != null) {
            player.displayClientMessage(
                    Component.translatable("message.ruinaarcana.catalisador_magico.linked"),
                    true
            );
        }

        return InteractionResult.CONSUME;
    }

    private void consumeCatalyst(Player player, ItemStack stack) {
        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            int charge = ArcaneChargeHelper.getCharge(stack);
            int maxCharge = ArcaneChargeHelper.getMaxCharge(stack);

            player.displayClientMessage(
                    Component.translatable("message.ruinaarcana.catalisador_magico.charge", charge, maxCharge),
                    true
            );
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (!(entity instanceof Player player) || level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        int interval = Math.max(1, RuinaArcanaConfig.VALUES.catalystPulseIntervalTicks.get());
        if (level.getGameTime() % interval != 0L) {
            return;
        }

        if (level.canSeeSky(player.blockPosition().above())) {
            ArcaneChargeHelper.addCharge(
                    stack,
                    RuinaArcanaConfig.VALUES.catalystSkyChargePerPulse.get()
            );
        }

        if (!isSelected && player.getOffhandItem() != stack) {
            return;
        }

        int budget = Math.min(
                ArcaneChargeHelper.getCharge(stack),
                RuinaArcanaConfig.VALUES.catalystTransferPerPulse.get()
        );

        if (budget <= 0) {
            return;
        }

        int movedToInventory = ArcaneChargeHelper.chargeInventoryFromStack(stack, player, budget);
        int remaining = Math.max(0, budget - movedToInventory);

        if (remaining > 0) {
            ArcaneChargeHelper.chargeNearbyItemEntitiesFromStack(
                    serverLevel,
                    stack,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    RuinaArcanaConfig.VALUES.catalystTransferRadius.get(),
                    remaining
            );
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        int charge = ArcaneChargeHelper.getCharge(stack);
        int maxCharge = ArcaneChargeHelper.getMaxCharge(stack);

        tooltip.add(Component.translatable("tooltip.ruinaarcana.catalisador_magico.line1").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.ruinaarcana.catalisador_magico.line2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.ruinaarcana.arcane_charge", charge, maxCharge).withStyle(ChatFormatting.AQUA));
    }
}
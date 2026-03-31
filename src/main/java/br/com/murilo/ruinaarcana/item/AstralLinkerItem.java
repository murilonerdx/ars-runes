package br.com.murilo.ruinaarcana.item;

import br.com.murilo.ruinaarcana.block.entity.ArcaneHarvestBenchBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.List;

public class AstralLinkerItem extends Item {

    private static final String SOURCE_POS_KEY = "SourceBenchPos";

    public AstralLinkerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = context.getItemInHand();
        BlockPos clickedPos = context.getClickedPos();
        BlockEntity blockEntity = context.getLevel().getBlockEntity(clickedPos);

        if (blockEntity instanceof ArcaneHarvestBenchBlockEntity bench) {
            stack.getOrCreateTag().put(SOURCE_POS_KEY, NbtUtils.writeBlockPos(clickedPos));
            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(Component.translatable("message.ruinaarcana.vinculo_astral.source_saved"), true);
            }
            return InteractionResult.CONSUME;
        }

        if (stack.getTag() == null || !stack.getTag().contains(SOURCE_POS_KEY)) {
            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(Component.translatable("message.ruinaarcana.vinculo_astral.no_source"), true);
            }
            return InteractionResult.CONSUME;
        }

        if (blockEntity == null || !blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null).isPresent()) {
            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(Component.translatable("message.ruinaarcana.vinculo_astral.invalid_target"), true);
            }
            return InteractionResult.CONSUME;
        }

        BlockPos sourcePos = NbtUtils.readBlockPos(stack.getTag().getCompound(SOURCE_POS_KEY));
        BlockEntity sourceEntity = context.getLevel().getBlockEntity(sourcePos);
        if (!(sourceEntity instanceof ArcaneHarvestBenchBlockEntity bench)) {
            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(Component.translatable("message.ruinaarcana.vinculo_astral.source_missing"), true);
            }
            return InteractionResult.CONSUME;
        }

        bench.setLinkedInventoryPos(clickedPos);
        stack.removeTagKey(SOURCE_POS_KEY);

        if (context.getPlayer() != null) {
            context.getPlayer().displayClientMessage(Component.translatable("message.ruinaarcana.vinculo_astral.link_complete"), true);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.level.Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ruinaarcana.vinculo_astral.line1").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("tooltip.ruinaarcana.vinculo_astral.line2").withStyle(ChatFormatting.GRAY));
        if (stack.getTag() != null && stack.getTag().contains(SOURCE_POS_KEY)) {
            tooltip.add(Component.translatable("tooltip.ruinaarcana.vinculo_astral.ready").withStyle(ChatFormatting.AQUA));
        }
    }
}

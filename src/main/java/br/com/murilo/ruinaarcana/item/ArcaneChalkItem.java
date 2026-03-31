package br.com.murilo.ruinaarcana.item;

import br.com.murilo.ruinaarcana.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class ArcaneChalkItem extends Item {

    private static final String MODE_KEY = "SigilMode";

    public ArcaneChalkItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide) {
            int nextMode = (getMode(stack) + 1) % 3;
            setMode(stack, nextMode);
            player.displayClientMessage(Component.translatable(getModeTranslationKey(nextMode)), true);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);

        // SHIFT + clique em um sigilo já colocado = troca o sigilo existente
        if (player != null && player.isShiftKeyDown() && isSigil(clickedState.getBlock())) {
            if (!level.isClientSide) {
                Block nextSigil = getNextSigilBlock(clickedState.getBlock());
                level.setBlock(clickedPos, nextSigil.defaultBlockState(), 3);

                // opcional: sincroniza o modo do giz com o sigilo resultante
                setMode(stack, getModeFromBlock(nextSigil));

                if (player instanceof ServerPlayer serverPlayer) {
                    stack.hurtAndBreak(1, serverPlayer, p -> p.broadcastBreakEvent(context.getHand()));
                }

                player.displayClientMessage(
                        Component.translatable(getModeTranslationKey(getModeFromBlock(nextSigil))),
                        true
                );
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // lógica normal de desenhar um sigilo novo
        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.FAIL;
        }

        BlockPos placePos = clickedPos.above();
        if (!level.getBlockState(placePos).canBeReplaced()) {
            return InteractionResult.FAIL;
        }

        Block blockToPlace = getSigilBlock(stack);
        if (blockToPlace == null) {
            return InteractionResult.FAIL;
        }

        if (!blockToPlace.defaultBlockState().canSurvive(level, placePos)) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide) {
            level.setBlock(placePos, blockToPlace.defaultBlockState(), 3);

            if (player instanceof ServerPlayer serverPlayer) {
                stack.hurtAndBreak(1, serverPlayer, p -> p.broadcastBreakEvent(context.getHand()));
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ruinaarcana.giz_arcano.line1").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.ruinaarcana.giz_arcano.line2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(getModeTranslationKey(getMode(stack))).withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    private static boolean isSigil(Block block) {
        return block == ModBlocks.SIGILO_CELESTE.get()
                || block == ModBlocks.SIGILO_CRESCIMENTO.get()
                || block == ModBlocks.SIGILO_GRAVITACIONAL.get();
    }

    private static Block getNextSigilBlock(Block current) {
        if (current == ModBlocks.SIGILO_CELESTE.get()) {
            return ModBlocks.SIGILO_CRESCIMENTO.get();
        }
        if (current == ModBlocks.SIGILO_CRESCIMENTO.get()) {
            return ModBlocks.SIGILO_GRAVITACIONAL.get();
        }
        return ModBlocks.SIGILO_CELESTE.get();
    }

    private static int getModeFromBlock(Block block) {
        if (block == ModBlocks.SIGILO_CRESCIMENTO.get()) {
            return 1;
        }
        if (block == ModBlocks.SIGILO_GRAVITACIONAL.get()) {
            return 2;
        }
        return 0;
    }

    private static Block getSigilBlock(ItemStack stack) {
        return switch (getMode(stack)) {
            case 1 -> ModBlocks.SIGILO_CRESCIMENTO.get();
            case 2 -> ModBlocks.SIGILO_GRAVITACIONAL.get();
            default -> ModBlocks.SIGILO_CELESTE.get();
        };
    }

    private static int getMode(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(MODE_KEY)) {
            return 0;
        }
        return Math.max(0, Math.min(2, tag.getInt(MODE_KEY)));
    }

    private static void setMode(ItemStack stack, int mode) {
        stack.getOrCreateTag().putInt(MODE_KEY, Math.max(0, Math.min(2, mode)));
    }

    private static String getModeTranslationKey(int mode) {
        return switch (mode) {
            case 1 -> "message.ruinaarcana.giz_arcano.mode_crescimento";
            case 2 -> "message.ruinaarcana.giz_arcano.mode_gravitacional";
            default -> "message.ruinaarcana.giz_arcano.mode_celeste";
        };
    }
}
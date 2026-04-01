package br.com.murilo.ruinaarcana.block;

import br.com.murilo.ruinaarcana.block.entity.RuneCatalystBlockEntity;
import br.com.murilo.ruinaarcana.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class RuneCatalystBlock extends BaseEntityBlock {

    public RuneCatalystBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RuneCatalystBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof RuneCatalystBlockEntity catalyst)) {
            return InteractionResult.PASS;
        }

        ItemStack heldItem = player.getItemInHand(hand);

        // coloca a runa no pedestal
        if (heldItem.is(ModItems.RUNA_DA_RUINA.get())) {
            if (catalyst.insertRune(heldItem)) {
                if (!player.getAbilities().instabuild) {
                    heldItem.shrink(1);
                }

                player.displayClientMessage(
                        Component.translatable("message.ruinaarcana.rune_catalyst.rune_inserted"),
                        true
                );
                return InteractionResult.CONSUME;
            }

            player.displayClientMessage(
                    Component.translatable("message.ruinaarcana.rune_catalyst.already_has_rune"),
                    true
            );
            return InteractionResult.CONSUME;
        }

        // shift + clique = remove a runa
        if (player.isShiftKeyDown()) {
            ItemStack removed = catalyst.removeRune();
            if (!removed.isEmpty()) {
                if (!player.addItem(removed)) {
                    player.drop(removed, false);
                }

                player.displayClientMessage(
                        Component.translatable("message.ruinaarcana.rune_catalyst.rune_removed"),
                        true
                );
                return InteractionResult.CONSUME;
            }
        }

        // clique normal = mostra carga atual
        player.displayClientMessage(
                Component.translatable(
                        "message.ruinaarcana.rune_catalyst.charge_status",
                        catalyst.getStoredRuneCharge(),
                        catalyst.getStoredRuneMaxCharge()
                ),
                true
        );

        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : (lvl, blockPos, blockState, blockEntity) -> {
            if (blockEntity instanceof RuneCatalystBlockEntity catalyst) {
                catalyst.serverTick();
            }
        };
    }
}

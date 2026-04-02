package br.com.murilo.ruinaarcana.block;

import br.com.murilo.ruinaarcana.block.entity.ArcaneHarvestBenchBlockEntity;
import br.com.murilo.ruinaarcana.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
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
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class ArcaneHarvestBenchBlock extends BaseEntityBlock {

    public ArcaneHarvestBenchBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcaneHarvestBenchBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof ArcaneHarvestBenchBlockEntity bench ? bench : null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof ArcaneHarvestBenchBlockEntity bench)) {
            return InteractionResult.PASS;
        }

        ItemStack heldItem = player.getItemInHand(hand);

        // deixa o próprio item tratar o clique com catalisador mágico
        if (heldItem.is(ModItems.CATALISADOR_MAGICO.get())) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        // instala a runa na bancada
        if (ModItems.isFarmRune(heldItem)) {
            if (bench.installRune(heldItem)) {
                if (!player.getAbilities().instabuild) {
                    heldItem.shrink(1);
                }

                player.displayClientMessage(
                        Component.translatable("message.ruinaarcana.bancada_colheita_arcana.rune_installed"),
                        true
                );
            } else {
                player.displayClientMessage(
                        Component.translatable("message.ruinaarcana.bancada_colheita_arcana.rune_already_installed"),
                        true
                );
            }
            return InteractionResult.CONSUME;
        }

        // shift + mão vazia remove a runa
        if (player.isShiftKeyDown() && heldItem.isEmpty()) {
            ItemStack removed = bench.removeInstalledRune();
            if (!removed.isEmpty()) {
                if (!player.addItem(removed)) {
                    player.drop(removed, false);
                }

                player.displayClientMessage(
                        Component.translatable("message.ruinaarcana.bancada_colheita_arcana.rune_removed"),
                        true
                );
                return InteractionResult.CONSUME;
            }
        }

        // abre GUI normal
        if (player instanceof ServerPlayer serverPlayer) {
            MenuProvider provider = getMenuProvider(state, level, pos);
            if (provider != null) {
                NetworkHooks.openScreen(serverPlayer, provider, pos);
            }
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ArcaneHarvestBenchBlockEntity bench
                    && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                bench.dropContents(serverLevel);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : (lvl, blockPos, blockState, blockEntity) -> {
            if (blockEntity instanceof ArcaneHarvestBenchBlockEntity bench) {
                bench.serverTick();
            }
        };
    }
}
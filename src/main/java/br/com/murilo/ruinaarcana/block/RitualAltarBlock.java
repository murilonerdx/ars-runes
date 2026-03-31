package br.com.murilo.ruinaarcana.block;

import br.com.murilo.ruinaarcana.magic.ArcaneChargeHelper;
import br.com.murilo.ruinaarcana.magic.RitualStructureHelper;
import br.com.murilo.ruinaarcana.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class RitualAltarBlock extends Block {

    public RitualAltarBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, net.minecraft.world.phys.BlockHitResult hitResult) {
        ItemStack heldItem = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (heldItem.is(ModItems.NUCLEO_CATALISADOR.get())) {
            if (!level.canSeeSky(pos.above())) {
                player.displayClientMessage(Component.translatable("message.ruinaarcana.altar_ritual.sky_required"), true);
                return InteractionResult.CONSUME;
            }

            if (!RitualStructureHelper.hasCatalystPattern(level, pos)) {
                player.displayClientMessage(Component.translatable("message.ruinaarcana.altar_ritual.pattern_incomplete"), true);
                return InteractionResult.CONSUME;
            }

            heldItem.shrink(1);
            RitualStructureHelper.clearCatalystPattern((ServerLevel) level, pos);

            ItemStack catalyst = new ItemStack(ModItems.CATALISADOR_MAGICO.get());
            ArcaneChargeHelper.addCharge(catalyst, 250);

            ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 1.15D, pos.getZ() + 0.5D, catalyst);
            entity.setDefaultPickUpDelay();
            level.addFreshEntity(entity);

            player.displayClientMessage(Component.translatable("message.ruinaarcana.altar_ritual.created_catalyst"), true);
            return InteractionResult.CONSUME;
        }

        player.displayClientMessage(Component.translatable("message.ruinaarcana.altar_ritual.hint"), true);
        return InteractionResult.CONSUME;
    }
}

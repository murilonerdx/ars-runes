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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class RitualAltarBlock extends Block {

    private static final List<RuneRitualRecipe> RUNE_RITUALS = List.of(
            new RuneRitualRecipe(Items.WHEAT, ModItems.RUNA_DA_COLHEITA, 220, "message.ruinaarcana.altar_ritual.created_rune_harvest"),
            new RuneRitualRecipe(Items.GOLDEN_CARROT, ModItems.RUNA_DA_VITALIDADE, 260, "message.ruinaarcana.altar_ritual.created_rune_vitality"),
            new RuneRitualRecipe(Items.REDSTONE, ModItems.RUNA_DO_FLUXO, 240, "message.ruinaarcana.altar_ritual.created_rune_flow"),
            new RuneRitualRecipe(Items.CHEST, ModItems.RUNA_DO_ARMAZENAMENTO, 300, "message.ruinaarcana.altar_ritual.created_rune_storage")
    );
    private static final List<RuneFusionRecipe> RUNE_FUSIONS = List.of(
            new RuneFusionRecipe(ModItems.RUNA_DA_COLHEITA, ModItems.RUNA_DA_VITALIDADE, new ItemStack(Items.SLIME_BALL, 4), "message.ruinaarcana.altar_ritual.created_item_slime"),
            new RuneFusionRecipe(ModItems.RUNA_DO_FLUXO, ModItems.RUNA_DO_ARMAZENAMENTO, new ItemStack(Items.ENDER_PEARL, 2), "message.ruinaarcana.altar_ritual.created_item_ender"),
            new RuneFusionRecipe(ModItems.RUNA_DA_RUINA, ModItems.RUNA_DO_FLUXO, new ItemStack(Items.NETHER_STAR, 1), "message.ruinaarcana.altar_ritual.created_item_nether_star")
    );

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

        if (ModItems.isFarmRune(heldItem)) {
            if (!level.canSeeSky(pos.above())) {
                player.displayClientMessage(Component.translatable("message.ruinaarcana.altar_ritual.sky_required"), true);
                return InteractionResult.CONSUME;
            }

            if (!RitualStructureHelper.hasCatalystPattern(level, pos)) {
                player.displayClientMessage(Component.translatable("message.ruinaarcana.altar_ritual.pattern_incomplete"), true);
                return InteractionResult.CONSUME;
            }

            ItemStack offhand = player.getOffhandItem();
            if (ModItems.isFarmRune(offhand)) {
                RuneFusionRecipe fusionRecipe = findFusionRecipe(heldItem, offhand);
                if (fusionRecipe == null) {
                    player.displayClientMessage(Component.translatable("message.ruinaarcana.altar_ritual.invalid_rune_combo"), true);
                    return InteractionResult.CONSUME;
                }

                heldItem.shrink(1);
                offhand.shrink(1);
                RitualStructureHelper.clearCatalystPattern((ServerLevel) level, pos);

                ItemEntity entity = new ItemEntity(
                        level,
                        pos.getX() + 0.5D,
                        pos.getY() + 1.15D,
                        pos.getZ() + 0.5D,
                        fusionRecipe.output().copy()
                );
                entity.setDefaultPickUpDelay();
                level.addFreshEntity(entity);

                player.displayClientMessage(Component.translatable(fusionRecipe.successMessageKey()), true);
                return InteractionResult.CONSUME;
            }

            if (!heldItem.is(ModItems.RUNA_DA_RUINA.get())) {
                player.displayClientMessage(Component.translatable("message.ruinaarcana.altar_ritual.invalid_rune_combo"), true);
                return InteractionResult.CONSUME;
            }

            RuneRitualRecipe ritualRecipe = findRuneRecipe(offhand);
            if (ritualRecipe == null) {
                player.displayClientMessage(Component.translatable("message.ruinaarcana.altar_ritual.rune_missing_reagent"), true);
                return InteractionResult.CONSUME;
            }

            heldItem.shrink(1);
            offhand.shrink(1);
            RitualStructureHelper.clearCatalystPattern((ServerLevel) level, pos);

            ItemStack upgradedRune = new ItemStack(ritualRecipe.outputRune().get());
            ArcaneChargeHelper.addCharge(upgradedRune, ritualRecipe.initialCharge());

            ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 1.15D, pos.getZ() + 0.5D, upgradedRune);
            entity.setDefaultPickUpDelay();
            level.addFreshEntity(entity);

            player.displayClientMessage(Component.translatable(ritualRecipe.successMessageKey()), true);
            return InteractionResult.CONSUME;
        }

        player.displayClientMessage(Component.translatable("message.ruinaarcana.altar_ritual.hint"), true);
        return InteractionResult.CONSUME;
    }

    private RuneRitualRecipe findRuneRecipe(ItemStack reagent) {
        if (reagent.isEmpty()) {
            return null;
        }

        for (RuneRitualRecipe recipe : RUNE_RITUALS) {
            if (reagent.is(recipe.reagent())) {
                return recipe;
            }
        }
        return null;
    }

    private RuneFusionRecipe findFusionRecipe(ItemStack mainHandRune, ItemStack offhandRune) {
        for (RuneFusionRecipe recipe : RUNE_FUSIONS) {
            boolean forward = mainHandRune.is(recipe.runeA().get()) && offhandRune.is(recipe.runeB().get());
            boolean reverse = mainHandRune.is(recipe.runeB().get()) && offhandRune.is(recipe.runeA().get());
            if (forward || reverse) {
                return recipe;
            }
        }
        return null;
    }

    private record RuneRitualRecipe(Item reagent, RegistryObject<Item> outputRune, int initialCharge, String successMessageKey) {
    }

    private record RuneFusionRecipe(RegistryObject<Item> runeA, RegistryObject<Item> runeB, ItemStack output, String successMessageKey) {
    }
}

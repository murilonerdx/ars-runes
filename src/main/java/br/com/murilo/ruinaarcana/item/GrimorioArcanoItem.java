package br.com.murilo.ruinaarcana.item;

import br.com.murilo.ruinaarcana.magic.TemporalFieldLogic;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;

import java.util.List;

public class GrimorioArcanoItem extends Item {

    private static final String SELECTED_SPELL_KEY = "SelectedSpell";
    private static final TagKey<Item> CATALYST_ITEMS = TagKey.create(
            Registries.ITEM,
            new ResourceLocation("ruinaarcana", "grimorio_catalysts")
    );

    public GrimorioArcanoItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                Spell next = getSelectedSpell(stack).next();
                setSelectedSpell(stack, next);
                player.displayClientMessage(Component.translatable(next.modeMessageKey), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        Spell selected = getSelectedSpell(stack);
        if (!consumeCatalyst(player, selected.catalystCost)) {
            player.displayClientMessage(Component.translatable("message.ruinaarcana.grimorio_arcano.not_enough_catalyst", selected.catalystCost), true);
            return InteractionResultHolder.fail(stack);
        }

        cast(level, player, selected);
        return InteractionResultHolder.consume(stack);
    }

    private void cast(Level level, Player player, Spell spell) {
        switch (spell) {
            case ASTRAL_LANTERN -> {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 40, 0));
                player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 6, 0));
                player.displayClientMessage(Component.translatable("message.ruinaarcana.grimorio_arcano.cast_lantern"), true);
            }
            case VERDANT_PULSE -> {
                if (level instanceof ServerLevel serverLevel) {
                    TemporalFieldLogic.pulse(serverLevel, player);
                }
                player.displayClientMessage(Component.translatable("message.ruinaarcana.grimorio_arcano.cast_growth"), true);
            }
            case STEP_THROUGH -> {
                Vec3 start = player.getEyePosition();
                Vec3 end = start.add(player.getLookAngle().scale(8.0D));
                HitResult hitResult = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

                Vec3 target = hitResult.getType() == HitResult.Type.MISS
                        ? end
                        : hitResult.getLocation().subtract(player.getLookAngle().scale(1.2D));

                BlockPos targetPos = BlockPos.containing(target);
                if (!level.getBlockState(targetPos).isAir() || !level.getBlockState(targetPos.above()).isAir()) {
                    player.displayClientMessage(Component.translatable("message.ruinaarcana.grimorio_arcano.cast_step_fail"), true);
                    return;
                }

                player.teleportTo(target.x, target.y, target.z);
                player.fallDistance = 0.0F;
                player.displayClientMessage(Component.translatable("message.ruinaarcana.grimorio_arcano.cast_step"), true);
            }
        }
    }

    private boolean consumeCatalyst(Player player, int amount) {
        if (player.getAbilities().instabuild) {
            return true;
        }

        int available = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack inventoryStack = player.getInventory().getItem(i);
            if (!inventoryStack.isEmpty() && inventoryStack.is(CATALYST_ITEMS)) {
                available += inventoryStack.getCount();
                if (available >= amount) {
                    break;
                }
            }
        }

        if (available < amount) {
            return false;
        }

        int remaining = amount;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack inventoryStack = player.getInventory().getItem(i);
            if (!inventoryStack.isEmpty() && inventoryStack.is(CATALYST_ITEMS)) {
                int consumed = Math.min(remaining, inventoryStack.getCount());
                inventoryStack.shrink(consumed);
                remaining -= consumed;
                if (remaining <= 0) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        Spell selected = getSelectedSpell(stack);
        tooltip.add(Component.translatable("tooltip.ruinaarcana.grimorio_arcano.line1").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("tooltip.ruinaarcana.grimorio_arcano.line2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.ruinaarcana.grimorio_arcano.current", Component.translatable(selected.nameKey))
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.ruinaarcana.grimorio_arcano.cost", selected.catalystCost)
                .withStyle(ChatFormatting.DARK_AQUA));
        if (ModList.get().isLoaded("ars_nouveau")) {
            tooltip.add(Component.translatable("tooltip.ruinaarcana.grimorio_arcano.compat_ars").withStyle(ChatFormatting.GRAY));
        }
        if (ModList.get().isLoaded("occultism")) {
            tooltip.add(Component.translatable("tooltip.ruinaarcana.grimorio_arcano.compat_occultism").withStyle(ChatFormatting.GRAY));
        }
    }

    private static Spell getSelectedSpell(ItemStack stack) {
        int ordinal = stack.getOrCreateTag().getInt(SELECTED_SPELL_KEY);
        return Spell.byOrdinal(ordinal);
    }

    private static void setSelectedSpell(ItemStack stack, Spell spell) {
        stack.getOrCreateTag().putInt(SELECTED_SPELL_KEY, spell.ordinal());
    }

    private enum Spell {
        ASTRAL_LANTERN("spell.ruinaarcana.astral_lantern", "message.ruinaarcana.grimorio_arcano.mode_lantern", 1),
        VERDANT_PULSE("spell.ruinaarcana.verdant_pulse", "message.ruinaarcana.grimorio_arcano.mode_growth", 2),
        STEP_THROUGH("spell.ruinaarcana.step_through", "message.ruinaarcana.grimorio_arcano.mode_step", 3);

        private final String nameKey;
        private final String modeMessageKey;
        private final int catalystCost;

        Spell(String nameKey, String modeMessageKey, int catalystCost) {
            this.nameKey = nameKey;
            this.modeMessageKey = modeMessageKey;
            this.catalystCost = catalystCost;
        }

        private Spell next() {
            Spell[] values = values();
            return values[(this.ordinal() + 1) % values.length];
        }

        private static Spell byOrdinal(int ordinal) {
            Spell[] values = values();
            if (ordinal < 0 || ordinal >= values.length) {
                return values[0];
            }
            return values[ordinal];
        }
    }
}

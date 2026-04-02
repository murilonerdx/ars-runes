package br.com.murilo.ruinaarcana.item;

import br.com.murilo.ruinaarcana.magic.ArcaneChargeHelper;
import br.com.murilo.ruinaarcana.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class CodexRunicoItem extends Item {

    private static final String SELECTED_EFFECT_KEY = "SelectedCustomRuneEffect";
    private static final String CUSTOM_EFFECT_KEY = "CustomRuneEffect";
    private static final String CUSTOM_POTENCY_KEY = "CustomRunePotency";

    public CodexRunicoItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        if (player.isShiftKeyDown()) {
            int next = (stack.getOrCreateTag().getInt(SELECTED_EFFECT_KEY) + 1) % CustomRuneEffect.values().length;
            stack.getOrCreateTag().putInt(SELECTED_EFFECT_KEY, next);
            player.displayClientMessage(Component.translatable("message.ruinaarcana.codex_runico.effect_selected",
                    Component.translatable(CustomRuneEffect.values()[next].nameKey)), true);
            return InteractionResultHolder.consume(stack);
        }

        ItemStack offhand = player.getOffhandItem();
        if (offhand.is(ModItems.RUNA_DA_RUINA.get())) {
            ItemStack jar = findArcaneJar(player);
            if (jar.isEmpty()) {
                player.displayClientMessage(Component.translatable("message.ruinaarcana.codex_runico.no_jar"), true);
                return InteractionResultHolder.fail(stack);
            }

            int sourceCost = 300;
            if (ArcaneChargeHelper.getCharge(jar) < sourceCost) {
                player.displayClientMessage(Component.translatable("message.ruinaarcana.codex_runico.low_source", sourceCost), true);
                return InteractionResultHolder.fail(stack);
            }

            ArcaneChargeHelper.removeCharge(jar, sourceCost);
            CustomRuneEffect selected = getSelectedEffect(stack);
            offhand.getOrCreateTag().putString(CUSTOM_EFFECT_KEY, selected.effectId);
            offhand.getOrCreateTag().putInt(CUSTOM_POTENCY_KEY, selected.basePotency);
            player.displayClientMessage(Component.translatable("message.ruinaarcana.codex_runico.rune_customized",
                    Component.translatable(selected.nameKey)), true);
            return InteractionResultHolder.consume(stack);
        }

        ItemStack guide = createGuideBook();
        if (!player.getInventory().add(guide)) {
            player.drop(guide, false);
        }

        player.displayClientMessage(Component.translatable("message.ruinaarcana.codex_runico.created"), true);
        return InteractionResultHolder.consume(stack);
    }

    private ItemStack findArcaneJar(Player player) {
        for (ItemStack inventoryStack : player.getInventory().items) {
            if (inventoryStack.is(ModItems.JARRO_ARCANO.get())) {
                return inventoryStack;
            }
        }
        for (ItemStack inventoryStack : player.getInventory().offhand) {
            if (inventoryStack.is(ModItems.JARRO_ARCANO.get())) {
                return inventoryStack;
            }
        }
        return ItemStack.EMPTY;
    }

    private CustomRuneEffect getSelectedEffect(ItemStack codex) {
        int index = codex.getOrCreateTag().getInt(SELECTED_EFFECT_KEY);
        if (index < 0 || index >= CustomRuneEffect.values().length) {
            index = 0;
        }
        return CustomRuneEffect.values()[index];
    }

    private ItemStack createGuideBook() {
        ItemStack writtenBook = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag tag = writtenBook.getOrCreateTag();

        tag.putString("title", "Codex Rúnico");
        tag.putString("author", "Ruina Arcana");

        ListTag pages = new ListTag();
        pages.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(
                "§5Runas de Fazenda\n\n" +
                        "Runa da Colheita: reduz custo de colheita.\n" +
                        "Runa da Vitalidade: reduz custo animal.\n" +
                        "Runa do Fluxo: puxa mais energia.\n" +
                        "Runa do Armazenamento: teleporte mais eficiente."
        ))));
        pages.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(
                "§6Rituais de Forja\n\n" +
                        "Runa da Ruína + Trigo -> Runa da Colheita\n" +
                        "Runa da Ruína + Cenoura Dourada -> Runa da Vitalidade\n" +
                        "Runa da Ruína + Redstone -> Runa do Fluxo\n" +
                        "Runa da Ruína + Baú -> Runa do Armazenamento"
        ))));
        pages.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(
                "§cFusão de Runas (itens raros)\n\n" +
                        "Colheita + Vitalidade -> Slime Ball x4\n" +
                        "Fluxo + Armazenamento -> Ender Pearl x2\n" +
                        "Ruína + Fluxo -> Nether Star x1\n\n" +
                        "Todos os rituais exigem céu aberto e padrão completo no Altar Ritual."
        ))));

        tag.put("pages", pages);
        return writtenBook;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ruinaarcana.codex_runico.line1").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("tooltip.ruinaarcana.codex_runico.line2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.ruinaarcana.codex_runico.current_effect",
                Component.translatable(getSelectedEffect(stack).nameKey)).withStyle(ChatFormatting.AQUA));
    }

    private enum CustomRuneEffect {
        HARVEST("harvest", "spell.ruinaarcana.custom.harvest", 2),
        VITALITY("vitality", "spell.ruinaarcana.custom.vitality", 2),
        FLOW("flow", "spell.ruinaarcana.custom.flow", 2),
        STORAGE("storage", "spell.ruinaarcana.custom.storage", 2),
        RUIN("ruin", "spell.ruinaarcana.custom.ruin", 1);

        private final String effectId;
        private final String nameKey;
        private final int basePotency;

        CustomRuneEffect(String effectId, String nameKey, int basePotency) {
            this.effectId = effectId;
            this.nameKey = nameKey;
            this.basePotency = basePotency;
        }
    }
}

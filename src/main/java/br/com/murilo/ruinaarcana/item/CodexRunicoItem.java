package br.com.murilo.ruinaarcana.item;

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

    public CodexRunicoItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        ItemStack guide = createGuideBook();
        if (!player.getInventory().add(guide)) {
            player.drop(guide, false);
        }

        player.displayClientMessage(Component.translatable("message.ruinaarcana.codex_runico.created"), true);
        return InteractionResultHolder.consume(stack);
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
    }
}

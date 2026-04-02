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

    public enum CodexType {
        GERAL("Codex Rúnico", "Ruina Arcana"),
        RITUAIS("Codex de Rituais", "Conclave Arcano"),
        ARTEFATOS("Codex de Artefatos", "Arquivo Arcano"),
        PROGRESSAO("Codex de Progressão", "Ordem da Ruína");

        private final String title;
        private final String author;

        CodexType(String title, String author) {
            this.title = title;
            this.author = author;
        }
    }

    private final CodexType codexType;

    public CodexRunicoItem(Properties properties, CodexType codexType) {
        super(properties);
        this.codexType = codexType;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        ItemStack guide = createGuideBook(codexType);
        if (!player.getInventory().add(guide)) {
            player.drop(guide, false);
        }

        player.displayClientMessage(Component.translatable("message.ruinaarcana.codex_runico.created"), true);
        return InteractionResultHolder.consume(stack);
    }

    public static ItemStack createGuideBook() {
        return createGuideBook(CodexType.GERAL);
    }

    public static ItemStack createRitualGuideBook() {
        return createGuideBook(CodexType.RITUAIS);
    }

    public static ItemStack createArtifactGuideBook() {
        return createGuideBook(CodexType.ARTEFATOS);
    }

    public static ItemStack createProgressionGuideBook() {
        return createGuideBook(CodexType.PROGRESSAO);
    }

    public static ItemStack createGuideBook(CodexType codexType) {
        ItemStack writtenBook = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag tag = writtenBook.getOrCreateTag();

        tag.putString("title", codexType.title);
        tag.putString("author", codexType.author);

        ListTag pages = switch (codexType) {
            case GERAL -> createGeneralPages();
            case RITUAIS -> createRitualPages();
            case ARTEFATOS -> createArtifactPages();
            case PROGRESSAO -> createProgressionPages();
        };

        tag.put("pages", pages);
        return writtenBook;
    }

    private static ListTag createGeneralPages() {
        ListTag pages = new ListTag();
        pages.add(page(
                "§5Ruína Arcana: Introdução\n\n" +
                        "Este códex resume todos os sistemas do mod.\n\n" +
                        "Itens-base:\n" +
                        "- Giz Arcano\n" +
                        "- Altar Ritual\n" +
                        "- Runa da Ruína\n" +
                        "- Bateria Arcana"
        ));
        pages.add(page(
                "§bSigilos (visuais)\n\n" +
                        "✶ Celeste\n" +
                        "✿ Crescimento\n" +
                        "◆ Gravitacional\n" +
                        "⬢ Transmutação\n" +
                        "⬡ Essência\n\n" +
                        "Use Shift + clique direito com Giz Arcano para alternar modos."
        ));
        pages.add(page(
                "§6Diagrama de Ritual\n\n" +
                        "  NE   N   NW\n" +
                        "   \\   |   /\n" +
                        "W -- ALTAR -- E\n" +
                        "   /   |   \\\n" +
                        "  SE   S   SW\n\n" +
                        "Cada estilo define qual sigilo vai em cada posição."
        ));
        return pages;
    }

    private static ListTag createRitualPages() {
        ListTag pages = new ListTag();
        pages.add(page(
                "§dRitual: Condensação\n\n" +
                        "Objetivo: Catalisador Mágico\n\n" +
                        "N/S: Celeste\n" +
                        "L/O: Crescimento\n" +
                        "Diagonais: Gravitacional\n\n" +
                        "Mão principal: Núcleo Catalisador"
        ));
        pages.add(page(
                "§6Ritual: Forja de Runas\n\n" +
                        "N/S: Celeste\n" +
                        "L/O: Transmutação\n" +
                        "Diagonais: Crescimento\n\n" +
                        "Mão principal: Runa da Ruína\n" +
                        "Mão secundária: reagente"
        ));
        pages.add(page(
                "§aReagentes de Forja\n\n" +
                        "Ruína + Trigo -> Colheita\n" +
                        "Ruína + Cenoura Dourada -> Vitalidade\n" +
                        "Ruína + Redstone -> Fluxo\n" +
                        "Ruína + Baú -> Armazenamento"
        ));
        pages.add(page(
                "§cRitual: Fusão\n\n" +
                        "N/S: Essência\n" +
                        "L/O: Gravitacional\n" +
                        "Diagonais: Transmutação\n\n" +
                        "Use duas runas (principal + secundária)."
        ));
        pages.add(page(
                "§eCombinações de Fusão\n\n" +
                        "Colheita + Vitalidade -> Slime x4\n" +
                        "Fluxo + Armazenamento -> Ender Pearl x2\n" +
                        "Ruína + Fluxo -> Nether Star x1\n\n" +
                        "Todos os rituais exigem céu aberto."
        ));
        return pages;
    }

    private static ListTag createArtifactPages() {
        ListTag pages = new ListTag();
        pages.add(page(
                "§9Artefatos Principais\n\n" +
                        "Bateria Arcana:\nacumula energia sob céu aberto.\n\n" +
                        "Bancada de Colheita:\nautomatiza plantas e animais com runas.\n\n" +
                        "Catalisador de Runas:\ninstala runas em blocos."
        ));
        pages.add(page(
                "§3Grimório Arcano\n\n" +
                        "Feitiços:\n" +
                        "- Luz Astral\n" +
                        "- Pulso Verdejante\n" +
                        "- Passo Etéreo\n\n" +
                        "Use Shift + clique para alternar feitiços."
        ));
        pages.add(page(
                "§dVínculo Astral\n\n" +
                        "1) Clique na bancada\n" +
                        "2) Clique no baú alvo\n\n" +
                        "Depois disso a bancada teleporta itens para o inventário vinculado."
        ));
        pages.add(page(
                "§6Como criar itens-chave\n\n" +
                        "Altar Ritual:\nPedra + ametista + runa.\n\n" +
                        "Bateria Arcana:\nPedra + ametista + catalisador.\n\n" +
                        "Codex avançado:\nCodex Rúnico + item temático."
        ));
        return pages;
    }

    private static ListTag createProgressionPages() {
        ListTag pages = new ListTag();
        pages.add(page(
                "§5Progressão Recomendada\n\n" +
                        "Etapa 1: Giz Arcano + Altar\n" +
                        "Etapa 2: Condensar Catalisador\n" +
                        "Etapa 3: Forjar runas\n" +
                        "Etapa 4: Bateria + Bancada\n" +
                        "Etapa 5: Fusão e automação"
        ));
        pages.add(page(
                "§bFluxo de Energia\n\n" +
                        "Céu aberto -> Bateria/Catalisador -> Itens rúnicos\n\n" +
                        "Sem carga suficiente, a automação perde eficiência.\n" +
                        "Mantenha baterias próximas da base."
        ));
        pages.add(page(
                "§aChecklist de Ritual\n\n" +
                        "[ ] Céu aberto\n" +
                        "[ ] Padrão correto\n" +
                        "[ ] Item principal certo\n" +
                        "[ ] Item secundário (quando exigido)\n" +
                        "[ ] Espaço para receber resultado"
        ));
        pages.add(page(
                "§eDicas de Build\n\n" +
                        "Monte uma sala ritual com marcações no chão:\n" +
                        "- Ala de condensação\n" +
                        "- Ala de forja\n" +
                        "- Ala de fusão\n\n" +
                        "Isso evita erro de padrão durante uso rápido."
        ));
        return pages;
    }

    private static StringTag page(String text) {
        return StringTag.valueOf(Component.Serializer.toJson(Component.literal(text)));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ruinaarcana.codex_runico.line1").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("tooltip.ruinaarcana.codex_runico.line2").withStyle(ChatFormatting.GRAY));
    }
}

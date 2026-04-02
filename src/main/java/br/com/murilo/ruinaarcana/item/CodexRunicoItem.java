package br.com.murilo.ruinaarcana.item;

import br.com.murilo.ruinaarcana.RuinaArcanaMod;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class CodexRunicoItem extends Item {

    public enum CodexType {
        GERAL("codex_arcano"),
        RITUAIS("codex_rituais"),
        ARTEFATOS("codex_artefatos"),
        PROGRESSAO("codex_progressao");

        private final String patchouliBookId;

        CodexType(String patchouliBookId) {
            this.patchouliBookId = patchouliBookId;
        }
    }

    private static final ResourceLocation PATCHOULI_GUIDE_BOOK = ResourceLocation.fromNamespaceAndPath("patchouli", "guide_book");

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
        if (guide.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.ruinaarcana.codex_runico.unavailable"), true);
            return InteractionResultHolder.fail(stack);
        }

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
        Item patchouliGuideBook = ForgeRegistries.ITEMS.getValue(PATCHOULI_GUIDE_BOOK);
        if (patchouliGuideBook == null) {
            return ItemStack.EMPTY;
        }

        ItemStack guide = new ItemStack(patchouliGuideBook);
        CompoundTag tag = guide.getOrCreateTag();
        tag.putString("patchouli:book", ResourceLocation.fromNamespaceAndPath(RuinaArcanaMod.MOD_ID, codexType.patchouliBookId).toString());
        return guide;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ruinaarcana.codex_runico.line1").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("tooltip.ruinaarcana.codex_runico.line2").withStyle(ChatFormatting.GRAY));
    }
}

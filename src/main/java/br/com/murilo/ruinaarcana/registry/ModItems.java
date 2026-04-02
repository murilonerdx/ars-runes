package br.com.murilo.ruinaarcana.registry;

import br.com.murilo.ruinaarcana.RuinaArcanaMod;
import br.com.murilo.ruinaarcana.item.ArcaneChalkItem;
import br.com.murilo.ruinaarcana.item.AstralLinkerItem;
import br.com.murilo.ruinaarcana.item.CodexRunicoItem;
import br.com.murilo.ruinaarcana.item.EntropyRuneItem;
import br.com.murilo.ruinaarcana.item.FarmRuneItem;
import br.com.murilo.ruinaarcana.item.GrimorioArcanoItem;
import br.com.murilo.ruinaarcana.item.JarroArcanoItem;
import br.com.murilo.ruinaarcana.item.MagicCatalystItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, RuinaArcanaMod.MOD_ID);

    public static final RegistryObject<Item> GIZ_ARCANO = ITEMS.register("giz_arcano",
            () -> new ArcaneChalkItem(new Item.Properties()
                    .durability(128)
                    .rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> NUCLEO_CATALISADOR = ITEMS.register("nucleo_catalisador",
            () -> new Item(new Item.Properties()
                    .stacksTo(16)
                    .rarity(Rarity.RARE)));

    public static final RegistryObject<Item> CATALISADOR_MAGICO = ITEMS.register("catalisador_magico",
            () -> new MagicCatalystItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.RARE)));

    public static final RegistryObject<Item> CATALISADOR_DE_RUNAS = ITEMS.register("catalisador_de_runas",
            () -> new BlockItem(ModBlocks.CATALISADOR_DE_RUNAS.get(), new Item.Properties()));

    public static final RegistryObject<Item> RUNA_DA_RUINA = ITEMS.register("runa_da_ruina",
            () -> new EntropyRuneItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> RUNA_DA_COLHEITA = ITEMS.register("runa_da_colheita",
            () -> new FarmRuneItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE),
                    "tooltip.ruinaarcana.runa_da_colheita.line1",
                    "tooltip.ruinaarcana.runa_da_colheita.line2"));

    public static final RegistryObject<Item> RUNA_DA_VITALIDADE = ITEMS.register("runa_da_vitalidade",
            () -> new FarmRuneItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE),
                    "tooltip.ruinaarcana.runa_da_vitalidade.line1",
                    "tooltip.ruinaarcana.runa_da_vitalidade.line2"));

    public static final RegistryObject<Item> RUNA_DO_FLUXO = ITEMS.register("runa_do_fluxo",
            () -> new FarmRuneItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE),
                    "tooltip.ruinaarcana.runa_do_fluxo.line1",
                    "tooltip.ruinaarcana.runa_do_fluxo.line2"));

    public static final RegistryObject<Item> RUNA_DO_ARMAZENAMENTO = ITEMS.register("runa_do_armazenamento",
            () -> new FarmRuneItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE),
                    "tooltip.ruinaarcana.runa_do_armazenamento.line1",
                    "tooltip.ruinaarcana.runa_do_armazenamento.line2"));

    public static final RegistryObject<Item> VINCULO_ASTRAL = ITEMS.register("vinculo_astral",
            () -> new AstralLinkerItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> GRIMORIO_ARCANO = ITEMS.register("grimorio_arcano",
            () -> new GrimorioArcanoItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.RARE)));

    public static final RegistryObject<Item> CODEX_RUNICO = ITEMS.register("codex_runico",
            () -> new CodexRunicoItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> JARRO_ARCANO = ITEMS.register("jarro_arcano",
            () -> new JarroArcanoItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.RARE)));

    public static final RegistryObject<Item> ALTAR_RITUAL = ITEMS.register("altar_ritual",
            () -> new BlockItem(ModBlocks.ALTAR_RITUAL.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> BATERIA_ARCANA = ITEMS.register("bateria_arcana",
            () -> new BlockItem(ModBlocks.BATERIA_ARCANA.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> BANCADA_COLHEITA_ARCANA = ITEMS.register("bancada_colheita_arcana",
            () -> new BlockItem(ModBlocks.BANCADA_COLHEITA_ARCANA.get(), new Item.Properties().rarity(Rarity.RARE)));

    public static final RegistryObject<Item> SIGILO_CELESTE = ITEMS.register("sigilo_celeste",
            () -> new BlockItem(ModBlocks.SIGILO_CELESTE.get(), new Item.Properties()));

    public static final RegistryObject<Item> SIGILO_CRESCIMENTO = ITEMS.register("sigilo_crescimento",
            () -> new BlockItem(ModBlocks.SIGILO_CRESCIMENTO.get(), new Item.Properties()));

    public static final RegistryObject<Item> SIGILO_GRAVITACIONAL = ITEMS.register("sigilo_gravitacional",
            () -> new BlockItem(ModBlocks.SIGILO_GRAVITACIONAL.get(), new Item.Properties()));

    public static boolean isFarmRune(ItemStack stack) {
        return stack.is(RUNA_DA_RUINA.get())
                || stack.is(RUNA_DA_COLHEITA.get())
                || stack.is(RUNA_DA_VITALIDADE.get())
                || stack.is(RUNA_DO_FLUXO.get())
                || stack.is(RUNA_DO_ARMAZENAMENTO.get());
    }

    private ModItems() {
    }
}

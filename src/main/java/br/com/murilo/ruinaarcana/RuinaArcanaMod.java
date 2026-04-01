package br.com.murilo.ruinaarcana;

import br.com.murilo.ruinaarcana.config.RuinaArcanaConfig;
import br.com.murilo.ruinaarcana.registry.ModBlockEntities;
import br.com.murilo.ruinaarcana.registry.ModBlocks;
import br.com.murilo.ruinaarcana.registry.ModCreativeModeTabs;
import br.com.murilo.ruinaarcana.registry.ModItems;
import br.com.murilo.ruinaarcana.registry.ModMenus;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(RuinaArcanaMod.MOD_ID)
public class RuinaArcanaMod {

    public static final String MOD_ID = "ruinaarcana";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RuinaArcanaMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModCreativeModeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        context.registerConfig(ModConfig.Type.COMMON, RuinaArcanaConfig.SPEC);
    }
}

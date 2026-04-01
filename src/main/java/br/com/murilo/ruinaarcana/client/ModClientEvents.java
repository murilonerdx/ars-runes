package br.com.murilo.ruinaarcana.client;

import br.com.murilo.ruinaarcana.RuinaArcanaMod;
import br.com.murilo.ruinaarcana.client.screen.ArcaneBatteryScreen;
import br.com.murilo.ruinaarcana.client.screen.ArcaneHarvestBenchScreen;
import br.com.murilo.ruinaarcana.registry.ModBlockEntities;
import br.com.murilo.ruinaarcana.registry.ModMenus;
import br.com.murilo.ruinaarcana.renderer.RuneCatalystBlockEntityRenderer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = RuinaArcanaMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModClientEvents {

    private ModClientEvents() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.ARCANE_HARVEST_BENCH.get(), ArcaneHarvestBenchScreen::new);
            MenuScreens.register(ModMenus.ARCANE_BATTERY.get(), ArcaneBatteryScreen::new);
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                ModBlockEntities.CATALISADOR_DE_RUNAS.get(),
                RuneCatalystBlockEntityRenderer::new
        );
    }
}
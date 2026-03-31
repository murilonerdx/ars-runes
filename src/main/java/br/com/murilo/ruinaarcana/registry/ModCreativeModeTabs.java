package br.com.murilo.ruinaarcana.registry;

import br.com.murilo.ruinaarcana.RuinaArcanaMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RuinaArcanaMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> RUINA_ARCANA =
            CREATIVE_MODE_TABS.register("ruina_arcana", () -> CreativeModeTab.builder()
                    .title(Component.translatable("item_group." + RuinaArcanaMod.MOD_ID + ".ruina_arcana"))
                    .icon(() -> new ItemStack(ModItems.BANCADA_COLHEITA_ARCANA.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.GIZ_ARCANO.get());
                        output.accept(ModItems.NUCLEO_CATALISADOR.get());
                        output.accept(ModItems.CATALISADOR_MAGICO.get());
                        output.accept(ModItems.RUNA_DA_RUINA.get());
                        output.accept(ModItems.VINCULO_ASTRAL.get());
                        output.accept(ModItems.ALTAR_RITUAL.get());
                        output.accept(ModItems.BATERIA_ARCANA.get());
                        output.accept(ModItems.BANCADA_COLHEITA_ARCANA.get());
                        output.accept(ModItems.SIGILO_CELESTE.get());
                        output.accept(ModItems.SIGILO_CRESCIMENTO.get());
                        output.accept(ModItems.SIGILO_GRAVITACIONAL.get());
                    })
                    .build());

    private ModCreativeModeTabs() {
    }
}

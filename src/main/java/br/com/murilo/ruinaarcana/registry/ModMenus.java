package br.com.murilo.ruinaarcana.registry;

import br.com.murilo.ruinaarcana.RuinaArcanaMod;
import br.com.murilo.ruinaarcana.menu.ArcaneBatteryMenu;
import br.com.murilo.ruinaarcana.menu.ArcaneHarvestBenchMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, RuinaArcanaMod.MOD_ID);

    public static final RegistryObject<MenuType<ArcaneHarvestBenchMenu>> ARCANE_HARVEST_BENCH =
            MENUS.register("arcane_harvest_bench", () -> IForgeMenuType.create(ArcaneHarvestBenchMenu::new));

    public static final RegistryObject<MenuType<ArcaneBatteryMenu>> ARCANE_BATTERY =
            MENUS.register("arcane_battery", () -> IForgeMenuType.create(ArcaneBatteryMenu::new));

    private ModMenus() {
    }
}

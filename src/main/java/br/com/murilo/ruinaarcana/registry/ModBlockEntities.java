package br.com.murilo.ruinaarcana.registry;

import br.com.murilo.ruinaarcana.RuinaArcanaMod;
import br.com.murilo.ruinaarcana.block.entity.ArcaneBatteryBlockEntity;
import br.com.murilo.ruinaarcana.block.entity.ArcaneHarvestBenchBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, RuinaArcanaMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<ArcaneBatteryBlockEntity>> BATERIA_ARCANA =
            BLOCK_ENTITY_TYPES.register("bateria_arcana",
                    () -> BlockEntityType.Builder.of(ArcaneBatteryBlockEntity::new, ModBlocks.BATERIA_ARCANA.get()).build(null));

    public static final RegistryObject<BlockEntityType<ArcaneHarvestBenchBlockEntity>> BANCADA_COLHEITA_ARCANA =
            BLOCK_ENTITY_TYPES.register("bancada_colheita_arcana",
                    () -> BlockEntityType.Builder.of(ArcaneHarvestBenchBlockEntity::new, ModBlocks.BANCADA_COLHEITA_ARCANA.get()).build(null));

    private ModBlockEntities() {
    }
}

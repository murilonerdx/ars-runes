package br.com.murilo.ruinaarcana.registry;

import br.com.murilo.ruinaarcana.RuinaArcanaMod;
import br.com.murilo.ruinaarcana.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.joml.Vector3f;

public final class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, RuinaArcanaMod.MOD_ID);

    public static final RegistryObject<Block> ALTAR_RITUAL = BLOCKS.register("altar_ritual",
            () -> new RitualAltarBlock(BlockBehaviour.Properties.of()
                    .strength(3.5F, 6.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> BATERIA_ARCANA = BLOCKS.register("bateria_arcana",
            () -> new ArcaneBatteryBlock(BlockBehaviour.Properties.of()
                    .strength(3.0F, 5.0F)
                    .sound(SoundType.AMETHYST)
                    .lightLevel(state -> 2 + state.getValue(ArcaneBatteryBlock.CHARGE_STAGE) * 2)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> BANCADA_COLHEITA_ARCANA = BLOCKS.register("bancada_colheita_arcana",
            () -> new ArcaneHarvestBenchBlock(BlockBehaviour.Properties.of()
                    .strength(4.0F, 6.0F)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> 4)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> SIGILO_CELESTE = BLOCKS.register("sigilo_celeste",
            () -> new SigilBlock(
                    BlockBehaviour.Properties.of()
                            .instabreak()
                            .noCollission()
                            .noOcclusion()
                            .lightLevel(state -> 10)
                            .sound(SoundType.WOOL),
                    new Vector3f(0.45F, 0.85F, 1.0F)
            ));

    public static final RegistryObject<Block> CATALISADOR_DE_RUNAS = BLOCKS.register("catalisador_de_runas",
            () -> new RuneCatalystBlock(BlockBehaviour.Properties.of()
                    .strength(3.0F, 5.0F)
                    .sound(SoundType.AMETHYST)
                    .lightLevel(state -> 6)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> SIGILO_CRESCIMENTO = BLOCKS.register("sigilo_crescimento",
            () -> new SigilBlock(
                    BlockBehaviour.Properties.of()
                            .instabreak()
                            .noCollission()
                            .noOcclusion()
                            .lightLevel(state -> 10)
                            .sound(SoundType.WOOL),
                    new Vector3f(0.35F, 1.0F, 0.45F)
            ));

    public static final RegistryObject<Block> SIGILO_GRAVITACIONAL = BLOCKS.register("sigilo_gravitacional",
            () -> new SigilBlock(
                    BlockBehaviour.Properties.of()
                            .instabreak()
                            .noCollission()
                            .noOcclusion()
                            .lightLevel(state -> 10)
                            .sound(SoundType.WOOL),
                    new Vector3f(0.75F, 0.45F, 1.0F)
            ));

    private ModBlocks() {
    }
}

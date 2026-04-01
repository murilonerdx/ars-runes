package br.com.murilo.ruinaarcana.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class RuinaArcanaConfig {

    public static final ForgeConfigSpec SPEC;
    public static final Values VALUES;

    static {
        Pair<Values, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(Values::new);
        VALUES = pair.getLeft();
        SPEC = pair.getRight();
    }

    private RuinaArcanaConfig() {
    }

    public static final class Values {

        public final ForgeConfigSpec.IntValue runeRadius;
        public final ForgeConfigSpec.IntValue runePulseIntervalTicks;
        public final ForgeConfigSpec.IntValue runeExtraRandomTicks;
        public final ForgeConfigSpec.IntValue runeActiveSeconds;
        public final ForgeConfigSpec.IntValue runeActivationCost;
        public final ForgeConfigSpec.DoubleValue runePullStrength;
        public final ForgeConfigSpec.IntValue runeMaxCharge;

        public final ForgeConfigSpec.IntValue catalystMaxCharge;
        public final ForgeConfigSpec.IntValue catalystSkyChargePerPulse;
        public final ForgeConfigSpec.IntValue catalystTransferPerPulse;
        public final ForgeConfigSpec.IntValue catalystTransferRadius;
        public final ForgeConfigSpec.IntValue catalystPulseIntervalTicks;

        public final ForgeConfigSpec.IntValue batteryMaxCharge;
        public final ForgeConfigSpec.IntValue batterySkyChargePerPulse;
        public final ForgeConfigSpec.IntValue batteryTransferPerPulse;
        public final ForgeConfigSpec.IntValue batteryTransferRadius;
        public final ForgeConfigSpec.IntValue batteryPulseIntervalTicks;
        public final ForgeConfigSpec.IntValue runePulseEnergyCost;
        public final ForgeConfigSpec.IntValue harvestBenchMaxCharge;
        public final ForgeConfigSpec.IntValue harvestBenchPullPerPulse;
        public final ForgeConfigSpec.IntValue harvestBenchBatteryRadius;
        public final ForgeConfigSpec.IntValue harvestBenchWorkRadius;
        public final ForgeConfigSpec.IntValue harvestBenchPulseIntervalTicks;
        public final ForgeConfigSpec.IntValue harvestBenchCropEnergyCost;
        public final ForgeConfigSpec.IntValue harvestBenchAnimalEnergyCost;
        public final ForgeConfigSpec.IntValue harvestBenchTeleportEnergyPerItem;
        public final ForgeConfigSpec.IntValue harvestBenchTeleportItemsPerPulse;
        public final ForgeConfigSpec.IntValue harvestBenchAnimalKeepCount;

        public final ForgeConfigSpec.IntValue runeCatalystPulseIntervalTicks;
        public final ForgeConfigSpec.IntValue runeCatalystChargePerPulse;
        public final ForgeConfigSpec.IntValue runeCatalystBatteryRadius;

        private Values(ForgeConfigSpec.Builder builder) {
            builder.push("runa_da_ruina");
            runeCatalystPulseIntervalTicks = builder
                    .defineInRange("runeCatalystPulseIntervalTicks", 20, 1, 200);

            runeCatalystChargePerPulse = builder
                    .defineInRange("runeCatalystChargePerPulse", 10, 1, 1000);

            runeCatalystBatteryRadius = builder
                    .defineInRange("runeCatalystBatteryRadius", 6, 1, 32);
            this.runePulseEnergyCost = builder
                    .comment("Energia gasta por pulso da Runa da Ruína")
                    .defineInRange("runePulseEnergyCost", 2, 1, 1000);
            runeRadius = builder.comment("Raio do campo temporal da runa.").defineInRange("radius", 4, 1, 12);
            runePulseIntervalTicks = builder.comment("Intervalo entre pulsos da runa.").defineInRange("pulseIntervalTicks", 10, 1, 200);
            runeExtraRandomTicks = builder.comment("Random ticks extras em blocos próximos.").defineInRange("extraRandomTicks", 3, 0, 64);
            runeActiveSeconds = builder.comment("Tempo ativo por uso.").defineInRange("activeSeconds", 18, 1, 300);
            runeActivationCost = builder.comment("Carga consumida para ativar a runa.").defineInRange("activationCost", 240, 1, 20000);
            runePullStrength = builder.comment("Força de tração sobre criaturas próximas.").defineInRange("pullStrength", 0.15D, 0.01D, 2.0D);
            runeMaxCharge = builder.comment("Carga máxima da runa.").defineInRange("maxCharge", 2500, 1, 100000);
            builder.pop();

            builder.push("catalisador_magico");
            catalystMaxCharge = builder.comment("Carga máxima do catalisador.").defineInRange("maxCharge", 12000, 1, 100000);
            catalystSkyChargePerPulse = builder.comment("Carga recebida do céu por pulso.").defineInRange("skyChargePerPulse", 14, 0, 500);
            catalystTransferPerPulse = builder.comment("Carga transferida por pulso para itens próximos.").defineInRange("transferPerPulse", 18, 0, 500);
            catalystTransferRadius = builder.comment("Raio de transferência do catalisador.").defineInRange("transferRadius", 5, 1, 16);
            catalystPulseIntervalTicks = builder.comment("Intervalo de pulso do catalisador.").defineInRange("pulseIntervalTicks", 20, 1, 200);
            builder.pop();

            builder.push("bateria_arcana");
            batteryMaxCharge = builder.comment("Carga máxima da bateria.").defineInRange("maxCharge", 2000, 1, 500000);
            batterySkyChargePerPulse = builder.comment("Carga recebida do céu por pulso.").defineInRange("skyChargePerPulse", 8, 0, 500);
            batteryTransferPerPulse = builder.comment("Carga enviada por pulso.").defineInRange("transferPerPulse", 24, 0, 500);
            batteryTransferRadius = builder.comment("Raio de distribuição da bateria.").defineInRange("transferRadius", 6, 1, 24);
            batteryPulseIntervalTicks = builder.comment("Intervalo de pulso da bateria.").defineInRange("pulseIntervalTicks", 20, 1, 200);
            builder.pop();

            builder.push("bancada_colheita_arcana");
            harvestBenchMaxCharge = builder.comment("Carga máxima armazenada na bancada.").defineInRange("maxCharge", 8000, 1, 100000);
            harvestBenchPullPerPulse = builder.comment("Carga puxada de baterias por pulso.").defineInRange("pullPerPulse", 60, 0, 2000);
            harvestBenchBatteryRadius = builder.comment("Raio para puxar energia das baterias.").defineInRange("batteryRadius", 8, 1, 32);
            harvestBenchWorkRadius = builder.comment("Raio de trabalho da bancada.").defineInRange("workRadius", 6, 1, 16);
            harvestBenchPulseIntervalTicks = builder.comment("Intervalo de pulso da bancada.").defineInRange("pulseIntervalTicks", 20, 1, 200);
            harvestBenchCropEnergyCost = builder.comment("Custo por colheita de planta.").defineInRange("cropEnergyCost", 16, 0, 1000);
            harvestBenchAnimalEnergyCost = builder.comment("Custo por coleta de carne.").defineInRange("animalEnergyCost", 70, 0, 5000);
            harvestBenchTeleportEnergyPerItem = builder.comment("Custo por item teleportado ao baú vinculado.").defineInRange("teleportEnergyPerItem", 3, 0, 1000);
            harvestBenchTeleportItemsPerPulse = builder.comment("Máximo de itens teleportados por pulso.").defineInRange("teleportItemsPerPulse", 16, 0, 4096);
            harvestBenchAnimalKeepCount = builder.comment("Quantidade mínima de animais adultos por espécie a ser mantida.").defineInRange("animalKeepCount", 2, 0, 16);
            builder.pop();
        }
    }
}

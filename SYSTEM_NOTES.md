# Notas técnicas

## Sistema de carga
- `ArcaneChargeable` define quem aceita carga.
- `ArcaneChargeHelper` lê/escreve carga em NBT.
- `ArcaneEnergyNetworkHelper` puxa energia das `ArcaneBatteryBlockEntity` próximas.
- Hoje a runa e o catalisador implementam `ArcaneChargeable`.
- A bancada usa reservatório interno simples e consome energia por ação.

## Ritual
O altar usa validação simples de padrão no mundo, sem block entity dedicada.
Isso deixa o ritual leve e fácil de evoluir.

## Bateria
A bateria usa:
- block entity para armazenar carga
- blockstate `charge_stage` para feedback visual
- mensagem no clique direito
- sinal de comparador
- método `extractCharge(int)` para abastecer máquinas mágicas

## Efeito da runa
A runa:
- consome carga para ativar
- acelera `randomTick` ao redor
- puxa criaturas para o centro
- desenha um círculo de partículas

## Bancada de Colheita Arcana
Arquivos centrais:
- `block/ArcaneHarvestBenchBlock.java`
- `block/entity/ArcaneHarvestBenchBlockEntity.java`
- `item/AstralLinkerItem.java`
- `magic/ArcaneEnergyNetworkHelper.java`

Responsabilidades:
- instala a Runa da Ruína diretamente no bloco;
- puxa energia de baterias próximas;
- colhe crops maduros e recomeça o ciclo;
- coleta drops soltos para o inventário interno;
- elimina apenas o excedente de animais adultos;
- teleporta itens para o inventário vinculado usando `ForgeCapabilities.ITEM_HANDLER`.

## Compatibilidade futura
Para evoluir com outros mods, os melhores próximos passos são:
- trocar o inventário interno da bancada por um menu/GUI;
- adicionar tags de crops especiais;
- migrar parte do sistema de energia para capability dedicada;
- adicionar filtros de item na rotina de teleporte.

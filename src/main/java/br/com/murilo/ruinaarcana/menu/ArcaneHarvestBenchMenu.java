package br.com.murilo.ruinaarcana.menu;

import br.com.murilo.ruinaarcana.block.entity.ArcaneHarvestBenchBlockEntity;
import br.com.murilo.ruinaarcana.registry.ModBlocks;
import br.com.murilo.ruinaarcana.registry.ModItems;
import br.com.murilo.ruinaarcana.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ArcaneHarvestBenchMenu extends AbstractContainerMenu {

    private static final int RUNE_SLOT = 0;
    private static final int STORAGE_SLOT_START = 1;
    private static final int STORAGE_SLOT_END = 19;
    private static final int PLAYER_INV_START = 19;
    private static final int PLAYER_INV_END = 46;

    private final ContainerLevelAccess access;
    private final ContainerData data;

    public ArcaneHarvestBenchMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(containerId, playerInventory, findBlockEntity(playerInventory, buffer.readBlockPos()), createFallbackData());
    }

    public ArcaneHarvestBenchMenu(int containerId, Inventory playerInventory, ArcaneHarvestBenchBlockEntity bench, ContainerData data) {
        super(ModMenus.ARCANE_HARVEST_BENCH.get(), containerId);
        this.access = bench.getLevel() == null ? ContainerLevelAccess.NULL : ContainerLevelAccess.create(bench.getLevel(), bench.getBlockPos());
        this.data = data;

        addSlot(new RuneSlot(bench.getRuneHandler(), 0, 26, 36));

        IItemHandler storageHandler = bench.getStorageHandler();
        int slot = 0;
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new ReadOnlySlot(storageHandler, slot++, 62 + col * 18, 18 + row * 18));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        addDataSlots(data);
    }

    private static ArcaneHarvestBenchBlockEntity findBlockEntity(Inventory playerInventory, BlockPos pos) {
        if (playerInventory.player.level().getBlockEntity(pos) instanceof ArcaneHarvestBenchBlockEntity bench) {
            return bench;
        }
        return ArcaneHarvestBenchBlockEntity.createClientDummy(pos);
    }

    private static ContainerData createFallbackData() {
        return new ContainerData() {
            private final int[] values = new int[4];

            @Override
            public int get(int index) {
                return values[index];
            }

            @Override
            public void set(int index, int value) {
                values[index] = value;
            }

            @Override
            public int getCount() {
                return values.length;
            }
        };
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.BANCADA_COLHEITA_ARCANA.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack quickMoved = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack raw = slot.getItem();
        quickMoved = raw.copy();

        if (index < PLAYER_INV_START) {
            if (!moveItemStackTo(raw, PLAYER_INV_START, PLAYER_INV_END, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (raw.is(ModItems.RUNA_DA_RUINA.get())) {
                if (!moveItemStackTo(raw, RUNE_SLOT, RUNE_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }
        }

        if (raw.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (raw.getCount() == quickMoved.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, raw);
        return quickMoved;
    }

    public int getCharge() {
        return data.get(0);
    }

    public int getMaxCharge() {
        return data.get(1);
    }

    public int getLinkedFlag() {
        return data.get(2);
    }

    public int getStoredStacks() {
        return data.get(3);
    }

    public boolean isLinked() {
        return getLinkedFlag() == 1;
    }

    private static final class RuneSlot extends SlotItemHandler {
        private RuneSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(ModItems.RUNA_DA_RUINA.get());
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    private static final class ReadOnlySlot extends SlotItemHandler {
        private ReadOnlySlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}

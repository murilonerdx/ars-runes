package br.com.murilo.ruinaarcana.menu;

import br.com.murilo.ruinaarcana.block.entity.ArcaneBatteryBlockEntity;
import br.com.murilo.ruinaarcana.registry.ModBlocks;
import br.com.murilo.ruinaarcana.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public class ArcaneBatteryMenu extends AbstractContainerMenu {

    private final ArcaneBatteryBlockEntity battery;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    // CLIENTE
    public ArcaneBatteryMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(
                containerId,
                playerInventory,
                ArcaneBatteryBlockEntity.createClientDummy(buf.readBlockPos()),
                new SimpleContainerData(2)
        );
    }

    // SERVIDOR
    public ArcaneBatteryMenu(int containerId, Inventory playerInventory, ArcaneBatteryBlockEntity battery, ContainerData data) {
        super(ModMenus.ARCANE_BATTERY.get(), containerId);
        this.battery = battery;
        this.data = data;
        this.access = ContainerLevelAccess.create(battery.getLevel(), battery.getBlockPos());

        checkContainerDataCount(data, 2);
        this.addDataSlots(data);

        // se quiser, aqui você adiciona slots do jogador
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new net.minecraft.world.inventory.Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        8 + col * 18,
                        84 + row * 18
                ));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new net.minecraft.world.inventory.Slot(
                    playerInventory,
                    col,
                    8 + col * 18,
                    142
            ));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.BATERIA_ARCANA.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    public int getCharge() {
        return data.get(0);
    }

    public int getMaxCharge() {
        return data.get(1);
    }

    public ArcaneBatteryBlockEntity getBattery() {
        return battery;
    }
}
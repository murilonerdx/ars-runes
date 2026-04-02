package br.com.murilo.ruinaarcana.client.screen;

import br.com.murilo.ruinaarcana.RuinaArcanaMod;
import br.com.murilo.ruinaarcana.menu.ArcaneBatteryMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ArcaneBatteryScreen extends AbstractContainerScreen<ArcaneBatteryMenu> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RuinaArcanaMod.MOD_ID, "textures/gui/arcane_battery.png");

    public ArcaneBatteryScreen(ArcaneBatteryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.titleLabelX = 0;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int charge = menu.getCharge();
        int maxCharge = Math.max(1, menu.getMaxCharge());

        int barHeight = 52;
        int filled = Math.min(barHeight, (int) Math.round((charge / (double) maxCharge) * barHeight));

        if (filled > 0) {
            guiGraphics.blit(TEXTURE, leftPos + 152, topPos + 69 - filled, 176, 52 - filled, 12, filled);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        Component energyText = Component.translatable("screen.ruinaarcana.arcane_battery.charge", menu.getCharge(), menu.getMaxCharge());
        int textX = this.imageWidth - 8 - this.font.width(energyText);
        guiGraphics.drawString(this.font, energyText, textX, 20, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}

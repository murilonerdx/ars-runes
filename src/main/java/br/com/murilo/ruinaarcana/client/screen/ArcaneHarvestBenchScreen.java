package br.com.murilo.ruinaarcana.client.screen;

import br.com.murilo.ruinaarcana.RuinaArcanaMod;
import br.com.murilo.ruinaarcana.menu.ArcaneHarvestBenchMenu;
import br.com.murilo.ruinaarcana.renderer.state.ArcaneHarvestBenchRangeState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;

public class ArcaneHarvestBenchScreen extends AbstractContainerScreen<ArcaneHarvestBenchMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(RuinaArcanaMod.MOD_ID, "textures/gui/arcane_harvest_bench.png");

    private Button toggleRangeButton;
    private boolean showRangeOverlay;

    public ArcaneHarvestBenchScreen(ArcaneHarvestBenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 230;
        this.imageHeight = 184;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();

        this.showRangeOverlay = this.minecraft != null
                && this.minecraft.level != null
                && ArcaneHarvestBenchRangeState.isEnabledFor(this.menu.getBenchPos(), this.minecraft.level.dimension());

        this.toggleRangeButton = this.addRenderableWidget(
                Button.builder(getToggleRangeText(), button -> toggleRangeOverlay())
                        .bounds(this.leftPos + 104, this.topPos + 99, 72, 20)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("-"), button -> sendMenuButton(ArcaneHarvestBenchMenu.BUTTON_RANGE_DOWN))
                        .bounds(this.leftPos + 181, this.topPos + 99, 20, 20)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("+"), button -> sendMenuButton(ArcaneHarvestBenchMenu.BUTTON_RANGE_UP))
                        .bounds(this.leftPos + 204, this.topPos + 99, 20, 20)
                        .build()
        );
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        if (this.minecraft != null && this.minecraft.level != null) {
            boolean trackingThisBench = ArcaneHarvestBenchRangeState.isEnabledFor(
                    this.menu.getBenchPos(),
                    this.minecraft.level.dimension()
            );

            this.showRangeOverlay = trackingThisBench;

            if (trackingThisBench) {
                ArcaneHarvestBenchRangeState.updateRange(this.menu.getWorkRange());
            }
        } else {
            this.showRangeOverlay = false;
        }

        if (this.toggleRangeButton != null) {
            this.toggleRangeButton.setMessage(getToggleRangeText());
        }
    }

    private void toggleRangeOverlay() {
        if (this.minecraft == null || this.minecraft.level == null) {
            return;
        }

        if (ArcaneHarvestBenchRangeState.isEnabledFor(this.menu.getBenchPos(), this.minecraft.level.dimension())) {
            ArcaneHarvestBenchRangeState.disable();
            this.showRangeOverlay = false;
        } else {
            ArcaneHarvestBenchRangeState.enable(
                    this.menu.getBenchPos(),
                    this.menu.getWorkRange(),
                    this.minecraft.level.dimension()
            );
            this.showRangeOverlay = true;
        }

        if (this.toggleRangeButton != null) {
            this.toggleRangeButton.setMessage(getToggleRangeText());
        }
    }

    @Override
    public void removed() {
        super.removed();

        if (this.minecraft != null
                && this.minecraft.level != null
                && ArcaneHarvestBenchRangeState.isEnabledFor(this.menu.getBenchPos(), this.minecraft.level.dimension())) {
            ArcaneHarvestBenchRangeState.updateRange(this.menu.getWorkRange());
        }
    }

    private Component getToggleRangeText() {
        return Component.literal(this.showRangeOverlay ? "Range ON" : "Range OFF");
    }

    private void sendMenuButton(int buttonId) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
        }
    }

    public boolean isShowingRangeOverlay() {
        return showRangeOverlay;
    }

    public ArcaneHarvestBenchMenu getBenchMenu() {
        return menu;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        int max = Math.max(1, menu.getMaxCharge());
        int filled = (int) Math.round((menu.getCharge() / (double) max) * 116.0D);
        guiGraphics.blit(TEXTURE, x + 105, y + 55, 0, 184, filled, 12);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, 0x4A3555, false);
        guiGraphics.drawString(this.font, Component.literal("Runa"), 18, 20, 0x5B4A63, false);
        guiGraphics.drawString(this.font, Component.literal("Storage"), 62, 6, 0x5B4A63, false);

        guiGraphics.drawString(
                this.font,
                Component.literal("Carga: " + menu.getCharge() + "/" + menu.getMaxCharge()),
                105, 42, 0x5B4A63, false
        );

        guiGraphics.drawString(
                this.font,
                Component.literal(menu.isLinked() ? "Ligada" : "Sem link"),
                105, 70,
                menu.isLinked() ? 0x2D7A52 : 0x8A3B4A,
                false
        );

        guiGraphics.drawString(
                this.font,
                Component.literal("Stacks: " + menu.getStoredStacks()),
                105, 84, 0x5B4A63, false
        );

        guiGraphics.drawString(
                this.font,
                Component.literal("Range: " + menu.getWorkRange()),
                105, 92, 0x5B4A63, false
        );

        guiGraphics.drawString(
                this.font,
                Component.literal(showRangeOverlay ? "Visualização 3D ativa" : "Visualização 3D desligada"),
                105, 120,
                showRangeOverlay ? 0x2D7A52 : 0x8A3B4A,
                false
        );

        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x4A3555, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
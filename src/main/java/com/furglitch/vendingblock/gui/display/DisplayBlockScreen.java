package com.furglitch.vendingblock.gui.display;

import java.util.ArrayList;
import java.util.List;

import com.furglitch.vendingblock.VendingBlock;
import com.furglitch.vendingblock.gui.components.FilterSlot;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class DisplayBlockScreen extends AbstractContainerScreen<DisplayBlockMenu> {

    private static final ResourceLocation BACKGROUND =  ResourceLocation.fromNamespaceAndPath(VendingBlock.MODID, "textures/gui/container/display.png");
    private static final ResourceLocation FACADE =  ResourceLocation.fromNamespaceAndPath(VendingBlock.MODID, "textures/gui/container/slot/facade.png");

    public DisplayBlockScreen(DisplayBlockMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BACKGROUND);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(BACKGROUND, x, y, 0, 0, imageWidth, imageHeight);

        List<FilterSlot> filterSlots = getFilterSlots();
        if (isSlotEmpty(1, filterSlots)) guiGraphics.blit(FACADE, x + 98, y + 35, 0, 0, 16, 16, 16, 16);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        this.renderFilterSlotTooltips(guiGraphics, mouseX, mouseY);
    }

    public List<FilterSlot> getFilterSlots() {
        List<FilterSlot> filterSlots = new ArrayList<>();
        for (Slot slot : this.menu.slots) {
            if (slot instanceof FilterSlot filterSlot) {
                filterSlots.add(filterSlot);
            }
        }
        return filterSlots;
    }
    
    private void renderFilterSlotTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        
        List<FilterSlot> filterSlots = getFilterSlots();

        if (isMouseOverSlot(mouseX, mouseY, x + 98, y + 35) && isSlotEmpty(1, filterSlots)) {
            Component tooltip = Component.translatable("menu.roll_mod_shops.tooltip.facade");
            guiGraphics.renderTooltip(this.font, tooltip, mouseX, mouseY);
        }
    }
    
    private boolean isSlotEmpty(int slotIndex, List<FilterSlot> filterSlots) {
        for (FilterSlot slot : filterSlots) {
            if (slot.getSlotIndex() == slotIndex) {
                return !slot.hasItem();
            }
        }
        return true;
    }
    
    private boolean isMouseOverSlot(int mouseX, int mouseY, int slotX, int slotY) {
        return mouseX >= slotX && mouseX < slotX + 16 && mouseY >= slotY && mouseY < slotY + 16;
    }
}

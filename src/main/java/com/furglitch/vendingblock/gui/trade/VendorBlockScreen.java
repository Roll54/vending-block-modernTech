package com.furglitch.vendingblock.gui.trade;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.furglitch.vendingblock.Config;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;

public class VendorBlockScreen extends AbstractContainerScreen<VendorBlockMenu> {

    private static final ResourceLocation BACKGROUND =  ResourceLocation.fromNamespaceAndPath(VendingBlock.MODID, "textures/gui/container/vendor_trade.png");
    private static final ResourceLocation ARROW =  ResourceLocation.fromNamespaceAndPath(VendingBlock.MODID, "textures/gui/container/slot/arrow.png");
    private static final ResourceLocation FACADE =  ResourceLocation.fromNamespaceAndPath(VendingBlock.MODID, "textures/gui/container/slot/facade.png");

    public VendorBlockScreen(VendorBlockMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BACKGROUND);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(BACKGROUND, x, y, 0, 0, 176, 166);

        List<FilterSlot> filterSlots = getFilterSlots();
        if (isSlotEmpty(0, filterSlots)) guiGraphics.blit(ARROW, x + 26, y + 17, 0, 0, 16, 16, 16, 16);

        // Currency price display (replacing the old price slot)
        long price = menu.blockEntity.getCurrencyPrice();
        String priceText = price == 0L ? "FREE" : String.valueOf(price);
        int textX = x + 26 + 8 - (font.width(priceText) / 2);
        int textY = y + 53 + 4;
        guiGraphics.drawString(font, priceText, textX, textY, 0xFFFFFF, true);

        if (isSlotEmpty(11, filterSlots)) guiGraphics.blit(FACADE, x + 134, y + 17, 0, 0, 16, 16, 16, 16);
    }
    
    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        Component scrollMsg = null;
        int msgColor = 0xFFAA00; // Minecraft gold
        Slot hoveredSlot = this.getSlotUnderMouse();
        if (hoveredSlot instanceof FilterSlot filterSlot) {
            int slotIndex = filterSlot.getSlotIndex();
            if (slotIndex == 0) {
                ItemStack stack = hoveredSlot.getItem();
                scrollMsg = Component.translatable("menu.vendingblock.tooltip.scroll.product");

                if (!stack.isEmpty()) {
                    if (Config.Client.SCROLLTIP_POSITION.get() == Config.Client.ScrolltipPosition.ITEM_TOOLTIP) {
                        scrollMsg = scrollMsg.copy().withColor(msgColor);
                        List<Component> tooltipLines = stack.getTooltipLines(
                            TooltipContext.EMPTY, this.minecraft.player, TooltipFlag.Default.NORMAL
                        );
                        tooltipLines.add(scrollMsg);
                        pGuiGraphics.renderTooltip(this.font, tooltipLines, Optional.empty(), pMouseX, pMouseY);
                    } else {
                        int tooltipX = (this.width/2) - (this.font.width(scrollMsg) / 2);
                        int tooltipY = ((this.height - this.imageHeight) / 2) - (this.font.lineHeight + 2);
                        pGuiGraphics.drawString(this.font, scrollMsg, tooltipX, tooltipY, msgColor, false);
                        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
                    }
                }
                return;
            }
            if (slotIndex == 11) {
                this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
            }
        }
        this.renderFilterSlotTooltips(pGuiGraphics, pMouseX, pMouseY);
    }
    
    private void renderFilterSlotTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        
        List<FilterSlot> filterSlots = getFilterSlots();
        
        if (isMouseOverSlot(mouseX, mouseY, x + 26, y + 17) && isSlotEmpty(0, filterSlots)) {
            Component tooltip = Component.translatable("menu.vendingblock.tooltip.product");
            guiGraphics.renderTooltip(this.font, tooltip, mouseX, mouseY);
        }
        else if (isMouseOverSlot(mouseX, mouseY, x + 26, y + 53)) {
            long price = menu.blockEntity.getCurrencyPrice();
            Component tooltip = price == 0L ?
                Component.translatable("menu.vendingblock.tooltip.price.free") :
                Component.translatable("menu.vendingblock.tooltip.price.currency", price);
            guiGraphics.renderTooltip(this.font, tooltip, mouseX, mouseY);
        }
        else if (isMouseOverSlot(mouseX, mouseY, x + 134, y + 17) && isSlotEmpty(11, filterSlots)) {
            Component tooltip = Component.translatable("menu.vendingblock.tooltip.facade");
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

    public List<FilterSlot> getFilterSlots() {
        List<FilterSlot> filterSlots = new ArrayList<>();
        for (Slot slot : this.menu.slots) {
            if (slot instanceof FilterSlot filterSlot) {
                filterSlots.add(filterSlot);
            }
        }
        return filterSlots;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double unused, double delta) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Product slot scrolling
        if (isMouseOverSlot((int)mouseX, (int)mouseY, x + 26, y + 17)) {
            ItemStack stack = menu.blockEntity.inventory.getStackInSlot(0).copy();
            if (!stack.isEmpty()) {
                int newCount = stack.getCount() + (delta < 0 ? -1 : 1);
                newCount = Math.max(1, newCount);
                newCount = Math.min(newCount, 99);
                if (stack.getMaxStackSize() == 1) newCount = Math.min(newCount, 9);
                stack.setCount(newCount);
                com.furglitch.vendingblock.network.FilterSlotUpdatePacket packet =
                    new com.furglitch.vendingblock.network.FilterSlotUpdatePacket(
                        menu.blockEntity.getBlockPos(), 0, stack);
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(packet);
            }
            return true;
        }
        // Currency price scrolling
        else if (isMouseOverSlot((int)mouseX, (int)mouseY, x + 26, y + 53)) {
            long currentPrice = menu.blockEntity.getCurrencyPrice();
            long adjustment = delta < 0 ? -1 : 1;

            // Shift for larger adjustments
            if (hasShiftDown()) adjustment *= 10;
            // Ctrl for even larger adjustments
            if (hasControlDown()) adjustment *= 100;

            long newPrice = Math.max(0L, currentPrice + adjustment);
            newPrice = Math.min(newPrice, 999999999L); // Cap at ~1 billion

            com.furglitch.vendingblock.network.CurrencyPriceUpdatePacket packet =
                new com.furglitch.vendingblock.network.CurrencyPriceUpdatePacket(
                    menu.blockEntity.getBlockPos(), newPrice);
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(packet);
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, unused, delta);
    }

}
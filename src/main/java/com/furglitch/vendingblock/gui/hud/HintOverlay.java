package com.furglitch.vendingblock.gui.hud;

import com.furglitch.vendingblock.blockentity.VendorBlockEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

public class HintOverlay {
    
    private static final int ColorBCK = 0xC0161616;
    private static final int ColorBDR = 0xD0161616;
    private static final int ColorTXT = 0xFFFFFFFF;
    private static Component sellItemText = Component.literal(""), buyItemText = Component.literal(""), errorText = Component.literal("");
    private static int saleType = 0;

    @SubscribeEvent
    public static void onRenderGUI(RenderGuiEvent.Post event){

        if (ModList.get().isLoaded("jade")) return;

        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null || mc.level == null) return;

        HitResult hit = mc.hitResult;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockEntity blockEntity = mc.level.getBlockEntity(blockHit.getBlockPos());
        if (! (blockEntity instanceof VendorBlockEntity entity)) return;

        renderHint(event.getGuiGraphics(), entity, mc);

    }

    private static void renderHint(GuiGraphics gui, VendorBlockEntity entity, Minecraft mc) {
        ItemStack sellItem = entity.inventory.getStackInSlot(0); // product
        long currencyPrice = entity.getCurrencyPrice(); // currency price
        if (sellItem.isEmpty()) return;

        // Determine sale type: 1 = paid sale, 2 = giveaway
        saleType = currencyPrice > 0L ? 1 : 2;

        if (entity.hasError) errorText = getErrorString(entity.errorCode);

        HintDimensions dimensions = calculateDimensions(mc, entity.getOwnerUser(), sellItem, currencyPrice, saleType, mc.font.lineHeight+4, entity.hasError);

        int margin = 8;
        int w = dimensions.width + (margin * 2);
        int h = dimensions.height + (margin * 2);
        int x = (mc.getWindow().getGuiScaledWidth() - w) / 2;
        int y = 8;

        renderContent(gui, mc, entity.getOwnerUser(), sellItem, currencyPrice, x, y + margin, w, mc.font.lineHeight+4, entity.hasError);
        drawBackground(gui, x, y, w, h);
    }

    private static HintDimensions calculateDimensions(Minecraft mc, String owner, ItemStack sellItem, long currencyPrice, int saleType, int lineHeight, boolean error) {
        int maxWidth = 0;
        int totalHeight = 0;

        maxWidth = Math.max(maxWidth, mc.font.width(owner));
        totalHeight += lineHeight;
        switch (saleType) {
            case 1: // Paid sale with currency
                sellItemText = Component.translatable("hint.roll_mod_shops.sell");
                buyItemText = Component.translatable("hint.roll_mod_shops.buy");
                String priceText = currencyPrice + " coins";
                maxWidth = Math.max(maxWidth, mc.font.width(sellItemText.getString()));
                maxWidth = Math.max(maxWidth, mc.font.width(buyItemText.getString()));
                maxWidth = Math.max(maxWidth, mc.font.width(priceText));
                maxWidth = Math.max(maxWidth, calculateItemDimensions(mc, sellItem).width);
                totalHeight += lineHeight * 4.2;
                break;
            case 2: // Free giveaway
                sellItemText = Component.translatable("hint.roll_mod_shops.giveaway");
                maxWidth = Math.max(maxWidth, mc.font.width(sellItemText.getString()));
                maxWidth = Math.max(maxWidth, calculateItemDimensions(mc, sellItem).width);
                totalHeight += lineHeight * 2.1;
                break;
        }

        if (error) {
            maxWidth = Math.max(maxWidth, mc.font.width(errorText.getString()));
            totalHeight += lineHeight / 2;
        }


        return new HintDimensions(maxWidth, totalHeight);
    }
    
    private static ItemDimensions calculateItemDimensions(Minecraft mc, ItemStack item) {
        String itemText = formatItemText(item);
        int width = 16 + 4 + mc.font.width(itemText);
        return new ItemDimensions(width);
    }
    
    private static String formatItemText(ItemStack item) {
        Component itemName = item.getHoverName();
        return item.getCount() > 1 ? 
            itemName.getString() + " x" + item.getCount() : 
            itemName.getString();
    }

    private static void drawBackground(GuiGraphics gui, int x, int y, int w, int h) {
        gui.fill(x, y, x + w, y + h, ColorBCK);
        gui.fill(x, y, x + w, y + 1, ColorBDR);
        gui.fill(x, y + h - 1, x + w, y + h, ColorBDR);
        gui.fill(x, y, x + 1, y + h, ColorBDR);
        gui.fill(x + w - 1, y, x + w, y + h, ColorBDR);
    }

    public static void renderContent(GuiGraphics gui, Minecraft mc, String owner, ItemStack sellItem, long currencyPrice, int x, int y, int w, int h, boolean error) {
        drawText(gui, mc, owner, x, y, w, ColorTXT);
        y += h;
        
        switch (saleType) {
            case 1: // Paid sale
                drawText(gui, mc, sellItemText.getString(), x, y, w, ColorTXT & 0x80FFFFFF);
                y += h * 1.2;
                drawText(gui, mc, sellItem, x, y, w, ColorTXT);
                y += h;
                drawText(gui, mc, buyItemText.getString(), x, y, w, ColorTXT & 0x80FFFFFF);
                y += h * 1.2;
                drawText(gui, mc, currencyPrice + " coins", x, y, w, 0xFFFFD700); // Gold color for currency
                if (error) {
                    y += h;
                    drawText(gui, mc, errorText.getString(), x, y, w, 0xFFba3c3c);
                }
                break;
            case 2: // Free giveaway
                drawText(gui, mc, sellItemText.getString(), x, y, w, ColorTXT & 0x80FFFFFF);
                y += h * 1.2;
                drawText(gui, mc, sellItem, x, y, w, ColorTXT);
                if (error) {
                    y += h;
                    drawText(gui, mc, errorText.getString(), x, y, w, 0xFFba3c3c);
                }
                break;
            case 0:
                if (error) {
                    drawText(gui, mc, errorText.getString(), x, y, w, 0xFFba3c3c);
                }
                break;
        }
    }

    private static void drawText(GuiGraphics gui, Minecraft mc, String text, int x, int y, int w, int color) {
        int txtW = mc.font.width(text);
        int txtX = x + (w - txtW) / 2;
        gui.drawString(mc.font, text, txtX, y, color);
    }

    private static void drawText(GuiGraphics gui, Minecraft mc, ItemStack item, int x, int y, int w, int color) {
        String text = formatItemText(item);
        int txtW = mc.font.width(text);
        int totW = 16 + 4 + txtW; 
        int txtX = x + (w - totW) / 2;

        gui.renderItem(item, txtX, y - 5);
        gui.drawString(mc.font, text, txtX + 20, y, color);
    }

    public static Component getErrorString(int code) {
        if (code == 1) return Component.translatable("hint.roll_mod_shops.error.sold");
        else if (code == 2) return Component.translatable("hint.roll_mod_shops.error.full");
        else if (code == 3) return Component.translatable("hint.roll_mod_shops.error.empty");
        else return Component.literal("");
    }

    private static class HintDimensions {
        final int width;
        final int height;

        HintDimensions(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    private static class ItemDimensions {
        final int width;

        ItemDimensions(int width) {
            this.width = width;
        }
    }

}

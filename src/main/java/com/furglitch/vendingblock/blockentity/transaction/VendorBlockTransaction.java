package com.furglitch.vendingblock.blockentity.transaction;

import com.furglitch.vendingblock.Config;
import com.furglitch.vendingblock.blockentity.VendorBlockEntity;
import com.furglitch.vendingblock.gui.chat.Messages;
import com.roll_54.roll_mod_currency.currency.CurrencyRepository;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class VendorBlockTransaction {

    public static void purchase(Level level, Player buyer, VendorBlockEntity vendor) {
        if (!(buyer instanceof ServerPlayer serverBuyer)) {
            return; // Currency operations require ServerPlayer
        }

        ItemStack product = vendor.inventory.getStackInSlot(0);
        long price = vendor.getCurrencyPrice();

        if (product.isEmpty()) {
            buyer.sendSystemMessage(Messages.vendorEmpty());
            return;
        }

        ServerPlayer owner = vendor.getOwnerID() != null ?
            (ServerPlayer) level.getPlayerByUUID(vendor.getOwnerID()) : null;
        String ownerName = vendor.getOwnerUser();
        String playerName = buyer.getName().getString();

        boolean blockHasStock = VendorBlockInventory.checkStock(vendor, product);
        boolean playerHasSpace = VendorBlockInventory.checkInventorySpace(buyer, product);

        if (!blockHasStock) {
            buyer.sendSystemMessage(Messages.vendorSold());
            if (owner != null && Config.Client.OUT_OF_STOCK_MESSAGES.get()) {
                owner.sendSystemMessage(Messages.ownerSold());
            }
            vendor.checkErrorState();
            return;
        }

        if (!playerHasSpace) {
            buyer.sendSystemMessage(Messages.playerFull());
            vendor.checkErrorState();
            return;
        }

        if (price == 0L) {
            // Free giveaway
            giveProduct(buyer, vendor, product);
            buyer.sendSystemMessage(Messages.playerGiveaway(product.getCount(), product.getHoverName(), ownerName));
            if (owner != null && Config.Client.GIVEAWAY_MESSAGES.get()) {
                owner.sendSystemMessage(Messages.ownerGiveaway(product.getCount(), product.getHoverName(), playerName));
            }
        } else {
            // Currency purchase
            long playerBalance = CurrencyRepository.getBalance(serverBuyer.getUUID());

            if (playerBalance < price) {
                buyer.sendSystemMessage(Messages.playerInsufficientCurrency(price));
                vendor.checkErrorState();
                return;
            }

            // Execute currency transfer
            boolean transferSuccess = false;
            if (vendor.isDiscarding()) {
                // Just deduct from buyer, don't transfer to owner
                CurrencyRepository.setBalance(serverBuyer.getUUID(), playerBalance - price);
                transferSuccess = true;
            } else if (owner != null) {
                // Transfer from buyer to owner
                transferSuccess = transfer(serverBuyer, owner, price);
            } else {
                // Owner offline, just deduct from buyer
                CurrencyRepository.setBalance(serverBuyer.getUUID(), playerBalance - price);
                transferSuccess = true;
            }

            if (transferSuccess) {
                giveProduct(buyer, vendor, product);
                buyer.sendSystemMessage(Messages.playerBoughtCurrency(
                    product.getCount(), product.getHoverName(), ownerName, price));
                if (owner != null && Config.Client.PURCHASE_MESSAGES.get()) {
                    owner.sendSystemMessage(Messages.ownerSoldCurrency(
                        product.getCount(), product.getHoverName(), playerName, price));
                }
            } else {
                buyer.sendSystemMessage(Messages.transactionFailed());
            }
        }

        vendor.checkErrorState();
    }

    public static boolean transfer(ServerPlayer from, ServerPlayer to, long amount) {
        if (amount <= 0L) {
            return false;
        }
        if (from.getUUID().equals(to.getUUID())) {
            return false;
        }
        return CurrencyRepository.transfer(from.getUUID(), to.getUUID(), amount);
    }

    private static void giveProduct(Player buyer, VendorBlockEntity vendor, ItemStack product) {
        int stock = product.getCount();
        if (vendor.isInfinite()) {
            stock = 0;
        } else {
            for (int i = 1; i <= 9 && stock > 0; i++) {
                ItemStack slot = vendor.inventory.getStackInSlot(i);
                if (slot.isEmpty()) continue;
                if (ItemStack.isSameItemSameComponents(slot, product)) {
                    int available = slot.getCount();
                    int slotStock = Math.min(available, stock);
                    slot.shrink(slotStock);
                    stock -= slotStock;
                }
            }
        }

        int transfer = product.getCount() - stock;
        for (int i = 0; i < 36 && transfer > 0; i++) {
            ItemStack slot = buyer.getInventory().getItem(i);
            if (slot.isEmpty()) {
                int space = Math.min(transfer, product.getMaxStackSize());
                ItemStack insert = product.copy();
                insert.setCount(space);
                buyer.getInventory().setItem(i, insert);
                transfer -= space;
            } else if (ItemStack.isSameItemSameComponents(slot, product)) {
                int space = slot.getMaxStackSize() - slot.getCount();
                int freeSpace = Math.min(space, transfer);
                if (freeSpace > 0) {
                    slot.grow(freeSpace);
                    transfer -= freeSpace;
                }
            }
        }
    }
    
}

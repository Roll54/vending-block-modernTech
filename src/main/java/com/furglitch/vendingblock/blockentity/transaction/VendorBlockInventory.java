package com.furglitch.vendingblock.blockentity.transaction;

import com.furglitch.vendingblock.blockentity.VendorBlockEntity;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class VendorBlockInventory {

    public static boolean checkInventorySpace(Player buyer, ItemStack product) {
        ItemStack[] fakeInv = new ItemStack[36];
        for (int i = 0; i < 36; i++) {
            fakeInv[i] = buyer.getInventory().getItem(i).copy();
        }

        int remaining = product.getCount();
        for (int i = 0; i < 36 && remaining > 0; i++) {
            ItemStack slot = fakeInv[i];
            if (slot.isEmpty()) {
                int canFit = Math.min(remaining, product.getMaxStackSize());
                remaining -= canFit;
            } else if (ItemStack.isSameItemSameComponents(slot, product)) {
                int availableSpace = slot.getMaxStackSize() - slot.getCount();
                int canFit = Math.min(remaining, availableSpace);
                remaining -= canFit;
            }
        }

        fakeInv = null;
        return remaining <= 0;
    }

    public static boolean checkStock(VendorBlockEntity vendor, ItemStack product) {
        if (vendor.isInfinite()) return true;

        int stock = 0;
        for (int i = 1; i <= 9; i++) {
            ItemStack slot = vendor.inventory.getStackInSlot(i);
            if (slot.getItem().equals(product.getItem())) {
                stock += slot.getCount();
            }
        }

        return stock >= product.getCount();
    }

}

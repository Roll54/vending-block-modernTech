package com.furglitch.vendingblock.integration.jade;

import com.furglitch.vendingblock.blockentity.VendorBlockEntity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum VendorBlockDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof VendorBlockEntity entity) {
            String ownerUser = entity.getOwnerUser();
            if (ownerUser != null) tag.putString("owner", ownerUser);

            ItemStack product = entity.inventory.getStackInSlot(0);
            if (!product.isEmpty()) {
                tag.putString("productName", product.getHoverName().getString());
                tag.putInt("productCount", product.getCount());
            }

            long currencyPrice = entity.getCurrencyPrice();
            tag.putLong("currencyPrice", currencyPrice);

            tag.putBoolean("hasError", entity.hasError);
            tag.putInt("errorCode", entity.errorCode);
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath("vendingblock", "vendor_data");
    }
}

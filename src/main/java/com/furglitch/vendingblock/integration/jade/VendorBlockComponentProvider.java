package com.furglitch.vendingblock.integration.jade;

import com.furglitch.vendingblock.blockentity.VendorBlockEntity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum VendorBlockComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockEntity() instanceof VendorBlockEntity) {
            CompoundTag serverData = accessor.getServerData();

            boolean hasProduct = serverData.contains("productName");
            boolean hasPrice = serverData.contains("currencyPrice");

            if (hasProduct) {
                String product = serverData.getString("productName");
                int count = serverData.getInt("productCount");
                String text = count > 1 ? count + "x " + product : product;

                if (hasPrice) {
                    tooltip.add(Component.translatable("jade.roll_mod_shops.sell", text));
                } else {
                    tooltip.add(Component.translatable("jade.roll_mod_shops.giveaway", text));
                }
            }

            if (hasPrice) {
                long price = serverData.getLong("currencyPrice");

                if (price > 0) {
                    String priceText = price + " coins";
                    tooltip.add(Component.translatable("jade.roll_mod_shops.buy", priceText)
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFD700)))); // Gold color
                } else if (hasProduct) {
                    tooltip.add(Component.translatable("jade.roll_mod_shops.giveaway.free")
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x00FF00)))); // Green for free
                }
            }

            if (serverData.contains("owner")) tooltip.add(Component.translatable("jade.roll_mod_shops.owner", serverData.getString("owner")));

            if (serverData.contains("hasError")) {
                boolean hasError = serverData.getBoolean("hasError");
                int errorCode = serverData.getInt("errorCode");
                if (hasError) {
                    switch (errorCode) {
                        case 1:
                            tooltip.add(Component.translatable("jade.roll_mod_shops.error.sold").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xba3c3c))));
                            break;
                        case 2:
                            tooltip.add(Component.translatable("jade.roll_mod_shops.error.full").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xba3c3c))));
                            break;
                        case 3:
                            tooltip.add(Component.translatable("jade.roll_mod_shops.error.empty").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xba3c3c))));
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath("roll_mod_shops", "vendor_component");
    }

}
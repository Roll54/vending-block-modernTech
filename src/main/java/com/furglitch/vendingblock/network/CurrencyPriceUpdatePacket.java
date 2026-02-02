package com.furglitch.vendingblock.network;

import com.furglitch.vendingblock.blockentity.VendorBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CurrencyPriceUpdatePacket(BlockPos pos, long price) implements CustomPacketPayload {

    public static final Type<CurrencyPriceUpdatePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("vendingblock", "currency_price_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CurrencyPriceUpdatePacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, CurrencyPriceUpdatePacket::pos,
        ByteBufCodecs.VAR_LONG, CurrencyPriceUpdatePacket::price,
        CurrencyPriceUpdatePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(CurrencyPriceUpdatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            Level level = player.level();

            if (level.getBlockEntity(packet.pos()) instanceof VendorBlockEntity vendorBlockEntity) {
                if (vendorBlockEntity.isOwner(player)) {
                    vendorBlockEntity.setCurrencyPrice(packet.price());
                }
            }
        });
    }
}

package com.furglitch.vendingblock.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {
    
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(OwnerChangePacket.TYPE, OwnerChangePacket.STREAM_CODEC, OwnerChangePacket::handle);
        registrar.playToServer(InfiniteInventoryPacket.TYPE, InfiniteInventoryPacket.STREAM_CODEC, InfiniteInventoryPacket::handle);
        registrar.playToServer(DiscardsPaymentPacket.TYPE, DiscardsPaymentPacket.STREAM_CODEC, DiscardsPaymentPacket::handle);
        registrar.playToServer(FilterSlotUpdatePacket.TYPE, FilterSlotUpdatePacket.STREAM_CODEC, FilterSlotUpdatePacket::handle);
        registrar.playToServer(CurrencyPriceUpdatePacket.TYPE, CurrencyPriceUpdatePacket.STREAM_CODEC, CurrencyPriceUpdatePacket::handle);
    }
}

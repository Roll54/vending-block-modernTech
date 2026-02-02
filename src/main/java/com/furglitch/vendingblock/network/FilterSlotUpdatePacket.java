package com.furglitch.vendingblock.network;

import com.furglitch.vendingblock.blockentity.VendorBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FilterSlotUpdatePacket(BlockPos pos, int slotIndex, ItemStack stack) implements CustomPacketPayload {
    
    public static final Type<FilterSlotUpdatePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("vendingblock", "filter_slot_update"));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemStack> OPTIONAL_ITEM_STACK_CODEC = StreamCodec.of(
        (buf, stack) -> {
            if (stack.isEmpty()) {
                buf.writeBoolean(false);
            } else {
                buf.writeBoolean(true);
                ItemStack.STREAM_CODEC.encode(buf, stack);
            }
        },
        (buf) -> {
            if (buf.readBoolean()) {
                return ItemStack.STREAM_CODEC.decode(buf);
            } else {
                return ItemStack.EMPTY;
            }
        }
    );
    
    public static final StreamCodec<RegistryFriendlyByteBuf, FilterSlotUpdatePacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, FilterSlotUpdatePacket::pos,
        ByteBufCodecs.VAR_INT, FilterSlotUpdatePacket::slotIndex,
        OPTIONAL_ITEM_STACK_CODEC, FilterSlotUpdatePacket::stack,
        FilterSlotUpdatePacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(FilterSlotUpdatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            Level level = player.level();
            
            if (level.getBlockEntity(packet.pos()) instanceof VendorBlockEntity vendorBlockEntity) {
                if (packet.slotIndex() == 0) {
                    vendorBlockEntity.setFilterContents(1, packet.stack());
                } else if (packet.slotIndex() == 11) {
                    vendorBlockEntity.setFilterContents(3, packet.stack());
                }
                
                vendorBlockEntity.setChanged();
            }
        });
    }
}

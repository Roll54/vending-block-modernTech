package com.furglitch.vendingblock.block;

import javax.annotation.Nullable;

import com.furglitch.vendingblock.blockentity.VendorBlockEntity;
import com.furglitch.vendingblock.blockentity.transaction.VendorBlockTransaction;
import com.furglitch.vendingblock.gui.admin.VendorAdminMenu;
import com.furglitch.vendingblock.registry.ItemRegistry;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.fml.ModList;

public class VendorBlock extends BaseEntityBlock {

    public static final VoxelShape SHAPE_DOME = Block.box(1, 0, 1, 15, 15, 15);
    public static final VoxelShape SHAPE_BASE = Block.box(0, 0, 0, 16, 2, 16);
    public static final VoxelShape SHAPE = Shapes.or(SHAPE_BASE, SHAPE_DOME);
    public static final MapCodec<VendorBlock> CODEC = simpleCodec(VendorBlock::new);

    public VendorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
    
    @Nullable @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new VendorBlockEntity(blockPos, blockState);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        
        super.setPlacedBy(level, pos, state, placer, stack);
        
        if (placer instanceof Player player && level.getBlockEntity(pos) instanceof VendorBlockEntity vendorBlockEntity) {
            vendorBlockEntity.setOwner(player);
        }
    
    }
    
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof VendorBlockEntity vendorBlockEntity) {
            if (vendorBlockEntity.isOwner(player) && ModList.get().isLoaded("carryon") && player.isShiftKeyDown()) {
                return InteractionResult.SUCCESS; // Allows owners to pick up the vendor block with CarryOn
            }

            vendorBlockEntity.updateOwnershipInfo(player);
            
            if (player.getMainHandItem().is(ItemRegistry.VENDOR_KEY.get()) && !level.isClientSide()) {
                ((ServerPlayer) player).openMenu(new SimpleMenuProvider(
                    (containerId, inventory, playerEntity) -> new VendorAdminMenu(containerId, inventory, vendorBlockEntity), 
                    Component.translatable("menu.roll_mod_shops.admin.settings")), pos);
                level.playSound(player, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 2.0F);
            }
            else if (vendorBlockEntity.isOwner(player) && !level.isClientSide()) {
                ((ServerPlayer) player).openMenu(new SimpleMenuProvider(vendorBlockEntity, Component.translatable("menu.roll_mod_shops.settings")), pos);
                level.playSound(player, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 2.0F);
            } else if (!vendorBlockEntity.isOwner(player) && !level.isClientSide()) {
                VendorBlockTransaction.purchase(level, player, vendorBlockEntity);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock()) {
            if(level.getBlockEntity(pos) instanceof VendorBlockEntity vendorBlockEntity) {
                vendorBlockEntity.drops();
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
    
}

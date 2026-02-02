package com.furglitch.vendingblock.gui.trade;

import com.furglitch.vendingblock.blockentity.VendorBlockEntity;
import com.furglitch.vendingblock.gui.components.FilterSlot;
import com.furglitch.vendingblock.registry.BlockRegistry;
import com.furglitch.vendingblock.registry.MenuRegistry;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class VendorBlockMenu extends AbstractContainerMenu{
    
    public final VendorBlockEntity blockEntity;
    private final Level level;

    public VendorBlockMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public VendorBlockMenu(int containerId, Inventory inv, BlockEntity blockEntity) {
        super(MenuRegistry.VENDOR_MENU.get(), containerId);
        this.blockEntity = ((VendorBlockEntity) blockEntity);
        this.level = inv.player.level();

        addPlayerInventory(inv);

        this.addSlot(new FilterSlot(this.blockEntity.inventory, 0, 26, 17, this.blockEntity));
        // Slot 10 removed - now using currency price field instead
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int slotIndex = 1 + (i * 3) + j; // slots 1-9
                this.addSlot(new SlotItemHandler(this.blockEntity.inventory, slotIndex, 62 + (j * 18), 17 + (i * 18)));
            }
        }
        this.addSlot(new FilterSlot(this.blockEntity.inventory, 11, 134, 17, this.blockEntity));
    }

    // CREDIT: diesieben07 | https://github.com/diesieben07/SevenCommons
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
    private static final int TE_INVENTORY_SLOT_COUNT = 12;
    private static final int TE_INVENTORY_STOCK_FIRST_SLOT_INDEX = TE_INVENTORY_FIRST_SLOT_INDEX + 2;
    private static final int TE_INVENTORY_STOCK_SLOT_COUNT = 9;

    @Override
    public ItemStack quickMoveStack(Player playerIn, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        if (pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_STOCK_FIRST_SLOT_INDEX,
                    TE_INVENTORY_STOCK_FIRST_SLOT_INDEX + TE_INVENTORY_STOCK_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (pIndex < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, BlockRegistry.VENDOR.get());
    }

    private void addPlayerInventory(Inventory inv) {
        for (int i = 0; i < PLAYER_INVENTORY_ROW_COUNT; i++) {
            for (int j = 0; j < PLAYER_INVENTORY_COLUMN_COUNT; j++) {
                this.addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < HOTBAR_SLOT_COUNT; i++) {
            this.addSlot(new Slot(inv, i, 8 + i * 18, 142));
        }
    }
    
    @Override
    public void clicked(int slotId, int dragType, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < this.slots.size()) {
            Slot slot = this.slots.get(slotId);
            if (slot instanceof FilterSlot filterSlot) {
                if (clickType == ClickType.PICKUP) {
                    ItemStack cursorStack = this.getCarried();
                    boolean leftClick = dragType == 0;
                    if (filterSlot.onClick(cursorStack, leftClick)) {
                        return;
                    }
                }
            }
        }
        super.clicked(slotId, dragType, clickType, player);
    }
}

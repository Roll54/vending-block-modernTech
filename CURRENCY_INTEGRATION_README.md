# Currency Integration - Vending Block Refactoring

## Overview
This document outlines the complete refactoring of the Vending Block mod to integrate with the `roll_mod_currency` money system, replacing item-based trading with currency-based transactions.

## Changes Made

### 1. **VendorBlockEntity.java** - Core Block Entity
**Added:**
- `private long currencyPrice = 0L` - Stores the price in currency units
- `getCurrencyPrice()` - Getter for currency price
- `setCurrencyPrice(long price)` - Setter for currency price with validation and sync
- Updated NBT serialization to save/load `currencyPrice`

**Removed:**
- Slot 10 (price item slot) from filter contents handling
- Item-based price checks from error state validation

**Modified:**
- `checkErrorState()` - Now only checks product and stock, not payment space
- `drops()` - Updated to exclude slot 10

### 2. **VendorBlockTransaction.java** - Transaction Logic
**Completely Rewritten:**
- Now uses `CurrencyRepository.getBalance()` to check player funds
- Uses `CurrencyRepository.transfer()` for owner-to-buyer currency transfer
- Uses `CurrencyRepository.setBalance()` for discarded payments or offline owners
- Removed all item-based payment handling (`recievePayment()` method removed)
- Added server-side-only transaction processing (requires `ServerPlayer`)
- Supports free giveaways when `currencyPrice == 0`

**API Integration:**
```java
public static boolean transfer(ServerPlayer from, ServerPlayer to, long amount) {
    if (amount <= 0L) return false;
    if (from.getUUID().equals(to.getUUID())) return false;
    return CurrencyRepository.transfer(from.getUUID(), to.getUUID(), amount);
}
```

### 3. **VendorBlockInventory.java** - Inventory Helper
**Simplified:**
- Removed `checkInventory()` - no longer need to check player has payment items
- Removed `checkStockSpace()` - no longer need to check vendor has space for payment
- Modified `checkInventorySpace()` - now only checks space for product (removed price parameter)

### 4. **Messages.java** - Chat Messages
**Added Currency-Specific Messages:**
- `playerBoughtCurrency(int count, Component item, String owner, long price)`
- `ownerSoldCurrency(int count, Component item, String player, long price)`
- `playerInsufficientCurrency(long required)`
- `transactionFailed()`

### 5. **VendorBlockMenu.java** - Container Menu
**Modified:**
- Removed slot 10 (price item slot) from slot initialization
- Inventory now only has: product (0), stock (1-9), facade (11)

### 6. **VendorBlockScreen.java** - Client GUI
**Major Changes:**
- Removed price item slot rendering
- Added currency price display (shows as text in place of old price slot)
- Added scroll-to-adjust currency price feature with modifiers:
  - Scroll: ±1 coin
  - Shift+Scroll: ±10 coins
  - Ctrl+Scroll: ±100 coins
- Updated tooltips to show currency information
- Price displayed as gold-colored text or "FREE" for 0 price

### 7. **Network Packets**
**New Packet Created:**
- `CurrencyPriceUpdatePacket` - Syncs currency price changes from client to server
- Registered in `NetworkHandler.java`

**Modified:**
- `FilterSlotUpdatePacket` - Removed slot 10 handling

### 8. **HUD Overlay** - `HintOverlay.java`
**Updated:**
- Changed from displaying buy item to displaying currency price
- Shows "X coins" in gold color for paid sales
- Shows free giveaway when price is 0
- Removed donation mode (slot 10 requests) - no longer supported

### 9. **Jade Integration**
**Updated:**
- `VendorBlockDataProvider` - Now sends `currencyPrice` instead of price item
- `VendorBlockComponentProvider` - Displays currency price in gold color
- Added "FREE" indicator for 0-price items

### 10. **Localization** - `en_us.json`
**Added Translation Keys:**
```json
"menu.vendingblock.tooltip.price.currency": "Price: %s coins"
"menu.vendingblock.tooltip.price.free": "Free (Scroll to set price)"
"menu.vendingblock.tooltip.scroll.currency": "Scroll to adjust price (Shift: x10, Ctrl: x100)"
"msg.vendingblock.sell.currency": "You bought %s %s from %s for %s coins"
"msg.vendingblock.sell.owner.currency": "Your vending block sold %s %s to %s for %s coins"
"msg.vendingblock.insufficient.currency": "You do not have enough currency (Need: %s coins)"
"msg.vendingblock.transaction.failed": "Transaction failed! Please try again."
"jade.vendingblock.giveaway.free": "FREE"
```

## How It Works

### Setting Up a Vending Block (Owner):
1. Place the vending block
2. Open the GUI (right-click as owner)
3. Set the product in slot 0 (what you're selling)
4. Scroll over the price area to set currency amount
5. Stock the inventory with products (slots 1-9)

### Using a Vending Block (Buyer):
1. Right-click the vending block
2. System checks:
   - Player has enough currency
   - Player has inventory space for product
   - Block has stock
3. If all checks pass:
   - Currency transferred from buyer to owner
   - Product given to buyer
   - Stock decreases (unless infinite)

### Transaction Flow:
```
Player Clicks Block
    ↓
Check: Product exists? → No → Show "Not Set Up"
    ↓ Yes
Check: Block has stock? → No → Show "Out of Stock"
    ↓ Yes  
Check: Player has inventory space? → No → Show "Inventory Full"
    ↓ Yes
If price > 0:
    Check: Player has currency? → No → Show "Insufficient Currency"
        ↓ Yes
    Transfer currency (buyer → owner)
Give product to player
Update block state
```

## Admin Features Preserved
- Infinite Inventory mode - endless stock
- Discard Payment mode - currency is deleted instead of transferred
- Owner management via Vendor's Key

## Breaking Changes
**Removed Features:**
- Item-for-item trading (all transactions now use currency)
- Donation mode (requesting specific items) - removed entirely
- Price item slot (slot 10) - replaced with currency price field

## Dependencies
**Required:**
- `roll_mod_currency-1.0.0.jar` - Must be in `jars/` folder
- NeoForge 21.1.194+
- Minecraft 1.21.1

**API Used:**
```java
com.roll_54.roll_mod_currency.currency.CurrencyRepository
    - long getBalance(UUID player)
    - void setBalance(UUID player, long amount)
    - boolean transfer(UUID from, UUID to, long amount)
```

## Known Issues

### Compilation Issue
The `roll_mod_currency-1.0.0.jar` appears to be compiled with Java 21 (class version 69.0), which may cause compatibility issues with some build environments. 

**Solutions:**
1. Ensure Java 21 is being used for compilation
2. OR: Request a recompiled version of roll_mod_currency
3. OR: Compile against the roll_mod_currency source if available

## Testing Checklist
- [ ] Owner can set currency price via scrolling
- [ ] Buyer with sufficient currency can purchase items
- [ ] Currency is transferred from buyer to owner
- [ ] Free items (price = 0) work correctly
- [ ] Insufficient currency shows proper error message
- [ ] Out of stock detection works
- [ ] Inventory full detection works
- [ ] Infinite inventory mode bypasses stock checks
- [ ] Discard payment mode prevents currency transfer
- [ ] GUI displays price correctly
- [ ] HUD overlay shows currency price
- [ ] Jade integration displays currency
- [ ] Network sync works properly

## Future Improvements
1. Add GUI button/text field for direct currency input
2. Add currency display in player inventory
3. Add transaction history/logs
4. Add configurable currency name instead of hardcoded "coins"
5. Add min/max price limits in config
6. Add tax/commission system for server owners

## Migration Guide
**For Existing Worlds:**
- Old vending blocks will have `currencyPrice = 0` (free) by default
- Owners will need to manually set prices
- Items in old price slots (slot 10) will remain but be ignored
- No data loss, but prices need to be reconfigured

---
**Author:** AI Assistant  
**Date:** February 1, 2026  
**Version:** 1.0.0-currency-integration

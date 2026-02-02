package com.furglitch.vendingblock.gui.chat;

import net.minecraft.network.chat.Component;

public class Messages {

    // Currency-based messages
    public static Component playerBoughtCurrency(int count, Component item, String owner, long price) {
        return Component.translatable("msg.vendingblock.sell.currency", count, item, owner, price);
    }

    public static Component ownerSoldCurrency(int count, Component item, String player, long price) {
        return Component.translatable("msg.vendingblock.sell.owner.currency", count, item, player, price);
    }

    public static Component playerInsufficientCurrency(long required) {
        return Component.translatable("msg.vendingblock.insufficient.currency", required);
    }

    public static Component transactionFailed() {
        return Component.translatable("msg.vendingblock.transaction.failed");
    }

    // Legacy item-based messages (kept for compatibility if needed)
    public static Component playerBought(int count, Component item, String owner, int sellCount, Component sellItem) {
        return Component.translatable("msg.vendingblock.sell", count, item, owner, sellCount, sellItem);
    }

    public static Component playerRequest(int count, Component item, String owner) {
        return Component.translatable("msg.vendingblock.request", count, item, owner);
    }

    public static Component playerGiveaway(int count, Component item, String owner) {
        return Component.translatable("msg.vendingblock.giveaway", count, item, owner);
    }

    public static Component playerEmpty(Component sellItem) {
        return Component.translatable("msg.vendingblock.empty.player", sellItem);
    }

    public static Component playerFull() {
        return Component.translatable("msg.vendingblock.full.player");
    }

    public static Component vendorFull() {
        return Component.translatable("msg.vendingblock.full");
    }

    public static Component vendorSold() {
        return Component.translatable("msg.vendingblock.sold");
    }

    public static Component vendorEmpty() {
        return Component.translatable("msg.vendingblock.empty");
    }

    public static Component ownerSold(int count, Component item, String player, int sellCount, Component sellItem) {
        return Component.translatable("msg.vendingblock.sell.owner", count, item, player, sellCount, sellItem);
    }

    public static Component ownerRequest(int count, Component item, String player) {
        return Component.translatable("msg.vendingblock.request.owner", count, item, player);
    }

    public static Component ownerGiveaway(int count, Component item, String player) {
        return Component.translatable("msg.vendingblock.giveaway.owner", count, item, player);
    }

    public static Component ownerSold() {
        return Component.translatable("msg.vendingblock.sold.owner");
    }

    public static Component ownerFull() {
        return Component.translatable("msg.vendingblock.full.owner");
    }
    
    public static Component blacklistedProduct(String item) {
        return Component.translatable("msg.vendingblock.blacklist.product", item);
    }

    public static Component blacklistedFacade(String item) {
        return Component.translatable("msg.vendingblock.blacklist.facade", item);
    }
    
    public static Component fullBlockFacade(String item) {
        return Component.translatable("msg.vendingblock.blacklist.fullBlock", item);
    }
}



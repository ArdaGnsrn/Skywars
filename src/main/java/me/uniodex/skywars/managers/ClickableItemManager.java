package me.uniodex.skywars.managers;

import me.uniodex.skywars.Skywars;
import me.uniodex.skywars.utils.Utils;
import org.bukkit.inventory.ItemStack;

public class ClickableItemManager {

    private Skywars plugin;

    private ItemStack wandItem, selectCageItem, chestToolItem, emptyItem, saveItem, plusItem, minusItem, quitItem, statsItem, teleporterItem, backItem, voteItem, achievementsItem, confirmItem, cancelItem, nextItem, previousItem, newGameItem, selectKitItem;

    public ClickableItemManager(Skywars plugin) {
        this.plugin = plugin;
        initItems();
    }

    private void initItems() {
        wandItem = Utils.getItemStack(plugin.configManager.getCustomizationConfig().getString("Items.Wand"), false, true);
        chestToolItem = Utils.getItemStack(plugin.configManager.getCustomizationConfig().getString("Items.ChestTool"), false, true);
        emptyItem = Utils.getItemStack(plugin.configManager.getCustomizationConfig().getString("Items.Empty"), false, true);
        saveItem = Utils.getItemStack(plugin.configManager.getCustomizationConfig().getString("Items.Save"), false, true);
        plusItem = Utils.getItemStack(plugin.configManager.getCustomizationConfig().getString("Items.Plus"), false, true);
        minusItem = Utils.getItemStack(plugin.configManager.getCustomizationConfig().getString("Items.Minus"), false, true);
        quitItem = Utils.getItemStack(plugin.configManager.getCustomizationConfig().getString("Items.Quit"), false, true);
        statsItem = Utils.getItemStack(plugin.configManager.getCustomizationConfig().getString("Items.Stats"), false, true);
        teleporterItem = Utils.getItemStack(plugin.configManager.getCustomizationConfig().getString("Items.Teleporter"), false, true);
        backItem = Utils.getItemStack(plugin.configManager.getCustomizationConfig().getString("Items.Back"), false, true);
        voteItem = Utils.getItemStack(plugin.configManager.getCustomizationConfig().getString("Items.Vote"), false, true);
        achievementsItem = Utils.getItemStack(plugin.configManager.getCustomizationConfig().getString("Items.Achievements"), false, true);
        confirmItem = Utils.getItemStack(plugin.configManager.getCustomizationConfig().getString("Items.Confirm"), false, true);
        cancelItem = Utils.getItemStack(plugin.configManager.getCustomizationConfig().getString("Items.Cancel"), false, true);
        nextItem = Utils.getItemStack(plugin.configManager.getCustomizationConfig().getString("Items.Next"), false, true);
        previousItem = Utils.getItemStack(plugin.configManager.getCustomizationConfig().getString("Items.Previous"), false, true);
        newGameItem = Utils.getItemStack(plugin.configManager.getCustomizationConfig().getString("Items.NewGame"), false, true);
        selectKitItem = Utils.getItemStack(plugin.configManager.getCustomizationConfig().getString("Items.SelectKit"), false, true);
        selectCageItem = Utils.getItemStack(plugin.configManager.getCustomizationConfig().getString("Items.SelectCage"), false, true);

    }

    public ItemStack getWandItem() {
        return wandItem;
    }

    public ItemStack getChestToolItem() {
        return chestToolItem;
    }

    public ItemStack getEmptyItem() {
        return emptyItem;
    }

    public ItemStack getSaveItem() {
        return saveItem;
    }

    public ItemStack getPlusItem() {
        return plusItem;
    }

    public ItemStack getMinusItem() {
        return minusItem;
    }

    public ItemStack getQuitItem() {
        return quitItem;
    }

    public ItemStack getStatsItem(String playerName) {
        return Utils.getSkull(playerName, statsItem.getItemMeta().getDisplayName());
    }

    public ItemStack getTeleporterItem() {
        return teleporterItem;
    }

    public ItemStack getBackItem() {
        return backItem;
    }

    public ItemStack getVoteItem() {
        return voteItem;
    }

    public ItemStack getAchievementsItem() {
        return achievementsItem;
    }

    public ItemStack getConfirmItem() {
        return confirmItem;
    }

    public ItemStack getCancelItem() {
        return cancelItem;
    }

    public ItemStack getNextItem() {
        return nextItem;
    }

    public ItemStack getPreviousItem() {
        return previousItem;
    }

    public ItemStack getNewGameItem() {
        return newGameItem;
    }

    public ItemStack getSelectKitItem() {
        return selectKitItem;
    }
    public ItemStack getSelectCageItem() {
        return selectCageItem;
    }
}

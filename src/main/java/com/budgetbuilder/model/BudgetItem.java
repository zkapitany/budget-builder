package com.budgetbuilder.model;

public class BudgetItem {
    private String newItemsId;
    private String newItems;
    private String itemId;
    private String item;

    public BudgetItem(String newItemsId, String newItems, String itemId, String item) {
        this.newItemsId = newItemsId;
        this.newItems = newItems;
        this.itemId = itemId;
        this.item = item;
    }

    // Getters and Setters
    public String getNewItemsId() { return newItemsId; }
    public void setNewItemsId(String newItemsId) { this.newItemsId = newItemsId; }

    public String getNewItems() { return newItems; }
    public void setNewItems(String newItems) { this.newItems = newItems; }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getItem() { return item; }
    public void setItem(String item) { this.item = item; }
}
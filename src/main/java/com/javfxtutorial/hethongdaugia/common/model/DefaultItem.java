package com.javfxtutorial.hethongdaugia.common.model;

import com.javfxtutorial.hethongdaugia.common.model.enums.ItemCategory;

public class DefaultItem extends Item{
    public DefaultItem(int sellerId, String name, String description, String image, String sellerName) {
        super(sellerId, name, description, image, sellerName);
    }

    public DefaultItem(int itemId, int sellerId, String name, String description, String image, String sellerName) {
        super(itemId, sellerId, name, description, image, sellerName);
    }

    public DefaultItem(int sellerId, int itemId, String name, String description, String image) {
        super(sellerId, itemId, name, description, image);
    }

    public DefaultItem(String sellerName, int sellerId, int itemId, String name, String description, String image, ItemCategory category) {
        super(sellerName, sellerId, itemId, name, description, image, category);
    }

    public DefaultItem() {
    }

}

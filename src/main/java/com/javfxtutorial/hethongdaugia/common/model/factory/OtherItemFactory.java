package com.javfxtutorial.hethongdaugia.common.model.factory;

import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.model.enums.ItemCategory;

import java.util.Map;

public class OtherItemFactory extends ItemFactory {
    @Override
    public Item createItem(Map<String, String> data) {
        Item i = new Item();
        i.setSellerId(Integer.parseInt(data.get("sellerId")));
        i.setSellerName(data.get("sellerName"));
        i.setName(data.get("name"));
        i.setDescription(data.get("description"));
        i.setImage(data.get("image"));
        i.setCategory(ItemCategory.OTHER);
        return i;
    }
}

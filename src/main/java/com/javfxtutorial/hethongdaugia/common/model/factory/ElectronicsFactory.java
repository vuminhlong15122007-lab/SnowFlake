package com.javfxtutorial.hethongdaugia.common.model.factory;

import com.javfxtutorial.hethongdaugia.common.model.domain.Electronics;
import com.javfxtutorial.hethongdaugia.common.model.domain.Item;
import com.javfxtutorial.hethongdaugia.common.model.enums.ItemCategory;
import java.util.Map;

public class ElectronicsFactory extends ItemFactory {

    @Override
    public Item createItem(Map<String, String> data) {
        Electronics e = new Electronics();
        e.setSellerId(Integer.parseInt(data.get("sellerId")));
        e.setSellerName(data.get("sellerName"));
        e.setName(data.get("name"));
        e.setDescription(data.get("description"));
        e.setImage(data.get("image"));
        e.setCategory(ItemCategory.ELECTRONICS);
        e.setBrand(data.get("brand"));
        e.setModel(data.get("model"));
        return e;
    }
}

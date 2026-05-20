package com.javfxtutorial.hethongdaugia.common.model.factory;

import com.javfxtutorial.hethongdaugia.common.model.Art;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.model.enums.ItemCategory;
import java.util.Map;

public class ArtFactory extends ItemFactory {
    @Override
    public Item createItem(Map<String, String> data) {
        Art art = new Art();
        art.setSellerId(Integer.parseInt(data.get("sellerId")));
        art.setSellerName(data.get("sellerName"));
        art.setName(data.get("name"));
        art.setDescription(data.get("description"));
        art.setImage(data.get("image"));
        art.setCategory(ItemCategory.ART);
        art.setArtist(data.get("artist"));
        art.setTitle(data.get("title"));
        art.setYearCreated(Integer.parseInt(data.get("yearCreated")));
        return art;
    }
}

package com.javfxtutorial.hethongdaugia.common.model.factory;

import com.javfxtutorial.hethongdaugia.common.model.domain.Item;
import com.javfxtutorial.hethongdaugia.common.model.enums.ItemCategory;
import java.util.Map;

public class OtherItemFactory extends ItemFactory {
    Item baseItem;

    public OtherItemFactory(Item baseItem) {
        this.baseItem = baseItem;
    }

    @Override
    public void showData() {}

    @Override
    public Item createItemFromForm() {
        return baseItem;
    }
}

package com.javfxtutorial.hethongdaugia.common.model.factory;

import com.javfxtutorial.hethongdaugia.common.model.DefaultItem;
import com.javfxtutorial.hethongdaugia.common.model.Item;

public class DefaultItemFactory extends ItemFactory {
    @Override
    public Item createItem() {
        return new DefaultItem();
    }
}

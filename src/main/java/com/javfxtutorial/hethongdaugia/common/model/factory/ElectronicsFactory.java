package com.javfxtutorial.hethongdaugia.common.model.factory;

import com.javfxtutorial.hethongdaugia.common.model.Electronics;
import com.javfxtutorial.hethongdaugia.common.model.Item;

public class ElectronicsFactory extends ItemFactory {

    private String brand;
    private String model;
    private int sellerId;
    private String name, description, image, sellerName;

    // truyền toàn bộ data vào factory
    public ElectronicsFactory(int sellerId, String name, String description, String image, String sellerName, String brand, String model) {
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.image = image;
        this.sellerName = sellerName;
        this.brand = brand;
        this.model = model;
    }

    // dùng khi chỉ cần factory để tạo object trống từ DB
    public ElectronicsFactory() {}

    @Override
    public Item createItem() {
        return new Electronics();
    }
}

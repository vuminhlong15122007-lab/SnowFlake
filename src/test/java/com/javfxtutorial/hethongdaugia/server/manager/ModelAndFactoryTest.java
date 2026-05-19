package com.javfxtutorial.hethongdaugia.server.manager;

import com.javfxtutorial.hethongdaugia.common.model.Art;
import com.javfxtutorial.hethongdaugia.common.model.Electronics;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.model.Vehicle;
import com.javfxtutorial.hethongdaugia.common.model.enums.ItemCategory;
import com.javfxtutorial.hethongdaugia.common.model.factory.ArtFactory;
import com.javfxtutorial.hethongdaugia.common.model.factory.ElectronicsFactory;
import com.javfxtutorial.hethongdaugia.common.model.factory.ItemFactory;
import com.javfxtutorial.hethongdaugia.common.model.factory.OtherItemFactory;
import com.javfxtutorial.hethongdaugia.common.model.factory.VehicleFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Factory tạo sản phẩm đấu giá")
class ModelAndFactoryTest {
    @Nested
    @DisplayName("Chọn factory theo nhóm sản phẩm")
    class ItemFactoryTest {
        @Test
        @DisplayName("trả về đúng factory cho từng ItemCategory")
        void getFactory_returnsCorrectFactoryForEachCategory() {
            assertInstanceOf(VehicleFactory.class, ItemFactory.getFactory(ItemCategory.VEHICLE));
            assertInstanceOf(ArtFactory.class, ItemFactory.getFactory(ItemCategory.ART));
            assertInstanceOf(ElectronicsFactory.class, ItemFactory.getFactory(ItemCategory.ELECTRONICS));
            assertInstanceOf(OtherItemFactory.class, ItemFactory.getFactory(ItemCategory.OTHER));
            assertInstanceOf(OtherItemFactory.class, ItemFactory.getFactory(null));
        }

        @Test
        @DisplayName("ElectronicsFactory tạo item điện tử kèm trường riêng")
        void electronicsFactory_createsElectronicsWithBaseAndSpecificFields() {
            Item item = ItemFactory.getFactory(ItemCategory.ELECTRONICS).createItem(baseData(
                    "brand", "Sony",
                    "model", "A7"
            ));

            Electronics electronics = assertInstanceOf(Electronics.class, item);
            assertCommonItemFields(electronics, ItemCategory.ELECTRONICS);
            assertEquals("Sony", electronics.getBrand());
            assertEquals("A7", electronics.getModel());
        }

        @Test
        @DisplayName("ArtFactory tạo item nghệ thuật kèm trường riêng")
        void artFactory_createsArtWithBaseAndSpecificFields() {
            Item item = ItemFactory.getFactory(ItemCategory.ART).createItem(baseData(
                    "artist", "Picasso",
                    "title", "Blue",
                    "yearCreated", "1901"
            ));

            Art art = assertInstanceOf(Art.class, item);
            assertCommonItemFields(art, ItemCategory.ART);
            assertEquals("Picasso", art.getArtist());
            assertEquals("Blue", art.getTitle());
            assertEquals(1901, art.getYearCreated());
        }

        @Test
        @DisplayName("VehicleFactory tạo item phương tiện kèm trường riêng")
        void vehicleFactory_createsVehicleWithBaseAndSpecificFields() {
            Item item = ItemFactory.getFactory(ItemCategory.VEHICLE).createItem(baseData(
                    "brand", "Toyota",
                    "licensePlate", "30A-12345",
                    "year", "2020",
                    "color", "white"
            ));

            Vehicle vehicle = assertInstanceOf(Vehicle.class, item);
            assertCommonItemFields(vehicle, ItemCategory.VEHICLE);
            assertEquals("Toyota", vehicle.getBrand());
            assertEquals("30A-12345", vehicle.getLicensePlate());
            assertEquals(2020, vehicle.getYear());
            assertEquals("white", vehicle.getColor());
        }

        @Test
        @DisplayName("OtherItemFactory tạo Item thường cho nhóm OTHER")
        void otherFactory_createsPlainItem() {
            Item item = ItemFactory.getFactory(ItemCategory.OTHER).createItem(baseData());

            assertEquals(Item.class, item.getClass());
            assertCommonItemFields(item, ItemCategory.OTHER);
        }

        @Test
        @DisplayName("factory báo lỗi khi trường số nghiệp vụ không hợp lệ")
        void factory_throwsWhenRequiredNumericFieldIsInvalid() {
            Map<String, String> data = baseData("year", "not-a-number");
            assertThrows(NumberFormatException.class,
                    () -> ItemFactory.getFactory(ItemCategory.VEHICLE).createItem(data));
        }
    }

    private static Map<String, String> baseData(String... extraPairs) {
        java.util.HashMap<String, String> data = new java.util.HashMap<>();
        data.put("sellerId", "42");
        data.put("sellerName", "seller");
        data.put("name", "item");
        data.put("description", "description");
        data.put("image", "image.png");

        for (int i = 0; i < extraPairs.length; i += 2) {
            data.put(extraPairs[i], extraPairs[i + 1]);
        }
        return data;
    }

    private static void assertCommonItemFields(Item item, ItemCategory category) {
        assertEquals(42, item.getSellerId());
        assertEquals("seller", item.getSellerName());
        assertEquals("item", item.getName());
        assertEquals("description", item.getDescription());
        assertEquals("image.png", item.getImage());
        assertEquals(category, item.getCategory());
    }

}

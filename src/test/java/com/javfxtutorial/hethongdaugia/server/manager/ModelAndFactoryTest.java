package com.javfxtutorial.hethongdaugia.server.manager;

import com.javfxtutorial.hethongdaugia.common.model.Art;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.AutoBidConfig;
import com.javfxtutorial.hethongdaugia.common.model.BidTransaction;
import com.javfxtutorial.hethongdaugia.common.model.Electronics;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.model.Vehicle;
import com.javfxtutorial.hethongdaugia.common.model.enums.AccountType;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.model.enums.ItemCategory;
import com.javfxtutorial.hethongdaugia.common.model.factory.ArtFactory;
import com.javfxtutorial.hethongdaugia.common.model.factory.ElectronicsFactory;
import com.javfxtutorial.hethongdaugia.common.model.factory.ItemFactory;
import com.javfxtutorial.hethongdaugia.common.model.factory.OtherItemFactory;
import com.javfxtutorial.hethongdaugia.common.model.factory.VehicleFactory;
import com.javfxtutorial.hethongdaugia.common.network.Command;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelAndFactoryTest {
    private static final LocalDateTime START = LocalDateTime.of(2026, 1, 1, 10, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 1, 1, 11, 0);

    @Nested
    @DisplayName("ItemFactory")
    class ItemFactoryTest {
        @Test
        void getFactory_returnsCorrectFactoryForEachCategory() {
            assertInstanceOf(VehicleFactory.class, ItemFactory.getFactory(ItemCategory.VEHICLE));
            assertInstanceOf(ArtFactory.class, ItemFactory.getFactory(ItemCategory.ART));
            assertInstanceOf(ElectronicsFactory.class, ItemFactory.getFactory(ItemCategory.ELECTRONICS));
            assertInstanceOf(OtherItemFactory.class, ItemFactory.getFactory(ItemCategory.OTHER));
            assertInstanceOf(OtherItemFactory.class, ItemFactory.getFactory(null));
        }

        @Test
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
        void otherFactory_createsPlainItem() {
            Item item = ItemFactory.getFactory(ItemCategory.OTHER).createItem(baseData());

            assertEquals(Item.class, item.getClass());
            assertCommonItemFields(item, ItemCategory.OTHER);
        }

        @Test
        void factory_throwsWhenRequiredNumericFieldIsInvalid() {
            Map<String, String> data = baseData("year", "not-a-number");
            assertThrows(NumberFormatException.class,
                    () -> ItemFactory.getFactory(ItemCategory.VEHICLE).createItem(data));
        }
    }

    @Nested
    @DisplayName("Domain models")
    class DomainModelTest {
        @Test
        void user_constructorAndSetters_roundTripFields() {
            User user = new User("alice", "secret", "alice@example.com", "0900", AccountType.USER);
            user.setId(7);
            user.setEmail("new@example.com");
            user.setName("Alice");
            user.setPassWord("new-secret");
            user.setSdt("0911");
            user.setImagePath("avatar.png");

            assertEquals(7, user.getId());
            assertEquals("Alice", user.getName());
            assertEquals("new-secret", user.getPassWord());
            assertEquals("new@example.com", user.getEmail());
            assertEquals("0911", user.getSdt());
            assertEquals(AccountType.USER, user.getAccountType());
            assertEquals("avatar.png", user.getImagePath());
            assertTrue(user.toString().contains("Alice"));
        }

        @Test
        void item_constructorAndSetters_roundTripFields() {
            Item item = new Item("seller", 2, 3, "phone", "desc", "image.png", ItemCategory.ELECTRONICS);
            item.setItemId(9);
            item.setSellerId(8);
            item.setSellerName("new seller");
            item.setName("camera");
            item.setDescription("new desc");
            item.setImage("new.png");
            item.setCategory(ItemCategory.ART);

            assertEquals(9, item.getItemId());
            assertEquals(8, item.getSellerId());
            assertEquals("new seller", item.getSellerName());
            assertEquals("camera", item.getName());
            assertEquals("new desc", item.getDescription());
            assertEquals("new.png", item.getImage());
            assertEquals(ItemCategory.ART, item.getCategory());
        }

        @Test
        void auction_constructorAndSetters_roundTripFields() {
            Item item = new Item("seller", 10, 20, "watch", "desc", "img", ItemCategory.OTHER);
            Auction auction = new Auction(
                    1,
                    item,
                    2,
                    3,
                    new BigDecimal("100"),
                    new BigDecimal("120"),
                    new BigDecimal("10"),
                    new BigDecimal("120"),
                    START,
                    END,
                    AuctionStatus.RUNNING
            );

            auction.setAuctionId(11);
            auction.setSellerId(22);
            auction.setWinnerId(33);
            auction.setInitPrice(new BigDecimal("200"));
            auction.setCurrentPrice(new BigDecimal("230"));
            auction.setStepPrice(new BigDecimal("20"));
            auction.setWinningPrice(new BigDecimal("230"));
            auction.setStartingTime(START.plusDays(1));
            auction.setEndingTime(END.plusDays(1));
            auction.setStatus(AuctionStatus.CLOSED);

            assertSame(item, auction.getItem());
            assertEquals(11, auction.getAuctionId());
            assertEquals(22, auction.getSellerId());
            assertEquals(33, auction.getWinnerId());
            assertEquals(new BigDecimal("200"), auction.getInitPrice());
            assertEquals(new BigDecimal("230"), auction.getCurrentPrice());
            assertEquals(new BigDecimal("20"), auction.getStepPrice());
            assertEquals(new BigDecimal("230"), auction.getWinningPrice());
            assertEquals(START.plusDays(1), auction.getStartingTime());
            assertEquals(END.plusDays(1), auction.getEndingTime());
            assertEquals(AuctionStatus.CLOSED, auction.getStatus());
            assertTrue(auction.toString().contains("auctionId=11"));
        }

        @Test
        void bidTransaction_constructorAndSetters_roundTripFields() {
            BidTransaction bid = new BidTransaction("alice", 1, new BigDecimal("150"), START);
            bid.setBidId(5);
            bid.setBidderId(6);
            bid.setBidderName("bob");
            bid.setAuctionId(7);
            bid.setAmount(new BigDecimal("170"));
            bid.setTimestamp(START.plusMinutes(1));
            bid.setNewEndingTime(END.plusMinutes(1));

            assertEquals(5, bid.getBidId());
            assertEquals(6, bid.getBidderId());
            assertEquals("bob", bid.getBidderName());
            assertEquals(7, bid.getAuctionId());
            assertEquals(new BigDecimal("170"), bid.getAmount());
            assertEquals(START.plusMinutes(1), bid.getTimestamp());
            assertEquals(END.plusMinutes(1), bid.getNewEndingTime());
        }

        @Test
        void autoBidConfig_constructorAndSetters_roundTripFields() {
            AutoBidConfig config = new AutoBidConfig(1, "alice", 2, new BigDecimal("500"), true);
            LocalDateTime registeredAt = START;

            config.setUserId(10);
            config.setUserName("bob");
            config.setAuctionId(20);
            config.setMaxPrice(new BigDecimal("600"));
            config.setActive(false);
            config.setRegisteredAt(registeredAt);

            assertEquals(10, config.getUserId());
            assertEquals("bob", config.getUserName());
            assertEquals(20, config.getAuctionId());
            assertEquals(new BigDecimal("600"), config.getMaxPrice());
            assertEquals(registeredAt, config.getRegisteredAt());
            assertTrue(!config.isActive());
        }
    }

    @Nested
    @DisplayName("Network DTO")
    class NetworkDtoTest {
        @Test
        void response_constructorsAndSetters_roundTripFields() {
            Command command = new NoopCommand();
            Response response = new Response(true, "ok", "payload", command);

            assertTrue(response.isSuccess());
            assertEquals("ok", response.getMessage());
            assertEquals("payload", response.getPayLoad());
            assertSame(command, response.getCommand());

            response.setSuccess(false);
            response.setMessage("fail");
            response.setPayLoad(null);

            assertTrue(!response.isSuccess());
            assertEquals("fail", response.getMessage());
            assertNull(response.getPayLoad());
        }

        @Test
        void commandDataMap_storesAndReturnsValues() {
            Command command = new NoopCommand();

            command.addData("auctionId", 123);
            command.addData("name", "alice");

            assertEquals(123, command.getData("auctionId"));
            assertEquals("alice", command.getData("name"));
            assertEquals(2, command.getData().size());
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

    private static class NoopCommand extends Command {
        @Override
        public Response handle() {
            return new Response(true, "noop", null, this);
        }
    }
}

//package com.javfxtutorial.hethongdaugia.client.controller;
//
//import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
//import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
//import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
//import com.javfxtutorial.hethongdaugia.common.model.Auction;
//import com.javfxtutorial.hethongdaugia.common.model.BidTransaction;
//import com.javfxtutorial.hethongdaugia.common.model.Item;
//import com.javfxtutorial.hethongdaugia.common.model.User;
//import com.javfxtutorial.hethongdaugia.common.model.Command.AddAccountCommand;
//import com.javfxtutorial.hethongdaugia.common.model.Command.AutoBidCommand;
//import com.javfxtutorial.hethongdaugia.common.model.Command.ResetPassWordCommand;
//import com.javfxtutorial.hethongdaugia.common.model.Command.UpdateProfileCommand;
//import com.javfxtutorial.hethongdaugia.common.model.enums.AccountType;
//import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
//import com.javfxtutorial.hethongdaugia.common.model.enums.ItemCategory;
//import javafx.application.Platform;
//import javafx.collections.ObservableList;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Node;
//import javafx.scene.control.Button;
//import javafx.scene.control.ComboBox;
//import javafx.scene.control.Label;
//import javafx.scene.control.ListView;
//import javafx.scene.control.PasswordField;
//import javafx.scene.control.TextField;
//import javafx.scene.control.ToggleButton;
//import javafx.util.Callback;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import org.mockito.MockedStatic;
//
//import java.lang.reflect.Field;
//import java.math.BigDecimal;
//import java.net.URL;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.ResourceBundle;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicReference;
//
//import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertInstanceOf;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.mockStatic;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.verifyNoInteractions;
//
//@SuppressWarnings({"unchecked", "rawtypes"})
//class ClientUiControllerTest {
//    /*
//     * This list is intentionally explicit. When a new screen is added, keeping it
//     * here makes the UI smoke test fail loudly until the new FXML is covered.
//     */
//    private static final List<FxmlCase> APPLICATION_FXML = List.of(
//            new FxmlCase("Admin_ProductManagement.fxml", AdminItemController.class),
//            new FxmlCase("Admin_UpdateAdminInfo.fxml", AdminUpdateController.class),
//            new FxmlCase("Admin_UpdateUserInfo.fxml", Edit_User_Popup_Controller.class),
//            new FxmlCase("Admin_UserManagement.fxml", AdminUserController.class),
//            new FxmlCase("AuctionCell.fxml", AuctionSessionController.class),
//            new FxmlCase("AuctionInformation.fxml", ProductDisplayController.class),
//            new FxmlCase("AuctionList.fxml", AuctionController.class),
//            new FxmlCase("BidTransactionCell.fxml", BidTransactionCellController.class),
//            new FxmlCase("LiveAuction.fxml", LiveAuctionController.class),
//            new FxmlCase("login.fxml", LoginController.class),
//            new FxmlCase("MainScene.fxml", HomeController.class),
//            new FxmlCase("ParticipatedAuctionCell.fxml", ParticipatedAuctionCellController.class),
//            new FxmlCase("reset_password.fxml", PasswordResetController.class),
//            new FxmlCase("Seller_ProductCell.fxml", SellerProductController.class),
//            new FxmlCase("Seller_ProductManagement.fxml", SellerManagementController.class),
//            new FxmlCase("SignUp.fxml", RegisterController.class),
//            new FxmlCase("SignUpSuccessPopup.fxml", null),
//            new FxmlCase("UserInformation.fxml", UserProfileController.class),
//            new FxmlCase("UserParticipatedAuction.fxml", ParticipatedAuctionController.class)
//    );
//
//    @BeforeAll
//    static void startJavaFxToolkit() throws Exception {
//        // Monocle is not bundled in this project. Clear it so JavaFX can use
//        // the platform available on the developer machine or CI runner.
//        if ("Monocle".equals(System.getProperty("glass.platform"))) {
//            System.clearProperty("glass.platform");
//        }
//
//        CountDownLatch latch = new CountDownLatch(1);
//        try {
//            Platform.startup(() -> {
//                Platform.setImplicitExit(false);
//                latch.countDown();
//            });
//        } catch (IllegalStateException alreadyStarted) {
//            latch.countDown();
//        }
//
//        assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX toolkit did not start");
//    }
//
//    @AfterEach
//    void clearClientModel() {
//        // ClientModel is a singleton, so every test leaves it clean for the next
//        // test. This prevents order-dependent UI failures.
//        ClientModel model = ClientModel.getInstance();
//        model.setCurrentUser(null);
//        model.setCurrentAuction(null);
//        model.setCurrentItem(null);
//    }
//
//    @Test
//    @DisplayName("Every application FXML loads with the expected controller")
//    void allApplicationFxml_canLoadWithoutStartingNetwork() throws Exception {
//        runOnFxThread(() -> {
//            try (NetworkMocks ignored = mockNetwork()) {
//                for (FxmlCase fxml : APPLICATION_FXML) {
//                    LoadedFxml<?> loaded = loadFxml(fxml.fileName(), ClientUiControllerTest::controllerForSmokeTest);
//
//                    assertNotNull(loaded.root(), () -> "FXML root must be created: " + fxml.fileName());
//                    if (fxml.controllerType() == null) {
//                        continue;
//                    }
//                    assertInstanceOf(
//                            fxml.controllerType(),
//                            loaded.controller(),
//                            () -> "Unexpected controller for " + fxml.fileName()
//                    );
//                }
//            }
//        });
//    }
//
//    @Test
//    @DisplayName("Bid transaction cell binds bidder, amount and time")
//    void bidTransactionCell_displaysTransactionFields() throws Exception {
//        runOnFxThread(() -> {
//            LoadedFxml<BidTransactionCellController> loaded = loadFxml("BidTransactionCell.fxml");
//            BidTransactionCellController controller = loaded.controller();
//
//            BidTransaction transaction = new BidTransaction();
//            transaction.setBidderName("Alice");
//            transaction.setAmount(money(1_500_000));
//            transaction.setTimestamp(LocalDateTime.of(2026, 1, 2, 9, 30, 0));
//
//            controller.setData(transaction);
//
//            assertEquals("Alice", field(controller, "bidderNameLabel", Label.class).getText());
//            assertEquals("1,500,000 VND", field(controller, "amountLabel", Label.class).getText());
//            assertTrue(field(controller, "timestampLabel", Label.class).getText().contains("2026"));
//        });
//    }
//
////    @Test
////    @DisplayName("Participated auction cell adapts label text by status")
////    void participatedAuctionCell_displaysStatusSpecificText() throws Exception {
////        runOnFxThread(() -> {
////            LoadedFxml<ParticipatedAuctionCellController> loaded = loadFxml("ParticipatedAuctionCell.fxml");
////            ParticipatedAuctionCellController controller = loaded.controller();
////
////            controller.setData(auction("Gaming Laptop", AuctionStatus.RUNNING, ItemCategory.ELECTRONICS));
////            assertEquals("Gaming Laptop", field(controller, "lbProductName", Label.class).getText());
////            assertEquals("1,100,000 VND", field(controller, "lbCurrentPrice", Label.class).getText());
////            assertEquals("THAM GIA", field(controller, "actionButton", Button.class).getText());
////            assertFalse(field(controller, "actionButton", Button.class).isDisabled());
////
////            controller.setData(auction("Office Chair", AuctionStatus.NOT_START, ItemCategory.OTHER));
////            assertEquals("1,000,000 VND", field(controller, "lbCurrentPrice", Label.class).getText());
////            assertFalse(field(controller, "actionButton", Button.class).getText().isBlank());
////
////            controller.setData(auction("Vintage Camera", AuctionStatus.CLOSED, ItemCategory.ELECTRONICS));
////            assertEquals("1,200,000 VND", field(controller, "lbCurrentPrice", Label.class).getText());
////            assertEquals("0", field(controller, "lbWinnerName", Label.class).getText());
////        });
////    }
//
//    @Test
//    @DisplayName("Auction session cell renders status, price and action state")
//    void auctionSessionCell_displaysStatusAndCurrentPrice() throws Exception {
//        runOnFxThread(() -> {
//            LoadedFxml<AuctionSessionController> loaded = loadFxml("AuctionCell.fxml");
//            AuctionSessionController controller = loaded.controller();
//
//            Auction running = auction("Gaming Laptop", AuctionStatus.RUNNING, ItemCategory.ELECTRONICS);
//            running.setCurrentPrice(money(2_300_000));
//            controller.setData(running);
//
//            assertEquals("Gaming Laptop", field(controller, "lbProductName", Label.class).getText());
//            assertEquals("2,300,000 VND", field(controller, "lbPrice", Label.class).getText());
//            assertEquals("THAM GIA", field(controller, "actionButton", Button.class).getText());
//            assertFalse(field(controller, "actionButton", Button.class).isDisabled());
//
//            Auction scheduled = auction("Office Chair", AuctionStatus.NOT_START, ItemCategory.OTHER);
//            scheduled.setInitPrice(money(900_000));
//            controller.setData(scheduled);
//
//            assertEquals("900,000 VND", field(controller, "lbPrice", Label.class).getText());
//            assertFalse(field(controller, "statusBadge", Label.class).getText().isBlank());
//        });
//    }
//
//    @Test
//    @DisplayName("Seller product cell keeps item and auction values in sync")
//    void sellerProductCell_displaysProductAndStatus() throws Exception {
//        runOnFxThread(() -> {
//            LoadedFxml<SellerProductController> loaded = loadFxml("Seller_ProductCell.fxml");
//            SellerProductController controller = loaded.controller();
//
//            Auction auction = auction("Desk Lamp", AuctionStatus.RUNNING, ItemCategory.OTHER);
//            auction.getItem().setItemId(42);
//            auction.setCurrentPrice(money(700_000));
//            controller.update(auction);
//
//            assertEquals("Desk Lamp", field(controller, "lbProductName", Label.class).getText());
//            assertEquals("42", field(controller, "ItemID", Label.class).getText());
//            assertTrue(field(controller, "lbPrice", Label.class).getText().contains("700,000"));
//            assertTrue(field(controller, "lbStatus", Label.class).getStyle().contains("green"));
//
//            auction.setStatus(AuctionStatus.CLOSED);
//            controller.update(auction);
//            assertTrue(field(controller, "lbStatus", Label.class).getStyle().contains("red"));
//        });
//    }
//
//    @Test
//    @DisplayName("Register validates required fields and format before network")
//    void registerController_rejectsInvalidInputWithoutNetworkCall() throws Exception {
//        runOnFxThread(() -> {
//            LoadedFxml<RegisterController> loaded = loadFxml("SignUp.fxml");
//            RegisterController controller = loaded.controller();
//
//            try (NetworkMocks network = mockNetwork()) {
//                controller.clickSignUp(null);
//                assertFalse(field(controller, "message", Label.class).getText().isBlank());
//
//                fillRegisterForm(controller, "john", "john@gmail.com", "short", "short", "0123456789");
//                controller.clickSignUp(null);
//                assertTrue(field(controller, "message", Label.class).getText().contains("6"));
//
//                fillRegisterForm(controller, "john", "john@gmail.com", "password1", "password2", "0123456789");
//                controller.clickSignUp(null);
//                assertFalse(field(controller, "message", Label.class).getText().isBlank());
//
//                fillRegisterForm(controller, "john", "john@gmail.com", "password1", "password1", "123456789");
//                controller.clickSignUp(null);
//                assertTrue(field(controller, "message", Label.class).getText().contains("10"));
//
//                fillRegisterForm(controller, "john", "john@example.com", "password1", "password1", "0123456789");
//                controller.clickSignUp(null);
//                assertTrue(field(controller, "message", Label.class).getText().contains("@gmail.com"));
//
//                verifyNoInteractions(network.connection());
//            }
//        });
//    }
//
//    @Test
//    @DisplayName("Register sends command when the form is valid")
//    void registerController_sendsRegisterCommandForValidInput() throws Exception {
//        runOnFxThread(() -> {
//            LoadedFxml<RegisterController> loaded = loadFxml("SignUp.fxml");
//            RegisterController controller = loaded.controller();
//
//            try (NetworkMocks network = mockNetwork()) {
//                fillRegisterForm(controller, "johndoe", "john@gmail.com", "password1", "password1", "0123456789");
//                controller.clickSignUp(null);
//                verify(network.manager()).register(AddAccountCommand.class, controller);
//            }
//        });
//    }
//
//    @Test
//    @DisplayName("Edit-user popup validates editable account fields")
//    void editUserPopup_rejectsInvalidInputBeforeNetworkCall() throws Exception {
//        runOnFxThread(() -> {
//            LoadedFxml<Edit_User_Popup_Controller> loaded = loadFxml("Admin_UpdateUserInfo.fxml");
//            Edit_User_Popup_Controller controller = loaded.controller();
//
//            try (NetworkMocks network = mockNetwork()) {
//                controller.clickToSave(null);
//                assertFalse(field(controller, "message", Label.class).getText().isBlank());
//
//                fillEditUserForm(controller, "Jane", "012345678", "jane@gmail.com", "USER");
//                controller.clickToSave(null);
//                assertTrue(field(controller, "message", Label.class).getText().contains("10"));
//
//                fillEditUserForm(controller, "Jane", "0123456789", "jane@example.com", "USER");
//                controller.clickToSave(null);
//                assertTrue(field(controller, "message", Label.class).getText().contains("@gmail.com"));
//
//                verifyNoInteractions(network.connection());
//            }
//        });
//    }
//
//    @Test
//    @DisplayName("Edit-user popup sends AddAccountCommand when valid")
//    void editUserPopup_sendsAddAccountCommandForValidInput() throws Exception {
//        runOnFxThread(() -> {
//            LoadedFxml<Edit_User_Popup_Controller> loaded = loadFxml("Admin_UpdateUserInfo.fxml");
//            Edit_User_Popup_Controller controller = loaded.controller();
//
//            try (NetworkMocks network = mockNetwork()) {
//                fillEditUserForm(controller, "Jane", "0123456789", "jane@gmail.com", "ADMIN");
//                controller.clickToSave(null);
//
//                assertCommandSent(network.connection(), AddAccountCommand.class);
//                verify(network.manager()).register(AddAccountCommand.class, controller);
//            }
//        });
//    }
//
//    @Test
//    @DisplayName("Profile screens copy current user into form fields")
//    void profileControllers_loadCurrentUserIntoFields() throws Exception {
//        runOnFxThread(() -> {
//            User user = user(7, "Alice Nguyen", "alice", "alice@gmail.com", "0912345678", "Ha Noi", AccountType.ADMIN);
//            ClientModel.getInstance().setCurrentUser(user);
//
//            LoadedFxml<UserProfileController> profile = loadFxml("UserInformation.fxml");
//            assertEquals("Alice Nguyen", field(profile.controller(), "updateNameText", TextField.class).getText());
//            assertEquals("alice@gmail.com", field(profile.controller(), "updateEmailText", TextField.class).getText());
//
//            LoadedFxml<AdminUpdateController> admin = loadFxml("Admin_UpdateAdminInfo.fxml");
//            assertEquals("Alice Nguyen", field(admin.controller(), "txtName", TextField.class).getText());
//            assertEquals("0912345678", field(admin.controller(), "txtPhone", TextField.class).getText());
//
//            LoadedFxml<PasswordResetController> password = loadFxml("reset_password.fxml");
//            assertTrue(field(password.controller(), "txtNewPW", TextField.class).getText().isBlank());
//            assertTrue(field(password.controller(), "txtConfirmPW", TextField.class).getText().isBlank());
//        });
//    }
//
//    @Test
//    @DisplayName("Profile update controllers send their network commands")
//    void profileControllers_sendUpdateCommands() throws Exception {
//        runOnFxThread(() -> {
//            ClientModel.getInstance().setCurrentUser(user(7, "Alice Nguyen", "alice", "alice@gmail.com", "0912345678", "Ha Noi", AccountType.ADMIN));
//
//            try (NetworkMocks network = mockNetwork()) {
//                LoadedFxml<UserProfileController> profile = loadFxml("UserInformation.fxml");
//                field(profile.controller(), "updateNameText", TextField.class).setText("Alice Tran");
//                field(profile.controller(), "updatePhoneText", TextField.class).setText("0987654321");
//                profile.controller().handleUpdateInfo();
//
//                assertCommandSent(network.connection(), UpdateProfileCommand.class);
//                verify(network.manager()).register(UpdateProfileCommand.class, profile.controller());
//            }
//
//            try (NetworkMocks network = mockNetwork()) {
//                LoadedFxml<AdminUpdateController> admin = loadFxml("Admin_UpdateAdminInfo.fxml");
//                field(admin.controller(), "txtName", TextField.class).setText("Admin Tran");
//                field(admin.controller(), "txtPhone", TextField.class).setText("0987654321");
//                admin.controller().handleUpdateInfo();
//
//                assertCommandSent(network.connection(), UpdateProfileCommand.class);
//                verify(network.manager()).register(UpdateProfileCommand.class, admin.controller());
//            }
//
//            try (NetworkMocks network = mockNetwork()) {
//                LoadedFxml<PasswordResetController> password = loadFxml("reset_password.fxml");
//                field(password.controller(), "txtNewPW", TextField.class).setText("new-password");
//                field(password.controller(), "txtConfirmPW", TextField.class).setText("new-password");
//                password.controller().updatePW();
//
//                assertCommandSent(network.connection(), ResetPassWordCommand.class);
//                verify(network.manager()).register(ResetPassWordCommand.class, password.controller());
//            }
//        });
//    }
//
//    @Test
//    @DisplayName("Auction list filters by text, status and category")
//    void auctionList_filtersBySearchStatusAndCategory() throws Exception {
//        runOnFxThread(() -> {
//            LoadedFxml<TestableAuctionController> loaded = loadFxml(
//                    "AuctionList.fxml",
//                    type -> type == AuctionController.class ? new TestableAuctionController() : instantiate(type)
//            );
//            TestableAuctionController controller = loaded.controller();
//
//            ObservableList<Auction> source = field(controller, "observable", ObservableList.class);
//            source.setAll(
//                    auction("Gaming Laptop", AuctionStatus.RUNNING, ItemCategory.ELECTRONICS),
//                    auction("Office Chair", AuctionStatus.NOT_START, ItemCategory.ART),
//                    auction("Vintage Watch", AuctionStatus.CLOSED, ItemCategory.VEHICLE)
//            );
//
//            ListView<Auction> list = field(controller, "featuredProductList", ListView.class);
//            assertEquals(3, list.getItems().size());
//
//            field(controller, "searchField", TextField.class).setText("laptop");
//            assertEquals(1, list.getItems().size());
//            assertEquals("Gaming Laptop", list.getItems().get(0).getItem().getName());
//
//            field(controller, "searchField", TextField.class).clear();
//            field(controller, "btnUpcoming", Button.class).fire();
//            assertEquals(1, list.getItems().size());
//            assertEquals(AuctionStatus.NOT_START, list.getItems().get(0).getStatus());
//
//            field(controller, "btnAll", Button.class).fire();
//            field(controller, "categoryFilter", ComboBox.class).setValue("Art");
//            assertEquals(1, list.getItems().size());
//            assertEquals(ItemCategory.ART, list.getItems().get(0).getItem().getCategory());
//        });
//    }
//
//    @Test
//    @DisplayName("Participated auction list filters by text, status and winner")
//    void participatedAuctionList_filtersBySearchStatusAndWinner() throws Exception {
//        runOnFxThread(() -> {
//            ClientModel.getInstance().setCurrentUser(user(7, "Alice", "alice", "alice@gmail.com", "0912345678", "Ha Noi", AccountType.USER));
//
//            LoadedFxml<TestableParticipatedAuctionController> loaded = loadFxml(
//                    "UserParticipatedAuction.fxml",
//                    type -> type == ParticipatedAuctionController.class ? new TestableParticipatedAuctionController() : instantiate(type)
//            );
//            TestableParticipatedAuctionController controller = loaded.controller();
//
//            Auction running = auction("Gaming Laptop", AuctionStatus.RUNNING, ItemCategory.ELECTRONICS);
//            Auction closedWon = auction("Vintage Watch", AuctionStatus.CLOSED, ItemCategory.ART);
//            closedWon.setWinnerId(7);
//            Auction closedLost = auction("Office Chair", AuctionStatus.CLOSED, ItemCategory.OTHER);
//            closedLost.setWinnerId(100);
//            Auction paidWon = auction("Tablet", AuctionStatus.PAID, ItemCategory.ELECTRONICS);
//            paidWon.setWinnerId(7);
//
//            ObservableList<Auction> source = field(controller, "participatedAuctionList", ObservableList.class);
//            source.setAll(running, closedWon, closedLost, paidWon);
//
//            ListView<Auction> list = field(controller, "productList", ListView.class);
//            assertEquals(4, list.getItems().size());
//
//            field(controller, "searchField", TextField.class).setText("watch");
//            assertEquals(1, list.getItems().size());
//            assertEquals("Vintage Watch", list.getItems().get(0).getItem().getName());
//
//            field(controller, "searchField", TextField.class).clear();
//            field(controller, "btnDTGia", Button.class).fire();
//            assertEquals(1, list.getItems().size());
//            assertEquals(AuctionStatus.RUNNING, list.getItems().get(0).getStatus());
//
//            field(controller, "btnCTToan", Button.class).fire();
//            assertEquals(1, list.getItems().size());
//            assertEquals(7, list.getItems().get(0).getWinnerId());
//
//            field(controller, "btnDTToan", Button.class).fire();
//            assertEquals(1, list.getItems().size());
//            assertEquals(AuctionStatus.PAID, list.getItems().get(0).getStatus());
//        });
//    }
//
//    @Test
//    @DisplayName("Product display copies current auction and item into labels")
//    void productDisplayController_displaysCurrentAuctionFromClientModel() throws Exception {
//        runOnFxThread(() -> {
//            Auction auction = auction("Gaming Laptop", AuctionStatus.NOT_START, ItemCategory.ELECTRONICS);
//            auction.setInitPrice(money(1_250_000));
//            auction.setCurrentPrice(money(1_250_000));
//            auction.setStartingTime(LocalDateTime.now().plusHours(3));
//            auction.setEndingTime(LocalDateTime.now().plusDays(2));
//            ClientModel.getInstance().setCurrentItem(auction.getItem());
//            ClientModel.getInstance().setCurrentAuction(auction);
//
//            LoadedFxml<ProductDisplayController> loaded = loadFxml("AuctionInformation.fxml");
//            ProductDisplayController controller = loaded.controller();
//
//            assertEquals("Gaming Laptop", field(controller, "ItemNameLabel", Label.class).getText());
//            assertEquals("1,250,000 VND", field(controller, "initPriceLabel", Label.class).getText());
//            assertFalse(field(controller, "lbtimeLeft", Label.class).getText().isBlank());
//        });
//    }
//
//    @Test
//    @DisplayName("Live auction renders current model and sends auto-bid command")
//    void liveAuctionController_displaysAuctionAndSendsAutoBidCommand() throws Exception {
//        runOnFxThread(() -> {
//            ClientModel.getInstance().setCurrentUser(user(7, "Alice", "alice", "alice@gmail.com", "0912345678", "Ha Noi", AccountType.USER));
//            Auction auction = auction("Gaming Laptop", AuctionStatus.RUNNING, ItemCategory.ELECTRONICS);
//            auction.setAuctionId(99);
//            auction.setCurrentPrice(money(1_500_000));
//            ClientModel.getInstance().setCurrentItem(auction.getItem());
//            ClientModel.getInstance().setCurrentAuction(auction);
//
//            try (NetworkMocks network = mockNetwork()) {
//                LoadedFxml<NoNetworkLiveAuctionController> loaded = loadFxml(
//                        "LiveAuction.fxml",
//                        type -> type == LiveAuctionController.class ? new NoNetworkLiveAuctionController() : instantiate(type)
//                );
//                NoNetworkLiveAuctionController controller = loaded.controller();
//                controller.setCurrentAuctionInfoToScene();
//
//                assertEquals("Gaming Laptop", field(controller, "itemNameLb", Label.class).getText());
//                assertEquals("1,500,000 VND", field(controller, "currentPrice_tf", Label.class).getText());
//
//                field(controller, "autoMaxPrice_tf", TextField.class).setText("2500000");
//                field(controller, "autoBidToggle", ToggleButton.class).fire();
//
//                assertCommandSent(network.connection(), AutoBidCommand.class);
//            }
//        });
//    }
//
//    @Test
//    @DisplayName("TimeLeft updates before and after the auction end")
//    void timeLeftTick_updatesLabelAndStopsWhenExpired() throws Exception {
//        runOnFxThread(() -> {
//            LocalDateTime soonEnding = LocalDateTime.now().plusSeconds(90);
//            Label label = new Label();
//
//            TimeLeft runningTimer = new TimeLeft(label, soonEnding);
//            runningTimer.tick();
//            assertTrue(label.getText().matches("\\d{2}:\\d{2}:\\d{2}"));
//
//            LocalDateTime ended = LocalDateTime.now().minusSeconds(1);
//            Label endedLabel = new Label();
//
//            TimeLeft endedTimer = new TimeLeft(endedLabel, ended);
//            endedTimer.tick();
//            assertEquals("00:00:00", endedLabel.getText());
//        });
//    }
//
//    @Test
//    @DisplayName("Safe smoke-test controllers preserve initialize-free FXML loading")
//    void smokeTestControllerFactory_isStableForNetworkHeavyControllers() {
//        assertDoesNotThrow(() -> {
//            try (NetworkMocks ignored = mockNetwork()) {
//                assertInstanceOf(NoNetworkLoginController.class, controllerForSmokeTest(LoginController.class));
//                assertInstanceOf(NoNetworkAuctionController.class, controllerForSmokeTest(AuctionController.class));
//                assertInstanceOf(NoNetworkLiveAuctionController.class, controllerForSmokeTest(LiveAuctionController.class));
//                assertInstanceOf(NoNetworkSellerManagementController.class, controllerForSmokeTest(SellerManagementController.class));
//            }
//        });
//    }
//
//    private static void fillRegisterForm(
//            RegisterController controller,
//            String userName,
//            String email,
//            String password,
//            String confirmPassword,
//            String phone
//    ) throws Exception {
//        field(controller, "Username", TextField.class).setText(userName);
//        field(controller, "Email", TextField.class).setText(email);
//        field(controller, "Password", PasswordField.class).setText(password);
//        field(controller, "Confirm_Password", PasswordField.class).setText(confirmPassword);
//        field(controller, "PhoneNumber", TextField.class).setText(phone);
//    }
//
//    private static void fillEditUserForm(
//            Edit_User_Popup_Controller controller,
//            String fullName,
//            String phone,
//            String email,
//            String role
//    ) throws Exception {
//        field(controller, "txtName", TextField.class).setText(fullName);
//        field(controller, "txtPhoneNumber", TextField.class).setText(phone);
//        field(controller, "txtEmail", TextField.class).setText(email);
//        field(controller, "cbRole", ComboBox.class).setValue(role);
//    }
//
//    private static Auction auction(String itemName, AuctionStatus status, ItemCategory category) {
//        Item item = new Item();
//        item.setItemId(11);
//        item.setSellerId(3);
//        item.setSellerName("Seller One");
//        item.setName(itemName);
//        item.setDescription("Description for " + itemName);
//        item.setCategory(category);
//        item.setImage("");
//
//        Auction auction = new Auction();
//        auction.setAuctionId(21);
//        auction.setItem(item);
//        auction.setSellerId(3);
//        auction.setStatus(status);
//        auction.setInitPrice(money(1_000_000));
//        auction.setCurrentPrice(money(1_100_000));
//        auction.setStepPrice(money(100_000));
//        auction.setWinningPrice(money(1_200_000));
//        auction.setStartingTime(LocalDateTime.now().minusHours(1));
//        auction.setEndingTime(LocalDateTime.now().plusHours(1));
//        return auction;
//    }
//
//    private static User user(
//            int id,
//            String fullName,
//            String userName,
//            String email,
//            String phone,
//            String address,
//            AccountType role
//    ) {
//        return new User(id, fullName, "password1", email, phone, role, null);
//    }
//
//    private static BigDecimal money(long value) {
//        return BigDecimal.valueOf(value);
//    }
//
//    private static <T> void assertCommandSent(ServerConnection connection, Class<T> expectedCommandType) throws Exception {
//        ArgumentCaptor<com.javfxtutorial.hethongdaugia.common.network.Command> captor =
//                ArgumentCaptor.forClass(com.javfxtutorial.hethongdaugia.common.network.Command.class);
//
//        verify(connection).sendCommand(captor.capture());
//        assertInstanceOf(expectedCommandType, captor.getValue());
//    }
//
//    @SafeVarargs
//    private static void assertCommandsSent(
//            ServerConnection connection,
//            Class<? extends com.javfxtutorial.hethongdaugia.common.network.Command>... expectedCommandTypes
//    ) throws Exception {
//        ArgumentCaptor<com.javfxtutorial.hethongdaugia.common.network.Command> captor =
//                ArgumentCaptor.forClass(com.javfxtutorial.hethongdaugia.common.network.Command.class);
//
//        verify(connection, times(expectedCommandTypes.length)).sendCommand(captor.capture());
//        for (int i = 0; i < expectedCommandTypes.length; i++) {
//            assertInstanceOf(expectedCommandTypes[i], captor.getAllValues().get(i));
//        }
//    }
//
//    private static Object controllerForSmokeTest(Class<?> type) {
//        /*
//         * Smoke loading should verify FXML wiring, not start real sockets or
//         * background loaders. These subclasses keep FXMLLoader injection intact
//         * while neutralizing initialize methods that have side effects.
//         */
//        if (type == LoginController.class) {
//            return new NoNetworkLoginController();
//        }
//        if (type == AdminUserController.class) {
//            return new NoNetworkAdminUserController();
//        }
//        if (type == AdminItemController.class) {
//            return new NoNetworkAdminItemController();
//        }
//        if (type == AuctionController.class) {
//            return new NoNetworkAuctionController();
//        }
//        if (type == ParticipatedAuctionController.class) {
//            return new NoNetworkParticipatedAuctionController();
//        }
//        if (type == SellerManagementController.class) {
//            return new NoNetworkSellerManagementController();
//        }
//        if (type == LiveAuctionController.class) {
//            return new NoNetworkLiveAuctionController();
//        }
//        if (type == ProductDisplayController.class) {
//            return new NoopProductDisplayController();
//        }
//        if (type == UserProfileController.class) {
//            return new NoopUserProfileController();
//        }
//        if (type == AdminUpdateController.class) {
//            return new NoopAdminUpdateController();
//        }
//        if (type == Edit_User_Popup_Controller.class) {
//            return new NoopEditUserPopupController();
//        }
//        if (type == PasswordResetController.class) {
//            return new NoopPasswordResetController();
//        }
//        return instantiate(type);
//    }
//
//    private static Object instantiate(Class<?> type) {
//        try {
//            return type.getDeclaredConstructor().newInstance();
//        } catch (ReflectiveOperationException exception) {
//            throw new AssertionError("Cannot create FXML controller " + type.getName(), exception);
//        }
//    }
//
//    private static <T> LoadedFxml<T> loadFxml(String fileName) throws Exception {
//        return loadFxml(fileName, null);
//    }
//
//    private static <T> LoadedFxml<T> loadFxml(String fileName, Callback<Class<?>, Object> controllerFactory) throws Exception {
//        URL resource = ClientUiControllerTest.class.getResource(
//                "/com/javfxtutorial/hethongdaugia/view/fxml/" + fileName
//        );
//        assertNotNull(resource, () -> "Missing FXML resource: " + fileName);
//
//        FXMLLoader loader = new FXMLLoader(resource);
//        if (controllerFactory != null) {
//            loader.setControllerFactory(controllerFactory);
//        }
//
//        Node root = loader.load();
//        @SuppressWarnings("unchecked")
//        T controller = loader.getController();
//        return new LoadedFxml<>(root, controller);
//    }
//
//    private static <T> T field(Object target, String fieldName, Class<T> fieldType) throws Exception {
//        Class<?> currentType = target.getClass();
//        while (currentType != null) {
//            try {
//                Field field = currentType.getDeclaredField(fieldName);
//                field.setAccessible(true);
//                Object value = field.get(target);
//                return fieldType.cast(value);
//            } catch (NoSuchFieldException ignored) {
//                currentType = currentType.getSuperclass();
//            }
//        }
//        throw new NoSuchFieldException(fieldName);
//    }
//
//    private static NetworkMocks mockNetwork() {
//        NetworkManager manager = mock(NetworkManager.class);
//        ServerConnection connection = mock(ServerConnection.class);
//        MockedStatic<NetworkManager> staticMock = mockStatic(NetworkManager.class);
//        staticMock.when(NetworkManager::getInstance).thenReturn(manager);
//        staticMock.when(NetworkManager::getConnection).thenReturn(connection);
//        return new NetworkMocks(manager, connection, staticMock);
//    }
//
//    private static void runOnFxThread(FxTask task) throws Exception {
//        if (Platform.isFxApplicationThread()) {
//            task.run();
//            return;
//        }
//
//        CountDownLatch latch = new CountDownLatch(1);
//        AtomicReference<Throwable> failure = new AtomicReference<>();
//        Platform.runLater(() -> {
//            try {
//                task.run();
//            } catch (Throwable throwable) {
//                failure.set(throwable);
//            } finally {
//                latch.countDown();
//            }
//        });
//
//        assertTrue(latch.await(10, TimeUnit.SECONDS), "JavaFX task did not finish");
//        if (failure.get() instanceof Exception exception) {
//            throw exception;
//        }
//        if (failure.get() instanceof Error error) {
//            throw error;
//        }
//    }
//
//    @FunctionalInterface
//    private interface FxTask {
//        void run() throws Exception;
//    }
//
//    private record LoadedFxml<T>(Node root, T controller) {
//    }
//
//    private record FxmlCase(String fileName, Class<?> controllerType) {
//    }
//
//    private record NetworkMocks(
//            NetworkManager manager,
//            ServerConnection connection,
//            MockedStatic<NetworkManager> staticMock
//    ) implements AutoCloseable {
//        @Override
//        public void close() {
//            staticMock.close();
//        }
//    }
//
//    public static class TestableAuctionController extends AuctionController {
//        @Override
//        public void loadData() {
//            // Keep initialize listener setup, but avoid the asynchronous network load.
//        }
//    }
//
//    public static class TestableParticipatedAuctionController extends ParticipatedAuctionController {
//        @Override
//        public void loadData() {
//            // Keep initialize listener setup, but avoid the asynchronous network load.
//        }
//    }
//
//    public static class NoNetworkLoginController extends LoginController {
//        @Override
//        public void initialize(URL location, ResourceBundle resources) {
//            // FXML wiring only; real initialize starts NetworkManager.
//        }
//    }
//
//    public static class NoNetworkAdminUserController extends AdminUserController {
//        @Override
//        public void initialize(URL location, ResourceBundle resources) {
//            // FXML wiring only; real initialize starts an account-loading thread.
//        }
//    }
//
//    public static class NoNetworkAdminItemController extends AdminItemController {
//        @Override
//        public void initialize(URL location, ResourceBundle resources) {
//            // FXML wiring only; real initialize starts an item-loading thread.
//        }
//    }
//
//    public static class NoNetworkAuctionController extends AuctionController {
//        @Override
//        public void initialize() {
//            // FXML wiring only; behavior tests use TestableAuctionController.
//        }
//    }
//
//    public static class NoNetworkParticipatedAuctionController extends ParticipatedAuctionController {
//        @Override
//        public void initialize() {
//            // FXML wiring only; behavior tests use TestableParticipatedAuctionController.
//        }
//    }
//
//    public static class NoNetworkSellerManagementController extends SellerManagementController {
//        @Override
//        public void initialize() {
//            // FXML wiring only; real initialize requires current user and network.
//        }
//    }
//
//    public static class NoNetworkLiveAuctionController extends LiveAuctionController {
//        @Override
//        public void initialize() {
//            // FXML wiring only; real initialize registers commands and loads history.
//        }
//    }
//
//    public static class NoopProductDisplayController extends ProductDisplayController {
//        @Override
//        public void initialize() {
//            // FXML wiring only; behavior tests load the real controller.
//        }
//    }
//
//    public static class NoopUserProfileController extends UserProfileController {
//        @Override
//        public void initialize() {
//            // FXML wiring only; behavior tests load the real controller.
//        }
//    }
//
//    public static class NoopAdminUpdateController extends AdminUpdateController {
//        @Override
//        public void initialize() {
//            // FXML wiring only; behavior tests load the real controller.
//        }
//    }
//
//    public static class NoopEditUserPopupController extends Edit_User_Popup_Controller {
//        @Override
//        public void initialize() {
//            // FXML wiring only; behavior tests load the real controller.
//        }
//    }
//
//    public static class NoopPasswordResetController extends PasswordResetController {
//        @Override
//        public void initialize() {
//            // FXML wiring only; behavior tests load the real controller.
//        }
//    }
//}

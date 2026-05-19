package com.javfxtutorial.hethongdaugia.server.manager;

import com.javfxtutorial.hethongdaugia.common.Exception.data.DataException;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.AutoBidConfig;
import com.javfxtutorial.hethongdaugia.common.model.Command.AddAccountCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.AutoBidCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.DeleteAuctionCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.GetAuctionStatusCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.PlaceBidCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.RegisterToAuctionCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.UpdateAuctionCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.UpdateProfileCommand;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.model.User;
import com.javfxtutorial.hethongdaugia.common.model.enums.AccountType;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;
import com.javfxtutorial.hethongdaugia.server.dao.ItemDAO;
import com.javfxtutorial.hethongdaugia.server.dao.UserDAO;
import com.javfxtutorial.hethongdaugia.server.network.ClientHandler;
import com.javfxtutorial.hethongdaugia.server.network.ClientHandlerContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Contract xử lý command từ client")
class CommandContractTest {
    private AuctionManager manager;

    @BeforeEach
    void setUp() throws Exception {
        manager = AuctionManager.getInstance();
        TestStateSupport.resetAuctionManager(manager);
    }

    @AfterEach
    void tearDown() throws Exception {
        TestStateSupport.resetAuctionManager(manager);
        ClientHandlerContextHolder.clear();
    }

    @Nested
    @DisplayName("AutoBidCommand")
    class AutoBidCommandTest {
        @Test
        @DisplayName("thiếu cấu hình auto-bid trả failure")
        void handle_returnsFailureWhenConfigIsMissing() {
            AutoBidCommand command = new AutoBidCommand();

            Response response = command.handle();

            assertFalse(response.isSuccess());
            assertNull(response.getPayLoad());
        }

        @Test
        @DisplayName("cấu hình tắt auto-bid đăng ký thành công không cần DB")
        void handle_returnsSuccessWhenInactiveConfigCanBeRegisteredWithoutDatabase() {
            AutoBidCommand command = new AutoBidCommand();
            AutoBidConfig config = new AutoBidConfig(1, "alice", 100, new BigDecimal("200"), false);
            command.addData("autoBidConfig", config);

            Response response = command.handle();

            assertTrue(response.isSuccess());
            assertSame(config, response.getPayLoad());
        }
    }

    @Nested
    @DisplayName("Auction commands")
    class AuctionCommandTest {
        @Test
        @DisplayName("trả về trạng thái hiện tại của phiên")
        void getAuctionStatusCommand_returnsCurrentStatus() {
            Auction auction = runningAuction(AuctionStatus.RUNNING);
            GetAuctionStatusCommand command = new GetAuctionStatusCommand(auction);

            Response response = command.handle();

            assertTrue(response.isSuccess());
            assertEquals(AuctionStatus.RUNNING, response.getPayLoad());
        }

        @Test
        @DisplayName("không xóa phiên đang chạy")
        void deleteAuctionCommand_rejectsRunningAuctionBeforeDaoDelete() {
            Auction auction = runningAuction(AuctionStatus.RUNNING);
            DeleteAuctionCommand command = new DeleteAuctionCommand(auction);

            Response response = command.handle();

            assertFalse(response.isSuccess());
            assertNull(response.getPayLoad());
        }

        @Test
        @DisplayName("xóa item khi phiên chưa bắt đầu")
        void deleteAuctionCommand_deletesItemWhenAuctionHasNotStarted() throws Exception {
            Auction auction = runningAuction(AuctionStatus.NOT_START);
            auction.setStartingTime(LocalDateTime.now().plusMinutes(5));
            Item item = new Item();
            auction.setItem(item);
            DeleteAuctionCommand command = new DeleteAuctionCommand(auction);

            ItemDAO itemDAO = mock(ItemDAO.class);
            when(itemDAO.delete(item)).thenReturn(1);

            try (MockedStatic<ItemDAO> mockedItemDAO = mockStatic(ItemDAO.class)) {
                mockedItemDAO.when(ItemDAO::getInstance).thenReturn(itemDAO);

                Response response = command.handle();

                assertTrue(response.isSuccess());
                assertNull(response.getPayLoad());
                verify(itemDAO).delete(item);
            }
        }

        @Test
        @DisplayName("không sửa phiên đang chạy")
        void updateAuctionCommand_rejectsRunningAuctionBeforeDaoUpdate() {
            Auction auction = runningAuction(AuctionStatus.RUNNING);
            UpdateAuctionCommand command = new UpdateAuctionCommand(auction);

            Response response = command.handle();

            assertFalse(response.isSuccess());
            assertNull(response.getPayLoad());
        }

        @Test
        @DisplayName("sửa item và auction khi phiên chưa bắt đầu")
        void updateAuctionCommand_updatesItemAndAuctionWhenAuctionHasNotStarted() throws Exception {
            Auction auction = runningAuction(AuctionStatus.NOT_START);
            auction.setStartingTime(LocalDateTime.now().plusMinutes(5));
            Item item = new Item();
            auction.setItem(item);
            UpdateAuctionCommand command = new UpdateAuctionCommand(auction);

            ItemDAO itemDAO = mock(ItemDAO.class);
            AuctionDAO auctionDAO = mock(AuctionDAO.class);
            when(itemDAO.update(item)).thenReturn(1);
            when(auctionDAO.update(auction)).thenReturn(1);

            try (MockedStatic<ItemDAO> mockedItemDAO = mockStatic(ItemDAO.class);
                 MockedStatic<AuctionDAO> mockedAuctionDAO = mockStatic(AuctionDAO.class)) {
                mockedItemDAO.when(ItemDAO::getInstance).thenReturn(itemDAO);
                mockedAuctionDAO.when(AuctionDAO::getInstance).thenReturn(auctionDAO);

                Response response = command.handle();

                assertTrue(response.isSuccess());
                assertSame(auction, response.getPayLoad());
                verify(itemDAO).update(item);
                verify(auctionDAO).update(auction);
            }
        }

        @Test
        @DisplayName("đăng ký listener hiện tại vào phòng auction")
        void registerToAuctionCommand_registersCurrentContextListenerAndReturnsSuccessResponse() throws Exception {
            Auction auction = runningAuction(AuctionStatus.RUNNING);
            auction.setAuctionId(123);
            ClientHandler currentClient = new ClientHandler(null);
            ClientHandlerContextHolder.set(currentClient);

            RegisterToAuctionCommand command = new RegisterToAuctionCommand();
            command.addData("currentAuction", auction);

            Response response = command.handle();

            assertNotNull(response);
            assertTrue(response.isSuccess());
            assertNull(response.getPayLoad());
            assertEquals(1, TestStateSupport.auctionSubscribers(manager).get(123).size());
            assertSame(currentClient, TestStateSupport.auctionSubscribers(manager).get(123).get(0));
        }

        @Test
        @DisplayName("thiếu auction thì không đăng ký listener")
        void registerToAuctionCommand_missingAuctionReturnsFailureWithoutRegisteringListener() throws Exception {
            RegisterToAuctionCommand command = new RegisterToAuctionCommand();

            Response response = command.handle();

            assertFalse(response.isSuccess());
            assertNull(response.getPayLoad());
            assertTrue(TestStateSupport.auctionSubscribers(manager).isEmpty());
        }

        @Test
        @DisplayName("thiếu bid thì trả failure")
        void placeBidCommand_missingBidReturnsFailureResponse() {
            PlaceBidCommand command = new PlaceBidCommand();

            Response response = command.handle();

            assertFalse(response.isSuccess());
            assertNull(response.getPayLoad());
        }
    }

    @Nested
    @DisplayName("Account command")
    class AccountCommandTest {
        @Test
        @DisplayName("tạo tài khoản khi DAO insert thành công")
        void addAccountCommand_returnsSuccessOnlyWhenDaoInsertSucceeds() throws DataException {
            AddAccountCommand command = validAddAccountCommand();
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.insert(any(User.class))).thenReturn(1);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                Response response = command.handle();

                assertTrue(response.isSuccess());
                assertNull(response.getPayLoad());
                verify(userDAO).insert(any(User.class));
            }
        }

        @Test
        @DisplayName("trả failure khi username bị trùng")
        void addAccountCommand_duplicateUsernameReturnsFailure() throws DataException {
            AddAccountCommand command = validAddAccountCommand();
            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.insert(any(User.class))).thenReturn(-1);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                Response response = command.handle();

                assertFalse(response.isSuccess());
                assertNull(response.getPayLoad());
            }
        }

        @Test
        @DisplayName("role không hợp lệ bị chặn trước DAO")
        void addAccountCommand_invalidRoleReturnsFailureBeforeDao() throws DataException {
            AddAccountCommand command = validAddAccountCommand();
            command.addData("accountType", "MANAGER");
            UserDAO userDAO = mock(UserDAO.class);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                Response response = command.handle();

                assertFalse(response.isSuccess());
                assertNull(response.getPayLoad());
                verify(userDAO, never()).insert(any(User.class));
            }
        }
    }

    @Nested
    @DisplayName("Profile command")
    class ProfileCommandTest {
        @Test
        @DisplayName("cập nhật profile thành công và giữ command để client dispatch")
        void updateProfileCommand_successResponseKeepsCommandForClientDispatch() throws DataException {
            UpdateProfileCommand command = new UpdateProfileCommand();
            command.addData("userId", 1);
            command.addData("username", "Alice Nguyen");
            command.addData("email", "alice.new@example.com");
            command.addData("phone", "0999999999");
            command.addData("avt", "avatar.png");

            UserDAO userDAO = mock(UserDAO.class);
            when(userDAO.selectById(1)).thenReturn(
                    new User(1, "Alice", "secret", "alice@example.com", "0900000000", AccountType.USER, "old.png")
            );
            when(userDAO.update(any(User.class))).thenReturn(1);

            try (MockedStatic<UserDAO> mockedUserDAO = mockStatic(UserDAO.class)) {
                mockedUserDAO.when(UserDAO::getInstance).thenReturn(userDAO);

                Response response = command.handle();

                assertTrue(response.isSuccess());
                assertSame(command, response.getCommand());
            }
        }
    }

    private static Auction runningAuction(AuctionStatus status) {
        Auction auction = new Auction();
        auction.setAuctionId(100);
        auction.setStartingTime(LocalDateTime.now().minusMinutes(5));
        auction.setEndingTime(LocalDateTime.now().plusMinutes(5));
        auction.setStatus(status);
        auction.setCurrentPrice(new BigDecimal("100"));
        auction.setStepPrice(new BigDecimal("10"));
        return auction;
    }

    private static AddAccountCommand validAddAccountCommand() {
        AddAccountCommand command = new AddAccountCommand();
        command.addData("username", "alice");
        command.addData("password", "secret123");
        command.addData("email", "alice@example.com");
        command.addData("sdt", "0901234567");
        command.addData("accountType", "USER");
        return command;
    }
}

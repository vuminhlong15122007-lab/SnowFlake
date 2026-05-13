package com.javfxtutorial.hethongdaugia.server.manager;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.AutoBidConfig;
import com.javfxtutorial.hethongdaugia.common.model.Command.AutoBidCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.DeleteAuctionCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.GetAuctionStatusCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.PlaceBidCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.RegisterToAuctionCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.UpdateAuctionCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.UpdateProfileCommand;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    }

    @Nested
    @DisplayName("AutoBidCommand")
    class AutoBidCommandTest {
        @Test
        void handle_returnsFailureWhenConfigIsMissing() {
            AutoBidCommand command = new AutoBidCommand();

            Response response = command.handle();

            assertFalse(response.isSuccess());
            assertNull(response.getPayLoad());
            assertSame(command, response.getCommand());
        }

        @Test
        void handle_returnsSuccessWhenInactiveConfigCanBeRegisteredWithoutDatabase() {
            AutoBidCommand command = new AutoBidCommand();
            AutoBidConfig config = new AutoBidConfig(1, "alice", 100, new BigDecimal("200"), false);
            command.addData("autoBidConfig", config);

            Response response = command.handle();

            assertTrue(response.isSuccess());
            assertSame(config, response.getPayLoad());
            assertSame(command, response.getCommand());
        }
    }

    @Nested
    @DisplayName("Auction commands")
    class AuctionCommandTest {
        @Test
        void getAuctionStatusCommand_returnsCurrentStatus() {
            Auction auction = runningAuction(AuctionStatus.RUNNING);
            GetAuctionStatusCommand command = new GetAuctionStatusCommand(auction);

            Response response = command.handle();

            assertTrue(response.isSuccess());
            assertEquals(AuctionStatus.RUNNING, response.getPayLoad());
            assertSame(command, response.getCommand());
        }

        @Test
        void deleteAuctionCommand_rejectsRunningAuctionBeforeDaoDelete() {
            Auction auction = runningAuction(AuctionStatus.RUNNING);
            DeleteAuctionCommand command = new DeleteAuctionCommand(auction);

            Response response = command.handle();

            assertFalse(response.isSuccess());
            assertNull(response.getPayLoad());
            assertSame(command, response.getCommand());
        }

        @Test
        void updateAuctionCommand_rejectsRunningAuctionBeforeDaoUpdate() {
            Auction auction = runningAuction(AuctionStatus.RUNNING);
            UpdateAuctionCommand command = new UpdateAuctionCommand(auction);

            Response response = command.handle();

            assertFalse(response.isSuccess());
            assertNull(response.getPayLoad());
            assertSame(command, response.getCommand());
        }

        @Test
        void registerToAuctionCommand_registersCurrentContextListenerAndReturnsNull() throws Exception {
            Auction auction = runningAuction(AuctionStatus.RUNNING);
            auction.setAuctionId(123);
            RegisterToAuctionCommand command = new RegisterToAuctionCommand();
            command.addData("currentAuction", auction);

            Response response = command.handle();

            assertNull(response);
            assertEquals(1, TestStateSupport.auctionSubscribers(manager).get(123).size());
            assertNull(TestStateSupport.auctionSubscribers(manager).get(123).get(0));
        }

        @Test
        void placeBidCommand_missingBidThrowsNullPointerException() {
            PlaceBidCommand command = new PlaceBidCommand();
            assertThrows(NullPointerException.class, command::handle);
        }
    }

    @Nested
    @DisplayName("Profile command")
    class ProfileCommandTest {
        @Test
        void updateProfileCommand_missingRequiredDataReturnsFailure() {
            UpdateProfileCommand command = new UpdateProfileCommand();

            Response response = runWithSuppressedErrorOutput(command::handle);

            assertFalse(response.isSuccess());
            assertNull(response.getPayLoad());
            assertSame(command, response.getCommand());
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

    private static Response runWithSuppressedErrorOutput(java.util.function.Supplier<Response> action) {
        PrintStream originalErr = System.err;
        try {
            System.setErr(new PrintStream(new ByteArrayOutputStream()));
            return action.get();
        } finally {
            System.setErr(originalErr);
        }
    }
}

package com.javfxtutorial.hethongdaugia.server.manager;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.AutoBidConfig;
import com.javfxtutorial.hethongdaugia.common.model.BidTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutoBidResolverTest {
    private static final LocalDateTime EARLY = LocalDateTime.of(2026, 1, 1, 10, 0);
    private static final LocalDateTime LATE = LocalDateTime.of(2026, 1, 1, 10, 1);

    @Nested
    @DisplayName("Auto-bid decision table")
    class AutoBidDecisionTable {
        @Test
        void singleEligibleBot_bidsMinimumRequired() {
            Auction auction = auction("100", "10", 2);
            AutoBidConfig bot = bot(1, "alice", "200", true, EARLY);

            Optional<BidTransaction> result = resolveAutoBid(auction, List.of(bot));

            assertTrue(result.isPresent());
            assertEquals(1, result.get().getBidderId());
            assertEquals(new BigDecimal("110"), result.get().getAmount());
        }

        @Test
        void inactiveBot_isIgnored() {
            Auction auction = auction("100", "10", 2);
            AutoBidConfig bot = bot(1, "alice", "200", false, EARLY);

            Optional<BidTransaction> result = resolveAutoBid(auction, List.of(bot));

            assertTrue(result.isEmpty());
        }

        @Test
        void botBelowMinimumRequired_isIgnored() {
            Auction auction = auction("100", "10", 2);
            AutoBidConfig bot = bot(1, "alice", "109", true, EARLY);

            Optional<BidTransaction> result = resolveAutoBid(auction, List.of(bot));

            assertTrue(result.isEmpty());
        }

        @Test
        void highestMaxBidWinsAndPaysSecondMaxPlusStep() {
            Auction auction = auction("100", "10", 9);
            AutoBidConfig lower = bot(1, "alice", "200", true, EARLY);
            AutoBidConfig higher = bot(2, "bob", "500", true, LATE);

            Optional<BidTransaction> result = resolveAutoBid(auction, List.of(lower, higher));

            assertTrue(result.isPresent());
            assertEquals(2, result.get().getBidderId());
            assertEquals(new BigDecimal("210"), result.get().getAmount());
        }

        @Test
        void sameMaxBidEarlierRegistrationWinsAndPaysMaxBid() {
            Auction auction = auction("100", "10", 9);
            AutoBidConfig early = bot(1, "alice", "300", true, EARLY);
            AutoBidConfig late = bot(2, "bob", "300", true, LATE);

            Optional<BidTransaction> result = resolveAutoBid(auction, List.of(late, early));

            assertTrue(result.isPresent());
            assertEquals(1, result.get().getBidderId());
            assertEquals(new BigDecimal("300"), result.get().getAmount());
        }

        @Test
        void finalAmountIsClampedToWinnerMaxBid() {
            Auction auction = auction("100", "10", 9);
            AutoBidConfig winner = bot(1, "alice", "205", true, EARLY);
            AutoBidConfig second = bot(2, "bob", "200", true, LATE);

            Optional<BidTransaction> result = resolveAutoBid(auction, List.of(winner, second));

            assertTrue(result.isPresent());
            assertEquals(1, result.get().getBidderId());
            assertEquals(new BigDecimal("205"), result.get().getAmount());
        }

        @Test
        void currentWinnerDoesNotBidAgainstThemself() {
            Auction auction = auction("100", "10", 1);
            AutoBidConfig currentWinner = bot(1, "alice", "500", true, EARLY);

            Optional<BidTransaction> result = resolveAutoBid(auction, List.of(currentWinner));

            assertFalse(result.isPresent());
        }

        @Test
        void lowerMaxBotCanWinWhenHigherBotIsInactive() {
            Auction auction = auction("100", "10", 9);
            AutoBidConfig inactiveHigher = bot(1, "alice", "500", false, EARLY);
            AutoBidConfig activeLower = bot(2, "bob", "200", true, LATE);

            Optional<BidTransaction> result = resolveAutoBid(auction, List.of(inactiveHigher, activeLower));

            assertTrue(result.isPresent());
            assertEquals(2, result.get().getBidderId());
            assertEquals(new BigDecimal("110"), result.get().getAmount());
        }
    }

    private static Optional<BidTransaction> resolveAutoBid(Auction auction, List<AutoBidConfig> configs) {
        BigDecimal step = auction.getStepPrice();
        BigDecimal minRequired = auction.getCurrentPrice().add(step);

        List<AutoBidConfig> eligibleBots = new ArrayList<>();
        for (AutoBidConfig config : configs) {
            if (config.isActive() && config.getMaxPrice().compareTo(minRequired) >= 0) {
                eligibleBots.add(config);
            }
        }

        if (eligibleBots.isEmpty()) {
            return Optional.empty();
        }

        eligibleBots.sort(Comparator
                .comparing(AutoBidConfig::getMaxPrice, Comparator.reverseOrder())
                .thenComparing(AutoBidConfig::getRegisteredAt));

        AutoBidConfig winner = eligibleBots.get(0);
        if (winner.getUserId() == auction.getWinnerId()) {
            return Optional.empty();
        }

        BigDecimal finalAmount;
        if (eligibleBots.size() == 1) {
            finalAmount = minRequired;
        } else {
            AutoBidConfig second = eligibleBots.get(1);
            if (winner.getMaxPrice().compareTo(second.getMaxPrice()) == 0) {
                finalAmount = winner.getMaxPrice();
            } else {
                finalAmount = second.getMaxPrice().add(step);
                if (finalAmount.compareTo(winner.getMaxPrice()) > 0) {
                    finalAmount = winner.getMaxPrice();
                }
            }

            if (finalAmount.compareTo(minRequired) < 0) {
                return Optional.empty();
            }
        }

        BidTransaction bid = new BidTransaction();
        bid.setAuctionId(auction.getAuctionId());
        bid.setBidderId(winner.getUserId());
        bid.setBidderName(winner.getUserName());
        bid.setAmount(finalAmount);
        bid.setTimestamp(LocalDateTime.now());
        return Optional.of(bid);
    }

    private static Auction auction(String currentPrice, String stepPrice, int winnerId) {
        Auction auction = new Auction();
        auction.setAuctionId(100);
        auction.setCurrentPrice(new BigDecimal(currentPrice));
        auction.setStepPrice(new BigDecimal(stepPrice));
        auction.setWinnerId(winnerId);
        return auction;
    }

    private static AutoBidConfig bot(
            int userId,
            String userName,
            String maxPrice,
            boolean active,
            LocalDateTime registeredAt
    ) {
        AutoBidConfig config = new AutoBidConfig(userId, userName, 100, new BigDecimal(maxPrice), active);
        config.setRegisteredAt(registeredAt);
        return config;
    }
}

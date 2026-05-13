package com.javfxtutorial.hethongdaugia.server.manager;

import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;
import com.javfxtutorial.hethongdaugia.common.Exception.auc.AuctionNotFoundException;
import com.javfxtutorial.hethongdaugia.common.Exception.auth.InvalidCredentialsException;
import com.javfxtutorial.hethongdaugia.common.Exception.auth.UserAlreadyExistsException;
import com.javfxtutorial.hethongdaugia.common.Exception.bid.InsufficientIncrementException;
import com.javfxtutorial.hethongdaugia.common.Exception.bid.LowerThanCurrentBidException;
import com.javfxtutorial.hethongdaugia.common.Exception.bid.SelfBidException;
import com.javfxtutorial.hethongdaugia.common.Exception.bus.InvalidInputException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.DuplicateKeyException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.EntityNotFoundException;
import com.javfxtutorial.hethongdaugia.common.Exception.net.SendFailedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExceptionContractTest {
    @Nested
    @DisplayName("ErrorCode")
    class ErrorCodeTest {
        @Test
        void fromCode_returnsMatchingCode() {
            assertSame(ErrorCode.AUTH_INVALID_CREDENTIALS, ErrorCode.fromCode("AUTH-001"));
            assertSame(ErrorCode.DATA_DUPLICATE, ErrorCode.fromCode("DATA-002"));
            assertSame(ErrorCode.NET_SEND_FAILED, ErrorCode.fromCode("NET-003"));
        }

        @Test
        void fromCode_returnsUnknownForMissingCode() {
            assertSame(ErrorCode.SYS_UNKNOW_ERROR, ErrorCode.fromCode("missing"));
        }

        @Test
        void enumFieldsExposeCodeMessageAndHttpStatus() {
            assertEquals("AUTH-001", ErrorCode.AUTH_INVALID_CREDENTIALS.getCode());
            assertEquals(401, ErrorCode.AUTH_INVALID_CREDENTIALS.getHttpStatusCode());
            assertTrue(ErrorCode.AUTH_INVALID_CREDENTIALS.getDefaultMessage().length() > 0);
        }
    }

    @Nested
    @DisplayName("Exception hierarchy")
    class ExceptionHierarchyTest {
        @Test
        void invalidCredentials_usesAuthErrorCode() {
            InvalidCredentialsException ex = new InvalidCredentialsException("bad login");

            assertEquals("AUTH-001", ex.getErrorCode());
            assertEquals(401, ex.getHttpStatusCode());
            assertEquals("bad login", ex.getMessage());
            assertTrue(ex.toString().contains("AUTH-001"));
        }

        @Test
        void userAlreadyExists_includesUsername() {
            UserAlreadyExistsException ex = new UserAlreadyExistsException("alice");

            assertEquals("AUTH-002", ex.getErrorCode());
            assertEquals(400, ex.getHttpStatusCode());
            assertTrue(ex.getMessage().contains("alice"));
        }

        @Test
        void auctionNotFound_storesAuctionId() {
            AuctionNotFoundException ex = new AuctionNotFoundException(123);

            assertEquals("AUC-001", ex.getErrorCode());
            assertEquals(404, ex.getHttpStatusCode());
            assertEquals(123, ex.getAuctionId());
        }

        @Test
        void lowerThanCurrentBid_storesPrices() {
            LowerThanCurrentBidException ex = new LowerThanCurrentBidException(200.0, 150.0);

            assertEquals("BID-001", ex.getErrorCode());
            assertEquals(400, ex.getHttpStatusCode());
            assertEquals(200.0, ex.getCurrentPrice());
            assertEquals(150.0, ex.getOfferedPrice());
        }

        @Test
        void insufficientIncrement_usesMinimumErrorCode() {
            InsufficientIncrementException ex = new InsufficientIncrementException(10.0, 5.0);

            assertEquals("BID-003", ex.getErrorCode());
            assertEquals(10.0, ex.getMinIncrement());
            assertEquals(5.0, ex.getActualIncrement());
        }

        @Test
        void selfBid_defaultUsesSelfBidCode() {
            SelfBidException ex = new SelfBidException();

            assertEquals("BID-002", ex.getErrorCode());
            assertEquals(400, ex.getHttpStatusCode());
        }

        @Test
        void duplicateKey_storesEntityFieldAndValue() {
            DuplicateKeyException ex = new DuplicateKeyException("User", "email", "a@example.com");

            assertEquals("DATA-002", ex.getErrorCode());
            assertEquals(409, ex.getHttpStatusCode());
            assertEquals("User", ex.getEntityType());
            assertEquals("email", ex.getFieldName());
            assertEquals("a@example.com", ex.getFieldValue());
        }

        @Test
        void entityNotFound_storesEntityTypeAndId() {
            EntityNotFoundException ex = new EntityNotFoundException("Auction", 9);

            assertEquals("DATA-001", ex.getErrorCode());
            assertEquals("Auction", ex.getEntityType());
            assertEquals(9, ex.getEntityId());
        }

        @Test
        void invalidInput_storesFieldValueAndReason() {
            InvalidInputException ex = new InvalidInputException("amount", "-1", "negative");

            assertEquals("BIZ-001", ex.getErrorCode());
            assertEquals("amount", ex.getFieldName());
            assertEquals("-1", ex.getInvalidValue());
            assertEquals("negative", ex.getValidationRule());
        }

        @Test
        void sendFailed_usesNetworkCode() {
            SendFailedException ex = new SendFailedException("Command");

            assertEquals("NET-003", ex.getErrorCode());
            assertEquals(500, ex.getHttpStatusCode());
            assertTrue(ex.getMessage().contains("Command"));
        }
    }
}

package com.javfxtutorial.hethongdaugia.server.manager;

import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;
import com.javfxtutorial.hethongdaugia.common.Exception.auc.AuctionNotFoundException;
import com.javfxtutorial.hethongdaugia.common.Exception.auth.InvalidCredentialsException;
import com.javfxtutorial.hethongdaugia.common.Exception.bid.InsufficientIncrementException;
import com.javfxtutorial.hethongdaugia.common.Exception.bid.LowerThanCurrentBidException;
import com.javfxtutorial.hethongdaugia.common.Exception.bid.SelfBidException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.DuplicateKeyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName("Mã lỗi nghiệp vụ trả về cho client")
class ExceptionContractTest {
    @Test
    @DisplayName("tra mã lỗi đúng và fallback về lỗi hệ thống")
    void fromCode_returnsKnownErrorCodeAndFallsBackToSystemError() {
        assertSame(ErrorCode.AUTH_INVALID_CREDENTIALS, ErrorCode.fromCode("AUTH-001"));
        assertSame(ErrorCode.BID_SELF_BID, ErrorCode.fromCode("BID-002"));
        assertSame(ErrorCode.SYS_UNKNOW_ERROR, ErrorCode.fromCode("missing"));
    }

    @Test
    @DisplayName("lỗi xác thực và dữ liệu giữ mã trả về cho client")
    void authenticationAndDataExceptions_keepClientFacingCodes() {
        InvalidCredentialsException invalidLogin = new InvalidCredentialsException("bad login");
        DuplicateKeyException duplicateEmail = new DuplicateKeyException("User", "email", "a@example.com");

        assertEquals("AUTH-001", invalidLogin.getErrorCode());
        assertEquals(401, invalidLogin.getHttpStatusCode());
        assertEquals("DATA-002", duplicateEmail.getErrorCode());
        assertEquals(409, duplicateEmail.getHttpStatusCode());
    }

    @Test
    @DisplayName("lỗi đấu giá và đặt giá giữ mã trả về cho client")
    void auctionAndBidExceptions_keepClientFacingCodes() {
        AuctionNotFoundException missingAuction = new AuctionNotFoundException(123);
        LowerThanCurrentBidException lowerBid = new LowerThanCurrentBidException(200.0, 150.0);
        InsufficientIncrementException smallStep = new InsufficientIncrementException(10.0, 5.0);
        SelfBidException selfBid = new SelfBidException();

        assertEquals("AUC-001", missingAuction.getErrorCode());
        assertEquals(404, missingAuction.getHttpStatusCode());
        assertEquals("BID-001", lowerBid.getErrorCode());
        assertEquals("BID-003", smallStep.getErrorCode());
        assertEquals("BID-002", selfBid.getErrorCode());
    }
}

# Test review and suggested test plan

Ngay kiem tra: 2026-05-13

Lenh nen chay:

```powershell
$env:JAVA_HOME='C:\Java'
.\mvnw.cmd test '-Djava.awt.headless=true' '-Dtestfx.headless=true' '-Dglass.platform=Monocle' --no-transfer-progress
```

## 1. Trang thai test hien co

Thu muc test hien co:

| File | Trang thai | Ghi chu |
|---|---:|---|
| `src/test/java/com/javfxtutorial/hethongdaugia/server/manager/AuctionManagerTest.java` | Co test | Dang test Singleton, status boundary, va mot doan logic auto-bid mo phong. |
| `src/test/java/com/javfxtutorial/hethongdaugia/server/manager/AuctionStatusTest.java` | Co test | Test `refreshAuctionStatus`, nhung dang co kha nang goi DB qua `AuctionDAO.update`. |
| `src/test/java/com/javfxtutorial/hethongdaugia/server/manager/BidValidationTest.java` | Co test | Test `checkValidBid`; nen doi `new BigDecimal(100.0)` thanh `new BigDecimal("100.0")`. |
| `src/test/java/com/javfxtutorial/hethongdaugia/server/manager/UserManagerTest.java` | Co test | Phan lon test dang mo phong logic bang ham `simulate...`, chua goi truc tiep `UserManager`. |
| `src/test/java/com/javfxtutorial/hethongdaugia/server/manager/AutoBidResolverTest.java` | Rong | Nen bo sung test auto-bid vao day. |

So test hien tai theo `@Test`: 33.

## 2. Van de nen sua trong test hien tai

1. `UserManagerTest` dang test lai logic bang ham `simulateAuthenticate`, `simulateMerge`, `simulateResetPassword`.
   - Cach nay giup mo ta y tuong, nhung khong bat duoc bug trong `UserManager`.
   - Nen refactor `UserManager` de inject/fake `UserDAO`, sau do test truc tiep `UserManager.authenticate`, `updateUserProfile`, `reset_password`.

2. `AuctionStatusTest` va mot phan `AuctionManagerTest` dang goi `refreshAuctionStatus`.
   - Method nay co goi `AuctionDAO.update` neu status thay doi.
   - Ket qua la unit test phu thuoc TiDB Cloud/database ngoai.
   - Nen tach logic tinh status thanh ham pure, vi du `calculateStatus(auction, now)`, sau do unit test ham pure.

3. `AutoBidResolverTest.java` dang rong.
   - Day la phan nang cao quan trong, nen uu tien them test.

4. `BidValidationTest` dung `new BigDecimal(100.0)`.
   - Nen dung `new BigDecimal("100.0")` hoac `BigDecimal.valueOf(100.0)` de tranh loi precision.

5. `AuctionManager` la Singleton va co state noi bo: `activeAuctions`, `auctionSubscribers`, `autoBidRegistry`.
   - Test co the bi anh huong lan nhau.
   - Nen co cach reset state trong test, hoac tach class xu ly logic thuan ra khoi Singleton.

## 3. Test nen bo sung theo muc uu tien

### P0 - Nen viet truoc

#### `BidValidationTest`

- `valid_equalCurrentPlusStep_returnsTrue`
- `valid_greaterThanCurrentPlusStep_returnsTrue`
- `invalid_equalCurrentPrice_returnsFalse`
- `invalid_belowCurrentPlusStep_returnsFalse`
- `invalid_negativeAmount_returnsFalse`
- `invalid_zeroAmount_returnsFalse`
- `invalid_nullAmount_shouldThrowOrReturnFalse`
- `invalid_nullCurrentPrice_shouldThrowOrReturnFalse`
- `invalid_nullStepPrice_shouldThrowOrReturnFalse`

Ghi chu: neu muon test null tra ve false thay vi throw, production code can sua `checkValidBid`.

#### `AuctionStatusTest`

Nen tach ham pure truoc. Sau khi tach, test cac case:

- `status_beforeStart_isNotStart`
- `status_atStart_isRunning`
- `status_betweenStartAndEnd_isRunning`
- `status_atEnd_isRunningOrClosed_theRuleMustBeClear`
- `status_afterEnd_isClosed`
- `status_paid_shouldRemainPaidIfAlreadyPaid`
- `status_cancelled_shouldRemainCancelledIfAlreadyCancelled`

Ghi chu: hien tai status enum la `NOT_START, RUNNING, CLOSED, CANCELLED, PAID`, khac de bai `OPEN -> RUNNING -> FINISHED -> PAID/CANCELED`. Nen thong nhat ten status de tranh mat diem.

#### `AutoBidResolverTest`

Nen test rieng logic chon auto-bid, khong goi DB:

- `singleEligibleBot_bidsMinimumRequired`
- `inactiveBot_isIgnored`
- `botWithMaxBelowMinRequired_isIgnored`
- `highestMaxBotWins`
- `sameMaxBid_earlierRegisteredWins`
- `finalAmount_neverExceedsWinnerMax`
- `winnerAlreadyLeading_doesNotBidAgain`
- `multipleBots_secondMaxPlusStep_usedWhenPossible`
- `secondMaxPlusStepAboveWinnerMax_clampedToWinnerMax`

Khuyen nghi: tach `checkAndExecuteAutoBids` thanh mot class pure, vi du `AutoBidResolver`, tra ve `Optional<BidTransaction>`.

#### `AuctionManagerPlaceBidTest`

Can fake DAO hoac refactor dependency injection. Test can co:

- `placeBid_returnsFalse_whenAuctionNotFound`
- `placeBid_returnsFalse_whenAmountInvalid`
- `placeBid_updatesCurrentPriceWinnerAndWinningPrice_whenValid`
- `placeBid_extendsEndingTime_whenBidInLast60Seconds`
- `placeBid_doesNotExtendEndingTime_whenMoreThan60SecondsLeft`
- `placeBid_notifiesSubscribersExceptSender`
- `placeBid_persistsAuctionBidAndParticipatedAuction_whenValid`
- `placeBid_doesNotPersistAnything_whenInvalid`
- `placeBid_rejectsBid_whenAuctionClosed`
- `placeBid_rejectsBid_whenAuctionNotStarted`

Hai case cuoi dang rat quan trong vi hien production code moi chi check gia, chua check status/time trong `placeBid`.

### P1 - Nen viet sau P0

#### `UserManagerTest`

Sau khi fake/inject `UserDAO`, test truc tiep:

- `authenticate_success_returnsUser`
- `authenticate_wrongPassword_returnsNull`
- `authenticate_unknownUsername_returnsNull`
- `authenticate_nullUsername_returnsNullOrThrowsClearException`
- `authenticate_nullPassword_returnsNullOrThrowsClearException`
- `checkExistedUsername_true_whenUserFound`
- `checkExistedUsername_false_whenUserMissing`
- `updateUserProfile_returnsNull_whenUserMissing`
- `updateUserProfile_keepsOldFields_whenInputBlank`
- `updateUserProfile_trimsValidFields`
- `resetPassword_returnsNull_whenUserMissing`
- `deleteUser_returnsFalse_whenUserMissing`

#### `CommandTest`

Can test command layer neu dependency duoc fake:

- `LoginCommand_success_returnsSuccessResponse`
- `LoginCommand_failure_returnsFailureResponse`
- `PlaceBidCommand_success_returnsSuccessResponse`
- `PlaceBidCommand_invalidBid_returnsFailureResponse`
- `AutoBidCommand_nullConfig_returnsFailureResponse`
- `RegisterToAuctionCommand_registersCurrentClient`

#### `ObserverTest`

- `registerToAuction_addsListenerOnlyOnce`
- `unregisterFromAuction_removesListener`
- `notifySubscribers_sendsBidToListeners`
- `notifySubscribers_doesNotNotifySender`
- `notifySubscribers_noSubscribers_doesNotThrow`

Hien `notifySubscribers` private, nen test qua `placeBid` hoac tach observer registry thanh class rieng.

### P2 - Bo sung de tang diem chat luong

#### `ItemFactoryTest`

- `vehicleCategory_returnsVehicleFactory`
- `artCategory_returnsArtFactory`
- `electronicsCategory_returnsElectronicsFactory`
- `otherCategory_returnsOtherItemFactory`
- `factoryCreatesCorrectItemSubtype`

#### `PaymentStatusTest`

- `paidAuction_remainsPaid_after24Hours`
- `unpaidAuction_becomesCancelled_after24Hours`
- `runningAuction_before24Hours_statusUnchanged`

#### `ConcurrentBiddingTest`

Chi nen viet sau khi co fake DAO/in-memory repository:

- `concurrentValidBids_finalWinnerIsHighestBid`
- `concurrentSameAmount_onlyOneWinner`
- `concurrentLowerLateBid_isRejected`
- `concurrentBids_doNotRollbackPrice`

## 4. Cau truc test nen huong toi

De test de viet va khong phu thuoc database, nen tach logic thanh cac class nho:

```text
AuctionStatusResolver
  - calculateStatus(Auction auction, LocalDateTime now)

BidValidator
  - validate(Auction auction, BidTransaction bid)

AutoBidResolver
  - resolve(Auction auction, List<AutoBidConfig> configs, LocalDateTime now)

AuctionNotifier
  - register/unregister/notify
```

Sau khi tach, `AuctionManager` chi lam viec dieu phoi:

```text
load auction -> validate -> update state -> persist -> notify -> run auto-bid
```

Cach nay giup unit test khong can MySQL/TiDB va khong can JavaFX.

## 5. Mau test nen viet ngay cho AutoBidResolver

Neu tach duoc class `AutoBidResolver`, test nen co dang:

```java
@Test
void sameMaxBid_shouldChooseEarlierUser_andUseMaxBidAsFinalAmount() {
    Auction auction = runningAuction("50", "10", 0);
    AutoBidConfig first = bot(1, "A", "100", true, LocalDateTime.of(2026, 1, 1, 10, 0));
    AutoBidConfig second = bot(2, "B", "100", true, LocalDateTime.of(2026, 1, 1, 10, 1));

    Optional<BidTransaction> result = resolver.resolve(auction, List.of(first, second), NOW);

    assertTrue(result.isPresent());
    assertEquals(1, result.get().getBidderId());
    assertEquals(new BigDecimal("100"), result.get().getAmount());
}
```

## 6. Muc tieu de lay diem test tot hon

- It nhat 1 test file cho moi nhom logic: bid validation, status, auto-bid, observer, command/user.
- Test khong goi database that.
- Coverage package `server.manager` nen tang len tren 60% that, khong chi pass do exclude.
- Test concurrency nen co it nhat 2 case dung `ExecutorService` hoac `CountDownLatch`.
- Ten test nen theo mau: `method_condition_expectedResult`.


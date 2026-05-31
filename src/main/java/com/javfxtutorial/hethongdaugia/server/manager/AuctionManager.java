package com.javfxtutorial.hethongdaugia.server.manager;

import com.javfxtutorial.hethongdaugia.common.Exception.auc.AuctionAlreadyEndedException;
import com.javfxtutorial.hethongdaugia.common.Exception.auc.AuctionCancelledException;
import com.javfxtutorial.hethongdaugia.common.Exception.auc.AuctionNotFoundException;
import com.javfxtutorial.hethongdaugia.common.Exception.auc.AuctionNotStartedException;
import com.javfxtutorial.hethongdaugia.common.Exception.bid.BidAmountExceedsLimitException;
import com.javfxtutorial.hethongdaugia.common.Exception.bid.InsufficientIncrementException;
import com.javfxtutorial.hethongdaugia.common.Exception.bid.LowerThanCurrentBidException;
import com.javfxtutorial.hethongdaugia.common.Exception.bid.SelfBidException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.DataException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.DuplicateKeyException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.QueryExecutionException;
import com.javfxtutorial.hethongdaugia.common.model.Command.AutoBidCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.UpdateAuctionCommand;
import com.javfxtutorial.hethongdaugia.common.model.domain.AntiSnipeExtender;
import com.javfxtutorial.hethongdaugia.common.model.domain.Auction;
import com.javfxtutorial.hethongdaugia.common.model.domain.AutoBidConfig;
import com.javfxtutorial.hethongdaugia.common.model.domain.BidTransaction;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;
import com.javfxtutorial.hethongdaugia.server.dao.BidDAO;
import com.javfxtutorial.hethongdaugia.server.dao.ParticipatedAuctionDAO;
import com.javfxtutorial.hethongdaugia.server.network.BidListener;
import com.javfxtutorial.hethongdaugia.server.network.ClientHandler;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

import com.javfxtutorial.hethongdaugia.server.network.ClientHandlerContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Quản lý nghiệp vụ đấu giá ở phía server.
 *
 * <p>Class này giữ cache các phiên đấu giá đang hoạt động, nhận yêu cầu đặt giá, kiểm tra tính hợp
 * lệ của bid, cập nhật database, phát thông báo realtime cho client và xử lý AutoBid.
 */
public class AuctionManager {
  private static final Logger log = LoggerFactory.getLogger(AuctionManager.class);

  // Khoảng thời gian còn lại tối đa để kích hoạt anti-snipe.
  private static final long ANTI_SNIPE_X_SECONDS = 60;
  // Số giây được cộng thêm vào thời gian kết thúc khi anti-snipe được kích hoạt.
  private static final long ANTI_SNIPE_Y_SECONDS = 60;
  // Giới hạn trần cho một lần đặt giá để tránh dữ liệu giá bất thường.
  private static final BigDecimal MAX_BID_AMOUNT = new BigDecimal("1000000000000000.00");
  // Mã thông báo gửi về client khi nhiều AutoBid có cùng mức giá tối đa.
  private static final String AUTO_BID_TIE_ALERT = "AUTO_BID_TIE_ALERT";

  // Thành phần chịu trách nhiệm gia hạn phiên nếu có bid ở sát thời điểm kết thúc.
  private final AntiSnipeExtender antiSnipeExtender =
      new AntiSnipeExtender(ANTI_SNIPE_X_SECONDS, ANTI_SNIPE_Y_SECONDS);

  // ── Singleton thread-safe ─────────────────────────
  // volatile đảm bảo các thread nhìn thấy đúng instance đã được khởi tạo.
  private static volatile AuctionManager instance;

  private AuctionManager() {}

  /**
   * Trả về instance duy nhất của AuctionManager.
   *
   * <p>Input: không có. Output: singleton dùng chung cho toàn server. Luồng xử lý chính: kiểm tra
   * instance, khóa class khi cần tạo mới để tránh nhiều thread tạo nhiều object.
   */
  public static AuctionManager getInstance() {
    // Nếu chưa có instance thì mới cần vào vùng synchronized để khởi tạo.
    if (instance == null) {
      synchronized (AuctionManager.class) {
        // Kiểm tra lần hai sau khi có lock để tránh tạo trùng trong môi trường đa luồng.
        if (instance == null) { // double-check
          instance = new AuctionManager();
        }
      }
    }
    return instance;
  }

  // ── Observer: auctionId → list listener ──────────
  // Lưu danh sách client đang theo dõi từng phiên đấu giá để phát bid realtime.
  private final Map<Integer, List<BidListener>> auctionSubscribers = new ConcurrentHashMap<>();

  // ── RAM cache ────────────────────────────────────
  // Cache auction theo auctionId để giảm số lần đọc database khi đặt giá liên tục.
  private final Map<Integer, Auction> activeAuctions = new ConcurrentHashMap<>();

  // ── AutoBid registry ─────────────────────────────
  // Lưu cấu hình AutoBid của từng auction, mỗi auction có thể có nhiều người đăng ký.
  private final Map<Integer, List<AutoBidConfig>> autoBidRegistry = new ConcurrentHashMap<>();
  // ── Lock riêng theo từng auctionId ───────────────
  // Mỗi auction có một lock riêng để các bid cùng auction được xử lý tuần tự.
  private final Map<Integer, ReentrantLock> auctionLocks = new ConcurrentHashMap<>();

  /**
   * Lấy lock tương ứng với một auction.
   *
   * <p>Input: auctionId. Output: ReentrantLock dùng để bảo vệ dữ liệu của auction đó. Nếu lock chưa
   * tồn tại thì tạo mới và lưu lại trong map.
   */
  private ReentrantLock getAuctionLock(int auctionId) {
    return auctionLocks.computeIfAbsent(auctionId, id -> new ReentrantLock());
  }

  // ─────────────────────────────────────────────────
  // SUBSCRIBE / UNSUBSCRIBE
  // ─────────────────────────────────────────────────

  /**
   * Đăng ký một listener vào phòng đấu giá.
   *
   * <p>Input: listener đại diện kết nối client và auctionId cần theo dõi. Output: không trả về. Luồng
   * xử lý: bỏ qua listener null, tạo danh sách listener nếu chưa có, sau đó thêm listener nếu chưa
   * đăng ký trước đó.
   */
  public void registerToAuction(BidListener listener, int auctionId) {
    // Không có listener hợp lệ để đăng ký thì bỏ qua để tránh lỗi null.
    if (listener == null) {
      log.warn("Không thể đăng ký auction {} vì listener null", auctionId);
      return;
    }
    // CopyOnWriteArrayList phù hợp cho danh sách được đọc nhiều và thay đổi ít.
    List<BidListener> listeners =
        auctionSubscribers.computeIfAbsent(auctionId, k -> new CopyOnWriteArrayList<>());
    // Chỉ thêm nếu listener chưa tồn tại để tránh client nhận trùng một thông báo.
    if (!listeners.contains(listener)) {
      listeners.add(listener);
    }

    log.info("Đã thêm {} vào phòng auction id: {}", listener, auctionId);
  }

  /**
   * Hủy đăng ký một listener khỏi một phòng đấu giá cụ thể.
   *
   * <p>Input: listener cần xóa và auctionId. Output: không trả về. Nếu auction chưa có danh sách
   * listener thì không làm gì.
   */
  public void unregisterFromAuction(BidListener listener, int auctionId) {
    List<BidListener> list = auctionSubscribers.get(auctionId);
    // Chỉ xóa khi auction đang có danh sách listener trong bộ nhớ.
    if (list != null) {
      list.remove(listener);
    }
    log.info("Client hủy đăng ký auction #{}", auctionId);
  }

  // ─────────────────────────────────────────────────
  // PLACE BID — logic chính
  // ─────────────────────────────────────────────────
  /**
   * Xóa một listener khỏi tất cả phòng đấu giá.
   *
   * <p>Input: listener đại diện client bị ngắt kết nối. Output: không trả về. Luồng xử lý: bỏ qua
   * listener null, sau đó duyệt toàn bộ danh sách subscriber và remove listener này.
   */
  public void unregisterListenerFromAll(BidListener listener) {
    // Client không có listener hợp lệ thì không có gì để hủy đăng ký.
    if (listener == null) {
      return;
    }
    // Duyệt mọi phòng đấu giá; mỗi vòng xóa listener khỏi danh sách của phòng đó nếu có.
    auctionSubscribers.values().forEach(list -> list.remove(listener));
  }

  /**
   * Đặt giá thủ công cho một auction.
   *
   * <p>Input: bid chứa auctionId, bidderId, số tiền và thông tin người đặt. Output: true nếu bid được
   * xử lý thành công, false nếu dữ liệu bid rỗng. Method này gọi overload chính với danh sách bid phụ
   * rỗng.
   */
  public boolean placeBid(BidTransaction bid)
      throws AuctionNotFoundException,
          AuctionNotStartedException,
          AuctionAlreadyEndedException,
          LowerThanCurrentBidException,
          SelfBidException,
          InsufficientIncrementException,
          DataException,
          BidAmountExceedsLimitException,
          AuctionCancelledException {

    return placeBid(bid, Collections.emptyList());
  }

  /**
   * Xử lý một lần đặt giá và các bid lịch sử phát sinh từ AutoBid nếu có.
   *
   * <p>Input: bid chính cần chấp nhận, pendingHistoryBids là các bid phụ chỉ dùng để lưu/hiển thị
   * lịch sử AutoBid. Output: true nếu lưu và thông báo thành công, false nếu bid hoặc amount null.
   * Luồng xử lý chính: khóa theo auction, nạp auction từ cache/database, kiểm tra trạng thái và giá,
   * cập nhật auction, lưu database, phát thông báo realtime rồi kích hoạt AutoBid tiếp theo.
   */
  public boolean placeBid(
      BidTransaction bid, List<BidTransaction> pendingHistoryBids)
      throws AuctionNotFoundException,
          AuctionNotStartedException,
          AuctionAlreadyEndedException,
          LowerThanCurrentBidException,
          SelfBidException,
          InsufficientIncrementException,
          DataException,
          BidAmountExceedsLimitException,
          AuctionCancelledException {

    // Bid thiếu object hoặc thiếu số tiền thì không đủ dữ liệu để xử lý.
    if (bid == null || bid.getAmount() == null) {
      return false;
    }
    // Khóa theo auctionId để hai người không thể cập nhật cùng một auction cùng lúc.
    ReentrantLock lock = getAuctionLock(bid.getAuctionId());
    lock.lock();
    // Bid đã được chấp nhận, dùng sau khi unlock để phát thông báo ngoài vùng khóa.
    BidTransaction acceptedBid;
    // Các bid phụ của AutoBid được lưu thành công, dùng để cập nhật lịch sử cho client.
    List<BidTransaction> acceptedHistoryBids;
    // Thời gian kết thúc mới sau khi áp dụng anti-snipe, có thể bằng thời gian cũ.
    LocalDateTime endTimeNew;
    // Auction đang được xử lý; khai báo ngoài try để dùng cho broadcast sau khi unlock.
    Auction auction;
    try {
      // 1. Lấy auction từ RAM
      auction = activeAuctions.get(bid.getAuctionId());

      // 2. Nếu RAM trống → nạp từ DB
      if (auction == null) {
        auction = AuctionDAO.getInstance().selectById(bid.getAuctionId());
        // Không tìm thấy auction trong database thì báo lỗi nghiệp vụ cho caller.
        if (auction == null) {
          throw new AuctionNotFoundException(bid.getAuctionId());
        } // auction không tồn tại
        // Cache lại auction vừa đọc để các lần đặt giá tiếp theo không phải đọc DB ngay.
        activeAuctions.put(auction.getAuctionId(), auction);
      }
      log.info("Đã lấy xong auction từ bidAuctionId");

      // Auction bị admin hủy không được nhận thêm bid.
      if (auction.getStatus() == AuctionStatus.CANCELLED_BY_ADMIN) {
        throw new AuctionCancelledException(auction.getAuctionId());
      }
      // Người bán không được đặt giá sản phẩm của chính mình.
      if (bid.getBidderId() == auction.getSellerId()) {
        log.warn("Người bán {} cố gắng đặt giá sản phẩm của chính mình", bid.getBidderId());
        throw new SelfBidException();
      }

      // 3. Kiểm tra hợp lệ
      AuctionStatus status = refreshAuctionStatus(auction);
      // Auction chưa tới thời gian bắt đầu nên chưa cho phép đặt giá.
      if (status == AuctionStatus.NOT_START) {
        throw new AuctionNotStartedException(bid.getAuctionId());
      }

      // Chỉ trạng thái RUNNING mới được đặt giá; các trạng thái khác xem như đã kết thúc.
      if (status != AuctionStatus.RUNNING) {
        throw new AuctionAlreadyEndedException(bid.getAuctionId());
      }

      // Giá tối thiểu phải lớn hơn giá hiện tại ít nhất một bước giá.
      BigDecimal minRequired = auction.getCurrentPrice().add(auction.getStepPrice());
      // Nếu số tiền không vượt giá hiện tại thì bid bị từ chối.
      if (bid.getAmount().compareTo(auction.getCurrentPrice()) <= 0) {
        throw new LowerThanCurrentBidException(
            auction.getCurrentPrice().doubleValue(), bid.getAmount().doubleValue());
      }
      // Nếu có tăng nhưng chưa đủ stepPrice thì vẫn không hợp lệ.
      if (bid.getAmount().compareTo(minRequired) < 0) {
        throw new InsufficientIncrementException(
            auction.getStepPrice().doubleValue(),
            bid.getAmount().subtract(auction.getCurrentPrice()).doubleValue());
      }
      // Chặn giá quá lớn so với giới hạn hệ thống.
      if (bid.getAmount().compareTo(MAX_BID_AMOUNT) > 0) {
        throw new BidAmountExceedsLimitException(
            MAX_BID_AMOUNT.doubleValue(), bid.getAmount().doubleValue());
      }

      log.info("Bid hợp lệ");

      // 4. Cập nhật auction trong RAM
      auction.setCurrentPrice(bid.getAmount());
      auction.setWinningPrice(bid.getAmount());
      auction.setWinnerName(bid.getBidderName());
      auction.setWinnerEmail(bid.getBidderEmail());
      auction.setWinnerSdt(bid.getBidderSdt());

      log.info("Đã cập nhật lại auction");

      // Logic gia hạn phiên đấu giá
      endTimeNew = antiSnipeExtender.applyIfNeeded(auction);

      // Lưu trạng thái auction mới, lịch sử bid phụ và bid chính xuống database.
      AuctionDAO.getInstance().update(auction);
      acceptedHistoryBids = persistPendingHistoryBids(pendingHistoryBids);
      persistBidTransaction(bid);

      log.info("Đã lưu vào database");
      acceptedBid = bid;
      acceptedBid.setNewEndingTime(endTimeNew);

    } finally {
      // Luôn mở khóa kể cả khi kiểm tra/lưu database ném exception.
      lock.unlock();
    }

    // 6. Thông báo cho tất cả subscriber của auction này
    // Duyệt các bid phụ đã lưu; mỗi vòng gửi một bid lịch sử AutoBid tới client đang xem auction.
    for (BidTransaction historyBid : acceptedHistoryBids) {
      notifySubscribers(historyBid.getAuctionId(), historyBid);
    }
    // Gửi bid chính đã được chấp nhận cho các client đang theo dõi phiên đấu giá.
    notifySubscribers(acceptedBid.getAuctionId(), acceptedBid);
    // Broadcast toàn bộ auction mới để các màn hình danh sách cũng cập nhật giá/trạng thái.
    ClientHandler.broadcast(
        new Response(true, "giá thay đổi", auction, new UpdateAuctionCommand(auction)));

    // 7. Kích hoạt AutoBid nếu có
    checkAndExecuteAutoBids(activeAuctions.get(acceptedBid.getAuctionId()));
    return true;
  }

  // ─────────────────────────────────────────────────
  // NOTIFY — thông báo cho tất cả subscriber

  /**
   * Gửi một bid tới tất cả listener đang theo dõi auction.
   *
   * <p>Input: auctionId và bid cần gửi. Output: không trả về. Nếu auction không có subscriber thì
   * thoát sớm.
   */
  private void notifySubscribers(int auctionId, BidTransaction bid) {
    List<BidListener> list = auctionSubscribers.get(auctionId);
    // Không có client theo dõi thì không cần gửi thông báo realtime.
    if (list == null || list.isEmpty()) return;
    // Duyệt từng listener và gọi callback onPlaceBid để đẩy bid về client.
    list.forEach(listener -> listener.onPlaceBid(bid));
  }

  // ─────────────────────────────────────────────────
  // CHECK VALID BID
  // ─────────────────────────────────────────────────
  /**
   * Kiểm tra nhanh một số tiền có đủ điều kiện vượt giá hiện tại theo bước giá hay không.
   *
   * <p>Input: auction và amount cần kiểm tra. Output: true nếu amount >= currentPrice + stepPrice,
   * false nếu thiếu dữ liệu hoặc chưa đủ giá.
   */
  public boolean checkValidBid(Auction auction, BigDecimal amount) {
    // Thiếu auction hoặc số tiền thì không thể xác định bid hợp lệ.
    if (auction == null || amount == null) {
      return false;
    }
    return amount.compareTo(auction.getCurrentPrice().add(auction.getStepPrice())) >= 0;
  }

  // AUCTION STATUS

  /**
   * Làm mới trạng thái auction dựa trên thời gian hiện tại và trạng thái thanh toán.
   *
   * <p>Input: auction cần kiểm tra. Output: trạng thái mới nhất của auction. Luồng xử lý: giữ nguyên
   * các trạng thái hủy/đã thanh toán, so sánh thời gian bắt đầu/kết thúc, kiểm tra quá hạn thanh toán
   * rồi cập nhật database nếu trạng thái thay đổi.
   */
  public AuctionStatus refreshAuctionStatus(Auction auction) throws DataException {
    // Lưu trạng thái ban đầu để biết có cần ghi lại vào database hay không.
    AuctionStatus previousStatus = auction.getStatus();
    // Thời điểm hiện tại dùng để so sánh với thời gian bắt đầu/kết thúc auction.
    LocalDateTime now = LocalDateTime.now();

    // Auction đã bị hủy thì giữ nguyên, không tự chuyển sang trạng thái khác theo thời gian.
    if (previousStatus == AuctionStatus.CANCELLED
        || previousStatus == AuctionStatus.CANCELLED_BY_ADMIN) {
      return previousStatus;
    }
    // Chưa tới giờ bắt đầu thì auction ở trạng thái NOT_START.
    if (now.isBefore(auction.getStartingTime())) {
      auction.setStatus(AuctionStatus.NOT_START);
    // Sau khi không còn ở giai đoạn chưa bắt đầu, auction đã thanh toán sẽ giữ nguyên PAID.
    } else if (previousStatus == AuctionStatus.PAID) {
      return previousStatus;
    // Sau 24 giờ kể từ lúc kết thúc cần kiểm tra người thắng đã thanh toán chưa.
    } else if (now.isAfter(auction.getEndingTime().plusHours(24))) {
      auction.setStatus(checkPaymentStatus(auction));
    // Đã qua giờ kết thúc nhưng chưa quá hạn thanh toán thì đóng auction.
    } else if (now.isAfter(auction.getEndingTime())) {
      auction.setStatus(AuctionStatus.CLOSED);
    // Các trường hợp còn lại là đang trong thời gian đấu giá.
    } else {
      auction.setStatus(AuctionStatus.RUNNING);
    }
    // auctionId bằng 0 thường là object tạm trong test, không ghi database.
    if (auction.getAuctionId() == 0) {
      return auction.getStatus();
    }
    // Chỉ update DB khi trạng thái thực sự thay đổi để tránh ghi thừa.
    if (previousStatus != auction.getStatus()) {
      AuctionDAO.getInstance().update(auction);
    }
    return auction.getStatus();
  }

  /**
   * Đăng ký hoặc cập nhật cấu hình AutoBid cho một user trong một auction.
   *
   * <p>Input: config chứa auctionId, userId, maxPrice và trạng thái active. Output: true nếu xử lý
   * xong, false nếu config null. Luồng xử lý: ghi thời điểm đăng ký, thay cấu hình cũ của cùng user,
   * nạp auction nếu cần và chạy AutoBid ngay nếu cấu hình đang active.
   */
  public synchronized boolean registerAutoBid(AutoBidConfig config)
      throws DataException,
          LowerThanCurrentBidException,
          SelfBidException,
          InsufficientIncrementException,
          AuctionNotFoundException,
          AuctionNotStartedException,
          AuctionAlreadyEndedException,
          BidAmountExceedsLimitException,
          AuctionCancelledException {
    // Không có cấu hình thì không có gì để đăng ký.
    if (config == null) {
      return false;
    }
    // Thời điểm đăng ký dùng để phân định ưu tiên khi nhiều AutoBid có cùng maxPrice.
    config.setRegisteredAt(LocalDateTime.now());
    // Lấy danh sách AutoBid của auction; nếu chưa có thì tạo danh sách đồng bộ.
    List<AutoBidConfig> configs =
        autoBidRegistry.computeIfAbsent(
            config.getAuctionId(), k -> Collections.synchronizedList(new ArrayList<>()));

    // Khóa riêng danh sách AutoBid của auction để tránh cập nhật trùng user cùng lúc.
    synchronized (configs) {
      // Một user chỉ có một cấu hình AutoBid mới nhất trong cùng auction.
      configs.removeIf(c -> c.getUserId() == config.getUserId());

      // Chỉ thêm vào registry khi AutoBid đang bật; config inactive có tác dụng xóa config cũ.
      if (config.isActive()) {
        configs.add(config);

        // Ưu tiên lấy auction từ cache, nếu chưa có thì đọc database.
        Auction current = activeAuctions.get(config.getAuctionId());
        if (current == null) {
          current = AuctionDAO.getInstance().selectById(config.getAuctionId());
          // Chỉ cache khi database trả về auction hợp lệ.
          if (current != null) {
            activeAuctions.put(current.getAuctionId(), current);
          }
        }

        // Sau khi đăng ký, chạy AutoBid ngay để phản ứng với giá hiện tại nếu đủ điều kiện.
        if (current != null) {
          checkAndExecuteAutoBids(current);
        }
      }
    }
    return true;
  }

  /**
   * Tìm AutoBid đủ điều kiện và tự động tạo bid chiến thắng tiếp theo.
   *
   * <p>Input: auction hiện tại. Output: không trả về, nhưng có thể gọi lại placeBid để lưu bid mới.
   * Luồng xử lý: lọc bot còn active và đủ maxPrice, sắp xếp theo maxPrice/quyền ưu tiên, tính số tiền
   * thắng hợp lệ rồi tạo BidTransaction tự động.
   */
  private void checkAndExecuteAutoBids(Auction auction)
      throws LowerThanCurrentBidException,
          DataException,
          SelfBidException,
          InsufficientIncrementException,
          AuctionNotFoundException,
          AuctionNotStartedException,
          AuctionAlreadyEndedException,
          BidAmountExceedsLimitException,
          AuctionCancelledException {
    List<AutoBidConfig> configs = autoBidRegistry.get(auction.getAuctionId());
    // Không có AutoBid nào trong auction thì dừng ngay.
    if (configs == null || configs.isEmpty()) return;

    BigDecimal step = auction.getStepPrice(); // Bước giá tối thiểu của auction.
    // Giá tối thiểu để một bid mới vượt qua giá hiện tại.
    BigDecimal minRequired = auction.getCurrentPrice().add(step);

    // Danh sách bot còn bật và có maxPrice đủ để đặt ít nhất minRequired.
    List<AutoBidConfig> eligibleBots = new ArrayList<>();
    // Duyệt tất cả AutoBid đã đăng ký; mỗi vòng kiểm tra một config có đủ điều kiện tham gia không.
    for (AutoBidConfig c : configs) {
      if (c.isActive() && c.getMaxPrice().compareTo(minRequired) >= 0) {
        eligibleBots.add(c);
      }
    }
    // Không có bot đủ giá tối thiểu thì không tạo bid tự động.
    if (eligibleBots.isEmpty()) return;

    // Sắp xếp để bot thắng đứng đầu: maxPrice cao hơn, đang dẫn đầu, rồi đăng ký sớm hơn.
    eligibleBots.sort(
        (b1, b2) -> {
          // Ưu tiên maxPrice cao hơn vì có khả năng trả giá cao hơn.
          int cmp = b2.getMaxPrice().compareTo(b1.getMaxPrice());
          if (cmp != 0) return cmp;

          // Nếu bằng maxPrice, người đang dẫn đầu được ưu tiên giữ vị trí.
          boolean b1IsCurrentWinner =
              b1.getUserName() != null && b1.getUserName().equals(auction.getWinnerName());
          boolean b2IsCurrentWinner =
              b2.getUserName() != null && b2.getUserName().equals(auction.getWinnerName());
          if (b1IsCurrentWinner != b2IsCurrentWinner) {
            return b1IsCurrentWinner ? -1 : 1;
          }

          // Nếu vẫn bằng nhau, người đăng ký AutoBid sớm hơn được ưu tiên.
          return b1.getRegisteredAt().compareTo(b2.getRegisteredAt());
        });

    // Bot đầu danh sách sau khi sắp xếp là bot được quyền đặt bid tiếp theo.
    AutoBidConfig winnerBot = eligibleBots.get(0);

    // Luật tính giá AutoBid:
    // - Nếu max cao nhất > max cao thứ hai: giá thắng = min(max cao thứ hai + step, max cao nhất).
    // - Nếu nhiều bot cùng max: ưu tiên người đang dẫn đầu, nếu vẫn hòa thì ưu tiên đăng ký sớm hơn;
    //   giá thắng bằng maxBid đang hòa.

    BigDecimal finalAmount;
    // Bid phụ ghi lại mức max của bot đứng thứ hai, dùng để hiển thị lịch sử AutoBid rõ hơn.
    BidTransaction pendingHistoryBid = null;

    // Chỉ có một bot đủ điều kiện thì bot đặt mức tối thiểu cần thiết.
    if (eligibleBots.size() == 1) {
      // Nếu bot đó đã là người thắng hiện tại thì không cần tự đặt thêm bid.
      if (winnerBot.getUserName().equals(auction.getWinnerName())) return;
      finalAmount = minRequired;
    } else {
      // Có từ hai bot, bot thứ hai dùng để tính mức giá bot thắng cần vượt qua.
      AutoBidConfig secondBot = eligibleBots.get(1);
      BigDecimal secondMax = secondBot.getMaxPrice();

      // Nếu các bot có maxPrice bằng nhau, dùng luật ưu tiên đã sắp xếp ở trên.
      if (winnerBot.getMaxPrice().compareTo(secondMax) == 0) {
        // Các bot cùng maxBid: người được ưu tiên thắng ở đúng maxBid đó.
        notifySameMaxAutoBidUsers(eligibleBots, winnerBot, winnerBot.getMaxPrice(), auction);
        finalAmount = winnerBot.getMaxPrice();
      } else {
        // Bot thắng chỉ cần hơn bot thứ hai một bước giá,
        // nhưng không được vượt maxBid của chính nó
        finalAmount = secondMax.add(step);

        // Trường hợp secondMax + step vượt max của bot thắng thì chỉ đặt tới max của bot thắng.
        if (finalAmount.compareTo(winnerBot.getMaxPrice()) > 0) {
          finalAmount = winnerBot.getMaxPrice();
        }
      }

      // Nếu giá tính ra vẫn thấp hơn mức tối thiểu hiện tại thì không đặt bid.
      if (finalAmount.compareTo(minRequired) < 0) {
        return;
      }

      // Khi maxPrice khác nhau, lưu thêm bid lịch sử của bot đứng thứ hai ở mức max của nó.
      if (winnerBot.getMaxPrice().compareTo(secondMax) != 0) {
        pendingHistoryBid = createAutoBidHistoryBid(auction, secondBot, secondMax);
      }
    }

    // Tạo bid tự động đại diện cho bot thắng, sau đó chuyển qua luồng placeBid chuẩn.
    BidTransaction autoBid = new BidTransaction();
    autoBid.setBidderId(winnerBot.getUserId());
    autoBid.setAuctionId(auction.getAuctionId());
    autoBid.setAmount(finalAmount);
    autoBid.setTimestamp(LocalDateTime.now());
    autoBid.setBidderName(winnerBot.getUserName());

    // Gọi lại placeBid để bid tự động đi qua cùng luồng kiểm tra, lưu DB và thông báo như bid thường.
    this.placeBid(
        autoBid,
        pendingHistoryBid == null ? Collections.emptyList() : List.of(pendingHistoryBid));
  }

  /**
   * Lưu một bid vào lịch sử bid và danh sách auction đã tham gia của user.
   *
   * <p>Input: bid cần lưu. Output: không trả về. Nếu user đã có bản ghi tham gia auction thì bỏ qua
   * lỗi trùng khóa, vì lịch sử bid chính vẫn đã được lưu.
   */
  private void persistBidTransaction(BidTransaction bid) throws DataException {
    // Lưu từng lần đặt giá vào bảng lịch sử bid.
    BidDAO.getInstance().insertBid(bid);
    try {
      // Lưu quan hệ user đã tham gia auction để phục vụ màn hình lịch sử tham gia.
      ParticipatedAuctionDAO.getInstance().insert(bid);
    } catch (DuplicateKeyException e) {
      // User có thể đặt nhiều bid trong cùng auction; bản ghi tham gia chỉ cần tồn tại một lần.
      log.info("User {} đã tham gia auction {} rồi, bỏ qua", bid.getBidderId(), bid.getAuctionId());
    }
  }

  /**
   * Lưu các bid phụ phát sinh trong quá trình AutoBid.
   *
   * <p>Input: danh sách pendingHistoryBids. Output: danh sách các bid phụ đã lưu thành công. Luồng
   * xử lý: bỏ qua danh sách rỗng, duyệt từng bid và chỉ giữ các bid lưu DB thành công.
   */
  private List<BidTransaction> persistPendingHistoryBids(List<BidTransaction> pendingHistoryBids) {
    // Không có bid phụ thì trả về danh sách rỗng để caller vẫn duyệt an toàn.
    if (pendingHistoryBids == null || pendingHistoryBids.isEmpty()) {
      return Collections.emptyList();
    }

    // Chỉ danh sách này mới được gửi thông báo, vì các bid trong đó đã lưu DB thành công.
    List<BidTransaction> persistedHistoryBids = new ArrayList<>();
    // Duyệt từng bid phụ AutoBid; mỗi vòng thử lưu một bid và ghi nhận nếu thành công.
    for (BidTransaction historyBid : pendingHistoryBids) {
      try {
        persistBidTransaction(historyBid);
        persistedHistoryBids.add(historyBid);
      } catch (DataException e) {
        // Bid phụ lỗi DB không làm hỏng bid chính, chỉ ghi log để điều tra sau.
        log.warn(
            "Khong luu duoc bid phu AutoBid auctionId={}, userId={}",
            historyBid.getAuctionId(),
            historyBid.getBidderId(),
            e);
      }
    }
    return persistedHistoryBids;
  }

  /**
   * Tạo bid lịch sử cho AutoBid bị vượt qua.
   *
   * <p>Input: auction, config của bot và amount cần ghi nhận. Output: BidTransaction mới chỉ chứa dữ
   * liệu cần lưu lịch sử, chưa lưu database.
   */
  private BidTransaction createAutoBidHistoryBid(
      Auction auction, AutoBidConfig config, BigDecimal amount) {
    // Bid này không phải bid thắng cuối, mà là mốc giá của bot thua để lịch sử đầy đủ hơn.
    BidTransaction historyBid = new BidTransaction();
    historyBid.setBidderId(config.getUserId());
    historyBid.setBidderName(config.getUserName());
    historyBid.setAuctionId(auction.getAuctionId());
    historyBid.setAmount(amount);
    historyBid.setTimestamp(LocalDateTime.now());
    return historyBid;
  }

  /**
   * Thông báo cho các user có AutoBid cùng maxPrice.
   *
   * <p>Input: danh sách bot đủ điều kiện, bot thắng, mức maxPrice bị hòa và auction hiện tại. Output:
   * không trả về. Luồng xử lý: lọc các bot cùng giá, xác định lý do ưu tiên và gửi thông báo riêng
   * đến từng user.
   */
  private void notifySameMaxAutoBidUsers(
      List<AutoBidConfig> eligibleBots,
      AutoBidConfig winnerBot,
      BigDecimal tiedMaxPrice,
      Auction auction) {
    // Chỉ lấy các bot thật sự có maxPrice bằng mức đang xét.
    List<AutoBidConfig> tiedBots =
        eligibleBots.stream()
            .filter(bot -> bot.getMaxPrice().compareTo(tiedMaxPrice) == 0)
            .toList();
    // Ít hơn hai bot thì không có tình huống hòa giá để thông báo.
    if (tiedBots.size() < 2) {
      return;
    }

    // Chuẩn bị nội dung hiển thị cho người dùng về mức giá hòa và lý do ưu tiên.
    String formattedPrice = String.format("%,.0f VND", tiedMaxPrice);
    String reason = tieBreakReason(winnerBot, auction);
    // Duyệt từng bot bị hòa; mỗi vòng gửi thông báo phù hợp cho user của bot đó.
    for (AutoBidConfig bot : tiedBots) {
      // User của winnerBot nhận thông báo rằng họ được ưu tiên, các user khác nhận lý do thua.
      boolean winner = bot.getUserId() == winnerBot.getUserId();
      String message =
          winner
              ? "AutoBid của bạn bằng với người khác ở mức "
                  + formattedPrice
                  + ". Bạn được ưu tiên vì "
                  + reason
                  + "."
              : "AutoBid của bạn bằng với người khác ở mức "
                  + formattedPrice
                  + ". Hệ thống ưu tiên người "
                  + reason
                  + ".";
      ClientHandler.broadcastToUserId(
          new Response(true, AUTO_BID_TIE_ALERT, message, new AutoBidCommand()), bot.getUserId());
    }
  }

  /**
   * Xác định lý do một AutoBid được ưu tiên khi hòa maxPrice.
   *
   * <p>Input: bot thắng và auction hiện tại. Output: chuỗi lý do để hiển thị cho user.
   */
  private String tieBreakReason(AutoBidConfig winnerBot, Auction auction) {
    // Nếu bot thắng đang là người dẫn đầu, hệ thống ưu tiên giữ người dẫn đầu hiện tại.
    if (winnerBot.getUserName() != null
        && winnerBot.getUserName().equals(auction.getWinnerName())) {
      return "đang dẫn đầu";
    }
    // Nếu không phải người đang dẫn đầu, ưu tiên còn lại là thời điểm đăng ký sớm hơn.
    return "đăng ký AutoBid sớm hơn";
  }

  /**
   * Lấy danh sách auction mà một bidder đã tham gia.
   *
   * <p>Input: userId của bidder. Output: danh sách Auction lấy từ ParticipatedAuctionDAO.
   */
  public List<Auction> getParticipatedAuctionsByBidder(int userId) throws QueryExecutionException {
    // Danh sách kết quả được ép kiểu theo DAO hiện tại để giữ nguyên contract cũ.
    ArrayList<Auction> auctionList = new ArrayList<>();
    auctionList =
        (ArrayList<Auction>)
            ParticipatedAuctionDAO.getInstance().getParticipatedAuctionsByBidder(userId);
    return auctionList;
  }

  /**
   * Kiểm tra trạng thái thanh toán sau khi auction kết thúc.
   *
   * <p>Input: auction cần kiểm tra. Output: trạng thái sau khi xét thanh toán. Nếu không có người
   * thắng thì đóng auction; nếu quá 24 giờ mà chưa PAID thì hủy auction.
   */
  public AuctionStatus checkPaymentStatus(Auction auction) throws DataException {
    // Không có người thắng nghĩa là phiên kết thúc mà không ai đặt giá hợp lệ.
    if (auction.getWinnerName() == null || auction.getWinnerName().isBlank()) {
      auction.setStatus(AuctionStatus.CLOSED);
    // Có người thắng nhưng đã quá hạn 24 giờ sau khi kết thúc thì kiểm tra thanh toán.
    } else if (LocalDateTime.now().isAfter(auction.getEndingTime().plusHours(24))) {
      // Chưa thanh toán đúng hạn thì chuyển sang CANCELLED.
      if (auction.getStatus() != AuctionStatus.PAID) {
        auction.setStatus(AuctionStatus.CANCELLED);
      }
    }
    return auction.getStatus();
  }

  /**
   * Cập nhật trạng thái auction trong cache RAM.
   *
   * <p>Input: auctionId và status mới. Output: không trả về. Method này chỉ cập nhật cache nếu
   * auction đang tồn tại trong activeAuctions, không tự ghi database.
   */
  public void updateAuctionStatus(int auctionId, AuctionStatus status) {
    Auction auction = activeAuctions.get(auctionId);
    // Chỉ cập nhật khi auction đang được cache trong RAM.
    if (auction != null) {
      auction.setStatus(status);
    }
  }
}

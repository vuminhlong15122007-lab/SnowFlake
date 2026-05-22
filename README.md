# SnowFox - Hệ Thống Đấu Giá Trực Tuyến

<p align="center">
  <img src="./src/main/resources/com/javfxtutorial/hethongdaugia/assets/Logo.png" alt="SnowFox Logo" width="150">
</p>

<p align="center">
  <b>Bài tập lớn Lập trình nâng cao: xây dựng hệ thống đấu giá trực tuyến bằng JavaFX, Java Socket Server và MySQL/TiDB.</b>
</p>

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-25-f97316?style=for-the-badge">
  <img alt="JavaFX" src="https://img.shields.io/badge/JavaFX-25.0.3-2563eb?style=for-the-badge">
  <img alt="Maven" src="https://img.shields.io/badge/Maven-3.9.11-7c3aed?style=for-the-badge">
  <img alt="Database" src="https://img.shields.io/badge/MySQL%2FTiDB-HikariCP-16a34a?style=for-the-badge">
  <img alt="Realtime" src="https://img.shields.io/badge/Realtime_Bidding-Socket-ef4444?style=for-the-badge">
</p>

<p align="center">
  <img alt="AutoBid" src="https://img.shields.io/badge/Auto--Bid-enabled-8b5cf6?style=flat-square">
  <img alt="Anti Snipe" src="https://img.shields.io/badge/Anti--Sniping-60s_%2B_60s-f59e0b?style=flat-square">
  <img alt="Tests" src="https://img.shields.io/badge/Tests-75_pass-10b981?style=flat-square">
  <img alt="Style" src="https://img.shields.io/badge/Style-Checkstyle_%2B_Spotless-111827?style=flat-square">
</p>

## Mục Lục

- [1. Giới Thiệu Bài Tập Lớn](#1-giới-thiệu-bài-tập-lớn)
- [2. Mô Tả Hệ Thống](#2-mô-tả-hệ-thống)
- [3. Các Yêu Cầu Cụ Thể](#3-các-yêu-cầu-cụ-thể)
- [4. Đối Chiếu Thang Điểm](#4-đối-chiếu-thang-điểm)

---

## 1. Giới Thiệu Bài Tập Lớn

SnowFox là ứng dụng desktop cho phép người dùng tạo phiên đấu giá, tham gia đặt giá realtime, sử dụng Auto-Bid và theo dõi kết quả phiên đấu giá. Dự án được tổ chức theo mô hình **Client-Server**, trong đó client JavaFX xử lý giao diện, còn server xử lý nghiệp vụ và truy cập database.

<p align="center">
  <img alt="Client" src="https://img.shields.io/badge/CLIENT-JavaFX_UI-2563eb?style=for-the-badge">
  <img alt="Common" src="https://img.shields.io/badge/COMMON-Command_Model-7c3aed?style=for-the-badge">
  <img alt="Server" src="https://img.shields.io/badge/SERVER-Business_Logic-ef4444?style=for-the-badge">
  <img alt="Data" src="https://img.shields.io/badge/DATA-MySQL_TiDB-16a34a?style=for-the-badge">
</p>

| Nội dung | Mô tả |
| --- | --- |
| Bài toán | Phát triển hệ thống đấu giá trực tuyến cho nhiều người dùng. |
| Mục tiêu | Áp dụng OOP, JavaFX, Socket, xử lý đồng thời, database, unit test và design pattern. |
| Đối tượng sử dụng | `USER` và `ADMIN`. Trong project, `USER` đảm nhiệm cả bidder và seller. |
| Trạng thái | Đã hoàn thành chức năng bắt buộc và một số chức năng nâng cao theo đề bài. |

---

## 2. Mô Tả Hệ Thống

Hệ thống cho phép người bán đăng sản phẩm và mở phiên đấu giá trong một khoảng thời gian nhất định. Người mua đặt giá cao hơn giá hiện tại để cạnh tranh. Server kiểm tra tính hợp lệ của bid, cập nhật người dẫn đầu, lưu lịch sử và thông báo realtime cho các client đang theo dõi cùng phiên.

### 2.1 Phạm vi hệ thống

| Nhóm | Phạm vi |
| --- | --- |
| Người dùng thường | Đăng ký, đăng nhập, cập nhật hồ sơ, tạo/sửa/xóa auction của mình, tham gia đấu giá, đặt giá, Auto-Bid, thanh toán mô phỏng. |
| Quản trị viên | Quản lý user, thêm/xóa tài khoản, reset mật khẩu, cập nhật thông tin admin, quản lý auction. |
| Server | Nhận command qua socket, xử lý nghiệp vụ, đồng bộ bid, broadcast realtime, lưu dữ liệu. |
| Database | Lưu user, item, auction, bid transaction, phiên đã tham gia và notification. |
| Ngoài phạm vi | Web/mobile app, cổng thanh toán thật, vận chuyển hàng hóa, triển khai production. |

### 2.2 Module chính

| Module | Vai trò |
| --- | --- |
| `client` | JavaFX app, controller, model trạng thái client, network manager. |
| `common` | Model, command, response, enum và exception dùng chung. |
| `server` | Socket server, client handler, manager nghiệp vụ, DAO, bảo mật mật khẩu. |
| `resources` | FXML, CSS, ảnh và tài nguyên giao diện. |
| `test` | Unit test cho manager, command, model, exception và password hashing. |

### 2.3 Danh sách chức năng đã hoàn thành

| Chức năng | Trạng thái |
| --- | --- |
| Đăng ký, đăng nhập, phân quyền `USER`/`ADMIN` | ![Done](https://img.shields.io/badge/Ho%C3%A0n_th%C3%A0nh-10b981?style=flat-square) |
| Cập nhật hồ sơ, reset mật khẩu, hash password bằng PBKDF2 | ![Done](https://img.shields.io/badge/Ho%C3%A0n_th%C3%A0nh-10b981?style=flat-square) |
| Admin quản lý user và auction | ![Done](https://img.shields.io/badge/Ho%C3%A0n_th%C3%A0nh-10b981?style=flat-square) |
| Seller tạo, sửa, xóa auction khi phiên chưa bắt đầu | ![Done](https://img.shields.io/badge/Ho%C3%A0n_th%C3%A0nh-10b981?style=flat-square) |
| Hiển thị danh sách, chi tiết, tìm kiếm/lọc auction | ![Done](https://img.shields.io/badge/Ho%C3%A0n_th%C3%A0nh-10b981?style=flat-square) |
| Đặt giá realtime và lưu lịch sử bid | ![Done](https://img.shields.io/badge/Ho%C3%A0n_th%C3%A0nh-10b981?style=flat-square) |
| Tự động cập nhật trạng thái auction | ![Done](https://img.shields.io/badge/Ho%C3%A0n_th%C3%A0nh-10b981?style=flat-square) |
| Thanh toán mô phỏng, trạng thái `PAID`/`CANCELLED` | ![Done](https://img.shields.io/badge/Ho%C3%A0n_th%C3%A0nh-10b981?style=flat-square) |
| Notification cho seller | ![Done](https://img.shields.io/badge/Ho%C3%A0n_th%C3%A0nh-10b981?style=flat-square) |
| Auto-Bid | ![Done](https://img.shields.io/badge/Ho%C3%A0n_th%C3%A0nh-8b5cf6?style=flat-square) |
| Chống bid phút cuối Anti-Sniping | ![Done](https://img.shields.io/badge/Ho%C3%A0n_th%C3%A0nh-f59e0b?style=flat-square) |
| LineChart lịch sử giá đấu realtime | ![Done](https://img.shields.io/badge/Ho%C3%A0n_th%C3%A0nh-0ea5e9?style=flat-square) |
| Unit test, coverage, CI, Checkstyle, Spotless | ![Done](https://img.shields.io/badge/Ho%C3%A0n_th%C3%A0nh-111827?style=flat-square) |

---

## 3. Các Yêu Cầu Cụ Thể

### 3.1 Chức Năng Bắt Buộc

<p align="center">
  <img alt="User" src="https://img.shields.io/badge/User_Management-2563eb?style=for-the-badge">
  <img alt="Auction" src="https://img.shields.io/badge/Auction_Core-ef4444?style=for-the-badge">
  <img alt="Bid" src="https://img.shields.io/badge/Realtime_Bid-f59e0b?style=for-the-badge">
  <img alt="GUI" src="https://img.shields.io/badge/JavaFX_GUI-16a34a?style=for-the-badge">
</p>

#### 3.1.1 Quản lý người dùng

| Yêu cầu | Hiện thực |
| --- | --- |
| Đăng ký / đăng nhập | `RegisterController`, `LoginController`, `LoginCommand`, `UserManager`. |
| Vai trò Bidder | `USER` tham gia auction, đặt giá, xem lịch sử bid. |
| Vai trò Seller | `USER` tạo/sửa/xóa auction của chính mình. |
| Vai trò Admin | `ADMIN` quản lý user và auction. |
| Bảo mật mật khẩu | `PasswordHasher` dùng PBKDF2 + salt. |

#### 3.1.2 Quản lý sản phẩm đấu giá

| Yêu cầu | Hiện thực |
| --- | --- |
| Thêm / sửa / xóa sản phẩm | `AddAuctionCommand`, `UpdateAuctionCommand`, `DeleteAuctionCommand`. |
| Thông tin sản phẩm | `Item`, `Electronics`, `Art`, `Vehicle`, `Auction`. |
| Giá khởi điểm, giá hiện tại | `startingPrice`, `currentPrice`, `winningPrice`. |
| Thời gian bắt đầu/kết thúc | `startingTime`, `endingTime`, `AuctionStatus`. |

#### 3.1.3 Tham gia đấu giá

| Yêu cầu | Hiện thực |
| --- | --- |
| Đặt giá cao hơn giá hiện tại | Server kiểm tra trong `AuctionManager.placeBid()`. |
| Kiểm tra bid hợp lệ | Kiểm tra trạng thái phiên, bước giá, giới hạn giá và seller tự bid. |
| Cập nhật người dẫn đầu | Cập nhật `winnerId`, `winnerName`, `winningPrice`. |
| Theo dõi realtime | `BidListener`, `ClientHandler`, `LiveAuctionController`. |

#### 3.1.4 Kết thúc phiên đấu giá

| Yêu cầu | Hiện thực |
| --- | --- |
| Tự động đóng phiên khi hết thời gian | `AuctionManager.refreshAuctionStatus()`. |
| Xác định người thắng | Lưu người dẫn đầu trong `Auction`. |
| Chuyển trạng thái phiên | Project dùng `NOT_START -> RUNNING -> CLOSED -> PAID/CANCELLED`. |
| Thanh toán sau khi thắng | `PaymentPopupController`, `UpdateAuctionStatusCommand`. |

#### 3.1.5 Xử lý lỗi và ngoại lệ

| Nhóm lỗi | Hiện thực |
| --- | --- |
| Bid thấp hơn giá hiện tại | `LowerThanCurrentBidException`. |
| Bid không đủ bước giá | `InsufficientIncrementException`. |
| Seller tự bid | `SelfBidException`. |
| Auction chưa bắt đầu / đã đóng | `AuctionNotStartedException`, `AuctionAlreadyEndedException`. |
| Lỗi database / network | `DataException`, `DatabaseConnectionException`, `NetworkException`. |

#### 3.1.6 Giao diện người dùng GUI

| Màn hình | File |
| --- | --- |
| Đăng nhập / đăng ký | `login.fxml`, `SignUp.fxml` |
| Danh sách auction | `AuctionList.fxml`, `AuctionCell.fxml` |
| Chi tiết sản phẩm | `AuctionInformation.fxml` |
| Đấu giá trực tiếp | `LiveAuction.fxml` |
| Quản lý sản phẩm seller | `Seller_ProductManagement.fxml` |
| Quản lý admin | `Admin_UserManagement.fxml`, `Admin_ProductManagement.fxml` |

### 3.2 Chức Năng Nâng Cao

<p align="center">
  <img alt="Auto Bid" src="https://img.shields.io/badge/Auto--Bidding-8b5cf6?style=for-the-badge">
  <img alt="Concurrent" src="https://img.shields.io/badge/Concurrent_Bidding-db2777?style=for-the-badge">
  <img alt="Anti Snipe" src="https://img.shields.io/badge/Anti--Sniping-f59e0b?style=for-the-badge">
  <img alt="Chart" src="https://img.shields.io/badge/Realtime_Chart-0ea5e9?style=for-the-badge">
</p>

| Chức năng nâng cao | Hiện thực |
| --- | --- |
| Auto-Bidding | `AutoBidConfig`, `AutoBidCommand`, `AuctionManager.registerAutoBid()`. |
| Concurrent Bidding | `ConcurrentHashMap`, `CopyOnWriteArrayList`, `ReentrantLock` theo `auctionId`. |
| Anti-Sniping | Nếu có bid trong 60 giây cuối, phiên được gia hạn thêm 60 giây. |
| Realtime Update | Observer + Socket, server notify client đang xem cùng auction. |
| Bid History Visualization | `LineChart` trong `LiveAuction.fxml`, cập nhật từ lịch sử bid. |

### 3.3 Thiết Kế Hướng Đối Tượng OOP

| Yêu cầu OOP | Hiện thực |
| --- | --- |
| Lớp chính | `User`, `Item`, `Auction`, `BidTransaction`, `AutoBidConfig`. |
| Kế thừa | `Electronics`, `Art`, `Vehicle` kế thừa `Item`; exception chia theo nhóm kế thừa base exception. |
| Đóng gói | Model dùng field private/protected và getter/setter. |
| Đa hình | Factory tạo item theo category; command xử lý theo type cụ thể. |
| Trừu tượng | DAO interface, command abstraction, listener interface. |

### 3.4 Thiết Kế Kiến Trúc Hệ Thống Networking & MVC

```mermaid
%%{init: {"theme": "base", "themeVariables": {"fontFamily": "Inter, Segoe UI, Arial", "primaryTextColor": "#0f172a", "lineColor": "#64748b"}}}%%
flowchart LR
    UI["JavaFX FXML + Controller"] --> NM["NetworkManager"]
    NM --> SC["ServerConnection"]
    SC --> CH["ClientHandler"]
    CH --> M["Manager Layer"]
    M --> DAO["DAO Layer"]
    DAO --> DB[("MySQL / TiDB")]
    M --> OBS["Observer / Subscriber"]
    OBS --> CH
    CH --> SC
    SC --> UI

    classDef client fill:#dbeafe,stroke:#2563eb,stroke-width:3px,color:#0f172a
    classDef network fill:#f5f3ff,stroke:#7c3aed,stroke-width:3px,color:#1e1b4b
    classDef server fill:#fee2e2,stroke:#ef4444,stroke-width:3px,color:#450a0a
    classDef data fill:#dcfce7,stroke:#16a34a,stroke-width:3px,color:#052e16
    classDef observer fill:#fef3c7,stroke:#f59e0b,stroke-width:3px,color:#451a03

    class UI,NM client
    class SC,CH network
    class M server
    class DAO,DB data
    class OBS observer
```

| Thành phần | Vai trò |
| --- | --- |
| Client MVC | FXML là View, controller xử lý sự kiện, `ClientModel` giữ trạng thái. |
| Networking | Java Socket, `ObjectInputStream`, `ObjectOutputStream`, `Command`/`Response`. |
| Server layer | `ClientHandler` nhận command, manager xử lý nghiệp vụ, DAO truy cập database. |
| Database rule | Chỉ server truy cập database. Client không gọi database trực tiếp. |

### 3.5 Tích Hợp Và Triển Khai

#### Công nghệ, môi trường chạy và yêu cầu cài đặt

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-25-f97316?style=flat-square">
  <img alt="JavaFX" src="https://img.shields.io/badge/JavaFX-25.0.3-2563eb?style=flat-square">
  <img alt="Maven" src="https://img.shields.io/badge/Maven-3.9.11-7c3aed?style=flat-square">
  <img alt="MySQL" src="https://img.shields.io/badge/MySQL_TiDB-16a34a?style=flat-square">
  <img alt="JUnit" src="https://img.shields.io/badge/JUnit_5-10b981?style=flat-square">
  <img alt="CI" src="https://img.shields.io/badge/GitHub_Actions-334155?style=flat-square">
</p>

| Nhóm | Công nghệ |
| --- | --- |
| Ngôn ngữ | Java 25 |
| GUI | JavaFX Controls/FXML 25.0.3 |
| Build | Maven 3.9.11 hoặc Maven Wrapper |
| Database | MySQL/TiDB qua MySQL Connector/J 9.3.0 |
| Connection pool | HikariCP 7.0.2 |
| Test | JUnit 5, Mockito, JaCoCo |
| Style | Checkstyle, Spotless |
| CI | GitHub Actions |

Yêu cầu cài đặt:

```text
JDK 25
Maven 3.9.11 hoặc Maven Wrapper
MySQL/TiDB database có schema phù hợp
```

Kiểm tra môi trường:

```powershell
java -version
mvn -version
```

#### Cấu trúc thư mục

```text
Project_SnowFlake/
|-- .github/workflows/ci.yml
|-- .mvn/wrapper/maven-wrapper.jar
|-- config/checkstyle/checkstyle.xml
|-- src/main/java/com/javfxtutorial/hethongdaugia/
|   |-- client/
|   |-- common/
|   `-- server/
|-- src/main/resources/com/javfxtutorial/hethongdaugia/
|   |-- assets/
|   `-- view/
|       |-- css/
|       `-- fxml/
|-- src/test/java/com/javfxtutorial/hethongdaugia/
|-- pom.xml
|-- mvnw
|-- mvnw.cmd
`-- README.md
```

Entry point chính:

| Thành phần | Đường dẫn |
| --- | --- |
| Client JavaFX | `src/main/java/com/javfxtutorial/hethongdaugia/client/MainApplication.java` |
| Server socket | `src/main/java/com/javfxtutorial/hethongdaugia/server/network/ServerApp.java` |
| Quản lý đấu giá | `src/main/java/com/javfxtutorial/hethongdaugia/server/manager/AuctionManager.java` |
| Cấu hình database | `src/main/java/com/javfxtutorial/hethongdaugia/server/dao/JDBCUtil.java` |

#### Vị trí file `.jar`

| File | Vị trí |
| --- | --- |
| JAR ứng dụng | `target/HeThongDauGia-1.0-SNAPSHOT.jar` |
| Maven Wrapper JAR | `.mvn/wrapper/maven-wrapper.jar` |

Tạo lại JAR:

```powershell
mvn package
```

Lưu ý: JAR trong `target/` là build output. Project hiện ưu tiên chạy bằng IDE hoặc Maven JavaFX plugin vì chưa cấu hình runnable fat JAR độc lập.

#### Cấu hình database

`JDBCUtil` đọc cấu hình theo thứ tự: system property, environment variable, default value trong code.

Các biến môi trường chính:

```powershell
$env:SNOWFLAKE_DB_HOST = 'localhost'
$env:SNOWFLAKE_DB_PORT = '3306'
$env:SNOWFLAKE_DB_NAME = 'snowfox'
$env:SNOWFLAKE_DB_USER = 'root'
$env:SNOWFLAKE_DB_PASSWORD = 'your_password'
```

Hoặc dùng JDBC URL:

```powershell
$env:SNOWFLAKE_DB_URL = 'jdbc:mysql://localhost:3306/snowfox?useSSL=false&allowPublicKeyRetrieval=true'
```

#### Hướng dẫn chạy Server/Client theo thứ tự

Thứ tự bắt buộc:

```text
1. Chuẩn bị database
2. Chạy ServerApp
3. Chạy MainApplication
4. Đăng nhập / đăng ký và sử dụng ứng dụng
```

Chạy bằng IDE:

```text
Server:
com.javfxtutorial.hethongdaugia.server.network.ServerApp

Client:
com.javfxtutorial.hethongdaugia.client.MainApplication
```

Chạy client bằng Maven sau khi server đã bật:

```powershell
mvn javafx:run
```

Hoặc dùng Maven Wrapper:

```powershell
.\mvnw.cmd javafx:run
```

Mặc định client kết nối đến:

```text
localhost:5000
```

#### Build, test và style

```powershell
mvn test
mvn verify
mvn checkstyle:check
mvn spotless:apply
```

### 3.6 Design Pattern Áp Dụng

<p align="center">
  <img alt="Singleton" src="https://img.shields.io/badge/Singleton-2563eb?style=flat-square">
  <img alt="Factory" src="https://img.shields.io/badge/Factory_Method-16a34a?style=flat-square">
  <img alt="Observer" src="https://img.shields.io/badge/Observer-f59e0b?style=flat-square">
  <img alt="Command" src="https://img.shields.io/badge/Command-8b5cf6?style=flat-square">
  <img alt="DAO" src="https://img.shields.io/badge/DAO-ef4444?style=flat-square">
</p>

| Pattern | Cách áp dụng |
| --- | --- |
| Singleton | `AuctionManager`, `UserManager`, `ServerConnection`. |
| Factory Method | Tạo item theo category qua các factory. |
| Observer | Server lưu subscriber và notify khi có bid mới. |
| Command | Mỗi request là một command object có `handle()`. |
| DAO | Tách truy cập database khỏi nghiệp vụ. |
| MVC/Layered | FXML/controller/model ở client; handler/manager/DAO ở server. |

---

## 4. Đối Chiếu Thang Điểm

| Nội dung đánh giá | Trạng thái |
| --- | --- |
| Thiết kế lớp và cây kế thừa | ![Dat](https://img.shields.io/badge/%C4%90%E1%BA%A1t-2563eb?style=flat-square) |
| Áp dụng OOP | ![Dat](https://img.shields.io/badge/%C4%90%E1%BA%A1t-7c3aed?style=flat-square) |
| Áp dụng design pattern | ![Dat](https://img.shields.io/badge/%C4%90%E1%BA%A1t-db2777?style=flat-square) |
| Quản lý người dùng, sản phẩm | ![Dat](https://img.shields.io/badge/%C4%90%E1%BA%A1t-16a34a?style=flat-square) |
| Chức năng đấu giá | ![Dat](https://img.shields.io/badge/%C4%90%E1%BA%A1t-ef4444?style=flat-square) |
| Xử lý lỗi và ngoại lệ | ![Dat](https://img.shields.io/badge/%C4%90%E1%BA%A1t-f59e0b?style=flat-square) |
| Concurrent bidding | ![Dat](https://img.shields.io/badge/%C4%90%E1%BA%A1t-0891b2?style=flat-square) |
| Realtime update | ![Dat](https://img.shields.io/badge/%C4%90%E1%BA%A1t-0ea5e9?style=flat-square) |
| Kiến trúc Client-Server | ![Dat](https://img.shields.io/badge/%C4%90%E1%BA%A1t-2563eb?style=flat-square) |
| MVC JavaFX + server layer | ![Dat](https://img.shields.io/badge/%C4%90%E1%BA%A1t-7c3aed?style=flat-square) |
| Maven, coding convention | ![Dat](https://img.shields.io/badge/%C4%90%E1%BA%A1t-111827?style=flat-square) |
| Unit Test | ![Dat](https://img.shields.io/badge/%C4%90%E1%BA%A1t-10b981?style=flat-square) |
| CI/CD cơ bản | ![Dat](https://img.shields.io/badge/%C4%90%E1%BA%A1t-334155?style=flat-square) |
| Auto-Bidding | ![Dat](https://img.shields.io/badge/%C4%90%E1%BA%A1t-8b5cf6?style=flat-square) |
| Anti-Sniping | ![Dat](https://img.shields.io/badge/%C4%90%E1%BA%A1t-f59e0b?style=flat-square) |
| Bid History Visualization | ![Dat](https://img.shields.io/badge/%C4%90%E1%BA%A1t-0ea5e9?style=flat-square) |

Kết quả kiểm tra gần nhất:

```text
mvn verify
Tests run: 75, Failures: 0, Errors: 0, Skipped: 0
```

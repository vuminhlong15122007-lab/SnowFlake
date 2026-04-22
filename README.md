<img width="1031" height="567" alt="classdiagram" src="https://github.com/user-attachments/assets/0f71095a-a1d5-4043-a6b7-6d894a394824" />

    Thiết kế clas : <Cả nhóm>
        Item : <Diệu Anh> chức năng : lưu trữ thông tin của Item
        Auction : <Nguyên> chức năng : lưu trữ giá thông tin thời gian 
        User : <Lan> chức năng : lưu trữ thông tin User
        Admin : <Long> chức năng : lưu trữ thông tin Admin
        Bid Transaction : <Nguyên> chức năng : Lưu trữ lịch sử đấu giá
    
    Thiết kế view : <Long>

    Thiết kế Controller : <Cả nhóm>
        màn hình cho Admin : <Lan , Long>
        màn hình +logic đăng ký tài khoản : <Lan>
        Quản lý thông tin User : <Lan>
        Logic đăng nhập : <Nguyên>
        Màn hình đấu giá trực tiếp : <Nguyên>
        Hiển thị hình ảnh trong các màn hình có ảnh : <Long>
        Xử lý logic thời gian : <Diệu Anh>
        Màn hình quản lý sản phảm seller : <Diệu Anh>
        Màn hình hiển thị auction : <Diệu Anh , Nguyên>
        
    Kết nối mạng : <Nguyên>
    Kết nối database : <Long , Nguyên>
    
    DAO : <cả nhóm>
        ItemDAO : Long
        UserDAO : Long + Nguyên + Lan + Diệu Anh
        Auction : Nguyên 
    Manager :
        Auction : Nguyên
        Item : Diệu Anh + Nguyên
        User : Nguyên , Lan , Long
List To Do
    
    fix bug : Nguyên (bug connectionServer , tranh chấp đấu giá observer )
    3 tính năng nâng cao : Long , Diệu Anh
    Exception : Lan
    Viết test : AI + team
    sửa Item thành abstractmethod : Long + Diệu Anh
        

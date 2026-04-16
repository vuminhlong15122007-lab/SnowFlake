package com.javfxtutorial.hethongdaugia.client.model;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Item;
import com.javfxtutorial.hethongdaugia.common.model.User;

public class ClientModel {            //class dùng để lưu trữ trạng thái toàn cục của ứng dụng Client(VD:ai đang đăng nhập, giỏ hàng, cài đặt...)
    private static ClientModel instance;
    private ClientModel(){};

    public static ClientModel getInstance() {
        if (instance == null){
            instance = new ClientModel();
        }
        return instance;
    }


    private User currentUser;       //Lưu và lấy thông tin obj User đã đăng nhập tcong
    public User getCurrentUser() {
        return currentUser;
    }
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }



    private Auction currentAuction;        // Khi người dùng nhấp vào một phiên đấu giá từ danh sách, phiên đó được lưu và lấy ở đây.
    public Auction getCurrentAuction() {
        return currentAuction;
    }
    public void setCurrentAuction(Auction currentAuction) {
        this.currentAuction = currentAuction;
    }

    private Item currentItem;   //dùng để lưu và lấy sản phẩm giữa đang xem ( chi tiết sp đang xem).
    public Item getCurrentItem(){
        return currentItem;
    }
    public void setCurrentItem(Item currentItem) {
        this.currentItem = currentItem;
    }
}

package com.javfxtutorial.hethongdaugia.service;

import com.javfxtutorial.hethongdaugia.dao.ItemDAO;
import com.javfxtutorial.hethongdaugia.model.AuctionItem;
import com.javfxtutorial.hethongdaugia.model.User;
import com.javfxtutorial.hethongdaugia.model.enums.ItemStatus;

import java.time.LocalDateTime;

public class AuctionService {

        private final ItemDAO itemDAO;

        public AuctionService(ItemDAO itemDAO) {
            this.itemDAO = itemDAO;
        }

        // Tạo item đấu giá
        public AuctionItem createItem(User seller, String name, double initPrice) {
            if (seller == null) {
                throw new IllegalArgumentException("User không tồn tại");
            }

            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Tên sản phẩm không hợp lệ");
            }

            if (initPrice <= 0) {
                throw new IllegalArgumentException("Giá khởi điểm phải > 0");
            }

            AuctionItem item = new AuctionItem();
            item.setName(name);
            item.setInitPrice(initPrice);
            item.setCurrentPrice(initPrice);
            item.setSellerId(seller.getId());
            item.setStatus(ItemStatus.NOT_START);


//            itemDAO.insert(item);
            return item;
        }

        // 🟢 Bắt đầu đấu giá
        // 🔴 Kết thúc đấu giá
        // 📋 Lấy tất cả item đang đấu giá
        // 📋 Lấy item theo seller

        // 🔍 Lấy chi tiết item

        // ❌ Xoá item (chỉ khi chưa đấu giá)

    }

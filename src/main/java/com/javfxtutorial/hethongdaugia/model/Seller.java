package com.javfxtutorial.hethongdaugia.model;
import java.util.List;
import java.util.ArrayList;

public class Seller extends Bidder{
    List<Item> list_item = new ArrayList<>();    // tạo 1 list rỗng kiểu Item
    double counter = 0;

    public Seller(String name, String passWord, String email, String fullName, double money, List<Item> list_item) {
        super(name, passWord, email, fullName, money);
        this.list_item = list_item;
    }

    public void addItem(Item new_item){
        if(list_item.contains(new_item)){    // Test sp đã trong list chưa
            System.out.println("Sản phẩm đã tồn tại !!!");
        }else {
            list_item.add(new_item);   // thêm sp new vào list
        }

    }

    public void deleteItem(Item item){
        if(list_item.contains(item)){
            System.out.println("Sản phẩm đã bị xóa !!!");
        }else{
            list_item.remove(item);  // xóa sp
        }
    }

    public void changeItem(){
        // chưa làm
    }

    public List<Item> getListItem(){
        return list_item;
    }

    @Override
    public String Generate_Id(){
        counter ++;
        return "SE " + counter + String.format("%03d", counter);
    }


}

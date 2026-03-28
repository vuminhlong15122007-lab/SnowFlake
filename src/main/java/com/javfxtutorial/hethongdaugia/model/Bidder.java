package com.javfxtutorial.hethongdaugia.model;

import com.javfxtutorial.hethongdaugia.dao.UserDAO;

public class Bidder extends User {
    private double money;

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        money = money;
    }



    public String Generate_Id(){
        int counter = UserDAO.getInstance().getSize();
        return "BD" + String.format("%03d", counter);
    }

    public Bidder(String name , String passWord , String email ){
        super(name, passWord, email);
    }
    public Bidder(String name , String passWord , String email , String fullName , double money){
        super(name , passWord , email , fullName);
        this.money = money ;
    }
    public void registerSeller(){} // chưa nghĩ ra
    public void upDateMoney(double money1){
        this.money = money1;
    }
    public void requestToBid(Auction ac ,double amount){
        if (this.money >= amount){
            System.out.println(this.getName() + " đang gửi yêu cầu đấu giá");
            boolean success = ac.placeBid(this , amount);
            if (success){
                //tạm thời trừ tiền
                // this.money -= amount
            }else{
                System.out.println(this.getName() + " không đủ tiền trong tài khoản");
            }
        }
    }

}

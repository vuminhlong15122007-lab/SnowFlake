package com.javfxtutorial.hethongdaugia.model;

public class Bidder extends User {
    private double money;
    private int counterID ;

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        money = money;
    }

    public int getCounterID() {
        return counterID;
    }

    public void setCounterID(int counterID) {
        this.counterID = counterID;
    }


    public String Generate_Id(){
        counterID  = this.getCountBidder() + 1;
        this.setCountBidder(counterID);
        return "BD" + String.format("%03d", counterID);
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

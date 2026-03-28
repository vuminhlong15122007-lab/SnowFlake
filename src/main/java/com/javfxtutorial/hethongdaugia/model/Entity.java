package com.javfxtutorial.hethongdaugia.model;
import java.time.LocalDate;
public abstract class Entity {
    private String id ;
    private LocalDate time = LocalDate.now(); // thời gian khởi tạo tài khoản / sản phẩm => cố định không sửa
    private String name ;
    private int countBidTransaction = 0 ;
    private int countAuction = 0 ;

    public int getCountAuction() {
        return countAuction;
    }

    public void setCountAuction(int countAuction) {
        this.countAuction = countAuction;
    }

    public int getCountBidTransaction() {
        return countBidTransaction;
    }

    public void setCountBidTransaction(int countBidTransaction) {
        this.countBidTransaction = countBidTransaction;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Entity(){
        this.id = Generate_Id() ;
    }
    public Entity(String name ){
        this.id = this.Generate_Id() ;
        this.name = name ;
    }

    public abstract String Generate_Id();
}

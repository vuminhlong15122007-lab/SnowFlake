package com.javfxtutorial.hethongdaugia.model;
import java.time.LocalDate;
public abstract class Entity {
    private String id ;
    private LocalDate time = LocalDate.now(); // thời gian khởi tạo tài khoản / sản phẩm => cố định không sửa
    private String name ;

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

    public Entity(String id , String name ){
        this.id = id ;
        this.name = name ;
    }

    public abstract String Generate_Id();
}

package com.javfxtutorial.hethongdaugia.model;
import java.time.LocalDate;
public abstract class Entity {
    private String id ;
    private LocalDate time ;

    public Entity(String id , LocalDate time ){
        this.id = id ;
        this.time = time ;
    }

    public abstract String Generate_Id();
}

package com.javfxtutorial.hethongdaugia.model;
import java.time.LocalDate;
public abstract class Entity {
    private String Id ;
    private LocalDate time ;

    public Entity(String Id , LocalDate time ){
        this.Id = Id ;
        this.time = time ;
    }

    public abstract String Generate_Id();
}

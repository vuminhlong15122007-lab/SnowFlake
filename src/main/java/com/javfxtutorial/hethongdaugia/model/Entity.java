package com.javfxtutorial.hethongdaugia.model;
import java.time.LocalDate;
public abstract class Entity {
    private String id ;
    private LocalDate time ;
    private String name ;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getTime() {
        return time;
    }

    public void setTime(LocalDate time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Entity(String id , LocalDate time ){
        this.id = id ;
        this.time = time ;
    }

    public abstract String Generate_Id();
}

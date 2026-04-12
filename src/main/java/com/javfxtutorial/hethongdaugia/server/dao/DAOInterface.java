package com.javfxtutorial.hethongdaugia.server.dao;

import javafx.collections.ObservableList;

import java.util.ArrayList;

public interface DAOInterface<T> {
    int insert(T t);
    int update(T t);
    int delete(T t);
    ObservableList<T> selectAll();
    T selectById(int id);
}

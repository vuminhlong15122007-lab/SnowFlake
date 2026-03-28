package com.javfxtutorial.hethongdaugia.dao;

import java.util.ArrayList;

public interface DAOInterface<T> {
    int create(T t);
    int update(T t);
    int delete(T t);
    ArrayList<T> selectAll();
    T selectById(T t);
    ArrayList<T> selectByCondition(String condition);
}

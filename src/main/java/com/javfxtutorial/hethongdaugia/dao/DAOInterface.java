package com.javfxtutorial.hethongdaugia.dao;

import java.util.ArrayList;

public interface DAOInterface<T> {
    int insert(T t);
    int update(T t);
    int delete(T t);
    ArrayList<T> selectAll();
    T selectById(int id);
    ArrayList<T> selectByCondition(String condition);
}

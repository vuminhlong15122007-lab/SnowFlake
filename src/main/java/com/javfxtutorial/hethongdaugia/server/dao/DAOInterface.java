package com.javfxtutorial.hethongdaugia.server.dao;

import com.javfxtutorial.hethongdaugia.common.Exception.data.DataException;
import java.util.List;

public interface DAOInterface<T> {
    int insert(T t) throws DataException;

    int update(T t) throws DataException;

    int delete(T t) throws DataException;

    List<T> selectAll() throws DataException;

    T selectById(int id) throws DataException;
}

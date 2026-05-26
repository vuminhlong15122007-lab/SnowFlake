package com.javfxtutorial.hethongdaugia.common.network;

import com.javfxtutorial.hethongdaugia.common.Exception.data.DataException;

import java.io.Serializable;
import java.util.HashMap;

// abstract class cho mọi Command
public abstract class Command implements Serializable {
    private String requestId;
    HashMap<String, Object> data;

    public Command() {
        this.data = new HashMap<>();
    }

    public Object getData(String key) {
        return data.get(key);
    }

    public HashMap<String, Object> getData() {
        return this.data;
    }

    public void addData(String key, Object value) {
        data.put(key, value);
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public abstract Response handle() throws DataException;
}

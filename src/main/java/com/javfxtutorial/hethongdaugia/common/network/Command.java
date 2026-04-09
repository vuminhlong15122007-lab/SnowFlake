package com.javfxtutorial.hethongdaugia.common.network;

import java.io.Serializable;
import java.util.HashMap;

// client gửi command qua cho server, server sẽ handle
// client = cách check request thpe, chạy logic và gửi lại respone
public abstract class Command implements Serializable {
    HashMap<String, Object> data;
    public Command() {
        this.data = new HashMap<>();
    }
    public Object getData(String key) {
        return data.get(key);
    }
    public HashMap<String, Object> getData(){
        return this.data;
    }
    public void addData(String key, Object value) {
        data.put(key, value);
    }
    public abstract Response handle();
}

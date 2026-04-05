package com.javfxtutorial.hethongdaugia.common.network;

import java.io.Serializable;

public class Response implements Serializable {
    private boolean success; //xem hành động có thnahf công hay k
    private String message;
    private Object payLoad; //dữ liệu mà server trả lại theo y/c của Client

    public Response(boolean success, String message, Object payLoad) {
        this.success = success;
        this.message = message;
        this.payLoad = payLoad;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getPayLoad() {
        return payLoad;
    }

    public void setPayLoad(Object payLoad) {
        this.payLoad = payLoad;
    }
}

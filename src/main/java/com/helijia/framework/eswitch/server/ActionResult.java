package com.helijia.framework.eswitch.server;

import java.io.Serializable;

public class ActionResult implements Serializable {

    private static final long serialVersionUID             = 1L;

    public static final int   ERROR_CODE_INVALID_PARAMETER = -1;
    public static final int   ERROR_CODE_NO_RESULT         = -2;
    public static final int   ERROR_CODE_SYSTEM            = -3;

    private boolean           success                      = false;
    private int               code;
    private String            message;
    private Object            data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void initErrorInfo(int code, String message) {
        this.setSuccess(false);
        this.setCode(code);
        this.setMessage(message);
        this.setData(null);
    }

    public void initSuccessInfo(String message, Object data) {
        this.setSuccess(true);
        this.setCode(200);
        this.setMessage(null);
        this.setData(data);
    }
}

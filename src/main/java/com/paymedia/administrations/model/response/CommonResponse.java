package com.paymedia.administrations.model.response;

public class CommonResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public CommonResponse() {
    }

    // Constructor with all parameters
    public CommonResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Constructor without data (this is what you need)
    public CommonResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.data = null; // or you can omit this line
    }

    // Getters and Setters
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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}

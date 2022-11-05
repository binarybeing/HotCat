package io.github.binarybeing.hotcat.plugin.server.dto;

/**
 * @author gn.binarybei
 * @date 2022/9/27
 * @note
 */
public class Response {
    private int code;
    private String msg;
    private Object data;

    protected Response() {
    }

    public Response(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static Response error(String msg) {
        return new Response(500, msg, null);
    }

    public static Response success(Object result) {
        return new Response(200, "success", result);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}

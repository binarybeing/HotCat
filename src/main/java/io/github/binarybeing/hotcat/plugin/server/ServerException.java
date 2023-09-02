package io.github.binarybeing.hotcat.plugin.server;

/**
 * @author gn.binarybei
 * @date 2023/3/24
 * @note
 */
public class ServerException extends Exception{
    private Throwable e;
    private String msg;
    private ServerException(Throwable e, String msg){
        this.e = e;
        this.msg = msg;
    }
    public static ServerException of(Throwable e, String msg){
        return new ServerException(e, msg);
    }

    @Override
    public String getMessage() {
        return msg + " " + getMessageFromException(e, 0);
    }

    private String getMessageFromException(Throwable t, int depth){
        if(depth > 10){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Caused by: ").append(t.getClass().getName()).append(": ").append(t.getMessage()).append("\n");
        for (StackTraceElement element : t.getStackTrace()) {
            sb.append("\tat ").append(element).append("\n");
        }
        sb.append("\n");
        if (t.getCause() != null) {
            sb.append(getMessageFromException(t.getCause(), depth + 1));
        }
        return sb.toString();
    }
}

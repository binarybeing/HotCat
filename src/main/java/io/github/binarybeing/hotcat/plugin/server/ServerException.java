package io.github.binarybeing.hotcat.plugin.server;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * @author gn.binarybei
 * @date 2023/3/24
 * @note
 */
public class ServerException extends Exception{
    private Exception e;
    private String msg;
    private ServerException(Exception e, String msg){
        this.e = e;
        this.msg = msg;
    }
    public static ServerException of(Exception e, String msg){
        return new ServerException(e, msg);
    }

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder();
        StackTraceElement[] traceElements = e.getStackTrace();
        for (StackTraceElement traceElement : traceElements) {
            builder.append("\tat ").append(traceElement).append("\n");
        }
        return this.e.getMessage() + "\n" + builder;
    }

    @Override
    public String getLocalizedMessage() {
        return this.e.getLocalizedMessage();
    }

    @Override
    public synchronized Throwable getCause() {
        return this.e.getCause();
    }

    @Override
    public synchronized Throwable initCause(Throwable cause) {
        return this.e.initCause(cause);
    }

    @Override
    public String toString() {
        return this.e.toString();
    }

    @Override
    public void printStackTrace() {
        super.printStackTrace();
    }

    @Override
    public void printStackTrace(PrintStream s) {
        super.printStackTrace(s);
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        super.printStackTrace(s);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this.e.fillInStackTrace();
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        return this.e.getStackTrace();
    }

    @Override
    public void setStackTrace(StackTraceElement[] stackTrace) {
        super.setStackTrace(stackTrace);
    }
}

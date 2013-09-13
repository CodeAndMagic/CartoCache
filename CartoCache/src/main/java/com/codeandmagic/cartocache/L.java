package com.codeandmagic.cartocache;

import android.util.Log;

import java.text.MessageFormat;

public class L {

    private static final String FORMAT = "[{0} line {1}]: ";

    private Class<?> clazz;
    private String tag;

    public static L getLog(Class<?> clazz) {
        return new L(clazz);
    }

    private L(Class<?> clazz) {
        this.clazz = clazz;
        this.tag = clazz.getSimpleName();
    }

    private String getCallingMethodLineNumber() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if(stackTraceElements != null) {
            for(int i = 0; i < stackTraceElements.length; i++) {
                StackTraceElement element = stackTraceElements[i];
                if(element != null && element.getClassName().equals(clazz.getName())) {
                    return MessageFormat.format(FORMAT, element.getMethodName(), element.getLineNumber());
                }
            }
        }
        return "";
    }

    private String makeTag() {
        return tag + getCallingMethodLineNumber();
    }

    public void v(String msg) {
        Log.v(makeTag(), msg);
    }

    public void d(String msg) {
        Log.d(makeTag(), msg);
    }

    public void i(String msg) {
        Log.i(makeTag(), msg);
    }

    public void w(String msg) {
        Log.w(makeTag(), msg);
    }

    public void w(String msg, Throwable e) {
        Log.w(makeTag(), msg, e);
    }

    public void e(String msg) {
        Log.e(makeTag(), msg);
    }

    public void e(String msg, Throwable e) {
        Log.e(makeTag(), msg, e);
    }
}

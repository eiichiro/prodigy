package io.eiichiro.prodigy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Invocation {

    private Object target;

    private Method method;

    private Object[] args;

    private Object result;

    private Throwable throwable;

    private boolean proceeded;

    public void proceed() throws Throwable {
        if (proceeded) {
            throw new IllegalStateException("");
        }

        proceeded = true;

        try {
            result = method.invoke(target, args);
        } catch (InvocationTargetException e) {
            this.throwable = e.getCause();
        }
    }

    public Object target() {
        return target;
    }

    public Invocation target(Object target) {
        this.target = target;
        return this;
    }

    public Method method() {
        return method;
    }

    public Invocation method(Method method) {
        this.method = method;
        return this;
    }

    public Object[] args() {
        return args;
    }

    public Invocation args(Object[] args) {
        this.args = args;
        return this;
    }

    public Object result() {
        return result;
    }

    public Invocation result(Object result) {
        this.result = result;
        return this;
    }

    public Throwable throwable() {
        return throwable;
    }

    public Invocation throwable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    public boolean proceeded() {
        return proceeded;
    }

}

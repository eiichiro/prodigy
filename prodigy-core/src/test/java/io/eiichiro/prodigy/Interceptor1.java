package io.eiichiro.prodigy;

public class Interceptor1 extends Fault implements Interceptor {

    private Object result;

    private Throwable throwable;

    private boolean applies;

    private boolean proceeds;

    private boolean invoked;

    public Interceptor1 result(Object result) {
        this.result = result;
        return this;
    }

    public Interceptor1 throwable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    public Interceptor1 applies(boolean applies) {
        this.applies = applies;
        return this;
    }

    public Interceptor1 proceeds(boolean proceeds) {
        this.proceeds = proceeds;
        return this;
    }

    public boolean invoked() {
        return invoked;
    }

    @Override
    public boolean apply(Invocation invocation) throws Throwable {
        invoked = true;

        if (throwable != null) {
            invocation.throwable(throwable);
        } else {
            invocation.result(result);
        }

        if (proceeds) {
            invocation.proceed();
        }

        return applies;
    }

}

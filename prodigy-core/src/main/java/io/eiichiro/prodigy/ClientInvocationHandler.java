package io.eiichiro.prodigy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ClientInvocationHandler implements InvocationHandler {

    private final Object target;
    
    public ClientInvocationHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Invocation invocation = new Invocation().target(target).method(method).args(args);

        for (Interceptor interceptor : Prodigy.container().interceptors().values()) {
            if (interceptor.intercept(invocation)) {
                return throwOrResult(invocation);
            }
        }

        invocation.proceed();
        return throwOrResult(invocation);
    }

    private Object throwOrResult(Invocation invocation) throws Throwable {
        if (invocation.throwable() != null) {
            throw invocation.throwable();
        } else {
            return invocation.result();
        }
    }

}

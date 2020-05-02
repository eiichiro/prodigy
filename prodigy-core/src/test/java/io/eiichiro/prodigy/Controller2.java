package io.eiichiro.prodigy;

public class Controller2 extends Fault implements Controller {

    @Override
    public void activate() {
        throw new IllegalStateException("hello");
    }

    @Override
    public void deactivate() {
        throw new IllegalStateException("hello");
    }

}
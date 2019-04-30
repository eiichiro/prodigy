package io.eiichiro.prodigy;

public class Controller1 extends Fault implements Controller {

    boolean activated;

    boolean deactivated;

    @Override
    public void activate() {
        activated = true;
    }

    @Override
    public void deactivate() {
        deactivated = true;
    }

}
package io.eiichiro.prodigy;

public class Violation {

    private final String message;

    public Violation(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }

}
package io.eiichiro.prodigy;

@Named("fault-4")
public class Fault4 extends Fault {

    private String property1;

    private int property2;

    public String getProperty1() {
        return property1;
    }

    public int getProperty2() {
        return property2;
    }

    public void setProperty2(int property2) {
        this.property2 = property2;
    }

    public void setProperty1(String property1) {
        this.property1 = property1;
    }
    
}

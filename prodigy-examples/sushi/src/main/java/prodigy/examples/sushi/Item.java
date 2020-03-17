package prodigy.examples.sushi;

public enum Item {

    SALMON("salmon"), 
    YELLOWTAIL("yellowtail"), 
    TUNA("tuna"), 
    MEDIUM_FATTY_TUNA("medium fatty tuna"), 
    SHRIMP("shrimp"), 
    SALMON_ROE("salmon roe"), 
    SQUID("squid"), 
    SCALLOP("scallop"), 
    SEA_URCHIN("sea urchin"), 
    HORSE_MACKEREL("horse mackerel");

    private final String name;

    private Item(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
}

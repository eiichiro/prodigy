/*
 * Copyright (C) 2019-present Eiichiro Uchiumi and the Prodigy Authors. 
 * All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package prodigy.examples.sushi;

/**
 * {@code Item} provides sushi item constants.
 * 
 * @author <a href="mailto:eiichiro.uchiumi@gmail.com">Eiichiro Uchiumi</a>
 */
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

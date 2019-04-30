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
package io.eiichiro.prodigy;

/**
 * {@code Fault} is the base class every concrete fault class must inherit.
 * 
 * @author <a href="mailto:eiichiro.uchiumi@gmail.com">Eiichiro Uchiumi</a>
 */
public abstract class Fault {

    private String id;

    /**
     * Returns the fault id.
     * 
     * @return The fault id.
     */
    public String id() {
        return id;
    }

    /**
     * Sets the specified fault id.
     * 
     * @param id The fault id.
     */
    public void id(String id) {
        this.id = id;
    }

}

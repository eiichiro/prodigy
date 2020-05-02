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

import java.util.Set;

/**
 * {@code Validator} is the interface that fault classes need to implement if 
 * they intend to validate input parameters before they are scheduled.
 * 
 * @author <a href="mailto:eiichiro.uchiumi@gmail.com">Eiichiro Uchiumi</a>
 */
public interface Validator {

    /**
     * Validates input parameters populated into instance fields.
     * 
     * @return Set of {@code Violation}. If input parameters are valid, the 
     * set needs to be empty.
     * @see Violation
     */
    public Set<Violation> validate();
    
}

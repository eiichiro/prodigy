/*
 * Copyright (C) 2019 Eiichiro Uchiumi and The Prodigy Authors. All Rights Reserved.
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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.logging.Log;

public class InjectHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final Log log = LambdaLogFactory.getLog(getClass());

    @Override
    @SuppressWarnings("unchecked")
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        APIGatewayProxyResponseEvent output = new APIGatewayProxyResponseEvent();

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> request = mapper.readValue(input.getBody(), new TypeReference<Map<String, Object>>() {});
            Object object = request.get("name");
            String name;
    
            if (object != null) {
                name = object.toString();
            } else {
                return output.withStatusCode(400).withBody("Parameter 'name' is required");
            }
    
            object = request.get("params");
            Map<String, Object> params;
    
            if (object == null) {
                params = new HashMap<>();
            } else if (object instanceof Map) {
                params = (Map<String, Object>) object;
            } else {
                return output.withStatusCode(400).withBody("Parameter 'params' must be a JSON object");
            }
    
            Fault fault = Prodigy.container().fault(name, params);
    
            if (fault == null) {
                return output.withStatusCode(400).withBody("Fault [" + name + "] not found");
            }
    
            if (fault instanceof Validator) {
                Set<Violation> violations = ((Validator) fault).validate();
    
                if (!violations.isEmpty()) {
                    return output.withStatusCode(400).withBody("Parameter 'params' is invalid: " + violations);
                }
            }
    
            Prodigy.container().scheduler().schedule(fault);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("id", fault.id());
            return output.withStatusCode(200).withBody(mapper.writeValueAsString(response));
        } catch (JsonParseException e) {
            String message = "Parameter must be a JSON object: " + e.getMessage();
            log.warn(message, e);
            return output.withStatusCode(400).withBody(message);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return output.withStatusCode(500).withBody(e.getMessage());
        }
    }

}

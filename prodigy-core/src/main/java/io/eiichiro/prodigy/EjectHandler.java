package io.eiichiro.prodigy;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.logging.Log;

import io.eiichiro.prodigy.Scheduler.Entry;

public class EjectHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final Log log = LambdaLogFactory.getLog(getClass());
    
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        APIGatewayProxyResponseEvent output = new APIGatewayProxyResponseEvent();

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> request = mapper.readValue(input.getBody(), new TypeReference<Map<String, Object>>() {
            });
            Object object = request.get("id");
            String id;
        
            if (object != null) {
                id = object.toString();
            } else {
                return output.withStatusCode(400).withBody("Parameter 'id' is required");
            }

            Entry entry = Prodigy.container().scheduler().get(id);

            if (entry == null) {
                return output.withStatusCode(400).withBody("Fault id [" + id + "] not found");
            }

            Prodigy.container().scheduler().unschedule(id);
            return output.withStatusCode(200).withBody(mapper.writeValueAsString("{}"));
        } catch (JsonParseException e) {
            String message = "Parameter must be JSON object: " + e.getMessage();
            log.warn(message, e);
            return output.withStatusCode(400).withBody(message);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return output.withStatusCode(500).withBody(e.getMessage());
        }
    }

}

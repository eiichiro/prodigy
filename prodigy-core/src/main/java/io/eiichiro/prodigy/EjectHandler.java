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

public class EjectHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final Log log = LambdaLogFactory.getLog(getClass());
    
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        APIGatewayProxyResponseEvent output = new APIGatewayProxyResponseEvent();

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> request = mapper.readValue(input.getBody(), new TypeReference<Map<String, String>>() {
            });
            String id = request.get("id");

            if (id == null) {
                return output.withStatusCode(400).withBody("Parameter 'id' is required");
            }

            log.info("Ejecting fault id [" + id + "]");
            boolean result = Prodigy.container().scheduler().unschedule(id);

            if (!result) {
                String message = "Fault id [" + id + "] not found";
                log.warn(message);
                return output.withStatusCode(400).withBody(message);
            }

            log.info("Fault id [" + id + "] ejected");
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

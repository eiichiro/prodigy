package io.eiichiro.prodigy;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.logging.Log;

import io.eiichiro.prodigy.Scheduler.Entry;

public class StatusHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final Log log = LambdaLogFactory.getLog(getClass());
    
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        APIGatewayProxyResponseEvent output = new APIGatewayProxyResponseEvent();

        try {
            String id = input.getQueryStringParameters().get("id");
            Object response;

            if (id == null) {
                response = Prodigy.container().scheduler().list();
            } else {
                Entry entry = Prodigy.container().scheduler().get(id);

                if (entry == null) {
                    return output.withStatusCode(400).withBody("Fault id [" + id + "] not found");
                }

                response = entry;
            }

            ObjectMapper mapper = new ObjectMapper();
            return output.withStatusCode(200).withBody(mapper.writeValueAsString(response));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return output.withStatusCode(500).withBody(e.getMessage());
        }
    }
    
}

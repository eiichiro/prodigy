package io.eiichiro.prodigy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.UploadContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.logging.Log;

public class PushHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final Log log = LambdaLogFactory.getLog(getClass());

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        APIGatewayProxyResponseEvent output = new APIGatewayProxyResponseEvent();
        byte[] body = Base64.getDecoder().decode(input.getBody());
        FileUpload upload = new FileUpload(new DiskFileItemFactory());

        try {
            FileItemIterator iterator = upload.getItemIterator(new UploadContext() {

                @Override
                public InputStream getInputStream() throws IOException {
                    return new ByteArrayInputStream(body);
                }
    
                @Override
                public String getContentType() {
                    return input.getHeaders().get("Content-type");
                }
    
                @Override
                public int getContentLength() {
                    return body.length;
                }
    
                @Override
                public String getCharacterEncoding() {
                    return StandardCharsets.UTF_8.name();
                }
    
                @Override
                public long contentLength() {
                    return body.length;
                }

            });

            while (iterator.hasNext()) {
                FileItemStream item = iterator.next();

                if (!item.isFormField() && item.getFieldName().equals("jar")) {
                    String name = item.getName();

                    if (!name.endsWith(".jar")) {
                        String message = "Parameter 'jar' must be a jar file";
                        log.warn(message);
                        return output.withStatusCode(400).withBody(message);
                    }

                    Prodigy.container().repository().save(name, item.openStream());
                    return output.withStatusCode(200).withBody("{}");
                }
            }

            String message = "Parameter 'jar' is required";
            log.warn(message);
            return output.withStatusCode(400).withBody(message);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return output.withStatusCode(500).withBody(e.getMessage());
        }
    }

}

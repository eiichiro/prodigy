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

    private static Log log = LambdaLogFactory.getLog(PushHandler.class);

    static {
        ProvisionedConcurrency.warmup();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        log.debug("'input' is [" + input + "]");
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
                    return input.getHeaders().get("Content-Type");
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
                    Prodigy.push(name, item.openStream());
                    return output.withStatusCode(200).withBody("{}");
                }
            }

            throw new IllegalArgumentException("Parameter 'jar' is required");
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage(), e);
            return output.withStatusCode(400).withBody(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return output.withStatusCode(500).withBody(e.getMessage());
        }
    }

}

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

import com.amazonaws.services.dynamodbv2.model.OperationType;
import com.amazonaws.services.dynamodbv2.model.StreamRecord;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;

import org.apache.commons.logging.Log;

public class SchedulerHandler implements RequestHandler<DynamodbEvent, Void> {

    private static Log log = LambdaLogFactory.getLog(SchedulerHandler.class);
    
    static {
        ProvisionedConcurrency.warmup();
    }

    @Override
    public Void handleRequest(DynamodbEvent input, Context context) {
        try {
            input.getRecords().forEach(r -> {
                OperationType type = OperationType.valueOf(r.getEventName());
                StreamRecord record = r.getDynamodb();
    
                if (type == OperationType.INSERT) {
                    if (record.getNewImage().get("status").getS().equals("ACTIVE")) {
                        String name = record.getNewImage().get("name").getS();
                        String id = record.getNewImage().get("id").getS();
                        String params = record.getNewImage().get("params").getS();
                        log.info("Activating fault id [" + id + "]");
                        Fault fault = Prodigy.container().fault(name, id, params);

                        if (fault instanceof Controller) {
                            Controller controller = (Controller) fault;
                            controller.activate();
                            log.info("Fault id [" + id + "] activated");
                        }
                    }

                } else if (type == OperationType.MODIFY) {
                    if (record.getNewImage().get("status").getS().equals("INACTIVE")) {
                        String name = record.getNewImage().get("name").getS();
                        String id = record.getNewImage().get("id").getS();
                        String params = record.getNewImage().get("params").getS();
                        log.info("Deactivating fault id [" + id + "]");
                        Fault fault = Prodigy.container().fault(name, id, params);

                        if (fault instanceof Controller) {
                            Controller controller = (Controller) fault;
                            controller.deactivate();
                            log.info("Fault id [" + id + "] deactivated");
                        }
                    }

                } else {
                    // OperationType.REMOVE
                    if (record.getOldImage().get("status").getS().equals("ACTIVE")) {
                        String name = record.getOldImage().get("name").getS();
                        String id = record.getOldImage().get("id").getS();
                        String params = record.getOldImage().get("params").getS();
                        log.info("Deactivating fault id [" + id + "]");
                        Fault fault = Prodigy.container().fault(name, id, params);

                        if (fault instanceof Controller) {
                            Controller controller = (Controller) fault;
                            controller.deactivate();
                            log.info("Fault id [" + id + "] deactivated");
                        }
                    }
                }
            });
        } catch (RuntimeException e) {
            log.error(e.getMessage(), e);
            throw e;
        }

        return null;
    }

}
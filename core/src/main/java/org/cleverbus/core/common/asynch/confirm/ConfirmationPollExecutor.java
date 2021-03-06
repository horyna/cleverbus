/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cleverbus.core.common.asynch.confirm;

import org.cleverbus.api.asynch.AsynchConstants;
import org.cleverbus.api.entity.ExternalCall;
import org.cleverbus.api.exception.LockFailureException;
import org.cleverbus.common.log.Log;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Reads confirmations (=external calls) from DB and sends them for next processing.
 * Execution will stop when there is no further confirmation for processing.
 * <p/>
 * This executor is invoked by {@link JobStarterForConfirmationPooling}.
 *
 * @author <a href="mailto:petr.juza@cleverlance.com">Petr Juza</a>
 */
public class ConfirmationPollExecutor implements Runnable {

    private static final int LOCK_FAILURE_LIMIT = 5;

    @Autowired
    private ConfirmationPool confirmationPool;

    @Autowired
    private ProducerTemplate producerTemplate;

    // note: this is because of setting different target URI for tests
    private String targetURI = AsynchConstants.URI_CONFIRM_MESSAGE;

    @Override
    public void run() {
        Log.debug("Confirmation pooling starts ...");

        // is there confirmation for processing?
        ExternalCall extCall = null;
        int lockFailureCount = 0;
        while (true) {
            try {
                extCall = confirmationPool.getNextConfirmation();
                if (extCall != null) {
                    // sends confirmation for next processing
                    producerTemplate.sendBody(targetURI, extCall);
                } else {
                    //there is no new confirmation for processing
                    //  => finish this executor and try it again after some time
                    break;
                }
            } catch (LockFailureException ex) {
                // try again to acquire next message with lock
                lockFailureCount++;

                if (lockFailureCount > LOCK_FAILURE_LIMIT) {
                    Log.warn("Probably problem with locking confirmations - count of lock failures exceeds limit ("
                            + LOCK_FAILURE_LIMIT + ").");
                    break;
                }
            } catch (Exception ex) {
                Log.error("Error occurred while getting confirmations "
                        + (extCall != null ? extCall.toHumanString() : ""), ex);
            }
        }

        Log.debug("Confirmation pooling finished.");
    }
}

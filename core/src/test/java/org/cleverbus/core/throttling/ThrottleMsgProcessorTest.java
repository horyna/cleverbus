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

package org.cleverbus.core.throttling;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Properties;

import org.cleverbus.api.entity.Message;
import org.cleverbus.api.exception.IntegrationException;
import org.cleverbus.api.exception.InternalErrorEnum;
import org.cleverbus.core.AbstractCoreTest;
import org.cleverbus.spi.throttling.ThrottleScope;
import org.cleverbus.spi.throttling.ThrottlingProcessor;
import org.cleverbus.test.ExternalSystemTestEnum;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;


/**
 * Test suite for {@link ThrottleProcessorImpl}.
 *
 * @author <a href="mailto:petr.juza@cleverlance.com">Petr Juza</a>
 */
public class ThrottleMsgProcessorTest extends AbstractCoreTest {

    @Produce(uri = "direct:start")
    private ProducerTemplate producer;

    @Autowired
    private ThrottlingProcessor throttlingProcessor;


    @Before
    public void prepareConfiguration() {
        // prepare properties
        String prefix = ThrottlingPropertiesConfiguration.PROPERTY_PREFIX;
        Properties props = new Properties();
        props.put(prefix + "*.sendSms", "2/10");
        props.put(prefix + "crm.createCustomer", "2/10");

        // create configuration
        ThrottlingPropertiesConfiguration conf = new ThrottlingPropertiesConfiguration(props);

        setPrivateField(throttlingProcessor, "configuration", conf);
    }

    @Test
    public void testSyncProcessor() throws Exception {
        RouteBuilder route = new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            ThrottleScope throttleScope = new ThrottleScope(ThrottleScope.ANY_SOURCE_SYSTEM, "sendSms");
                            throttlingProcessor.throttle(throttleScope);
                        }
                    });
            }
        };

        getCamelContext().addRoutes(route);

        producer.sendBody("something");
        producer.sendBody("something");

        try {
            producer.sendBody("something");
            fail();
        } catch (CamelExecutionException ex) {
            assertThat(ex.getCause(), instanceOf(IntegrationException.class));
            assertErrorCode(((IntegrationException)ex.getCause()).getError(), InternalErrorEnum.E114);
        }
    }

    @Test
    public void testSyncProcessorWithDefaults() throws Exception {
        // create configuration
        ThrottlingPropertiesConfiguration confDefaults = new ThrottlingPropertiesConfiguration(new Properties());

        setPrivateField(throttlingProcessor, "configuration", confDefaults);

        RouteBuilder route = new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            ThrottleScope throttleScope = new ThrottleScope(ThrottleScope.ANY_SOURCE_SYSTEM, "sendSms");
                            throttlingProcessor.throttle(throttleScope);
                        }
                    });
            }
        };

        getCamelContext().addRoutes(route);

        for (int i = 0; i < AbstractThrottlingConfiguration.DEFAULT_LIMIT; i++) {
            producer.sendBody("something");
        }

        try {
            producer.sendBody("something");
            fail();
        } catch (CamelExecutionException ex) {
            assertThat(ex.getCause(), instanceOf(IntegrationException.class));
            assertErrorCode(((IntegrationException)ex.getCause()).getError(), InternalErrorEnum.E114);
        }
    }

    @Test
    public void testAsyncProcessor() throws Exception {
        RouteBuilder route = new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            Message msg = exchange.getIn().getBody(Message.class);

                            Assert.notNull(msg, "the msg must not be null");

                            ThrottleScope throttleScope = new ThrottleScope(msg.getSourceSystem().getSystemName(),
                                    msg.getOperationName());
                            throttlingProcessor.throttle(throttleScope);
                        }
                    });
            }
        };

        getCamelContext().addRoutes(route);

        Message msg = new Message();
        msg.setSourceSystem(ExternalSystemTestEnum.CRM);
        msg.setOperationName("createCustomer");

        producer.sendBody(msg);
        producer.sendBody(msg);

        try {
            producer.sendBody(msg);
            fail();
        } catch (CamelExecutionException ex) {
            assertThat(ex.getCause(), instanceOf(IntegrationException.class));
            assertErrorCode(((IntegrationException)ex.getCause()).getError(), InternalErrorEnum.E114);
        }
    }
}

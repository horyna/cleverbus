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

package org.cleverbus.modules.in.hello;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.cleverbus.common.Tools;
import org.cleverbus.modules.AbstractModulesTest;
import org.cleverbus.modules.in.hello.model.SyncHelloResponse;
import org.cleverbus.test.ActiveRoutes;
import org.cleverbus.test.route.TestWsUriBuilder;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;


/**
 * Test suite for {@link SyncHelloRoute}.
 *
 * @author <a href="mailto:petr.juza@cleverlance.com">Petr Juza</a>
 */
@ActiveRoutes(classes = SyncHelloRoute.class)
public class SyncHelloRouteTest extends AbstractModulesTest {

    @Produce(uri = TestWsUriBuilder.URI_WS_IN + "syncHelloRequest")
    private ProducerTemplate producer;

    @EndpointInject(uri = "mock:test")
    private MockEndpoint mock;

    /**
     * Checking successful calling.
     */
    @Test
    public void testSyncHelloRoute() throws Exception {
        String xml = "<syncHelloRequest xmlns=\"http://cleverbus.org/ws/HelloService-v1\">"
                + "    <name>Mr. Parker</name>"
                + "</syncHelloRequest>";

        String output = producer.requestBody((Object)xml, String.class);
        SyncHelloResponse res = Tools.unmarshalFromXml(output, SyncHelloResponse.class);

        assertThat(res, notNullValue());
        assertThat(res.getGreeting(), is("Hello Mr. Parker"));
    }
}

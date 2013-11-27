/*
 * Copyright 2013 The Apache Software Foundation.
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
package org.apache.camel.processor.aggregate;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;

public class ConcatenationAggregationStrategyTest extends ContextTestSupport {

  public void testAggregate() throws Exception {
    context.setTracing(true);
    
    MockEndpoint mock = getMockEndpoint("mock:end");
    mock.expectedMessageCount(2);
    mock.expectedBodiesReceived("01234", "56789");

    for (int c = 0; c <= 10; c++) {
      template.sendBody("direct:start", String.valueOf(c));
    }

    assertMockEndpointsSatisfied();
  }

  public void testAggregateWithNewLine() throws Exception {
    context.setTracing(true);
    
    MockEndpoint mock = getMockEndpoint("mock:endWithNewLine");
    mock.expectedMessageCount(2);
    mock.expectedBodiesReceived("0\n1\n2\n3\n4", "5\n6\n7\n8\n9");

    for (int c = 0; c <= 10; c++) {
      template.sendBody("direct:startWithNewLine", String.valueOf(c));
    }

    assertMockEndpointsSatisfied();
  }

  @Override
  protected RouteBuilder createRouteBuilder() throws Exception {
    return new RouteBuilder() {
      @Override
      public void configure() throws Exception {
        from("direct:start")
                .aggregate(constant(true), new ConcatenationAggregationStrategy())
                .completionSize(5)
                .to("log:org.apache.camel.processor.aggregate")
                .to("mock:end");
        
        from("direct:startWithNewLine")
                .aggregate(constant(true), new ConcatenationAggregationStrategy("\n", "UTF-8"))
                .completionSize(5)
                .to("log:org.apache.camel.processor.aggregate")
                .to("mock:endWithNewLine");
      }
    };
  }
}

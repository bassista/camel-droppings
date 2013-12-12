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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.XZDataFormat;
import org.apache.camel.util.IOHelper;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;

public class XZDataFormatTest extends ContextTestSupport {

  protected String getPlainTextBody() {
    return "Hello world!";
  }
  
  protected byte[] getCompressedBody() throws IOException {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    XZCompressorOutputStream zout = new XZCompressorOutputStream(bout);
    try {
      zout.write(getPlainTextBody().getBytes("UTF-8"));
    } finally {
      IOHelper.close(zout);
    }
    return bout.toByteArray();
  }
  
  public void testMarshal() throws Exception {
    context.setTracing(true);
    
    String body = getPlainTextBody();
    byte[] expectedBody = getCompressedBody();
    
    MockEndpoint mock = getMockEndpoint("mock:endMarshal");
    mock.expectedBodiesReceived(expectedBody);

    template.sendBody("direct:startMarshal", body);

    assertMockEndpointsSatisfied();
  }
  
  public void testUnmarshal() throws Exception {
    context.setTracing(true);
    
    byte[] body = getCompressedBody();
    String expectedBody = getPlainTextBody();
    
    MockEndpoint mock = getMockEndpoint("mock:endUnmarshal");
    mock.expectedBodiesReceived(expectedBody);

    template.sendBody("direct:startUnmarshal", body);

    assertMockEndpointsSatisfied();
  }

  @Override
  protected RouteBuilder createRouteBuilder() throws Exception {
    return new RouteBuilder() {
      @Override
      public void configure() throws Exception {
        from("direct:startMarshal")
          .marshal(new XZDataFormat())
          //.to("file:///tmp/?fileName=test.xz")
          .to("mock:endMarshal");

        from("direct:startUnmarshal")
          .unmarshal(new XZDataFormat())
          .to("mock:endUnmarshal");
      }
    };
  }
}

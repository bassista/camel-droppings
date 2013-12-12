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
package org.apache.camel.impl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.util.IOHelper;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;

public class XZDataFormat implements DataFormat {

  @Override
  public void marshal(Exchange exchange, Object graph, OutputStream stream) throws Exception {
    InputStream is = exchange.getContext().getTypeConverter().mandatoryConvertTo(InputStream.class, exchange, graph);

    XZCompressorOutputStream zipOutput = new XZCompressorOutputStream(stream);
    try {
      IOHelper.copy(is, zipOutput);
    } finally {
      IOHelper.close(is, zipOutput);
    }
  }

  @Override
  public Object unmarshal(Exchange exchange, InputStream stream) throws Exception {
    InputStream is = exchange.getIn().getMandatoryBody(InputStream.class);
    XZCompressorInputStream unzipInput = null;

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      unzipInput = new XZCompressorInputStream(is);
      IOHelper.copy(unzipInput, bos);
      return bos.toByteArray();
    } finally {
      IOHelper.close(unzipInput, is);
    }
  }
}

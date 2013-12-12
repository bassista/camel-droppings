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
import java.util.List;
import org.apache.camel.Exchange;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

public class TarAggregationStrategy extends GroupedExchangeAggregationStrategy {

  public static final String TAR_ENTRY_NAME = "CamelTarEntryName";
  
  @Override
  public void onCompletion(Exchange exchange) {
    List<Exchange> list = exchange.getProperty(Exchange.GROUPED_EXCHANGE, List.class);
    try {
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      TarArchiveOutputStream tout = new TarArchiveOutputStream(bout);
      for (Exchange item : list) {
        String name = item.getProperty(TAR_ENTRY_NAME, item.getProperty(Exchange.FILE_NAME, item.getExchangeId(), String.class), String.class);
        byte[] body = item.getIn().getBody(byte[].class);
        TarArchiveEntry entry = new TarArchiveEntry(name);
        entry.setSize(body.length);
        tout.putArchiveEntry(entry);
        tout.write(body);
        tout.closeArchiveEntry();
      }
      tout.close();
      exchange.getIn().setBody(bout.toByteArray());
      exchange.removeProperty(Exchange.GROUPED_EXCHANGE);
    } catch (Exception e) {
      throw new RuntimeException("Unable to tar exchanges!", e);
    }
  }
}

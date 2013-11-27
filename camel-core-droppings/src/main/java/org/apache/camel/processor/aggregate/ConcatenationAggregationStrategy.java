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

import java.nio.ByteBuffer;
import org.apache.camel.Exchange;

public class ConcatenationAggregationStrategy implements AggregationStrategy {

  public static final String DEFAULT_DELIMITER_ENCODING = "UTF-8";
  
  private String delimiter;
  private String delimiterEncoding;

  public ConcatenationAggregationStrategy() {
  }

  public ConcatenationAggregationStrategy(String delimiter, String delimiterEncoding) {
    this.delimiter = delimiter;
    this.delimiterEncoding = delimiterEncoding;
  }

  public String getDelimiter() {
    return delimiter;
  }

  public void setDelimiter(String delimiter) {
    this.delimiter = delimiter;
  }

  public String getDelimiterEncoding() {
    if (delimiterEncoding == null) {
      delimiterEncoding = DEFAULT_DELIMITER_ENCODING;
    }
    return delimiterEncoding;
  }

  public void setDelimiterEncoding(String delimiterEncoding) {
    this.delimiterEncoding = delimiterEncoding;
  }
  
  @Override
  public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
    if (oldExchange == null) {
      oldExchange = newExchange;
    } else {
      try {
        byte[] oldBody = oldExchange.getIn().getBody(byte[].class);
        byte[] newBody = newExchange.getIn().getBody(byte[].class);
        ByteBuffer concatBody = ByteBuffer.allocate(oldBody.length + ((delimiter != null) ? delimiter.getBytes(getDelimiterEncoding()).length : 0) + newBody.length);
        concatBody.put(oldBody);
        if (delimiter != null) {
          concatBody.put(delimiter.getBytes(getDelimiterEncoding()));
        }
        concatBody.put(newBody);
        oldExchange.getIn().setBody(concatBody.array());
      } catch (Exception e) {
        throw new RuntimeException("Unable to concatenate exchanges!", e);
      }
    }

    return oldExchange;
  }
}

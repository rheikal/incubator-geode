/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gemstone.gemfire.security;

import java.io.Serializable;

import org.apache.geode.security.PostProcessor;

public class NoShowValue1PostProcessor implements PostProcessor {

  @Override
  public Object processRegionValue(final Serializable principal,
                                   final String regionName,
                                   final Object key,
                                   final Object value) {
    if (value.equals("value1")) {
      return null;
    } else {
      return value;
    }
  }
}

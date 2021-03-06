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

package org.cleverbus.core.common.spring;

import javax.xml.namespace.QName;

import org.springframework.core.convert.converter.Converter;


/**
 * Converts String to {@link QName} via {@link QName#valueOf(String)}.
 */
public class SpringQNameConverter implements Converter<String, QName> {

    @Override
    public QName convert(String source) {
        return QName.valueOf(source);
    }
}

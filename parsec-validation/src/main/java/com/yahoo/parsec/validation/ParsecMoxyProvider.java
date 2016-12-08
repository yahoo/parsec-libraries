// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.validation;

import org.glassfish.jersey.moxy.json.internal.ConfigurableMoxyJsonProvider;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Customize moxy provider, add ValidationEventHandler for error handling.
 */
@Provider
public class ParsecMoxyProvider extends ConfigurableMoxyJsonProvider {
    @Override
    protected void preReadFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                               MultivaluedMap<String, String> httpHeaders, Unmarshaller unmarshaller)
            throws JAXBException {
        super.preReadFrom(type, genericType, annotations, mediaType, httpHeaders, unmarshaller);
        unmarshaller.setEventHandler(new ParsecValidationEventHandler());
    }
}


// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.validation;

import org.glassfish.jersey.internal.spi.AutoDiscoverable;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;

import javax.ws.rs.Priorities;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.FeatureContext;

/**
 * Created by guang001 on 6/24/15.
 */
public class ParsecValidationAutoDiscoverable implements AutoDiscoverable {

    @Override
    public void configure(FeatureContext context) {
        final int priorityInc = 3000;

        // Jersey MOXY provider have higher priority(7000), so we need set higher than it
        int priority = Priorities.USER + priorityInc;
        Configuration config = context.getConfiguration();

        if (!config.isRegistered(ParsecValidationExceptionMapper.class)) {
            context.register(ParsecValidationExceptionMapper.class, priority);
        }
        if (!config.isRegistered(ValidationConfigurationContextResolver.class)) {
            context.register(ValidationConfigurationContextResolver.class, priority);
        }
        if (!config.isRegistered(ParsecMoxyFeature.class)) {
            context.register(ParsecMoxyFeature.class, priority);
        }
        if (!config.isRegistered(JaxbExceptionMapper.class)) {
            context.register(JaxbExceptionMapper.class, priority);
        }
    }
}

// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.validation;

import com.thoughtworks.paranamer.AdaptiveParanamer;
import com.thoughtworks.paranamer.AnnotationParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import org.glassfish.jersey.server.validation.ValidationConfig;
import org.glassfish.jersey.server.validation.internal.InjectingConstraintValidatorFactory;
import org.hibernate.validator.parameternameprovider.ParanamerParameterNameProvider;

import javax.inject.Named;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.ContextResolver;
import java.lang.annotation.Annotation;


/**
 * Created by hankting on 6/22/15.
 */
public class ValidationConfigurationContextResolver implements ContextResolver<ValidationConfig> {

    /** the resource context. */
    @Context
    private ResourceContext resourceContext;

    /** Get a context. */
    @Override
    public ValidationConfig getContext(Class<?> type) {
        final ValidationConfig config = new ValidationConfig();
        config.constraintValidatorFactory(resourceContext.getResource(InjectingConstraintValidatorFactory.class));
        CachingParanamer paranamer = new CachingParanamer(new CustomAnnotationParanamer());
        config.parameterNameProvider(new ParanamerParameterNameProvider(paranamer));
        return config;
    }


    /**
     * A custom annotation based {@code Paranamer} implementation using the {@code Named} annotation.
     */
    private static class CustomAnnotationParanamer extends AnnotationParanamer {

        /**
         * default constructor.
         *
         * set failback paranamer as AdaptiveParanamer which can resolve error path by argument variable name
         */
        public CustomAnnotationParanamer() {
            super(new AdaptiveParanamer());
        }

        @Override
        protected boolean isNamed(Annotation annotation) {
            return Named.class == annotation.annotationType();
        }

        @Override
        protected String getNamedValue(Annotation annotation) {
            return ((Named) annotation).value();
        }
    }
}

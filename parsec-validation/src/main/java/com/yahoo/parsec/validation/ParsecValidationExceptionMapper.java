// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.validation;


import org.glassfish.jersey.server.validation.ValidationError;
import org.glassfish.jersey.server.validation.internal.LocalizationMessages;
import org.glassfish.jersey.server.validation.internal.ValidationExceptionMapper;
import org.glassfish.jersey.server.validation.internal.ValidationHelper;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ParsecValidationExceptionMapper.
 */
public class ParsecValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    /** the logger. */
    private static final Logger LOGGER = Logger.getLogger(ValidationExceptionMapper.class.getName());
    /** the default message. */
    private static final String DEFAULT_MSG = "constraint violation validate error";
    /** the default code. */
    private static final int DEFAULT_CODE = 0;

    /** the default error code property. */
    public static final String PROP_VALIDATION_DEFAULT_ERROR_CODE = "parsec.config.validation.defaultErrorCode";
    /** the default error msg property. */
    public static final String PROP_VALIDATION_DEFAULT_ERROR_MSG = "parsec.config.validation.defaultErrorMsg";

    /** the config. */
    @Context
    private Configuration config;

    /**
     * implement toResponse.
     *
     * @param e the validation exception object
     *
     * @return the response
     */
    @Override
    public Response toResponse(ConstraintViolationException e) {
        int code = DEFAULT_CODE;
        String message = DEFAULT_MSG;
        if (this.config != null) {
            Object propErrorCode = this.config.getProperty(PROP_VALIDATION_DEFAULT_ERROR_CODE);
            if (propErrorCode != null) {
                code = Integer.valueOf(propErrorCode.toString());
            }
            Object propErrorMsg = this.config.getProperty(PROP_VALIDATION_DEFAULT_ERROR_MSG);
            if (propErrorMsg != null) {
                message = propErrorMsg.toString();
            }
        }

        return toResponse(e, code, message);
    }

    /**
     * implement toResponse with code, message.
     *
     * @param e the validation exception object
     * @param code the code
     * @param message the message
     *
     * @return the response
     */
    private Response toResponse(ConstraintViolationException e, int code, String message) {
        LOGGER.log(Level.FINER, LocalizationMessages.CONSTRAINT_VIOLATIONS_ENCOUNTERED(), e);
        List<ValidationError> errors = ValidationHelper.constraintViolationToValidationErrors(e);
        ParsecErrorResponse<ValidationError> errorResponse = ValidateUtil.buildErrorResponse(errors, code, message);
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new GenericEntity<ParsecErrorResponse<ValidationError>>(errorResponse) { }).build();
    }
}

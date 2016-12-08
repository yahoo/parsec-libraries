// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.validation;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guang001 on 11/5/15.
 */
public class JaxbExceptionMapper implements ExceptionMapper<JaxbException> {

    /** the default error code property. */
    public static final String PROP_JAXB_DEFAULT_ERROR_CODE = "parsec.config.jaxb.defaultErrorCode";
    /** the default error msg property. */
    public static final String PROP_JAXB_DEFAULT_ERROR_MSG = "parsec.config.jaxb.defaultErrorMsg";

    /** the config. */
    @Context
    private Configuration config;

    @Override
    public Response toResponse(JaxbException exception) {
        int errorCode = 1;
        String errorMsg = "JAXB convert/validate error, please check your input data";

        if (config != null) {
            Object propErrorCode = config.getProperty(PROP_JAXB_DEFAULT_ERROR_CODE);
            Object propErrorMsg = config.getProperty(PROP_JAXB_DEFAULT_ERROR_MSG);
            if (propErrorCode != null) {
                errorCode = Integer.valueOf(propErrorCode.toString());
            }
            if (propErrorMsg != null) {
                errorMsg = propErrorMsg.toString();
            }
        }

        List<JaxbError> errors = new ArrayList<>();
        errors.add(new JaxbError(exception.getMessage()));
        ParsecErrorResponse<JaxbError> errorResponse = ValidateUtil.buildErrorResponse(errors, errorCode, errorMsg);
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new GenericEntity<ParsecErrorResponse<JaxbError>>(errorResponse) { }).build();
    }
}

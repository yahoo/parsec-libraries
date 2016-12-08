// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.validation;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

/**
 * Created by guang001 on 11/5/15.
 */
public class ParsecValidationEventHandler implements ValidationEventHandler {
    @Override
    public boolean handleEvent(ValidationEvent event) {
        if (event.getSeverity() > ValidationEvent.WARNING) {
            String message = event.getMessage().replace("\n", " ").trim();
            throw new JaxbException(message);
        }
        return true;
    }
}

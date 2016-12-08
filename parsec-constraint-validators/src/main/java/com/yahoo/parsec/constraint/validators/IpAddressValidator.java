// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.constraint.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * the (@link IpAddress} annotation implementation.
 */
public class IpAddressValidator implements ConstraintValidator<IpAddress, String> {

    @Override
    public void initialize(IpAddress constraintAnnotation) {
        // this is an empty method
    }

    @Override
    public boolean isValid(String input, ConstraintValidatorContext context) {
        boolean isValid = false;

        try {
            InetAddress.getByName(input);
            isValid = true;
        } catch (UnknownHostException e) {
            isValid = false;
        }

        return isValid;
    }
}

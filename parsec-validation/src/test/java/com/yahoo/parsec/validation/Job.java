// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.validation;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Enum type for test.
 */
@XmlRootElement
public enum Job {
    STUDENT,
    ENGINEER,
    PRODUCER,
    TEACHER,
    OTHER
}

// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.validation.annotations;

/**
 * Created by guang001 on 6/22/15.
 */
public enum Filter {
    /**
     * value using getStripped function.
     */
    STRIPPED,

    /**
     * value using getCooked function.
     */
    COOKED,

    /**
     * value using getHtml function.
     */
    HTML,

    /**
     * value using getEmail function.
     */
    EMAIL,

    /**
     * value using getUrl function.
     */
    URL,

    /**
     * value using getNumber function.
     */
    NUMBER
}

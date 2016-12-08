// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.validation.annotations;

import java.lang.annotation.*;

/**
 * @author sho
 */
@Documented
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Yiv {

    /**
     * Filter type.
     */
    Filter value() default Filter.HTML;

    /**
     * The list of groups this class/method belongs to.
     */
    String[] groups() default {};

    /**
     * Flags.
     */
    int flags() default 0;

    /**
     * Subset.
     */
    String subset() default "YIV_HTML_NO_TAGS";

    /**
     * Charset.
     */
    String charset() default "utf-8";

    /**
     * Defines several {@code @Yiv} annotations on the same element.
     */
    @Target({ ElementType.FIELD })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface List {
        /**
         * Yiv.
         */
        Yiv[] value();
    }

}

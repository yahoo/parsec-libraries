// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.validation;

import javax.validation.constraints.NotNull;

/**
 * Created by hankting on 6/24/15.
 */
public class MySubDto {
    private String birthday;
    private String nickname;

    public void setBirthday(String bd) {
        this.birthday = bd;
    }

    @NotNull
    public String getBirthday() {
        return this.birthday;
    }

    public void setNickname(String nn) {
        this.nickname = nn;
    }

    @NotNull
    public String getNickname() {
        return this.nickname;
    }
}

// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.validation;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by hankting on 6/22/15.
 */
@XmlRootElement
public class MyDto {

    private String firstName;

    private String lastName;

    private MySubDto subClass;

    private Integer age;

    public void setFirstName(String name) {
        this.firstName = name;
    }

    @NotNull
    public String getFirstName() {
        return this.firstName;
    }

    public void setLastName(String name) {
        this.lastName = name;
    }

    @NotNull
    public String getLastName() {
        return this.lastName;
    }

    public void setSubClass(MySubDto sc) {
        this.subClass = sc;
    }

    @NotNull
    @Valid
    public MySubDto getSubClass() {
        return this.subClass;
    }

    public Integer getAge() {
        return this.age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}

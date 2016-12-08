// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.validation;

/**
 * Created by hankting on 6/23/15.
 */

import javax.inject.Named;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("/")
public class MyResource {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @Path("myresource/{id1}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String getIt(
            @Named("namedValue1") @NotNull @Size(min=5,max=10,message="${validatedValue} min {min}") @PathParam("id1") String value1,
            @Named("namedValue2") @NotNull @Size(min=2,max=10) @QueryParam("key1") String value2,
            @NotNull @Min(1) @QueryParam("key2") Integer value3
    ) {
        return "OK-" + value1 + "-" + value2 + "-" + value3.toString();
    }

    @Path("myresource/{id1}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String postIt(
            @NotNull @Size(min=5,max=10,message="'${validatedValue}' min {min}") @PathParam("id1") String value1,
            @Valid MyDto dto
    ) {
        return "OK-" + value1;
    }

    @Path("myresource/jobs")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Job postJob(Job job) {
        return job;
    }

    @Path("myresource/students")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public StudentDto postJob(StudentDto student) {
        return student;
    }

    @Path("myresource/students/{name}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public StudentDto getStudentByName(@PathParam("name") String name, @QueryParam("age") int age) {
        StudentDto dto = new StudentDto();
        dto.setName(name);
        dto.setAge(age);
        return dto;
    }

}


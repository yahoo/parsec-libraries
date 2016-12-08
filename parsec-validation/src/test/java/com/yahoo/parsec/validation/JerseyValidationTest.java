// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.validation;

import org.eclipse.persistence.jaxb.BeanValidationMode;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.moxy.json.MoxyJsonConfig;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.validation.ValidationError;
import org.glassfish.jersey.test.JerseyTestNg;
import org.testng.annotations.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static javax.ws.rs.core.Response.Status.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;

public class JerseyValidationTest extends JerseyTestNg.ContainerPerClassTest {

    final private int VALIDATION_ERROR_CODE = 40001;
    final private String VALIDATION_ERROR_MSG = "constraint violation validate error unittest";
    final private int JAXB_ERROR_CODE = 40002;
    final private String JAXB_ERROR_MSG = "JAXB convert/validate error, please check your input data(unittest)";

    @Override
    protected Application configure() {
        //enable(TestProperties.LOG_TRAFFIC);
        //enable(TestProperties.DUMP_ENTITY);
        //
        // TODO: load web.xml directly
        // .property(
        //        "contextConfigLocation",
        //        "classpath:**/my-web-test-context.xml"
        //
        return new ResourceConfig(MyResource.class)
                .register(ParsecMoxyProvider.class)
                .register(JaxbExceptionMapper.class)
                .property(JaxbExceptionMapper.PROP_JAXB_DEFAULT_ERROR_CODE, JAXB_ERROR_CODE)
                .property(JaxbExceptionMapper.PROP_JAXB_DEFAULT_ERROR_MSG, JAXB_ERROR_MSG)
                .register(ValidationConfigurationContextResolver.class)
                .register(ParsecValidationExceptionMapper.class)
                .property(ParsecValidationExceptionMapper.PROP_VALIDATION_DEFAULT_ERROR_CODE, VALIDATION_ERROR_CODE)
                .property(ParsecValidationExceptionMapper.PROP_VALIDATION_DEFAULT_ERROR_MSG, VALIDATION_ERROR_MSG)
                .property(ServerProperties.METAINF_SERVICES_LOOKUP_DISABLE, true)
                .register(new MoxyJsonConfig().setFormattedOutput(true)
                        .property(MarshallerProperties.BEAN_VALIDATION_MODE, BeanValidationMode.NONE).resolver());

    }

    @Override
    protected void configureClient(final ClientConfig config) {
        super.configureClient(config);
        config.register(MoxyJsonFeature.class)
              .property(ClientProperties.METAINF_SERVICES_LOOKUP_DISABLE, true)
              .register(new MoxyJsonConfig().setFormattedOutput(true)
                      // Turn off BV otherwise the entities on server would be validated by MOXy as well.
                      .property(MarshallerProperties.BEAN_VALIDATION_MODE, BeanValidationMode.NONE).resolver());
    }


    /**
     * Test to see that the message "Got it!" is sent in the response.
     */
    @Test
    public void testGetItValidationError() {
        Response resp = target().path("/myresource/id")
                .queryParam("key1", "2")
                .queryParam("key2", "")
                .request(MediaType.APPLICATION_JSON_TYPE).get();

        int code = resp.getStatus();
        ParsecErrorResponse err = resp.readEntity(new GenericType<ParsecErrorResponse<ValidationError>>() {
        });

        assertEquals(code, BAD_REQUEST.getStatusCode());
        assertValidationErrorEquals(
                err,
                40001,
                "constraint violation validate error unittest",
                Arrays.asList("MyResource.getIt.namedValue1", "MyResource.getIt.namedValue2", "MyResource.getIt.value3")
        );
    }

    /**
     * assert parsec error response
     */
    private void assertValidationErrorEquals(
            ParsecErrorResponse parsecError, int expectErrCode, String expectMessage, List<String> expectPaths) {
        assertEquals(parsecError.getError().getCode(), expectErrCode, "error code not equals");
        assertEquals(parsecError.getError().getMessage(), expectMessage, "error message not equals");
        assertEquals(parsecError.getError().getDetail().size(), expectPaths.size(), "error detail count not equals");

        List<String> errPaths = new ArrayList<>();
        for (Object obj : parsecError.getError().getDetail()){
            errPaths.add(((ValidationError) obj).getPath());
        }
        assertEqualsNoOrder(errPaths.toArray(), expectPaths.toArray(), "error paths not equals");
    }

    /**
     * Test to see that the message "Got it!" is sent in the response.
     */
    @Test
    public void testGetItSuccess() {
        Response resp = target().path("/myresource/id12345")
                .queryParam("key1", "key1")
                .queryParam("key2", "5")
                .request(MediaType.APPLICATION_JSON_TYPE).get();

        String responseMsg = resp.readEntity(String.class);
        int code = resp.getStatus();

        assertEquals(code, OK.getStatusCode());
        assertEquals(responseMsg, "OK-id12345-key1-5");
    }

    @Test
    public void testPostItValidationError() {
        MyDto dto = new MyDto();
        MySubDto subDto = new MySubDto();
        dto.setSubClass(subDto);
        Response resp = target().path("/myresource/id")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.json(dto));

        int code = resp.getStatus();
        ParsecErrorResponse err = resp.readEntity(new GenericType<ParsecErrorResponse<ValidationError>>(){});

        assertEquals(code, BAD_REQUEST.getStatusCode());
        assertValidationErrorEquals(
                err,
                40001,
                "constraint violation validate error unittest",
                Arrays.asList("MyResource.postIt.dto.subClass.nickname", "MyResource.postIt.dto.firstName",
                              "MyResource.postIt.value1", "MyResource.postIt.dto.lastName", "MyResource.postIt.dto.subClass.birthday")
        );
    }

    @Test
    public void testPostItSuccess() {
        MySubDto subDto = new MySubDto();
        subDto.setBirthday("bd");
        subDto.setNickname("nn");
        MyDto dto = new MyDto();
        dto.setLastName("ln");
        dto.setFirstName("fn");
        dto.setSubClass(subDto);

        Response resp = target().path("/myresource/id12345")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.json(dto));

        String responseMsg = resp.readEntity(String.class);
        int code = resp.getStatus();

        assertEquals(code, OK.getStatusCode());
        assertEquals(responseMsg, "OK-id12345");
    }

    @Test
    public void testPostInvalidEnum() {
        Response resp = target().path("/myresource/jobs")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity("{\"value\":\"xxx\"}", MediaType.APPLICATION_JSON_TYPE));

        ParsecErrorResponse error = resp.readEntity(new GenericType<ParsecErrorResponse<JaxbError>>(){});

        assertEquals(resp.getStatus(), BAD_REQUEST.getStatusCode());
        assertEquals(error.getError().getCode(), JAXB_ERROR_CODE);
        assertEquals(error.getError().getMessage(), JAXB_ERROR_MSG);
    }

    @Test
    public void testPostInvalidEnum2() {
        Response resp = target().path("/myresource/jobs")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity("{\"abc\":\"def\"}", MediaType.APPLICATION_JSON_TYPE));

        ParsecErrorResponse error = resp.readEntity(new GenericType<ParsecErrorResponse<JaxbError>>(){});

        assertEquals(resp.getStatus(), BAD_REQUEST.getStatusCode());
        assertEquals(error.getError().getCode(), JAXB_ERROR_CODE);
        assertEquals(error.getError().getMessage(), JAXB_ERROR_MSG);
    }

    @Test
    public void testPostInvalidType() {
        // note: age is number but supply as string
        Response resp = target().path("/myresource/students")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity("{\"age\":\"xxx\"}", MediaType.APPLICATION_JSON_TYPE));
        ParsecErrorResponse error = resp.readEntity(new GenericType<ParsecErrorResponse<JaxbError>>(){});

        assertEquals(resp.getStatus(), BAD_REQUEST.getStatusCode());
        assertEquals(error.getError().getCode(), JAXB_ERROR_CODE);
        assertEquals(error.getError().getMessage(), JAXB_ERROR_MSG);
    }

    @Test
    public void testGetWithInvalidType() {
        // note: age is number but supply as string
        Response resp = target().path("/myresource/students/hankting")
                .queryParam("age", "invalidTypeAge")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();

        assertEquals(resp.getStatus(), NOT_FOUND.getStatusCode());
    }
}

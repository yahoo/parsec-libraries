// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import com.ning.http.client.ProxyServer;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.multipart.ByteArrayPart;
import com.ning.http.client.multipart.FilePart;
import com.ning.http.client.multipart.Part;
import com.ning.http.client.multipart.StringPart;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertTrue;

/**
 * @author sho
 */
public class ParsecEqualsUtilTest {

    @Test
    public void testPartEquals() throws Exception {
        Assert.assertTrue(
            ParsecEqualsUtil.partEquals(
                new ByteArrayPart("abc", new byte[] {1,2,3}), new ByteArrayPart("abc", new byte[] {1,2,3})
            )
        );

        Assert.assertFalse(
            ParsecEqualsUtil.partEquals(
                new ByteArrayPart("abc", new byte[] {1,2,3}), new ByteArrayPart("abc", new byte[] {4,5,6})
            )
        );

        File file1 = new File("src/test/java/com/yahoo/parsec/clients/ParsecEqualsUtilTest.java"),
            file2 = new File("src/test/java/com/yahoo/parsec/clients/ParsecAsyncHttpRequestTest.java");

        Assert.assertTrue(ParsecEqualsUtil.partEquals(new FilePart("abc", file1), new FilePart("abc", file1)));
        Assert.assertFalse(ParsecEqualsUtil.partEquals(new FilePart("abc", file1), new FilePart("def", file1)));
        Assert.assertFalse(ParsecEqualsUtil.partEquals(new FilePart("abc", file1), new FilePart("abc", file2)));
        Assert.assertTrue(ParsecEqualsUtil.partEquals(new StringPart("abc", "def"), new StringPart("abc", "def")));
        Assert.assertFalse(ParsecEqualsUtil.partEquals(new StringPart("abc", "def"), new StringPart("def", "def")));
        Assert.assertFalse(ParsecEqualsUtil.partEquals(new StringPart("abc", "def"), new StringPart("abc", "abc")));
    }

    @Test
    public void testPartListEquals() throws Exception {
        List<Part> partList1 = Arrays.asList(
            new ByteArrayPart("abc", new byte[] {1,2,3}),
            new FilePart("abc", new File("src/test/java/com/yahoo/parsec/clients/ParsecEqualsUtilTest.java")),
            new StringPart("abc", "def")
        );

        List<Part> partList2 = new ArrayList<>();
        partList2.addAll(partList1);
        Assert.assertTrue(ParsecEqualsUtil.partListEquals(partList1, partList2));

        partList2.add(new StringPart("def", "def"));
        Assert.assertFalse(ParsecEqualsUtil.partListEquals(partList1, partList2));

        partList2.remove(1);
        Assert.assertFalse(ParsecEqualsUtil.partListEquals(partList1, partList2));
    }

    @Test
    public void testByteArrayListEquals() throws Exception {
        List<byte[]> byteArryList1 = Arrays.asList(
            new byte[] {1,2,3},
            new byte[] {4,5,6}
        );

        List<byte[]> byteArrayList2 = new ArrayList<>();
        byteArrayList2.addAll(byteArryList1);
        Assert.assertTrue(ParsecEqualsUtil.byteArrayListEquals(byteArryList1, byteArrayList2));

        byteArrayList2.add(new byte[] {7,8,9});
        Assert.assertFalse(ParsecEqualsUtil.byteArrayListEquals(byteArryList1, byteArrayList2));

        byteArrayList2.remove(1);
        Assert.assertFalse(ParsecEqualsUtil.byteArrayListEquals(byteArryList1, byteArrayList2));
    }

    @Test
    public void testToStringEquals() throws Exception {
        Assert.assertTrue(
            ParsecEqualsUtil.toStringEquals(
                new ProxyServer("localhost", 4080), new ProxyServer("localhost", 4080)
            )
        );

        Assert.assertFalse(
            ParsecEqualsUtil.toStringEquals(
                new ProxyServer("localhost", 4080), new ProxyServer("localhost", 8080)
            )
        );
    }

    @Test
    public void testNingRequestEquals() throws Exception {
        Assert.assertTrue(
            ParsecEqualsUtil.ningRequestEquals(
                new RequestBuilder().setUrl("http://localhost:4080").build(),
                new RequestBuilder().setUrl("http://localhost:4080").build()
            )
        );

        Assert.assertFalse(
            ParsecEqualsUtil.ningRequestEquals(
                new RequestBuilder().setUrl("http://localhost:4080")
                    .setBody(Arrays.asList(new byte[] {1,2,3}))
                    .build(),
                new RequestBuilder().setUrl("http://localhost:4080")
                    .build()
            )
        );

        Assert.assertFalse(
            ParsecEqualsUtil.ningRequestEquals(
                new RequestBuilder().setUrl("http://localhost:4080")
                    .addBodyPart(new StringPart("abc", "def"))
                    .build(),
                new RequestBuilder().setUrl("http://localhost:4080")
                    .build()
            )
        );

        Assert.assertFalse(
            ParsecEqualsUtil.ningRequestEquals(
                new RequestBuilder().setUrl("http://localhost:4080")
                    .setProxyServer(new ProxyServer("localhost", 8080))
                    .build(),
                new RequestBuilder().setUrl("http://localhost:4080")
                    .build()
            )
        );

        Assert.assertFalse(
            ParsecEqualsUtil.ningRequestEquals(
                new RequestBuilder().setUrl("http://localhost:4080")
                    .build(),
                new RequestBuilder().setUrl("http://localhost:4080")
                    .setBody(Arrays.asList(new byte[] {1,2,3}))
                    .build()
            )
        );

        Assert.assertFalse(
            ParsecEqualsUtil.ningRequestEquals(
                new RequestBuilder().setUrl("http://localhost:4080")
                    .build(),
                new RequestBuilder().setUrl("http://localhost:4080")
                    .addBodyPart(new StringPart("abc", "def"))
                    .build()
            )
        );

        Assert.assertFalse(
            ParsecEqualsUtil.ningRequestEquals(
                new RequestBuilder().setUrl("http://localhost:4080")
                    .build(),
                new RequestBuilder().setUrl("http://localhost:4080")
                    .setProxyServer(new ProxyServer("localhost", 8080))
                    .build()
            )
        );
    }

    @Test
    public void testPrivateConstructor() throws Exception {
        Constructor constructor = ParsecEqualsUtil.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));

        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
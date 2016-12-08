// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.clients;

import com.ning.http.client.Request;
import com.ning.http.client.multipart.ByteArrayPart;
import com.ning.http.client.multipart.FilePart;
import com.ning.http.client.multipart.Part;
import com.ning.http.client.multipart.StringPart;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class that helps compare objects by value.
 *
 * @author sho
 */
@SuppressWarnings("PMD.CompareObjectsWithEquals")
final class ParsecEqualsUtil {
    /**
     * Unused private constructor.
     */
    private ParsecEqualsUtil() {

    }

    /**
     * Ning request equals.
     *
     * @param lhs lhs
     * @param rhs rhs
     *
     * @return true when two Ning Request are equal by value
     */
    static boolean ningRequestEquals(final Request lhs, final Request rhs) {
        if (!toStringEquals(lhs.getProxyServer(), rhs.getProxyServer())) {
            return false;
        }

        if (!byteArrayListEquals(lhs.getCompositeByteData(), rhs.getCompositeByteData())) {
            return false;
        }

        if (!partListEquals(lhs.getParts(), rhs.getParts())) {
            return false;
        }

        return new EqualsBuilder()
            .append(lhs.getBodyEncoding(), rhs.getBodyEncoding())
            .append(lhs.getByteData(), rhs.getByteData())
            .append(lhs.getContentLength(), rhs.getContentLength())
            .append(lhs.getFollowRedirect(), rhs.getFollowRedirect())
            .append(lhs.getMethod(), rhs.getMethod())
            .append(lhs.getRangeOffset(), rhs.getRangeOffset())
            .append(lhs.getRequestTimeout(), rhs.getRequestTimeout())
            .append(lhs.getStringData(), rhs.getStringData())
            .append(lhs.getUrl(), rhs.getUrl())
            .append(lhs.getVirtualHost(), rhs.getVirtualHost())
            .isEquals();
    }

    /**
     * ToString equals.
     *
     * @param lhs lhs
     * @param rhs rhs
     *
     * @return true when two objects are equal by toString value
     */
    static boolean toStringEquals(final Object lhs, final Object rhs) {
        if (lhs != rhs && (lhs == null || rhs == null || !lhs.toString().equals(rhs.toString()))) {
            return false;
        }
        return true;
    }

    /**
     * Byte array list equals.
     *
     * @param lhs lhs
     * @param rhs rhs
     *
     * @return true when two byte array list are equal by value
     */
    static boolean byteArrayListEquals(final List<byte[]> lhs, final List<byte[]> rhs) {
        if (lhs != rhs) {
            if (lhs == null || rhs == null || lhs.size() != rhs.size()) {
                return false;
            }

            for (int i = 0; i < lhs.size(); i++) {
                if (!Arrays.equals(lhs.get(i), rhs.get(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Ning Part list equals.
     *
     * @param lhs lhs
     * @param rhs rhs
     *
     * @return true when two Ning Part list are equal by value
     */
    static boolean partListEquals(final List<Part> lhs, final List<Part> rhs) {
        if (lhs != rhs) {
            if (lhs == null || rhs == null || lhs.size() != rhs.size()) {
                return false;
            }

            for (int i = 0; i < lhs.size(); i++) {
                if (!partEquals(lhs.get(i), rhs.get(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Ning Part equals.
     *
     * @param lhs lhs
     * @param rhs rhs
     *
     * @return true when two Ning Part are equal by value
     */
    static boolean partEquals(final Part lhs, final Part rhs) {
        if (!lhs.equals(rhs)) {
            if (lhs.getClass() != rhs.getClass()) {
                return false;
            }

            if (!new EqualsBuilder()
                .append(lhs.getCharset(), rhs.getCharset())
                .append(lhs.getContentId(), rhs.getContentId())
                .append(lhs.getContentType(), rhs.getContentType())
                .append(lhs.getDispositionType(), rhs.getDispositionType())
                .append(lhs.getName(), rhs.getName())
                .append(lhs.getTransferEncoding(), rhs.getTransferEncoding())
                .isEquals()) {
                return false;
            }

            switch (lhs.getClass().getSimpleName()) {
                case "ByteArrayPart":
                    if (!Arrays.equals(((ByteArrayPart) lhs).getBytes(), ((ByteArrayPart) rhs).getBytes())) {
                        return false;
                    }
                    break;
                case "FilePart":
                    if (!((FilePart) lhs).getFile().equals(((FilePart) rhs).getFile())) {
                        return false;
                    }
                    break;
                case "StringPart":
                    if (!((StringPart) lhs).getValue().equals(((StringPart) rhs).getValue())) {
                        return false;
                    }
                    break;
                default:
                    return false;
            }
        }
        return true;
    }
}

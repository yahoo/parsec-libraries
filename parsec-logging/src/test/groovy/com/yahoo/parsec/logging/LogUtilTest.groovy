// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

package com.yahoo.parsec.logging


import spock.lang.Specification


/**
 * @author lightyear
 */
class LogUtilTest extends Specification {

    def "test getLogger should return logger object for ParsecLog class"() {
        when:
        def result = LogUtil.getLogger()

        then:
        result.getName() == "com.yahoo.parsec.logging.LogUtil"
    }

    def "test generateLog with simple inputs"() {
        when:
        def result = LogUtil.generateLog("tag", "msg", [a: "b"], [c: "d"]);

        then:
        result.matches(/.* timestamp=".+", log_tag="tag", log_msg="msg", a="b", log_data=\{"c":"d"\}/)
    }

    def "test generateLog with null inputs should return minimal timestamp info"() {
        when:
        def result = LogUtil.generateLog(null, null, null, null);

        then:
        result.matches(/.* timestamp=".+"/)
    }

    def "test generateLog with custom data object should return info contain custom json data"() {
        when:
        def result = LogUtil.generateLog(null, null, null, new SimpleDataObject("key1", "value1"))

        then:
        result.matches(/.* timestamp=".+", log_data=\{"key":"key1","value":"value1"\}/)
    }

    def "test generateLog with quote values should return slashed quoted values"() {
        when:
        def result = LogUtil.generateLog("tag", "msg \"111'\\", [a: "\"1'\\", b: "2'\"\\"], null);

        then:
        result.matches(/.* timestamp=".+", log_tag="tag", log_msg="msg \\"111\\'\\\\", a="\\"1\\'\\\\", b="2\\'\\"\\\\"/)
    }

    def "test generateLog overloading with tag, message arguments"() {
        when:
        def result = LogUtil.generateLog("tag", "msg");

        then:
        result.matches(/.* timestamp=".+", log_tag="tag", log_msg="msg"/)
    }

    def "test generateLog overloading with tag, message, meta arguments"() {
        when:
        def result = LogUtil.generateLog("tag", "msg", [a: "b"])

        then:
        result.matches(/.* timestamp=".+", log_tag="tag", log_msg="msg", a="b"/)
    }

    def "test generateLog overloading with specific stack trace element argument"() {
        when:
        def result = LogUtil.generateLog("tag", "msg", [a: "b"], null, 0)

        then:
        result.matches(/\[java.lang.Thread\] \[getStackTrace\] \[\d+\] timestamp=".+", log_tag="tag", log_msg="msg", a="b"/)
    }

    def "test generateLog overloading with overflowed stack trace element argument"() {
        when:
        def result = LogUtil.generateLog("tag", "msg", [a: "b"], null, 1000)

        then:
        result.matches(/timestamp=".+", log_tag="tag", log_msg="msg", a="b"/)
    }

    class SimpleDataObject {
        private String key;
        private String value;

        public SimpleDataObject(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public String getKey() {
            return key;
        }

        @Override
        public String toString() {
            return "should not be converted here";
        }
    }
}

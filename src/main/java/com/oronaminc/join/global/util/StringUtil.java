package com.oronaminc.join.global.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.helpers.MessageFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StringUtil {

    public static String format(String format, Object... args) {
        return MessageFormatter.arrayFormat(format, args).getMessage();
    }
}
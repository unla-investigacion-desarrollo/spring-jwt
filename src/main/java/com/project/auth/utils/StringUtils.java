package com.project.auth.utils;

import com.google.common.base.CaseFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public final class StringUtils {

    private StringUtils() {
    }

    /**
     * Given a string, return a new string UPPER CASE. In case it was originally camenCase, it will
     * convert to snakeCase
     *
     * @param oldString
     *
     * @return NEW_STRING
     */
    public static String toSnackUpperCase(String oldString) {
        if (oldString.contains("_")) {
            return oldString.toUpperCase();
        } else if (isCamelCase(oldString)) {
            return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, oldString);
        } else {
            return oldString.toUpperCase();
        }
    }

    public static boolean isCamelCase(String input) {
        int lowerCaseCharCount = 0;
        int upperCaseCharCount = 0;
        int specialCharsCount = 0;

        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) >= 97 && input.charAt(i) <= 122) {
                lowerCaseCharCount++;
            } else if (input.charAt(i) >= 65 && input.charAt(i) <= 90) {
                upperCaseCharCount++;
            } else {
                specialCharsCount++;
            }
        }

        return specialCharsCount == 0 && lowerCaseCharCount > 0 && upperCaseCharCount > 0;
    }

    public static <T> Predicate<T> distinctByKey(
            Function<? super T, ?> keyExtractor) {

        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

}

package com.teamcity.core.utils;

public final class LogUtils {

    private LogUtils() {

// Utility class

    }

    public static String printable(String value) {
        if (value == null) {
            return "null";

        }

        return value
                .replace("\t", "\t")
                .replace("\n", "\n")
                .replace("\r", "\r")
                .replace(" ", "·");

    }

}
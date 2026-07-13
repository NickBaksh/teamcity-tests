package com.teamcity.core.generators;

import com.github.curiousoddman.rgxgen.RgxGen;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Набор функций для генерации случайных тестовых данных.
 */
public final class RandomData {

    private static final String ALPHA = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ALPHANUM = ALPHA + "0123456789";
    private static final SecureRandom SECURE = new SecureRandom();

    private RandomData() {
    }

    public static String string(int length) {
        if (length <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUM.charAt(ThreadLocalRandom.current().nextInt(ALPHANUM.length())));
        }
        return sb.toString();
    }

    public static String alpha(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHA.charAt(ThreadLocalRandom.current().nextInt(ALPHA.length())));
        }
        return sb.toString();
    }

    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    public static String shortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public static String unique(String prefix) {
        return prefix + "_" + System.currentTimeMillis() + "_" + shortId();
    }

    public static String email() {
        return "test_" + System.currentTimeMillis() + "_" + shortId() + "@example.com";
    }

    public static String email(String localPartPrefix) {
        return localPartPrefix + "_" + shortId() + "@example.com";
    }

    public static String password() {
        return "P@ssw0rd_" + string(8) + "!";
    }

    public static String password(int length) {
        if (length < 8) {
            length = 8;
        }
        String special = "!@#$%";
        StringBuilder sb = new StringBuilder(length);
        sb.append(Character.toUpperCase(ALPHA.charAt(SECURE.nextInt(26))));
        sb.append(Character.toLowerCase(ALPHA.charAt(SECURE.nextInt(26))));
        sb.append(SECURE.nextInt(10));
        sb.append(special.charAt(SECURE.nextInt(special.length())));
        while (sb.length() < length) {
            sb.append(ALPHANUM.charAt(SECURE.nextInt(ALPHANUM.length())));
        }
        return sb.toString();
    }

    public static String fromRegex(String regex) {
        return new RgxGen(regex).generate();
    }

    public static int number(int minInclusive, int maxExclusive) {
        return ThreadLocalRandom.current().nextInt(minInclusive, maxExclusive);
    }
}

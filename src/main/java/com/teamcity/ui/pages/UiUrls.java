package com.teamcity.ui.pages;

import java.net.URI;

/**
 * Converts TeamCity API absolute webUrls (often {@code http://localhost:8111/...})
 * into browser-reachable relative paths for Selenide {@code Configuration.baseUrl}.
 */
public final class UiUrls {

    private UiUrls() {
    }

    public static String toRelative(String absoluteOrRelativeUrl) {
        if (absoluteOrRelativeUrl == null || absoluteOrRelativeUrl.isBlank()) {
            throw new IllegalArgumentException("URL must not be blank");
        }
        String value = absoluteOrRelativeUrl.trim();
        if (value.startsWith("/")) {
            return value;
        }
        URI uri = URI.create(value);
        String path = uri.getRawPath();
        if (path == null || path.isBlank()) {
            path = "/";
        }
        StringBuilder result = new StringBuilder(path);
        if (uri.getRawQuery() != null) {
            result.append('?').append(uri.getRawQuery());
        }
        if (uri.getRawFragment() != null) {
            result.append('#').append(uri.getRawFragment());
        }
        return result.toString();
    }
}

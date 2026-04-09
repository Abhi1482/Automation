package com.saas.analytics.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UTMParser {

    public static String extractUtmSource(String noteAttributesJson, String landingSiteUrl) {
        // 1. Check note_attributes for "utm_source"
        if (noteAttributesJson != null && noteAttributesJson.contains("utm_source")) {
            Pattern pattern = Pattern.compile("\"name\"\\s*:\\s*\"utm_source\"\\s*,\\s*\"value\"\\s*:\\s*\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(noteAttributesJson);
            if (matcher.find()) {
                return decode(matcher.group(1));
            }
        }

        // 2. Parse from landing_site URL
        if (landingSiteUrl != null && landingSiteUrl.contains("utm_source=")) {
            try {
                String[] splitUrl = landingSiteUrl.split("\\?");
                if (splitUrl.length > 1) {
                    String query = splitUrl[1];
                    String[] params = query.split("&");
                    for (String param : params) {
                        if (param.startsWith("utm_source=")) {
                            String value = param.substring("utm_source=".length());
                            return decode(value);
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore and fallback
            }
        }

        // 3. Fallback
        return "SR_facebook";
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return value;
        }
    }
}

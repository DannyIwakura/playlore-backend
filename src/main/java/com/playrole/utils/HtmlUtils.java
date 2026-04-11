package com.playrole.utils;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.owasp.html.CssSchema;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

public class HtmlUtils {

    private static final CssSchema CSS_POLICY = CssSchema.withProperties(
        Set.of("font-size", "text-align", "color")
    );

    private static final PolicyFactory POLICY = new HtmlPolicyBuilder()
        .allowElements("p", "br", "span", "strong", "em", "ul", "ol", "li",
                       "h1", "h2", "h3", "a", "img")
        .allowAttributes("style").onElements("p", "span", "strong", "em",
                                             "li", "h1", "h2", "h3")
        .allowAttributes("src", "alt", "width", "height").onElements("img")
        .allowStandardUrlProtocols()
        .allowAttributes("href").onElements("a")
        .allowStyling(CSS_POLICY)
        .toFactory()
        .and(Sanitizers.LINKS);

    public static String sanitize(String html) {
        if (html == null) return null;
        html = convertRgbToHex(html);
        return POLICY.sanitize(html);
    }

    private static String convertRgbToHex(String html) {
        Pattern pattern = Pattern.compile("rgb\\((\\d+),\\s*(\\d+),\\s*(\\d+)\\)");
        Matcher matcher = pattern.matcher(html);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            int r = Integer.parseInt(matcher.group(1));
            int g = Integer.parseInt(matcher.group(2));
            int b = Integer.parseInt(matcher.group(3));
            String hex = String.format("#%02x%02x%02x", r, g, b);
            matcher.appendReplacement(sb, hex);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
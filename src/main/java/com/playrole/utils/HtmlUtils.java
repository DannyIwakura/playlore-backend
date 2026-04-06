package com.playrole.utils;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

public class HtmlUtils {
	// Política: permite formato básico, enlaces e imágenes
    private static final PolicyFactory POLICY = Sanitizers.FORMATTING
                                                    .and(Sanitizers.LINKS)
                                                    .and(Sanitizers.IMAGES);

    public static String sanitize(String html) {
        if(html == null) return null;
        return POLICY.sanitize(html);
    }
}

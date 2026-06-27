package com.playrole.utils;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.owasp.html.CssSchema;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

public class HtmlUtils {
	//filtro de eituetas de estilo
    private static final CssSchema CSS_POLICY = CssSchema.withProperties(
        Set.of("font-size", "text-align", "color", "font-weight", "font-style", "text-decoration")
    );
    //polita de etiquetas HTML
    private static final PolicyFactory POLICY = new HtmlPolicyBuilder()
        .allowElements("p", "br", "span", "strong", "em", "b", "i", "u", "s",
                       "strike", "del", "ul", "ol", "li",
                       "h1", "h2", "h3", "h4", "a", "img",
                       "blockquote", "details", "summary")
        .allowAttributes("style").onElements("p", "span", "strong", "em", "b", "i", "u", "s",
                                              "li", "h1", "h2", "h3", "h4", "blockquote")
        .allowAttributes("src", "alt", "width", "height").onElements("img")
        .allowStandardUrlProtocols()
        .allowUrlProtocols("profile")
        .allowAttributes("href").onElements("a")
        .allowStyling(CSS_POLICY)
        .toFactory();
    //santizar el html para asegurarnos que solo nos quedamos con las etiquetas aceptadas
    public static String sanitize(String html) {
        if (html == null) return null;
        html = convertRgbToHex(html);
        return POLICY.sanitize(html);
    }
    
    private static String convertRgbToHex(String html) {
    	//en el front usare rgb() para colorear el texto
        Pattern pattern = Pattern.compile("rgb\\((\\d+),\\s*(\\d+),\\s*(\\d+)\\)");
        Matcher matcher = pattern.matcher(html);
        StringBuffer sb = new StringBuffer();
        //con matcher filtramos los parametro rgb
        while (matcher.find()) {
            int r = Integer.parseInt(matcher.group(1));
            int g = Integer.parseInt(matcher.group(2));
            int b = Integer.parseInt(matcher.group(3));
            String hex = String.format("#%02x%02x%02x", r, g, b);
            matcher.appendReplacement(sb, hex);
        }
        matcher.appendTail(sb);
        //despues del filtro convertimos a strign que es lo que se guarda en base de datos
        return sb.toString();
    }
}
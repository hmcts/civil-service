package uk.gov.hmcts.reform.dashboard.utilities;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtility {

    private StringUtility() {
        //utility to remove anchor
    }

    private static final Pattern closeAnchor = Pattern.compile("</a\\s*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern openAnchor = Pattern.compile("<a\\s+", Pattern.CASE_INSENSITIVE);
    private static final String HREF = "{VIEW_EVIDENCE_UPLOAD_DOCUMENTS}";
    private static final Pattern A_NO_HREF = Pattern.compile("<a\\b(?![^>]*\\bhref\\s*=)([^>]*)>([\\s\\S]*?)</a>",
                                                     Pattern.CASE_INSENSITIVE);

    public static String removeAnchor(String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        String current = text;
        Matcher matcherClose;
        Matcher matcherOpen;
        while ((matcherClose = closeAnchor.matcher(current)).find()
            && (matcherOpen = openAnchor.matcher(current)).find()
            && matcherOpen.start() < matcherClose.start()) {
            current = matcherClose.replaceFirst("");
            int open1 = matcherOpen.start();
            int open2 = current.indexOf('>', open1);
            if (open2 > -1) {
                current = current.substring(0, open1) + current.substring(open2 + 1);
            }
        }
        return current;
    }

    public static String activateLink(String html) {
        Matcher m = A_NO_HREF.matcher(html);
        StringBuffer out = new StringBuffer();

        while (m.find()) {
            String attrs = m.group(1);   // existing attributes (no href)
            String inner = m.group(2);   // keep whatever text/HTML is inside

            // If it already has class="", append govuk-link; else add a class=""
            if (attrs.toLowerCase().contains("class=")) {
                attrs = attrs.replaceFirst("(?i)\\bclass\\s*=\\s*([\"'])([^\"']*)\\1",
                                           "class=\"$2 govuk-link\"");
            } else {
                attrs = attrs + " class=\"govuk-link\"";
            }

            String replacement = "<a" + attrs + " href=\"" + HREF + "\">" + inner + "</a>";
            m.appendReplacement(out, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(out);
        return out.toString();
    }
}

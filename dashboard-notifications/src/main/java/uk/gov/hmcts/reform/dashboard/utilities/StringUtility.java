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
}

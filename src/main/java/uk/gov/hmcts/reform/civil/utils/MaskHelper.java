package uk.gov.hmcts.reform.civil.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class MaskHelper {

    private MaskHelper() {
    }

    public static String maskEmailsInErrorMessages(String text) {
        return Optional.ofNullable(text)
            .map(t -> Arrays.stream(t.split(" "))
                .map(word -> word.contains("@") ? maskEmail(word) : word)
                .collect(Collectors.joining()))
            .orElse(StringUtils.EMPTY);
    }

    public static String maskEmail(String email) {
        return Optional.ofNullable(email)
            .map(e -> e.replaceAll("[^@]", "*"))
            .orElse(StringUtils.EMPTY);
    }

}

package uk.gov.hmcts.reform.civil.utils;

import org.apache.commons.lang3.RandomStringUtils;

import java.security.SecureRandom;

public class AccessCodeGenerator {

    private static final String ALLOWED_CHARS = "ABCDEFGHJKLMNPRSTVWXYZ23456789";

    private AccessCodeGenerator() {
    }

    public static String generateAccessCode() {
        return RandomStringUtils.random(12, 0, ALLOWED_CHARS.length(),
                                        false, false, ALLOWED_CHARS.toCharArray(), new SecureRandom());
    }
}

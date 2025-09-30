package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.StringJoiner;

public class IdamUserUtils {

    private IdamUserUtils() {
    }

    public static String getIdamUserFullName(UserInfo userInfo) {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(userInfo.getGivenName()).add(userInfo.getFamilyName());
        return sj.toString();
    }
}

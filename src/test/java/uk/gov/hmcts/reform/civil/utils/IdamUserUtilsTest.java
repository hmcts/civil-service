package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IdamUserUtilsTest {

    @Test
    void getIdamUserFullName() {
        UserInfo userInfo = UserInfo.builder().givenName("John").familyName("Doe").build();
        assertEquals("John Doe", IdamUserUtils.getIdamUserFullName(userInfo));
    }
}

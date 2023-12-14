package uk.gov.hmcts.reform.civil.enums;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class CaseRoleTest {

    @Nested
    class GetFormattedName {

        @ParameterizedTest
        @EnumSource(value = CaseRole.class)
        void shouldReturnCorrectlyFormattedName_whenInvoked(CaseRole caseRole) {
            String formattedName = caseRole.getFormattedName();

            assertThat(formattedName).isEqualTo('[' + caseRole.name() + ']');
        }
    }
}

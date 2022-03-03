package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1ExtensionExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant2ExtensionExists;

public class PredicateUtilsTest {

    @Nested
    class DefendantExtension {

        @Test
        void shouldReturnTrue_whenDefendant1ExtensionExists() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                .respondent1TimeExtensionDate(LocalDateTime.now())
                .build();
            assertTrue(defendant1ExtensionExists.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenDefendant2ExtensionExists() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                .respondent2TimeExtensionDate(LocalDateTime.now())
                .build();
            assertTrue(defendant2ExtensionExists.test(caseData));
        }

        @Test
        void shouldReturnFalse_when1v1Case() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotifiedTimeExtension()
                .build();
            assertFalse(defendant2ExtensionExists.test(caseData));
        }

        @Test
        void shouldReturnFalse_when1v2SameSolCase() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateAddLitigationFriend_1v2_SameSolicitor()
                .build();
            assertFalse(defendant2ExtensionExists.test(caseData));
        }
    }
}

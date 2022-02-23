package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant2Extension;

public class PredicateUtilsTest {

    @Nested
    class Defendant2Extension {

        @Test
        void shouldReturnTrue_whenDefendant2ExtensionOnly() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                .respondent2TimeExtensionDate(LocalDateTime.now())
                .build();
            assertTrue(defendant2Extension.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenDefendant2ExtensionAfterDefendant1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                .respondent1TimeExtensionDate(LocalDateTime.now().minusSeconds(1))
                .respondent2TimeExtensionDate(LocalDateTime.now())
                .build();
            assertTrue(defendant2Extension.test(caseData));
        }

        @Test
        void shouldReturnFalse_when1v1Case() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotifiedTimeExtension()
                .build();
            assertFalse(defendant2Extension.test(caseData));
        }

        @Test
        void shouldReturnFalse_when1v2SameSolCase() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateAddLitigationFriend_1v2_SameSolicitor()
                .build();
            assertFalse(defendant2Extension.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenOnlyDefendant1Extension() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                .respondent1TimeExtensionDate(LocalDateTime.now())
                .build();
            assertFalse(defendant2Extension.test(caseData));
        }

        @Test
        void shouldReturnFalse_whenDefendant2ExtensionNotAfterDefendant1() {
            var extensionDate = LocalDateTime.now();
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                .respondent1TimeExtensionDate(extensionDate)
                .respondent2TimeExtensionDate(extensionDate)
                .build();
            assertFalse(defendant2Extension.test(caseData));
        }
    }
}

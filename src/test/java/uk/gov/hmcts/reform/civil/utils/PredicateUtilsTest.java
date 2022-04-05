package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.ResponseIntention;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1AckExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1ExtensionExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1ResponseExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant2AckExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant2ExtensionExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant2ResponseExists;

public class PredicateUtilsTest {

    @Nested
    class DefendantExtension {

        @Test
        void shouldReturnTrue_whenDefendant1ExtensionExists() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                .respondent1TimeExtensionDate(LocalDateTime.now())
                .respondentSolicitor1AgreedDeadlineExtension(LocalDate.now().plusDays(3))
                .build();EventHistoryMapper.java:201:13
            assertTrue(defendant1ExtensionExists.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenDefendant2ExtensionExists() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                .respondent2TimeExtensionDate(LocalDateTime.now())
                .respondentSolicitor2AgreedDeadlineExtension(LocalDate.now().plusDays(3))
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

    @Nested
    class AcknowledgementOfService {

        @Test
        void shouldReturnTrue_whenDefendant1AcknowledgementExists() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged1v2SameSolicitor()
                .multiPartyClaimTwoDefendantSolicitors()
                .respondent2AcknowledgeNotificationDate(null)
                .build();
            assertTrue(defendant1AckExists.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenDefendant2AcknowledgementExists() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged1v2SameSolicitor()
                .multiPartyClaimTwoDefendantSolicitors()
                .respondent1AcknowledgeNotificationDate(null)
                .build();
            assertTrue(defendant2AckExists.test(caseData));
        }

        @Test
        void shouldReturnFalse_when1v1Case() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged()
                .build();
            assertFalse(defendant2ExtensionExists.test(caseData));
        }
    }

    @Nested
    class DefendantResponse {

        @Test
        void shouldReturnTrue_whenDefendant1AcknowledgementExists() {
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimOneDefendantSolicitor()
                .atState(FlowState.Main.FULL_DEFENCE)
                .respondent2Responds1v2SameSol(FULL_DEFENCE)
                .respondentResponseIsSame(YES)
                .respondent2DQ(Respondent2DQ.builder().build())
                .respondent2ClaimResponseIntentionType(null)
                .build();
            assertTrue(defendant1ResponseExists.test(caseData));
        }

        @Test
        void shouldReturnTrue_whenDefendant2AcknowledgementExists() {
            CaseData caseData = CaseDataBuilder.builder()
                .multiPartyClaimOneDefendantSolicitor()
                .atState(FlowState.Main.FULL_DEFENCE)
                .respondent2Responds1v2SameSol(FULL_DEFENCE)
                .respondentResponseIsSame(YES)
                .respondent2DQ(Respondent2DQ.builder().build())
                .respondent2ClaimResponseIntentionType(ResponseIntention.FULL_DEFENCE)
                .build();
            assertTrue(defendant2ResponseExists.test(caseData));
        }

        @Test
        void shouldReturnFalse_when1v1Case() {
            CaseData caseData = CaseDataBuilder.builder()
                .atState(FlowState.Main.FULL_DEFENCE)
                .respondent1AcknowledgeNotificationDate(null)
                .build();
            assertFalse(defendant2ResponseExists.test(caseData));
        }
    }
}

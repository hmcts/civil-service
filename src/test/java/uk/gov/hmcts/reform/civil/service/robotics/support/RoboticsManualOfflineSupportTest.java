package uk.gov.hmcts.reform.civil.service.robotics.support;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.ReasonForProceedingOnPaper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimProceedsInCaseman;
import uk.gov.hmcts.reform.civil.model.ClaimProceedsInCasemanLR;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoboticsManualOfflineSupportTest {

    private final RoboticsManualOfflineSupport support =
        new RoboticsManualOfflineSupport(new RoboticsEventTextFormatter());

    @Test
    void prepareTakenOfflineEventDetailsThrowsWhenUnspecDetailsMissing() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
            .build();

        assertThatThrownBy(() -> support.prepareTakenOfflineEventDetails(caseData))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void prepareTakenOfflineEventDetailsThrowsWhenSpecDetailsMissing() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .build();

        assertThatThrownBy(() -> support.prepareTakenOfflineEventDetails(caseData))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void prepareTakenOfflineEventDetailsHonoursProvidedDetails() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .claimProceedsInCasemanLR(ClaimProceedsInCasemanLR.builder()
                .reason(ReasonForProceedingOnPaper.APPLICATION)
                .date(LocalDate.of(2023, 3, 2))
                .build())
            .build();

        assertThat(support.prepareTakenOfflineEventDetails(caseData))
            .contains("APPLICATION")
            .contains("2023-03-02");
    }

    @Test
    void prepareTakenOfflineEventDetailsHonoursUnspecDetails() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
            .claimProceedsInCaseman(ClaimProceedsInCaseman.builder()
                .reason(ReasonForProceedingOnPaper.OTHER)
                .other("Manual review")
                .date(LocalDate.of(2023, 6, 10))
                .build())
            .build();

        assertThat(support.prepareTakenOfflineEventDetails(caseData))
            .contains("Manual review")
            .contains("2023-06-10");
    }
}

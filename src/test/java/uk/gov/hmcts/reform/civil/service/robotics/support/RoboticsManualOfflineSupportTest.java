package uk.gov.hmcts.reform.civil.service.robotics.support;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.ReasonForProceedingOnPaper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimProceedsInCaseman;
import uk.gov.hmcts.reform.civil.model.ClaimProceedsInCasemanLR;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoboticsManualOfflineSupportTest {

    private final RoboticsManualOfflineSupport support =
        new RoboticsManualOfflineSupport(new RoboticsEventTextFormatter());

    @Test
    void prepareTakenOfflineEventDetailsThrowsWhenUnspecDetailsMissing() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseAccessCategory(CaseCategory.UNSPEC_CLAIM);

        assertThatThrownBy(() -> support.prepareTakenOfflineEventDetails(caseData))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void prepareTakenOfflineEventDetailsThrowsWhenSpecDetailsMissing() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseAccessCategory(CaseCategory.SPEC_CLAIM);

        assertThatThrownBy(() -> support.prepareTakenOfflineEventDetails(caseData))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void prepareTakenOfflineEventDetailsHonoursProvidedDetails() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseAccessCategory(CaseCategory.SPEC_CLAIM);
        ClaimProceedsInCasemanLR claimProceedsInCasemanLR = new ClaimProceedsInCasemanLR();
        claimProceedsInCasemanLR.setReason(ReasonForProceedingOnPaper.APPLICATION);
        claimProceedsInCasemanLR.setDate(LocalDate.of(2023, 3, 2));
        caseData.setClaimProceedsInCasemanLR(claimProceedsInCasemanLR);

        assertThat(support.prepareTakenOfflineEventDetails(caseData))
            .contains("APPLICATION")
            .contains("2023-03-02");
    }

    @Test
    void prepareTakenOfflineEventDetailsHonoursUnspecDetails() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseAccessCategory(CaseCategory.UNSPEC_CLAIM);
        ClaimProceedsInCaseman claimProceedsInCaseman = new ClaimProceedsInCaseman();
        claimProceedsInCaseman.setReason(ReasonForProceedingOnPaper.OTHER);
        claimProceedsInCaseman.setOther("Manual review");
        claimProceedsInCaseman.setDate(LocalDate.of(2023, 6, 10));
        caseData.setClaimProceedsInCaseman(claimProceedsInCaseman);

        assertThat(support.prepareTakenOfflineEventDetails(caseData))
            .contains("Manual review")
            .contains("2023-06-10");
    }
}

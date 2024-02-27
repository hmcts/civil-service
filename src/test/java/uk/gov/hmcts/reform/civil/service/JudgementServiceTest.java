package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JudgementServiceTest {

    @InjectMocks
    private JudgementService judgementService;

    @Mock
    private FeatureToggleService featureToggleService;

    @Test
    void setCcjClaimFee_HelpWithFeesClaimIssued() {
        // Given
        CaseData caseData = CaseData.builder()
            .caseDataLiP(CaseDataLiP.builder()
                             .helpWithFees(HelpWithFees.builder()
                                               .helpWithFeesReferenceNumber("Test")
                                               .build())
                             .build())
            .claimIssuedHwfDetails(HelpWithFeesDetails.builder()
                                       .outstandingFeeInPounds(BigDecimal.ZERO)
                                       .build())
            .build();
        // When
        assertThat(judgementService.ccjJudgmentClaimFee(caseData)).isEqualTo(BigDecimal.ZERO);
    }
}

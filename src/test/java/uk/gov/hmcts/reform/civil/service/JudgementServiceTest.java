package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
    JudgementService.class,
    JacksonAutoConfiguration.class
})
class JudgementServiceTest {

    @Autowired
    private JudgementService judgementService;

    @MockBean
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

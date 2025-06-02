package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClaimSettledAmountParamsBuilderTest {

    private ClaimSettledAmountParamsBuilder builder;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        builder = new ClaimSettledAmountParamsBuilder();
        caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
    }

    @Test
    void shouldAddClaimSettledAmountWhenPresent() {
        caseData = caseData.toBuilder().respondent1ResponseDeadline(null)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .respondToClaim(RespondToClaim.builder()
                .howMuchWasPaid(new BigDecimal("10000"))
                .whenWasThisAmountPaid(LocalDate.parse("2023-03-29"))
                .build())
            .build();

        HashMap<String, Object> params = new HashMap<>();

        builder.addParams(caseData, params);

        assertThat(params).containsEntry("claimSettledAmount", "£100");
    }

    @Test
    void shouldNotAddClaimSettledAmountWhenNotPresent() {
        // Arrange
        CaseData caseData = mock(CaseData.class);
        when(caseData.getRespondToClaim()).thenReturn(null);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).isEmpty();
    }
}

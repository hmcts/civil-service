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

class AdmissionPaidAmountParamsBuilderTest {

    private AdmissionPaidAmountParamsBuilder builder;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        builder = new AdmissionPaidAmountParamsBuilder();
        caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();

    }

    @Test
    void shouldAddAdmissionPaidAmountWhenPresent() {
        // Arrange
        caseData = caseData.toBuilder().respondent1ResponseDeadline(null)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .respondToClaim(new RespondToClaim()
                .setHowMuchWasPaid(new BigDecimal("10000"))
                .setWhenWasThisAmountPaid(LocalDate.parse("2023-03-29"))
                )
            .build();

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).containsEntry("admissionPaidAmount", "£100");
    }

    @Test
    void shouldNotAddAdmissionPaidAmountWhenNotPresent() {
        // Arrange
        CaseData caseData = mock(CaseData.class);
        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).isEmpty();
    }
}

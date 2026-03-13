package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClaimSettledDateParamsBuilderTest {

    private static final LocalTime END_OF_DAY = LocalTime.MAX;

    private ClaimSettledDateParamsBuilder builder;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        builder = new ClaimSettledDateParamsBuilder();
        caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
    }

    @Test
    void shouldAddClaimSettledDateParamsWhenPresent() {

        caseData = caseData.toBuilder().respondent1ResponseDeadline(null)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .respondToClaim(new RespondToClaim()
                .setHowMuchWasPaid(new BigDecimal("100050"))
                .setWhenWasThisAmountPaid(LocalDate.parse("2023-03-29"))
                )
            .build();

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).extracting("claimSettledObjectionsDeadline").isEqualTo(
            LocalDate.parse("2023-04-17").atTime(LocalTime.of(23, 59, 59))
        );
        LocalDate settledDate = LocalDate.parse("2023-03-29");
        assertThat(params).containsEntry("claimSettledDateEn", DateUtils.formatDate(settledDate));
        assertThat(params).containsEntry("claimSettledDateCy", DateUtils.formatDateInWelsh(settledDate, false));
    }

    @Test
    void shouldNotAddClaimSettledDateParamsWhenNotPresent() {
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

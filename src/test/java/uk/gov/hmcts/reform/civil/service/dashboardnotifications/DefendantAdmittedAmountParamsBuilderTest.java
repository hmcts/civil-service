package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.ClaimantResponseUtils;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefendantAdmittedAmountParamsBuilderTest {

    private DefendantAdmittedAmountParamsBuilder builder;
    private ClaimantResponseUtils claimantResponseUtils;

    @BeforeEach
    void setUp() {
        claimantResponseUtils = mock(ClaimantResponseUtils.class);
        builder = new DefendantAdmittedAmountParamsBuilder(claimantResponseUtils);
    }

    @Test
    void shouldAddDefendantAdmittedAmountWhenPresent() {
        CaseData caseData = mock(CaseData.class);
        when(claimantResponseUtils.getDefendantAdmittedAmount(caseData)).thenReturn(new BigDecimal("10000.00"));

        HashMap<String, Object> params = new HashMap<>();
        builder.addParams(caseData, params);

        assertThat(params).containsEntry("defendantAdmittedAmount", "£10000");
    }

    @Test
    void shouldNotAddDefendantAdmittedAmountWhenNotPresent() {
        CaseData caseData = mock(CaseData.class);
        when(claimantResponseUtils.getDefendantAdmittedAmount(caseData)).thenReturn(null);

        HashMap<String, Object> params = new HashMap<>();

        builder.addParams(caseData, params);

        assertThat(params).isEmpty();
    }
}

package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GeneralAppPbaDetailsParamsBuilderTest {

    private GeneralAppPbaDetailsParamsBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new GeneralAppPbaDetailsParamsBuilder();
    }

    @Test
    void shouldAddApplicationFeeWhenGeneralAppPBADetailsIsPresent() {
        CaseData caseData = mock(CaseData.class);
        GAPbaDetails pbaDetails = mock(GAPbaDetails.class);
        Fee fee = mock(Fee.class);

        when(caseData.getGeneralAppPBADetails()).thenReturn(pbaDetails);
        when(pbaDetails.getFee()).thenReturn(fee);
        when(fee.getCalculatedAmountInPence()).thenReturn(new BigDecimal(10000));

        HashMap<String, Object> params = new HashMap<>();
        builder.addParams(caseData, params);

        assertThat(params).containsEntry("applicationFee", "£100");
    }

    @Test
    void shouldNotAddApplicationFeeWhenGeneralAppPBADetailsIsNull() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getGeneralAppPBADetails()).thenReturn(null);

        HashMap<String, Object> params = new HashMap<>();
        builder.addParams(caseData, params);

        assertThat(params).isEmpty();
    }
}

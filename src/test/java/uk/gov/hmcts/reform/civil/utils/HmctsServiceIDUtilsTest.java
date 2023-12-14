package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
public class HmctsServiceIDUtilsTest {

    private static final String SPEC_ID = "AAA6";
    private static final String UNSPEC_ID = "AAA7";

    @Mock
    private PaymentsConfiguration paymentsConfiguration;

    @BeforeEach
    void setUp() {
        given(paymentsConfiguration.getSiteId()).willReturn(UNSPEC_ID);
        given(paymentsConfiguration.getSpecSiteId()).willReturn(SPEC_ID);
    }

    @Test
    void shouldReturnSpecServiceId_whenCaseCategoryIsSpec() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .setClaimTypeToSpecClaim().build();

        String hmctsServiceID =
            HmctsServiceIDUtils.getHmctsServiceID(caseData, paymentsConfiguration);

        assertThat(hmctsServiceID).isEqualTo(SPEC_ID);
    }

    @Test
    void shouldReturnUnspecServiceId_whenCaseCategoryIsUnspec() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .setClaimTypeToUnspecClaim().build();

        String hmctsServiceID =
            HmctsServiceIDUtils.getHmctsServiceID(caseData, paymentsConfiguration);

        assertThat(hmctsServiceID).isEqualTo(UNSPEC_ID);
    }
}

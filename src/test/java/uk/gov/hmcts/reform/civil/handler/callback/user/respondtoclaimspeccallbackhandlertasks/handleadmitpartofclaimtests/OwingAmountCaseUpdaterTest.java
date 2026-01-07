package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaimtests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaim.OwingAmountCaseUpdater;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OwingAmountCaseUpdaterTest {

    @InjectMocks
    private OwingAmountCaseUpdater updater;

    @BeforeEach
    void setUp() {

    }

    @Test
    void shouldUpdateOwingAmountInPounds() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondToAdmittedClaimOwingAmount(BigDecimal.valueOf(1000L));
        caseData.setRespondToAdmittedClaimOwingAmount2(BigDecimal.valueOf(2000L));

        updater.update(caseData);

        assertThat(caseData.getRespondToAdmittedClaimOwingAmountPounds()).isEqualTo(MonetaryConversions.penniesToPounds(BigDecimal.valueOf(1000L)));
        assertThat(caseData.getRespondToAdmittedClaimOwingAmountPounds2()).isEqualTo(MonetaryConversions.penniesToPounds(BigDecimal.valueOf(2000L)));
    }

    @Test
    void shouldNotUpdateOwingAmountWhenNull() {
        CaseData caseData = CaseDataBuilder.builder().build();

        updater.update(caseData);

        assertThat(caseData.getRespondToAdmittedClaimOwingAmountPounds()).isNull();
        assertThat(caseData.getRespondToAdmittedClaimOwingAmountPounds2()).isNull();
    }
}

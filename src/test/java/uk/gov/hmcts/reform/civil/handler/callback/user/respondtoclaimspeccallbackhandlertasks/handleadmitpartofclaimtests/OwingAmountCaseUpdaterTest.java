package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaimtests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaim.OwingAmountCaseUpdater;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OwingAmountCaseUpdaterTest {

    @InjectMocks
    private OwingAmountCaseUpdater updater;

    private CaseData.CaseDataBuilder<?, ?> caseDataBuilder;

    @BeforeEach
    void setUp() {
        caseDataBuilder = CaseData.builder();
    }

    @Test
    void shouldUpdateOwingAmountInPounds() {
        CaseData caseData = CaseData.builder()
                .respondToAdmittedClaimOwingAmount(BigDecimal.valueOf(1000L))
                .respondToAdmittedClaimOwingAmount2(BigDecimal.valueOf(2000L))
                .build();

        updater.update(caseData, caseDataBuilder);

        CaseData updatedCaseData = caseDataBuilder.build();
        assertThat(updatedCaseData.getRespondToAdmittedClaimOwingAmountPounds()).isEqualTo(MonetaryConversions.penniesToPounds(BigDecimal.valueOf(1000L)));
        assertThat(updatedCaseData.getRespondToAdmittedClaimOwingAmountPounds2()).isEqualTo(MonetaryConversions.penniesToPounds(BigDecimal.valueOf(2000L)));
    }

    @Test
    void shouldNotUpdateOwingAmountWhenNull() {
        CaseData caseData = CaseData.builder().build();

        updater.update(caseData, caseDataBuilder);

        CaseData updatedCaseData = caseDataBuilder.build();
        assertThat(updatedCaseData.getRespondToAdmittedClaimOwingAmountPounds()).isNull();
        assertThat(updatedCaseData.getRespondToAdmittedClaimOwingAmountPounds2()).isNull();
    }
}
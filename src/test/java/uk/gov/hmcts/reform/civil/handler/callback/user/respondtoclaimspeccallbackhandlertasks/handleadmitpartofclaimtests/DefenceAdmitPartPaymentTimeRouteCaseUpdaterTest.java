package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaimtests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaim.DefenceAdmitPartPaymentTimeRouteCaseUpdater;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class DefenceAdmitPartPaymentTimeRouteCaseUpdaterTest {

    @InjectMocks
    private DefenceAdmitPartPaymentTimeRouteCaseUpdater updater;

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldUpdateDefenceAdmitPartPaymentTimeRouteForRespondent1() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setIsRespondent1(YES);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY);

        updater.update(caseData);

        assertThat(caseData.getDefenceAdmitPartPaymentTimeRouteGeneric()).isEqualTo(IMMEDIATELY);
    }

    @Test
    void shouldUpdateDefenceAdmitPartPaymentTimeRouteForRespondent2() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setIsRespondent2(YES);
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired2(IMMEDIATELY);

        updater.update(caseData);

        assertThat(caseData.getDefenceAdmitPartPaymentTimeRouteGeneric()).isEqualTo(IMMEDIATELY);
    }

    @Test
    void shouldNotUpdateWhenNoConditionsMet() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setIsRespondent1(NO);
        caseData.setIsRespondent2(NO);

        updater.update(caseData);

        assertThat(caseData.getDefenceAdmitPartPaymentTimeRouteGeneric()).isNull();
    }
}

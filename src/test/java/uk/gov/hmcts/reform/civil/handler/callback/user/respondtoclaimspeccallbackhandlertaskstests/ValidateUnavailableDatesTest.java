package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandler.ValidateUnavailableDates;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.SmallClaimHearing;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@ExtendWith(MockitoExtension.class)
class ValidateUnavailableDatesTest {

    @InjectMocks
    private ValidateUnavailableDates validateUnavailableDates;

    @Mock
    private UnavailableDateValidator unavailableDateValidator;

    @Mock
    private CallbackParams callbackParams;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder()
            .respondent1DQ(Respondent1DQ.builder().build())
            .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);
    }

    private void setupCaseDataForSmallClaim(SmallClaimHearing smallClaimHearing, boolean isRespondent2) {
        caseData = CaseData.builder()
            .responseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM)
            .isRespondent2(isRespondent2 ? YES : NO)
            .respondent1DQ(Respondent1DQ.builder().respondent1DQHearingSmallClaim(isRespondent2 ? null : smallClaimHearing).build())
            .respondent2DQ(isRespondent2 ? Respondent2DQ.builder().respondent2DQHearingSmallClaim(smallClaimHearing).build() : null)
            .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);
    }

    private void setupCaseDataForFastClaim(Hearing hearing) {
        caseData = CaseData.builder()
            .responseClaimTrack("FAST_CLAIM")
            .respondent1DQ(Respondent1DQ.builder().respondent1DQHearingFastClaim(hearing).build())
            .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);
    }

    @Test
    void shouldReturnErrorsForRespondent2SmallClaimHearing() {
        SmallClaimHearing smallClaimHearing = SmallClaimHearing.builder().build();
        setupCaseDataForSmallClaim(smallClaimHearing, true);
        when(unavailableDateValidator.validateSmallClaimsHearing(smallClaimHearing)).thenReturn(List.of("Error"));

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateUnavailableDates.execute(callbackParams);

        assertThat(response.getErrors()).contains("Error");
    }

    @Test
    void shouldReturnNoErrorsForRespondent1SmallClaimHearing() {
        SmallClaimHearing smallClaimHearing = SmallClaimHearing.builder().build();
        setupCaseDataForSmallClaim(smallClaimHearing, false);
        when(unavailableDateValidator.validateSmallClaimsHearing(smallClaimHearing)).thenReturn(List.of());

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateUnavailableDates.execute(callbackParams);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnErrorsForFastClaimHearing() {
        Hearing hearing = Hearing.builder().build();
        setupCaseDataForFastClaim(hearing);
        when(unavailableDateValidator.validateFastClaimHearing(hearing)).thenReturn(List.of("Error"));

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateUnavailableDates.execute(callbackParams);

        assertThat(response.getErrors()).contains("Error");
    }
}

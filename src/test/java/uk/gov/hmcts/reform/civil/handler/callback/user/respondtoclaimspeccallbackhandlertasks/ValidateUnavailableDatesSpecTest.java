package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.SmallClaimHearing;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class ValidateUnavailableDatesSpecTest {

    @InjectMocks
    private ValidateUnavailableDatesSpec validateUnavailableDates;

    @Mock
    private UnavailableDateValidator unavailableDateValidator;

    @Mock
    private CallbackParams callbackParams;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseDataBuilder.builder()
                .respondent1DQ(new Respondent1DQ())
                .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);
    }

    private void setupCaseDataForSmallClaim(SmallClaimHearing smallClaimHearing, boolean isRespondent2) {
        Respondent1DQ respondent1DQ = new Respondent1DQ();
        respondent1DQ.setRespondent1DQHearingSmallClaim(isRespondent2 ? null : smallClaimHearing);
        Respondent2DQ respondent2DQ = new Respondent2DQ();
        respondent2DQ.setRespondent2DQHearingSmallClaim(smallClaimHearing);
        caseData = CaseDataBuilder.builder()
                .responseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM)
                .isRespondent2(isRespondent2 ? YES : NO)
                .respondent1DQ(respondent1DQ)
                .respondent2DQ(isRespondent2 ? respondent2DQ : null)
                .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);
    }

    private void setupCaseDataForFastClaim(Hearing hearing) {
        Respondent1DQ respondent1DQ = new Respondent1DQ();
        respondent1DQ.setRespondent1DQHearingFastClaim(hearing);
        caseData = CaseDataBuilder.builder()
                .responseClaimTrack("FAST_CLAIM")
                .respondent1DQ(respondent1DQ)
                .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);
    }

    @Test
    void shouldReturnErrorsForRespondent2SmallClaimHearing() {
        SmallClaimHearing smallClaimHearing = new SmallClaimHearing();
        setupCaseDataForSmallClaim(smallClaimHearing, true);
        when(unavailableDateValidator.validateSmallClaimsHearing(smallClaimHearing)).thenReturn(List.of("Error"));

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateUnavailableDates.execute(callbackParams);

        assertThat(response.getErrors()).contains("Error");
    }

    @Test
    void shouldReturnNoErrorsForRespondent1SmallClaimHearing() {
        SmallClaimHearing smallClaimHearing = new SmallClaimHearing();
        setupCaseDataForSmallClaim(smallClaimHearing, false);
        when(unavailableDateValidator.validateSmallClaimsHearing(smallClaimHearing)).thenReturn(List.of());

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateUnavailableDates.execute(callbackParams);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnErrorsForFastClaimHearing() {
        Hearing hearing = new Hearing();
        setupCaseDataForFastClaim(hearing);
        when(unavailableDateValidator.validateFastClaimHearing(hearing)).thenReturn(List.of("Error"));

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateUnavailableDates.execute(callbackParams);

        assertThat(response.getErrors()).contains("Error");
    }
}

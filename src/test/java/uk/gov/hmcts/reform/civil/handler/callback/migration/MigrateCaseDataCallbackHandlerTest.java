package uk.gov.hmcts.reform.civil.handler.callback.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;

@ExtendWith(MockitoExtension.class)
class MigrateCaseDataCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private MigrateCaseDataCallbackHandler handler;

    @Test
    void shouldReturnNoError_whenAboutToSubmitIsInvoked_UnSpec() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .courtLocation()
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
            .respondent1DQWithLocation()
            .applicant1DQWithLocation()
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);
        assertThat(response.getErrors()).isNull();

    }

    @Test
    void shouldReturnNoError_whenSubmittedIsInvoked_UnSpec() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .build();

        CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

        handler.handle(params);
    }

}


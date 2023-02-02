package uk.gov.hmcts.reform.civil.handler.callback.migration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.CaseMigrationUtility;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;

@SpringBootTest(classes = {
    MigrateCaseDataCallbackHandler.class,
    CaseMigrationUtility.class,
    JacksonAutoConfiguration.class})
public class MigrateCaseDataCallbackHandlerTest extends BaseCallbackHandlerTest {

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

    }

    @Test
    void shouldReturnNoError_whenSUbmittedIsInvoked_UnSpec() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .courtLocation()
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
            .respondent1DQWithLocation()
            .applicant1DQWithLocation()
            .build();
        CallbackParams params = callbackParamsOf(caseData, SUBMITTED);


    }

}


package uk.gov.hmcts.reform.civil.handler.callback.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@SpringBootTest(classes = {
    MigrateCaseDataCallbackHandler.class,
    JacksonAutoConfiguration.class})
public class MigrateCaseDataCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MigrateCaseDataCallbackHandler handler;

    @Test
    void shouldReturnNoError_whenAboutToSubmitIsInvoked() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldMigrateData_whenSpecClaim() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued()
            .setSuperClaimTypeToSpecClaim()
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);

        CaseData newCaseData = objectMapper.convertValue(response.getData(), CaseData.class);

        assertThat(newCaseData.getCaseAccessCategory())
            .isEqualTo(CaseCategory.SPEC_CLAIM);

        assertThat(newCaseData.getApplicant1OrganisationPolicy().getOrgPolicyCaseAssignedRole())
            .isEqualTo(CaseRole.APPLICANTSOLICITORONE.getFormattedName());

        assertThat(newCaseData.getRespondent1OrganisationPolicy().getOrgPolicyCaseAssignedRole())
            .isEqualTo(CaseRole.RESPONDENTSOLICITORONE.getFormattedName());

        assertThat(newCaseData.getRespondent2OrganisationPolicy().getOrgPolicyCaseAssignedRole())
            .isEqualTo(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName());
    }
}

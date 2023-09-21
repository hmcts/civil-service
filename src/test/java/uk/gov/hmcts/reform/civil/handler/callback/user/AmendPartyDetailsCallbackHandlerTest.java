package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.validation.ValidateEmailService;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;

@SpringBootTest(classes = {
    AmendPartyDetailsCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    ValidateEmailService.class
})
class AmendPartyDetailsCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private AmendPartyDetailsCallbackHandler handler;

    @Autowired
    private ValidateEmailService validateEmailService;

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    @Nested
    class AboutToStart {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseDetails caseDetails = CaseDetailsBuilder.builder().atStatePendingClaimIssued().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseDetails).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class AboutToSubmit {

        @Test
        void shouldReturnNoErrors_whenIdamEmailIsNotCorrectButAdditionalEmailIsValid() {
            String validEmail = "john@example.com";

            CaseData caseData = CaseData.builder()
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(validEmail).build())
                .respondentSolicitor1EmailAddress(validEmail)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldReturnErrors_whenIdamEmailIsNotCorrectAndAdditionalEmailIsInvalid() {
            String invalidEmail = "a@a";

            CaseData caseData = CaseData.builder()
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(invalidEmail).build())
                .respondentSolicitor1EmailAddress(invalidEmail)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).hasSize(2)
                .contains("Enter an email address in the correct format, for example john.smith@example.com");
        }

        @Nested
        class SetOrganisationPolicy {
            OrganisationPolicy organisationPolicy = OrganisationPolicy.builder()
                .organisation(Organisation.builder()
                                  .organisationID(null)
                                  .build())
                .orgPolicyReference("orgreference")
                .orgPolicyCaseAssignedRole("orgassignedrole")
                .build();

            OrganisationPolicy expectedOrganisationPolicy1 = OrganisationPolicy.builder()
                .organisation(Organisation.builder()
                                  .organisationID("QWERTY R")
                                  .build())
                .orgPolicyReference("orgreference")
                .orgPolicyCaseAssignedRole("orgassignedrole")
                .build();
            String validEmail = "john@example.com";

            @Test
            void shouldSetOrganisationPolicy_1v1() {
                CaseData caseData = CaseData.builder()
                    .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(validEmail).build())
                    .respondentSolicitor1EmailAddress(validEmail)
                    .respondent1OrganisationIDCopy("QWERTY R")
                    .respondent1OrganisationPolicy(organisationPolicy)
                    .build();

                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                assertThat(updatedData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID())
                    .isEqualTo("QWERTY R");
                assertThat(updatedData.getRespondent1OrganisationPolicy()).isEqualTo(expectedOrganisationPolicy1);
            }

            @Test
            void shouldSetOrganisationPolicy_1v2() {
                OrganisationPolicy expectedOrganisationPolicy2 = OrganisationPolicy.builder()
                    .organisation(Organisation.builder()
                                      .organisationID("QWERTY R2")
                                      .build())
                    .orgPolicyReference("orgreference")
                    .orgPolicyCaseAssignedRole("orgassignedrole")
                    .build();

                CaseData caseData = CaseData.builder()
                    .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(validEmail).build())
                    .respondentSolicitor1EmailAddress(validEmail)
                    .respondent1OrganisationIDCopy("QWERTY R")
                    .respondent1OrganisationPolicy(organisationPolicy)
                    .respondent2OrganisationIDCopy("QWERTY R2")
                    .respondent2OrganisationPolicy(organisationPolicy)
                    .build();

                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                assertThat(caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID())
                    .isEqualTo(null);
                assertThat(caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID())
                    .isEqualTo(null);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                assertThat(updatedData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID())
                    .isEqualTo("QWERTY R");
                assertThat(updatedData.getRespondent1OrganisationPolicy()).isEqualTo(expectedOrganisationPolicy1);
                assertThat(updatedData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID())
                    .isEqualTo("QWERTY R2");
                assertThat(updatedData.getRespondent2OrganisationPolicy()).isEqualTo(expectedOrganisationPolicy2);
            }
        }
    }

    @Nested
    class Submitted {

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader("# You have updated a legal representative's information")
                    .confirmationBody("<br />")
                    .build());
        }
    }
}

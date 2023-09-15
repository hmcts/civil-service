package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;
import uk.gov.hmcts.reform.civil.validation.ValidateEmailService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;

@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    ChangeSolicitorEmailCallbackHandler.class,
    ValidateEmailService.class
})
class ChangeSolicitorEmailCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private ChangeSolicitorEmailCallbackHandler handler;

    @Autowired
    private UserService userService;

    @MockBean
    private ValidateEmailService validateEmailService;

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @MockBean
    private PostcodeValidator postcodeValidator;

    @BeforeEach
    void setupTest() {
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
    }

    @Nested
    class AboutToStart {
        CallbackParams params;

        @BeforeEach
        void setupAboutToStartTests() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged()
                .build();
            this.params = callbackParamsOf(caseData, ABOUT_TO_START);
        }

        @Test
        void shouldAmendSolicitorEmail_WhenInvokedByUserWith_Applicant1Roles() {
            List<String> caseRoles = new ArrayList<>();
            caseRoles.add("[APPLICANTSOLICITORONE]");

            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(caseRoles);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertEquals("Yes", response.getData().get("isApplicant1"), "isApplicant1");
            assertEquals("No", response.getData().get("isRespondent1"), "isRespondent1");
            assertEquals("No", response.getData().get("isRespondent2"), "isRespondent2");
        }

        @Test
        void shouldSetPartyFlags_WhenInvokedByUserWith_Respondent1Roles() {
            List<String> caseRoles = new ArrayList<>();
            caseRoles.add("[RESPONDENTSOLICITORONE]");

            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(caseRoles);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertEquals("No", response.getData().get("isApplicant1"), "isApplicant1");
            assertEquals("Yes", response.getData().get("isRespondent1"), "isRespondent1");
            assertEquals("No", response.getData().get("isRespondent2"), "isRespondent2");
        }

        @Test
        void shouldSetServiceAddress_whenSpec() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged()
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .build().toBuilder()
                .specRespondentCorrespondenceAddressRequired(YesOrNo.YES)
                .specRespondentCorrespondenceAddressdetails(Address.builder()
                                                                .postCode("mail post code")
                                                                .addressLine1("mail line 1")
                                                                .build())
                .build();
            params = callbackParamsOf(caseData, ABOUT_TO_START);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            Assertions.assertThat(response.getData().get("respondentSolicitor1ServiceAddressRequired"))
                    .isEqualTo("Yes");
            Assertions.assertThat(response.getData().get("respondentSolicitor1ServiceAddress"))
                .extracting("AddressLine1")
                    .isEqualTo("mail line 1");
            Assertions.assertThat(response.getData().get("respondentSolicitor1ServiceAddress"))
                .extracting("PostCode")
                    .isEqualTo("mail post code");
        }

        @Test
        void shouldSetPartyFlags_WhenInvokedByUserWith_Respondent2Roles() {
            List<String> caseRoles = new ArrayList<>();
            caseRoles.add("[RESPONDENTSOLICITORTWO]");

            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
                .thenReturn(caseRoles);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertEquals("No", response.getData().get("isApplicant1"), "isApplicant1");
            assertEquals("No", response.getData().get("isRespondent1"), "isRespondent1");
            assertEquals("Yes", response.getData().get("isRespondent2"), "isRespondent2");
        }

        @Test
        void shouldSetPartyFlags_WhenInvokedByUserWith_Respondent1And2Roles() {
            List<String> caseRoles = new ArrayList<>();
            caseRoles.add("[RESPONDENTSOLICITORONE]");
            caseRoles.add("[RESPONDENTSOLICITORTWO]");

            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
                .thenReturn(caseRoles);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertEquals("No", response.getData().get("isApplicant1"), "isApplicant1");
            assertEquals("Yes", response.getData().get("isRespondent1"), "isRespondent1");
            assertEquals("Yes", response.getData().get("isRespondent2"), "isRespondent2");
        }
    }

    @Nested
    class AboutToSubmit {

        @Test
        void shouldReturnResponse_WithApplicant1SolicitorEmail() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged()
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicant1solicitor@gmail.com").build()
                ).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            var actual = ((HashMap)response.getData().get("applicantSolicitor1UserDetails")).get("email");
            assertEquals("applicant1solicitor@gmail.com", actual, "applicant1SolicitorEmail");
        }

        @Test
        void shouldReturnResponse_WithRespondent1SolicitorEmail() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged()
                .respondentSolicitor1EmailAddress("respondent1solicitor@gmail.com")
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertEquals("respondent1solicitor@gmail.com",
                         response.getData().get("respondentSolicitor1EmailAddress"),
                "respondentSolicitor1EmailAddress");
        }

        @Test
        void shouldReturnResponse1and2_when1v2ss() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged()
                .respondentSolicitor1EmailAddress("respondent1solicitor@gmail.com")
                .build().toBuilder()
                .respondent2(Party.builder()
                                 .companyName("c3")
                                 .type(Party.Type.COMPANY)
                                 .build())
                .respondent2SameLegalRepresentative(YesOrNo.YES)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertEquals("respondent1solicitor@gmail.com",
                         response.getData().get("respondentSolicitor2EmailAddress"),
                         "respondentSolicitor2EmailAddress");
        }

        @Test
        void shouldReturnResponse_WithRespondent2SolicitorEmail() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged()
                .respondentSolicitor2EmailAddress("respondent2solicitor@gmail.com")
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertEquals("respondent2solicitor@gmail.com",
                         response.getData().get("respondentSolicitor2EmailAddress"),
                         "respondentSolicitor2EmailAddress");
        }

        @Test
        void shouldClearPartyFlags() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertNull(response.getData().get("isApplicant1"), "isApplicant1");
            assertNull(response.getData().get("isRespondent1"), "isRespondent1");
            assertNull(response.getData().get("isRespondent2"), "isRespondent2");
        }

        @Test
        void shouldCopyBack_whenSpecApp1() {
            List<String> caseRoles = new ArrayList<>();
            caseRoles.add("[APPLICANTSOLICITORONE]");
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(caseRoles);

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build()
                .toBuilder()
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .applicantSolicitor1ServiceAddressRequired(YesOrNo.YES)
                .applicantSolicitor1ServiceAddress(Address.builder()
                                                       .addressLine1("mail line 1")
                                                       .postCode("mail post code")
                                                       .build()
                )
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            Assertions.assertThat(response.getData().get("specApplicantCorrespondenceAddressdetails"))
                .extracting("AddressLine1")
                .isEqualTo("mail line 1");
            Assertions.assertThat(response.getData().get("specApplicantCorrespondenceAddressdetails"))
                .extracting("PostCode")
                .isEqualTo("mail post code");
        }

        @Test
        void shouldCopyBack_whenSpecDef1() {
            List<String> caseRoles = new ArrayList<>();
            caseRoles.add("[RESPONDENTSOLICITORONE]");
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(caseRoles);

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build()
                .toBuilder()
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .respondentSolicitor1ServiceAddressRequired(YesOrNo.YES)
                .respondentSolicitor1ServiceAddress(Address.builder()
                                                       .addressLine1("mail line 1")
                                                       .postCode("mail post code")
                                                       .build()
                )
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            Assertions.assertThat(response.getData().get("specRespondentCorrespondenceAddressdetails"))
                .extracting("AddressLine1")
                .isEqualTo("mail line 1");
            Assertions.assertThat(response.getData().get("specRespondentCorrespondenceAddressdetails"))
                .extracting("PostCode")
                .isEqualTo("mail post code");
        }

        @Test
        void shouldCopyBack_whenSpecDef2() {
            List<String> caseRoles = new ArrayList<>();
            caseRoles.add("[RESPONDENTSOLICITORTWO]");
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(caseRoles);

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build()
                .toBuilder()
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .respondentSolicitor2ServiceAddressRequired(YesOrNo.YES)
                .respondentSolicitor2ServiceAddress(Address.builder()
                                                       .addressLine1("mail line 1")
                                                       .postCode("mail post code")
                                                       .build()
                )
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            Assertions.assertThat(response.getData().get("specRespondent2CorrespondenceAddressdetails"))
                .extracting("AddressLine1")
                .isEqualTo("mail line 1");
            Assertions.assertThat(response.getData().get("specRespondent2CorrespondenceAddressdetails"))
                .extracting("PostCode")
                .isEqualTo("mail post code");
        }

        @Test
        void shouldBackUp_whenNewRequiredIsNo() {
            List<String> caseRoles = new ArrayList<>();
            caseRoles.add("[RESPONDENTSOLICITORTWO]");
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(caseRoles);

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build()
                .toBuilder()
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .respondentSolicitor2ServiceAddressRequired(YesOrNo.NO)
                .respondentSolicitor2ServiceAddress(null)
                .specRespondent2CorrespondenceAddressdetails(Address.builder()
                                                                 .addressLine1("mail line 1")
                                                                 .postCode("mail post code")
                                                                 .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            Assertions.assertThat(response.getData().get("specRespondent2CorrespondenceAddressdetails"))
                .extracting("AddressLine1")
                .isEqualTo("mail line 1");
            Assertions.assertThat(response.getData().get("specRespondent2CorrespondenceAddressdetails"))
                .extracting("PostCode")
                .isEqualTo("mail post code");
        }

        @Test
        void shouldBackUp_whenNewRequiredIsNo1v2ss() {
            List<String> caseRoles = new ArrayList<>();
            caseRoles.add("[RESPONDENTSOLICITORONE]");
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(caseRoles);

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build()
                .toBuilder()
                .respondent2(Party.builder()
                                 .type(Party.Type.COMPANY)
                                 .companyName("c3")
                                 .build())
                .respondent2SameLegalRepresentative(YesOrNo.YES)
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .respondentSolicitor1ServiceAddressRequired(YesOrNo.NO)
                .respondentSolicitor1ServiceAddress(null)
                .specRespondentCorrespondenceAddressdetails(Address.builder()
                                                                 .addressLine1("mail line 1")
                                                                 .postCode("mail post code")
                                                                 .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            Assertions.assertThat(response.getData().get("specRespondent2CorrespondenceAddressdetails"))
                .extracting("AddressLine1")
                .isEqualTo("mail line 1");
            Assertions.assertThat(response.getData().get("specRespondent2CorrespondenceAddressdetails"))
                .extracting("PostCode")
                .isEqualTo("mail post code");
        }

        @Test
        void shouldUpdateReferenceApplicant1() {
            List<String> caseRoles = new ArrayList<>();
            caseRoles.add("[APPLICANTSOLICITORONE]");
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(caseRoles);

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData = caseData.toBuilder()
                .isApplicant1(YesOrNo.YES)
                .applicant1OrganisationPolicy(caseData.getApplicant1OrganisationPolicy().toBuilder()
                                                  .orgPolicyReference("new reference")
                                                  .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //noinspection unchecked
            Assertions.assertThat(response.getData().get("solicitorReferences"))
                    .extracting("applicantSolicitor1Reference")
                        .isEqualTo("new reference");
        }

        @Test
        void shouldUpdateReferenceRespondent1() {
            List<String> caseRoles = new ArrayList<>();
            caseRoles.add("[RESPONDENTSOLICITORONE]");
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(caseRoles);

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData = caseData.toBuilder()
                .isRespondent1(YesOrNo.YES)
                .respondent1OrganisationPolicy(caseData.getRespondent1OrganisationPolicy().toBuilder()
                                                  .orgPolicyReference("new reference")
                                                  .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //noinspection unchecked
            Assertions.assertThat(response.getData().get("solicitorReferences"))
                    .extracting("respondentSolicitor1Reference")
                        .isEqualTo("new reference");
        }

        @Test
        void shouldUpdateReferenceRespondent2() {
            List<String> caseRoles = new ArrayList<>();
            caseRoles.add("[RESPONDENTSOLICITORTWO]");
            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(caseRoles);

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData = caseData.toBuilder()
                .isRespondent2(YesOrNo.YES)
                .respondent2OrganisationPolicy(caseData.getRespondent2OrganisationPolicy().toBuilder()
                                                  .orgPolicyReference("new reference")
                                                  .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //noinspection unchecked
            Assertions.assertThat(response.getData().get("solicitorReferences"))
                    .extracting("respondentSolicitor2Reference")
                        .isEqualTo("new reference");
        }
    }

    @Nested
    class ValidateSolicitorEmail {

        ArrayList<String> mockErrors;

        @BeforeEach
        void setupTests() {
            mockErrors = new ArrayList<>();
            mockErrors.add("mock-error");
        }

        @Test
        void validateApplicant1SolicitorEmail_shouldInvokeEmailValidationServiceAndReturnOutput() {
            CaseData caseData = CaseDataBuilder.builder()
                .applicantSolicitor1UserDetails(
                    IdamUserDetails.builder().email("applicant1solicitor@gmail.com").build()).build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validate-applicant1-solicitor-email");

            when(validateEmailService.validate(anyString())).thenReturn(mockErrors);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(validateEmailService, times(1))
                .validate("applicant1solicitor@gmail.com");
            assertEquals(mockErrors, response.getErrors(), "errors");
        }

        @Test
        void validateRespondent1SolicitorEmail_shouldInvokeEmailValidationServiceAndReturnOutput() {
            CaseData caseData = CaseDataBuilder.builder()
                .respondentSolicitor1EmailAddress("respondent1solicitor@gmail.com")
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validate-respondent1-solicitor-email");

            when(validateEmailService.validate(anyString())).thenReturn(mockErrors);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(validateEmailService, times(1))
                .validate("respondent1solicitor@gmail.com");
            assertEquals(mockErrors, response.getErrors(), "errors");
        }

        @Test
        void validateRespondent2SolicitorEmail_shouldInvokeEmailValidationServiceAndReturnOutput() {
            CaseData caseData = CaseDataBuilder.builder()
                .respondentSolicitor2EmailAddress("respondent2solicitor@gmail.com")
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, "validate-respondent2-solicitor-email");

            when(validateEmailService.validate(anyString())).thenReturn(mockErrors);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(validateEmailService, times(1))
                .validate("respondent2solicitor@gmail.com");
            assertEquals(mockErrors, response.getErrors(), "errors");
        }
    }

    @Nested
    class Submitted {

        CaseData caseData;
        CallbackParams params;

        @BeforeEach
        void setupTests() {
            caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged()
                .respondentSolicitor1EmailAddress("respondent1solicitor@gmail.com")
                .build();
            params = callbackParamsOf(caseData, SUBMITTED);
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_WhenInvokedByUserWith_Applicant1Role() {
            List<String> caseRoles = new ArrayList<>();
            caseRoles.add("[APPLICANTSOLICITORONE]");

            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(caseRoles);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertEquals("# You have updated a claimant's legal representative's information",
                         response.getConfirmationHeader(), "confirmationHeader");
            assertEquals("<br />",
                         response.getConfirmationBody(), "confirmationBody");
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_WhenInvokedByUserWith_Respondent1Role() {
            List<String> caseRoles = new ArrayList<>();
            caseRoles.add("[RESPONDENTSOLICITORONE]");

            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(caseRoles);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertEquals("# You have updated a defendant's legal representative's information",
                         response.getConfirmationHeader(), "confirmationHeader");
            assertEquals("<br />",
                         response.getConfirmationBody(), "confirmationBody");
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_WhenInvokedByUserWith_Respondent2Role() {
            List<String> caseRoles = new ArrayList<>();
            caseRoles.add("[RESPONDENTSOLICITORTWO]");

            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(caseRoles);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertEquals("# You have updated a defendant's legal representative's information",
                         response.getConfirmationHeader(), "confirmationHeader");
            assertEquals("<br />",
                         response.getConfirmationBody(), "confirmationBody");
        }

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_WhenInvokedByUserWith_Respondent1And2Roles() {
            List<String> caseRoles = new ArrayList<>();
            caseRoles.add("[RESPONDENTSOLICITORONE]");
            caseRoles.add("[RESPONDENTSOLICITORTWO]");

            when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(caseRoles);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertEquals("# You have updated a defendant's legal representative's information",
                         response.getConfirmationHeader(), "confirmationHeader");
            assertEquals("<br />",
                         response.getConfirmationBody(), "confirmationBody");
        }
    }
}

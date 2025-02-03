package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.sdo.ReasonForReconsideration;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.LiPRequestReconsiderationGeneratorService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REQUEST_FOR_RECONSIDERATION;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    RequestForReconsiderationCallbackHandler.class,
    JacksonAutoConfiguration.class
})
class RequestForReconsiderationCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private RequestForReconsiderationCallbackHandler handler;

    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private CoreCaseUserService coreCaseUserService;
    @MockBean
    private LiPRequestReconsiderationGeneratorService documentService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private UserService userService;
    private static final String CONFIRMATION_BODY = "### What happens next \n" +
        "You should receive an update on your request for determination after 10 days, please monitor" +
        " your notifications/dashboard for an update.";

    private static final String ERROR_MESSAGE_DEADLINE_EXPIRED
        = "You can no longer request a reconsideration because the deadline has expired";

    private static final String ERROR_MESSAGE_SPEC_AMOUNT_GREATER_THAN_THOUSAND = "You can only request a reconsideration for claims of £1,000 or less.";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(REQUEST_FOR_RECONSIDERATION);
    }

    @Nested
    class AboutToStartCallback {
        @Test
        void shouldAllowRequestIfLessThan7DaysElapsed() {
            //Given : Casedata containing an SDO order created 6 days ago
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .totalClaimAmount(BigDecimal.valueOf(800))
                .systemGeneratedCaseDocuments(List.of(ElementUtils
                                                  .element(CaseDocument.builder()
                                                               .documentType(DocumentType.SDO_ORDER)
                                                               .createdDatetime(LocalDateTime.now().minusDays(6))
                                                               .build())))
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("APPLICANTSOLICITORONE"));
            //When: handler is called with ABOUT_TO_START event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: No errors should be displayed
            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldAllowRequestIfLessThan7DaysElapsedForLatestSDO() {
            //Given : Casedata containing two SDO order and latest created 6 days ago
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .totalClaimAmount(BigDecimal.valueOf(800))
                .systemGeneratedCaseDocuments(Arrays.asList(
                    ElementUtils.element(CaseDocument.builder()
                                             .documentType(DocumentType.SDO_ORDER)
                                             .createdDatetime(LocalDateTime.now().minusDays(10))
                                             .build()),
                    ElementUtils.element(CaseDocument.builder()
                                             .documentType(DocumentType.SDO_ORDER)
                                             .createdDatetime(LocalDateTime.now().minusDays(6))
                                             .build())))
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("APPLICANTSOLICITORONE"));
            //When: handler is called with ABOUT_TO_START event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: No errors should be displayed
            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldSendErrorMessageIf7DaysElapsedForLatestSDO() {
            //Given : Casedata containing two SDO order and latest created 7 days ago
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .totalClaimAmount(BigDecimal.valueOf(800))
                .systemGeneratedCaseDocuments(Arrays.asList(
                    ElementUtils.element(CaseDocument.builder()
                                             .documentType(DocumentType.SDO_ORDER)
                                             .createdDatetime(LocalDateTime.now().minusDays(10))
                                             .build()),
                    ElementUtils.element(CaseDocument.builder()
                                             .documentType(DocumentType.SDO_ORDER)
                                             .createdDatetime(LocalDateTime.now().minusDays(7))
                                             .build())))
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("APPLICANTSOLICITORONE"));

            //When: handler is called with ABOUT_TO_START event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: The error should be displayed
            assertThat(response.getErrors().contains(ERROR_MESSAGE_DEADLINE_EXPIRED));
        }

        @Test
        void shouldSendErrorMessageIf7DaysElapsed() {
            //Given : Casedata containing an SDO order created 7 days ago
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .totalClaimAmount(BigDecimal.valueOf(800))
                .systemGeneratedCaseDocuments(List.of(ElementUtils
                                                          .element(CaseDocument.builder()
                                                                       .documentType(DocumentType.SDO_ORDER)
                                                                       .createdDatetime(LocalDateTime.now().minusDays(7))
                                                                       .build())))
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("APPLICANTSOLICITORONE"));

            //When: handler is called with ABOUT_TO_START event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: The error should be displayed
            assertThat(response.getErrors().contains(ERROR_MESSAGE_DEADLINE_EXPIRED));
        }

        @ParameterizedTest
        @ValueSource(strings = {"APPLICANTSOLICITORONE", "RESPONDENTSOLICITORONE", "RESPONDENTSOLICITORTWO"})
        void shouldGetSelectedUserRole(String userRole) {
            //Given : Casedata and return applicant solicitor role
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .totalClaimAmount(BigDecimal.valueOf(800))
                .systemGeneratedCaseDocuments(List.of(ElementUtils
                                                          .element(CaseDocument.builder()
                                                                       .documentType(DocumentType.SDO_ORDER)
                                                                       .createdDatetime(LocalDateTime.now().minusDays(5))
                                                                       .build()))).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(userRole));

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: It should set casePartyRequestForReconsideration to selected user
            if (userRole.equals("APPLICANTSOLICITORONE")) {
                assertThat(response.getData()).extracting("casePartyRequestForReconsideration")
                    .isEqualTo("Applicant");
            } else if (userRole.equals("RESPONDENTSOLICITORONE")) {
                assertThat(response.getData()).extracting("casePartyRequestForReconsideration")
                    .isEqualTo("Respondent1");
            } else {
                assertThat(response.getData()).extracting("casePartyRequestForReconsideration")
                    .isEqualTo("Respondent2");
            }
        }

        @Test
        void shouldAllowEventForCaseWithClaimAmountLessThan1000() {
            //Given : Casedata with small claim
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedSmallClaim()
                .systemGeneratedCaseDocuments(List.of(ElementUtils
                                                          .element(CaseDocument.builder()
                                                                       .documentType(DocumentType.SDO_ORDER)
                                                                       .createdDatetime(LocalDateTime.now().minusDays(5))
                                                                       .build()))).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("APPLICANTSOLICITORONE"));

            //When: handler is called with ABOUT_TO_START event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: No errors should be displayed
            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldNotAllowEventForCaseWithClaimAmountGreaterThan1000() {
            //Given : Casedata with claim amount greater than 1000
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted()
                .totalClaimAmount(new BigDecimal(1200))
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("APPLICANTSOLICITORONE"));

            //When: handler is called with ABOUT_TO_START event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: No errors should be displayed
            assertThat(response.getErrors().contains(ERROR_MESSAGE_SPEC_AMOUNT_GREATER_THAN_THOUSAND));
        }
    }

    @Nested
    class AboutToSubmitCallback {
        @Test
        void shouldPopulateReasonAndRequestorDetailsOfApplicant() {
            //Given : Casedata
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .reasonForReconsiderationApplicant(ReasonForReconsideration.builder().reasonForReconsiderationTxt("Reason").build())
                .applicant1(Party.builder()
                                 .individualFirstName("FirstName")
                                 .individualLastName("LastName")
                                 .type(Party.Type.INDIVIDUAL)
                                 .partyName("test").build()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("APPLICANTSOLICITORONE"));

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: setAsideDate should be set correctly
            assertThat(response.getData()).extracting("reasonForReconsiderationApplicant")
                .extracting("reasonForReconsiderationTxt")
                .isEqualTo("Reason");
            assertThat(response.getData()).extracting("reasonForReconsiderationApplicant")
                .extracting("requestor")
                .isEqualTo("Applicant - FirstName LastName");
        }

        @Test
        void shouldPopulateReasonAndRequestorDetailsOfBothApplicantsWhen2V1() {
            //Given : Casedata
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .reasonForReconsiderationApplicant(ReasonForReconsideration.builder().reasonForReconsiderationTxt("Reason").build())
                .applicant1(Party.builder()
                                .individualFirstName("FirstName")
                                .individualLastName("LastName")
                                .type(Party.Type.INDIVIDUAL)
                                .partyName("Applicant1").build())
                .addApplicant2(YesOrNo.YES)
                .applicant2(Party.builder()
                                 .individualFirstName("FirstName2")
                                 .individualLastName("LastName2")
                                 .type(Party.Type.INDIVIDUAL)
                                 .partyName("Applicant2").build()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("APPLICANTSOLICITORONE"));

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: setAsideDate should be set correctly
            assertThat(response.getData()).extracting("reasonForReconsiderationApplicant")
                .extracting("reasonForReconsiderationTxt")
                .isEqualTo("Reason");
            assertThat(response.getData()).extracting("reasonForReconsiderationApplicant")
                .extracting("requestor")
                .isEqualTo("Applicant - FirstName LastName and FirstName2 LastName2");
        }

        @Test
        void shouldPopulateReasonAndRequestorDetailsOfRespondent1() {
            //Given : Casedata
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .reasonForReconsiderationRespondent1(ReasonForReconsideration.builder().reasonForReconsiderationTxt("Reason").build())
                .respondent1(Party.builder()
                                .individualFirstName("FirstName")
                                .individualLastName("LastName")
                                .type(Party.Type.INDIVIDUAL)
                                .partyName("test").build()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("RESPONDENTSOLICITORONE"));

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: setAsideDate should be set correctly
            assertThat(response.getData()).extracting("reasonForReconsiderationRespondent1")
                .extracting("reasonForReconsiderationTxt")
                .isEqualTo("Reason");
            assertThat(response.getData()).extracting("reasonForReconsiderationRespondent1")
                .extracting("requestor")
                .isEqualTo("Defendant - FirstName LastName");
        }

        @Test
        void shouldPopulateReasonAndRequestorDetailsOfRespondent1Blank() {
            //Given : Casedata
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .reasonForReconsiderationRespondent1(ReasonForReconsideration.builder().build())
                .respondent1(Party.builder()
                                .individualFirstName("FirstName")
                                .individualLastName("LastName")
                                .type(Party.Type.INDIVIDUAL)
                                .partyName("test").build()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("RESPONDENTSOLICITORONE"));

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: setAsideDate should be set correctly
            assertThat(response.getData()).extracting("reasonForReconsiderationRespondent1")
                .extracting("reasonForReconsiderationTxt")
                .isNotNull();
            assertThat(response.getData()).extracting("reasonForReconsiderationRespondent1")
                .extracting("requestor")
                .isEqualTo("Defendant - FirstName LastName");
        }

        @Test
        void shouldPopulateReasonAndRequestorDetailsOfRespondent2() {
            //Given : Casedata
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .reasonForReconsiderationRespondent2(ReasonForReconsideration.builder().reasonForReconsiderationTxt("Reason").build())
                .addRespondent2(YesOrNo.YES)
                .respondent2(Party.builder()
                                .individualFirstName("FirstName")
                                .individualLastName("LastName")
                                .type(Party.Type.INDIVIDUAL)
                                .partyName("test").build()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("RESPONDENTSOLICITORTWO"));

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: setAsideDate should be set correctly
            assertThat(response.getData()).extracting("reasonForReconsiderationRespondent2")
                .extracting("reasonForReconsiderationTxt")
                .isEqualTo("Reason");
            assertThat(response.getData()).extracting("reasonForReconsiderationRespondent2")
                .extracting("requestor")
                .isEqualTo("Defendant - FirstName LastName");
        }

        @Test
        void shouldPopulateReasonAndRequestorDetailsOfRespondent2Blank() {
            //Given : Casedata
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .reasonForReconsiderationRespondent2(ReasonForReconsideration.builder().build())
                .addRespondent2(YesOrNo.YES)
                .respondent2(Party.builder()
                                .individualFirstName("FirstName")
                                .individualLastName("LastName")
                                .type(Party.Type.INDIVIDUAL)
                                .partyName("test").build()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("RESPONDENTSOLICITORTWO"));

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: setAsideDate should be set correctly
            assertThat(response.getData()).extracting("reasonForReconsiderationRespondent2")
                .extracting("reasonForReconsiderationTxt")
                .isNotNull();
            assertThat(response.getData()).extracting("reasonForReconsiderationRespondent2")
                .extracting("requestor")
                .isEqualTo("Defendant - FirstName LastName");
        }

        @Test
        void shouldPopulateReasonAndRequestorDetailsOfRespondentsWhen2V1SameDefSol() {
            //Given : Casedata
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .reasonForReconsiderationRespondent1(ReasonForReconsideration.builder().reasonForReconsiderationTxt("Reason").build())
                .respondent1(Party.builder()
                                 .individualFirstName("FirstName")
                                 .individualLastName("LastName")
                                 .type(Party.Type.INDIVIDUAL)
                                 .partyName("test").build())
                .addRespondent2(YesOrNo.YES)
                .respondent2SameLegalRepresentative(YesOrNo.YES)
                .respondent2(Party.builder()
                                 .individualFirstName("FirstName2")
                                 .individualLastName("LastName2")
                                 .type(Party.Type.INDIVIDUAL)
                                 .partyName("test").build()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("RESPONDENTSOLICITORONE"));

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: setAsideDate should be set correctly
            assertThat(response.getData()).extracting("reasonForReconsiderationRespondent1")
                .extracting("reasonForReconsiderationTxt")
                .isEqualTo("Reason");
            assertThat(response.getData()).extracting("reasonForReconsiderationRespondent1")
                .extracting("requestor")
                .isEqualTo("Defendant - FirstName LastName and FirstName2 LastName2");
        }

        @Test
        void shouldPopulateReasonAndRequestorDetailsOfRespondentsWhen2V1SameDefSolBlank() {
            //Given : Casedata
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .reasonForReconsiderationRespondent1(ReasonForReconsideration.builder().build())
                .respondent1(Party.builder()
                                 .individualFirstName("FirstName")
                                 .individualLastName("LastName")
                                 .type(Party.Type.INDIVIDUAL)
                                 .partyName("test").build())
                .addRespondent2(YesOrNo.YES)
                .respondent2SameLegalRepresentative(YesOrNo.YES)
                .respondent2(Party.builder()
                                 .individualFirstName("FirstName2")
                                 .individualLastName("LastName2")
                                 .type(Party.Type.INDIVIDUAL)
                                 .partyName("test").build()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("RESPONDENTSOLICITORONE"));

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: setAsideDate should be set correctly
            assertThat(response.getData()).extracting("reasonForReconsiderationRespondent1")
                .extracting("reasonForReconsiderationTxt")
                .isNotNull();
            assertThat(response.getData()).extracting("reasonForReconsiderationRespondent1")
                .extracting("requestor")
                .isEqualTo("Defendant - FirstName LastName and FirstName2 LastName2");
        }

        @Test
        void shouldPopulateReasonAndRequestorDetailsOfApplicantWhenRespondent1LiPWhenCuiCpEnabled() {
            //Given : Casedata
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .reasonForReconsiderationApplicant(ReasonForReconsideration.builder().reasonForReconsiderationTxt("Reason").build())
                .applicant1(Party.builder()
                                .individualFirstName("FirstName")
                                .individualLastName("LastName")
                                .type(Party.Type.INDIVIDUAL)
                                .partyName("test").build())
                .respondent1Represented(YesOrNo.NO).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("APPLICANTSOLICITORONE"));
            when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
            when(documentService.generateLiPDocument(any(), anyString(), anyBoolean())).thenReturn(CaseDocument.builder().documentName("Name of document").build());

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: setAsideDate should be set correctly
            assertThat(response.getData()).extracting("reasonForReconsiderationApplicant")
                .extracting("reasonForReconsiderationTxt")
                .isEqualTo("Reason");
            assertThat(response.getData()).extracting("reasonForReconsiderationApplicant")
                .extracting("requestor")
                .isEqualTo("Applicant - FirstName LastName");
            assertThat(response.getData()).extracting("requestForReconsiderationDocument")
                .extracting("documentName")
                .isEqualTo("Name of document");
        }

        @Test
        void shouldPopulateReasonAndRequestorDetailsOfRespondent1WhenApplicantLiPWhenCuiCpEnabled() {
            //Given : Casedata
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .reasonForReconsiderationRespondent1(ReasonForReconsideration.builder().reasonForReconsiderationTxt("Reason").build())
                .respondent1(Party.builder()
                                 .individualFirstName("FirstName")
                                 .individualLastName("LastName")
                                 .type(Party.Type.INDIVIDUAL)
                                 .partyName("test").build())
                .applicant1Represented(YesOrNo.NO).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("RESPONDENTSOLICITORONE"));
            when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
            when(documentService.generateLiPDocument(any(), anyString(), anyBoolean())).thenReturn(CaseDocument.builder().documentName("Name of document").build());

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: setAsideDate should be set correctly
            assertThat(response.getData()).extracting("reasonForReconsiderationRespondent1")
                .extracting("reasonForReconsiderationTxt")
                .isEqualTo("Reason");
            assertThat(response.getData()).extracting("reasonForReconsiderationRespondent1")
                .extracting("requestor")
                .isEqualTo("Defendant - FirstName LastName");
            assertThat(response.getData()).extracting("requestForReconsiderationDocumentRes")
                .extracting("documentName")
                .isEqualTo("Name of document");
        }

        @Test
        void shouldPopulateReasonAndRequestorDetailsOfApplicantWhenRespondent1LiPWhenCuiCpDisabled() {
            //Given : Casedata
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .reasonForReconsiderationApplicant(ReasonForReconsideration.builder().reasonForReconsiderationTxt("Reason").build())
                .applicant1(Party.builder()
                                .individualFirstName("FirstName")
                                .individualLastName("LastName")
                                .type(Party.Type.INDIVIDUAL)
                                .partyName("test").build())
                .respondent1Represented(YesOrNo.NO).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("APPLICANTSOLICITORONE"));
            when(featureToggleService.isCaseProgressionEnabled()).thenReturn(false);
            when(documentService.generateLiPDocument(any(), anyString(), anyBoolean())).thenReturn(CaseDocument.builder().documentName("Name of document").build());

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: setAsideDate should be set correctly
            assertThat(response.getData()).extracting("reasonForReconsiderationApplicant")
                .extracting("reasonForReconsiderationTxt")
                .isEqualTo("Reason");
            assertThat(response.getData()).extracting("reasonForReconsiderationApplicant")
                .extracting("requestor")
                .isEqualTo("Applicant - FirstName LastName");
            assertThat(response.getData()).extracting("requestForReconsiderationDocument").isNull();
        }

        @Test
        void shouldPopulateReasonAndRequestorDetailsOfRespondent1WhenApplicantLiPWhenCuiCpDisabled() {
            //Given : Casedata
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .reasonForReconsiderationRespondent1(ReasonForReconsideration.builder().reasonForReconsiderationTxt("Reason").build())
                .respondent1(Party.builder()
                                 .individualFirstName("FirstName")
                                 .individualLastName("LastName")
                                 .type(Party.Type.INDIVIDUAL)
                                 .partyName("test").build())
                .applicant1Represented(YesOrNo.NO).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("RESPONDENTSOLICITORONE"));
            when(featureToggleService.isCaseProgressionEnabled()).thenReturn(false);
            when(documentService.generateLiPDocument(any(), anyString(), anyBoolean())).thenReturn(CaseDocument.builder().documentName("Name of document").build());

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: setAsideDate should be set correctly
            assertThat(response.getData()).extracting("reasonForReconsiderationRespondent1")
                .extracting("reasonForReconsiderationTxt")
                .isEqualTo("Reason");
            assertThat(response.getData()).extracting("reasonForReconsiderationRespondent1")
                .extracting("requestor")
                .isEqualTo("Defendant - FirstName LastName");
            assertThat(response.getData()).extracting("requestForReconsiderationDocumentRes").isNull();
        }

        @Test
        void shouldPopulateOrderRequestedForReviewClaimantWhenItIsClaimantLiPRequest() {
            //Given : Casedata
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .caseDataLip(CaseDataLiP.builder()
                                 .requestForReviewCommentsClaimant("Comments from claimant")
                                 .build())
                .respondent1Represented(YesOrNo.NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("CLAIMANT"));
            when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
            CaseDocument caseDocument = mock(CaseDocument.class);
            when(caseDocument.getDocumentName()).thenReturn("Generated document name");
            when(documentService.generateLiPDocument(
                caseData,
                params.getParams().get(BEARER_TOKEN).toString(),
                true
            )).thenReturn(caseDocument);

            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: setAsideDate should be set correctly
            assertThat(response.getData()).extracting("orderRequestedForReviewClaimant")
                .isEqualTo("Yes");
            assertThat(response.getData()).extracting("reasonForReconsiderationApplicant")
                .extracting("reasonForReconsiderationTxt")
                .isEqualTo("Comments from claimant");
            // link to document is shown in confirmation page, so we always need it
            assertThat(response.getData()).extracting("requestForReconsiderationDocument")
                .extracting("documentName")
                .isEqualTo(caseDocument.getDocumentName());
        }

        @Test
        void shouldPopulateOrderRequestedForReviewDefendantWhenItIsDefendantLiPRequest() {
            //Given : Casedata
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .caseDataLip(CaseDataLiP.builder()
                                 .requestForReviewCommentsDefendant("Comments from defendant")
                                 .build())
                .applicant1Represented(YesOrNo.NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
            when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("DEFENDANT"));
            when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);
            CaseDocument caseDocument = mock(CaseDocument.class);
            when(caseDocument.getDocumentName()).thenReturn("Generated document for defendant");
            when(documentService.generateLiPDocument(
                caseData,
                params.getParams().get(BEARER_TOKEN).toString(),
                false
            )).thenReturn(caseDocument);
            //When: handler is called with ABOUT_TO_SUBMIT event
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            //Then: setAsideDate should be set correctly
            assertThat(response.getData()).extracting("orderRequestedForReviewDefendant")
                .isEqualTo("Yes");
            assertThat(response.getData()).extracting("reasonForReconsiderationRespondent1")
                .extracting("reasonForReconsiderationTxt")
                .isEqualTo("Comments from defendant");
            // link to document is shown in confirmation page, so we always need it
            assertThat(response.getData()).extracting("requestForReconsiderationDocumentRes")
                .extracting("documentName")
                .isEqualTo(caseDocument.getDocumentName());
        }
    }

    @Nested
    class SubmittedCallback {
        @Test
        void whenSubmitted_thenIncludeHeader() {
            CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
            CallbackParams params = CallbackParams.builder()
                .caseData(caseData)
                .type(CallbackType.SUBMITTED)
                .build();
            SubmittedCallbackResponse response =
                (SubmittedCallbackResponse) handler.handle(params);
            assertThat(response.getConfirmationHeader()).isEqualTo("# Your request has been submitted");
            assertThat(response.getConfirmationBody()).isEqualTo(CONFIRMATION_BODY);
        }
    }

}

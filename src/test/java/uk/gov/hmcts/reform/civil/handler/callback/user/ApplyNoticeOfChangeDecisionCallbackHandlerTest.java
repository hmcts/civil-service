package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.noc.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.civil.model.noc.DecisionRequest;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@SpringBootTest(classes = {
    ApplyNoticeOfChangeDecisionCallbackHandler.class,
    JacksonAutoConfiguration.class
})
public class ApplyNoticeOfChangeDecisionCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private ApplyNoticeOfChangeDecisionCallbackHandler handler;

    @MockBean
    private CaseAssignmentApi caseAssignmentApi;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String CHANGE_ORGANISATION_REQUEST_FIELD = "changeOrganisationRequestField";
    private static final String ORG_ID_FOR_AUTO_APPROVAL =
        "org id to persist updated change organisation request field";
    private static final String RESPONDENT_ONE_ORG_POLICY  = "respondent1OrganisationPolicy";
    private static final String RESPONDENT_TWO_ORG_POLICY  = "respondent2OrganisationPolicy";
    private static final String APPLICANT_ONE_ORG_POLICY  = "applicant1OrganisationPolicy";
    private static final String NEW_ORG_ID = "new org id";

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(handler, "objectMapper", new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    }

    @Nested
    class AboutToSubmitCallback {

        @Nested
        class OneVOne {

            @Test
            void shouldApplyNoticeOfChange_whenInvokedByRespondent1For1v1Represented() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .changeOrganisationRequestField(false, false, "1234", "QWERTY R")
                    .build();
                CallbackParams params = callbackParamsOf(caseData,
                                                         CaseDetails.builder().data(caseData.toMap(mapper)).build(),
                                                         ABOUT_TO_SUBMIT);

                CaseDetails caseDetailsAfterNoCApplied =
                    caseDetailsAfterNoCApplied(CaseDetails.builder().data(caseData.toMap(mapper)).build(),
                                                                                    RESPONDENT_ONE_ORG_POLICY);

                when(caseAssignmentApi.applyDecision(params.getParams().get(BEARER_TOKEN).toString(),
                                                     authTokenGenerator.generate(),
                                                     DecisionRequest.decisionRequest(
                                                         params.getRequest().getCaseDetails())))
                    .thenReturn(
                        AboutToStartOrSubmitCallbackResponse.builder()
                        .data(caseDetailsAfterNoCApplied.getData()).build());

                AboutToStartOrSubmitCallbackResponse response =
                    (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertChangeOrganisationFieldIsUpdated(response);
                assertOrgIDIsUpdated(response, RESPONDENT_ONE_ORG_POLICY);
                assertCamundaEventIsReady(response);
            }

            @Test
            void shouldApplyNoticeOfChange_whenInvokedByRespondent1For1v1RepresentedOldOrgNullCopyExists() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .changeOrganisationRequestField(false, false, "1234", null)
                    .build();
                CallbackParams params = callbackParamsOf(caseData,
                                                         CaseDetails.builder().data(caseData.toMap(mapper)).build(),
                                                         ABOUT_TO_SUBMIT);

                CaseDetails caseDetailsAfterNoCApplied =
                    caseDetailsAfterNoCApplied(CaseDetails.builder().data(caseData.toMap(mapper)).build(),
                                               RESPONDENT_ONE_ORG_POLICY);

                when(caseAssignmentApi.applyDecision(params.getParams().get(BEARER_TOKEN).toString(),
                                                     authTokenGenerator.generate(),
                                                     DecisionRequest.decisionRequest(
                                                         params.getRequest().getCaseDetails())))
                    .thenReturn(
                        AboutToStartOrSubmitCallbackResponse.builder()
                            .data(caseDetailsAfterNoCApplied.getData()).build());

                AboutToStartOrSubmitCallbackResponse response =
                    (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertChangeOrganisationFieldIsUpdated(response);
                assertOrgIDIsUpdated(response, RESPONDENT_ONE_ORG_POLICY);
                assertCamundaEventIsReady(response);
            }

            @Test
            void shouldApplyNoticeOfChange_whenInvokedByRespondent1For1v1LiP() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP()
                    .changeOrganisationRequestField(false, false, "1234", null)
                    .build();
                caseData = caseData.toBuilder().respondent1OrganisationIDCopy(null).build();
                CallbackParams params = callbackParamsOf(caseData,
                                                         CaseDetails.builder().data(caseData.toMap(mapper)).build(),
                                                         ABOUT_TO_SUBMIT);

                CaseDetails caseDetailsAfterNoCApplied =
                    caseDetailsAfterNoCApplied(CaseDetails.builder().data(caseData.toMap(mapper)).build(),
                                               RESPONDENT_ONE_ORG_POLICY);

                when(caseAssignmentApi.applyDecision(params.getParams().get(BEARER_TOKEN).toString(),
                                                     authTokenGenerator.generate(),
                                                     DecisionRequest.decisionRequest(
                                                         params.getRequest().getCaseDetails())))
                    .thenReturn(
                        AboutToStartOrSubmitCallbackResponse.builder()
                        .data(caseDetailsAfterNoCApplied.getData()).build());

                AboutToStartOrSubmitCallbackResponse response =
                    (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertChangeOrganisationFieldIsUpdated(response);
                assertOrgIDIsUpdated(response, RESPONDENT_ONE_ORG_POLICY);
                assertCamundaEventIsReady(response);
            }

            @Test
            void shouldApplyNoticeOfChange_whenInvokedByApplicant1() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .changeOrganisationRequestField(true, false, "1234", "QWERTY A")
                    .build();
                CallbackParams params = callbackParamsOf(caseData,
                                                         CaseDetails.builder().data(caseData.toMap(mapper)).build(),
                                                         ABOUT_TO_SUBMIT);

                CaseDetails caseDetailsAfterNoCApplied =
                    caseDetailsAfterNoCApplied(CaseDetails.builder().data(caseData.toMap(mapper)).build(),
                                               APPLICANT_ONE_ORG_POLICY);

                when(caseAssignmentApi.applyDecision(params.getParams().get(BEARER_TOKEN).toString(),
                                                     authTokenGenerator.generate(),
                                                     DecisionRequest.decisionRequest(
                                                         params.getRequest().getCaseDetails())))
                    .thenReturn(
                        AboutToStartOrSubmitCallbackResponse.builder()
                        .data(caseDetailsAfterNoCApplied.getData()).build());

                AboutToStartOrSubmitCallbackResponse response =
                    (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertChangeOrganisationFieldIsUpdated(response);
                assertOrgIDIsUpdated(response, APPLICANT_ONE_ORG_POLICY);
                assertCamundaEventIsReady(response);
            }
        }

        @Nested
        class OneVTwo {

            @Test
            void shouldApplyNoticeOfChange_whenInvokedByApplicant1For1v2() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .changeOrganisationRequestField(true, false, "1234", "QWERTY A")
                    .build();
                CallbackParams params = callbackParamsOf(caseData,
                                                         CaseDetails.builder().data(caseData.toMap(mapper)).build(),
                                                         ABOUT_TO_SUBMIT);

                CaseDetails caseDetailsAfterNoCApplied =
                    caseDetailsAfterNoCApplied(CaseDetails.builder().data(caseData.toMap(mapper)).build(),
                                               APPLICANT_ONE_ORG_POLICY);

                when(caseAssignmentApi.applyDecision(params.getParams().get(BEARER_TOKEN).toString(),
                                                     authTokenGenerator.generate(),
                                                     DecisionRequest.decisionRequest(
                                                         params.getRequest().getCaseDetails())))
                    .thenReturn(
                        AboutToStartOrSubmitCallbackResponse.builder()
                        .data(caseDetailsAfterNoCApplied.getData()).build());

                AboutToStartOrSubmitCallbackResponse response =
                    (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertChangeOrganisationFieldIsUpdated(response);
                assertOrgIDIsUpdated(response, APPLICANT_ONE_ORG_POLICY);
                assertCamundaEventIsReady(response);
            }

            @Test
            void shouldApplyNoticeOfChange_whenInvokedByRespondent1For1v2DSRepresented() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .changeOrganisationRequestField(false, false, "1234", "QWERTY R")
                    .build();
                CallbackParams params = callbackParamsOf(caseData,
                                                         CaseDetails.builder().data(caseData.toMap(mapper)).build(),
                                                         ABOUT_TO_SUBMIT);

                CaseDetails caseDetailsAfterNoCApplied =
                    caseDetailsAfterNoCApplied(CaseDetails.builder().data(caseData.toMap(mapper)).build(),
                                               RESPONDENT_ONE_ORG_POLICY);

                when(caseAssignmentApi.applyDecision(params.getParams().get(BEARER_TOKEN).toString(),
                                                     authTokenGenerator.generate(),
                                                     DecisionRequest.decisionRequest(
                                                         params.getRequest().getCaseDetails())))
                    .thenReturn(
                        AboutToStartOrSubmitCallbackResponse.builder()
                        .data(caseDetailsAfterNoCApplied.getData()).build());

                AboutToStartOrSubmitCallbackResponse response =
                    (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertChangeOrganisationFieldIsUpdated(response);
                assertOrgIDIsUpdated(response, RESPONDENT_ONE_ORG_POLICY);
                assertCamundaEventIsReady(response);
            }

            @Test
            void shouldApplyNoticeOfChange_whenInvokedByRespondent2For1v2DSRepresented() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .changeOrganisationRequestField(false, true, "1234", "QWERTY R2")
                    .build();
                CallbackParams params = callbackParamsOf(caseData,
                                                         CaseDetails.builder().data(caseData.toMap(mapper)).build(),
                                                         ABOUT_TO_SUBMIT);

                CaseDetails caseDetailsAfterNoCApplied =
                    caseDetailsAfterNoCApplied(CaseDetails.builder().data(caseData.toMap(mapper)).build(),
                                               RESPONDENT_TWO_ORG_POLICY);

                when(caseAssignmentApi.applyDecision(params.getParams().get(BEARER_TOKEN).toString(),
                                                     authTokenGenerator.generate(),
                                                     DecisionRequest.decisionRequest(
                                                         params.getRequest().getCaseDetails())))
                    .thenReturn(
                        AboutToStartOrSubmitCallbackResponse.builder()
                        .data(caseDetailsAfterNoCApplied.getData()).build());

                AboutToStartOrSubmitCallbackResponse response =
                    (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertChangeOrganisationFieldIsUpdated(response);
                assertOrgIDIsUpdated(response, RESPONDENT_TWO_ORG_POLICY);
                assertCamundaEventIsReady(response);
            }

            @Test
            void shouldApplyNoticeOfChange_whenInvokedByRespondent1For1v2SSRepresented() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .multiPartyClaimOneDefendantSolicitor()
                    .changeOrganisationRequestField(false, false,
                                                    "1234", "QWERTY R")
                    .build();
                CallbackParams params = callbackParamsOf(caseData,
                                                         CaseDetails.builder().data(caseData.toMap(mapper)).build(),
                                                         ABOUT_TO_SUBMIT);

                CaseDetails caseDetailsAfterNoCApplied =
                    caseDetailsAfterNoCApplied(CaseDetails.builder().data(caseData.toMap(mapper)).build(),
                                               RESPONDENT_ONE_ORG_POLICY);

                when(caseAssignmentApi.applyDecision(
                    params.getParams().get(BEARER_TOKEN).toString(),
                    authTokenGenerator.generate(),
                    DecisionRequest.decisionRequest(
                        params.getRequest().getCaseDetails())))
                    .thenReturn(
                        AboutToStartOrSubmitCallbackResponse.builder()
                        .data(caseDetailsAfterNoCApplied.getData()).build());

                AboutToStartOrSubmitCallbackResponse response =
                    (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertChangeOrganisationFieldIsUpdated(response);
                assertOrgIDIsUpdated(response, RESPONDENT_ONE_ORG_POLICY);
                assertCamundaEventIsReady(response);
            }

            @Test
            void shouldApplyNoticeOfChange_whenInvokedByRespondent2For1v2SSRepresented() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .multiPartyClaimOneDefendantSolicitor()
                    .changeOrganisationRequestField(false, true, "1234", "QWERTY R2")
                    .build();
                CallbackParams params = callbackParamsOf(caseData,
                                                         CaseDetails.builder().data(caseData.toMap(mapper)).build(),
                                                         ABOUT_TO_SUBMIT);

                CaseDetails caseDetailsAfterNoCApplied =
                    caseDetailsAfterNoCApplied(CaseDetails.builder().data(caseData.toMap(mapper)).build(),
                                               RESPONDENT_TWO_ORG_POLICY);

                when(caseAssignmentApi.applyDecision(params.getParams().get(BEARER_TOKEN).toString(),
                                                     authTokenGenerator.generate(),
                                                     DecisionRequest.decisionRequest(
                                                         params.getRequest().getCaseDetails())))
                    .thenReturn(
                        AboutToStartOrSubmitCallbackResponse.builder()
                        .data(caseDetailsAfterNoCApplied.getData()).build());

                AboutToStartOrSubmitCallbackResponse response =
                    (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertChangeOrganisationFieldIsUpdated(response);
                assertOrgIDIsUpdated(response, RESPONDENT_TWO_ORG_POLICY);
                assertCamundaEventIsReady(response);
            }

            @Test
            void shouldApplyNoticeOfChange_whenInvokedByRespondent2For1v2DSOldOrgNullCopyExists() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .changeOrganisationRequestField(false, true, "1234", null)
                    .build();
                CallbackParams params = callbackParamsOf(caseData,
                                                         CaseDetails.builder().data(caseData.toMap(mapper)).build(),
                                                         ABOUT_TO_SUBMIT);

                CaseDetails caseDetailsAfterNoCApplied =
                    caseDetailsAfterNoCApplied(CaseDetails.builder().data(caseData.toMap(mapper)).build(),
                                               RESPONDENT_TWO_ORG_POLICY);

                when(caseAssignmentApi.applyDecision(params.getParams().get(BEARER_TOKEN).toString(),
                                                     authTokenGenerator.generate(),
                                                     DecisionRequest.decisionRequest(
                                                         params.getRequest().getCaseDetails()))).thenReturn(
                    AboutToStartOrSubmitCallbackResponse.builder()
                        .data(caseDetailsAfterNoCApplied.getData()).build()
                );

                AboutToStartOrSubmitCallbackResponse response =
                    (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertChangeOrganisationFieldIsUpdated(response);
                assertOrgIDIsUpdated(response, RESPONDENT_TWO_ORG_POLICY);
                assertCamundaEventIsReady(response);
            }

            @Test
            void shouldApplyNoticeOfChange_whenInvokedByRespondent2For1v2DSLiP() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v2Respondent2LiP()
                    .changeOrganisationRequestField(false, true, "1234", null)
                    .build();
                caseData = caseData.toBuilder().respondent2OrganisationIDCopy(null).build();
                CallbackParams params = callbackParamsOf(caseData,
                                                         CaseDetails.builder().data(caseData.toMap(mapper)).build(),
                                                         ABOUT_TO_SUBMIT);

                CaseDetails caseDetailsAfterNoCApplied =
                    caseDetailsAfterNoCApplied(CaseDetails.builder().data(caseData.toMap(mapper)).build(),
                                               RESPONDENT_TWO_ORG_POLICY);

                when(caseAssignmentApi.applyDecision(params.getParams().get(BEARER_TOKEN).toString(),
                                                     authTokenGenerator.generate(),
                                                     DecisionRequest.decisionRequest(
                                                         params.getRequest().getCaseDetails()))).thenReturn(
                                                             AboutToStartOrSubmitCallbackResponse.builder()
                                                                 .data(caseDetailsAfterNoCApplied.getData()).build()
                );

                AboutToStartOrSubmitCallbackResponse response =
                    (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertChangeOrganisationFieldIsUpdated(response);
                assertOrgIDIsUpdated(response, RESPONDENT_TWO_ORG_POLICY);
                assertCamundaEventIsReady(response);
            }
        }

        @NotNull
        private CaseDetails caseDetailsAfterNoCApplied(CaseDetails caseDetails, String applicantOrRespondentOrgPolicy) {
            caseDetails.getData().put(CHANGE_ORGANISATION_REQUEST_FIELD, null);
            caseDetails.getData().put(applicantOrRespondentOrgPolicy, OrganisationPolicy.builder()
                .organisation(Organisation.builder()
                                  .organisationID(NEW_ORG_ID)
                                  .build()).build());
            return caseDetails;
        }

        private void assertChangeOrganisationFieldIsUpdated(AboutToStartOrSubmitCallbackResponse response) {
            ChangeOrganisationRequest updatedCoR = mapper.convertValue(response.getData().get(
                CHANGE_ORGANISATION_REQUEST_FIELD), ChangeOrganisationRequest.class);

            assertThat(updatedCoR.getApprovalStatus()).isNull();
            assertThat(updatedCoR.getRequestTimestamp()).isNull();
            assertThat(updatedCoR.getCaseRoleId()).isNull();
            assertThat(updatedCoR.getOrganisationToRemove()).isNull();
            assertThat(updatedCoR.getOrganisationToAdd().getOrganisationID())
                .isEqualTo(ORG_ID_FOR_AUTO_APPROVAL);
        }

        private void assertOrgIDIsUpdated(AboutToStartOrSubmitCallbackResponse response,
                                          String applicantOrRespondentOrgPolicy) {
            OrganisationPolicy updatedOrgPolicy = mapper.convertValue(response.getData().get(
                applicantOrRespondentOrgPolicy), OrganisationPolicy.class);

            assertThat(updatedOrgPolicy.getOrganisation().getOrganisationID()).isEqualTo(NEW_ORG_ID);
        }

        private void assertCamundaEventIsReady(AboutToStartOrSubmitCallbackResponse response) {
            assertThat(response.getData())
                .containsEntry("businessProcess", Map.of(
                    "status", "READY",
                    "camundaEvent", "APPLY_NOC_DECISION"
                ));
        }
    }
}

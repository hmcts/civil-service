package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.cas.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.civil.cas.model.DecisionRequest;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.noc.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@ExtendWith(MockitoExtension.class)
class ApplyNoticeOfChangeDecisionCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private ApplyNoticeOfChangeDecisionCallbackHandler handler;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private CaseAssignmentApi caseAssignmentApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    private final ObjectMapper mapper = createObjectMapper();

    private static final String CHANGE_ORGANISATION_REQUEST_FIELD = "changeOrganisationRequestField";
    private static final String ORG_ID_FOR_AUTO_APPROVAL = "org id to persist updated change organisation request field";
    private static final String RESPONDENT_ONE_ORG_POLICY = "respondent1OrganisationPolicy";
    private static final String RESPONDENT_TWO_ORG_POLICY = "respondent2OrganisationPolicy";
    private static final String APPLICANT_ONE_ORG_POLICY = "applicant1OrganisationPolicy";
    private static final String NEW_ORG_ID = "new org id";
    private static final String REQUESTER_EMAIL = "requester.email@example.com";

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(handler, "objectMapper", mapper);
    }

    @Nested
    class AboutToSubmitCallback {

        @Nested
        class OneVOne {

            @Test
            void shouldApplyNoticeOfChange_whenInvokedByRespondent1For1v1Represented() {
                executeTest(
                    CaseDataBuilder.builder().atStateClaimIssued()
                        .changeOrganisationRequestField(false, false, "1234", "QWERTY R", REQUESTER_EMAIL)
                        .build(),
                    RESPONDENT_ONE_ORG_POLICY
                );
            }

            @Test
            void shouldApplyNoticeOfChange_whenInvokedByRespondent1For1v1RepresentedOldOrgNullCopyExists() {
                executeTest(
                    CaseDataBuilder.builder().atStateClaimIssued()
                        .changeOrganisationRequestField(false, false, "1234", null, REQUESTER_EMAIL)
                        .build(),
                    RESPONDENT_ONE_ORG_POLICY
                );
            }

            @Test
            void shouldApplyNoticeOfChange_whenInvokedByRespondent1For1v1LiP() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP()
                    .changeOrganisationRequestField(false, false, "1234", null, REQUESTER_EMAIL)
                    .build().toBuilder().respondent1OrganisationIDCopy(null).build();

                executeTest(caseData, RESPONDENT_ONE_ORG_POLICY, "APPLY_NOC_DECISION_DEFENDANT_LIP");
            }

            @Test
            void shouldApplyNoticeOfChange_whenInvokedByApplicant1() {
                executeTest(
                    CaseDataBuilder.builder().atStateClaimIssued()
                        .changeOrganisationRequestField(true, false, "1234", "QWERTY A", REQUESTER_EMAIL)
                        .build(),
                    APPLICANT_ONE_ORG_POLICY
                );
            }
        }

        @Nested
        class OneVTwo {

            @Test
            void shouldApplyNoticeOfChange_whenInvokedByApplicant1For1v2() {
                executeTest(
                    CaseDataBuilder.builder().atStateClaimIssued()
                        .multiPartyClaimTwoDefendantSolicitors()
                        .changeOrganisationRequestField(true, false, "1234", "QWERTY A", REQUESTER_EMAIL)
                        .build(),
                    APPLICANT_ONE_ORG_POLICY
                );
            }

            @Test
            void shouldApplyNoticeOfChange_whenInvokedByRespondent1For1v2DSRepresented() {
                executeTest(
                    CaseDataBuilder.builder().atStateClaimIssued()
                        .multiPartyClaimTwoDefendantSolicitors()
                        .changeOrganisationRequestField(false, false, "1234", "QWERTY R", REQUESTER_EMAIL)
                        .build(),
                    RESPONDENT_ONE_ORG_POLICY
                );
            }

            @Test
            void shouldApplyNoticeOfChange_whenInvokedByRespondent2For1v2DSRepresented() {
                executeTest(
                    CaseDataBuilder.builder().atStateClaimIssued()
                        .multiPartyClaimTwoDefendantSolicitors()
                        .changeOrganisationRequestField(false, true, "1234", "QWERTY R2", REQUESTER_EMAIL)
                        .build(),
                    RESPONDENT_TWO_ORG_POLICY
                );
            }

            @Test
            void shouldApplyNoticeOfChange_whenInvokedByRespondent1For1v2SSRepresented() {
                executeTest(
                    CaseDataBuilder.builder().atStateClaimIssued()
                        .multiPartyClaimOneDefendantSolicitor()
                        .changeOrganisationRequestField(false, false, "1234", "QWERTY R", REQUESTER_EMAIL)
                        .build(),
                    RESPONDENT_ONE_ORG_POLICY
                );
            }

            @Test
            void shouldApplyNoticeOfChange_whenInvokedByRespondent2For1v2SSRepresented() {
                executeTest(
                    CaseDataBuilder.builder().atStateClaimIssued()
                        .multiPartyClaimOneDefendantSolicitor()
                        .changeOrganisationRequestField(false, true, "1234", "QWERTY R2", REQUESTER_EMAIL)
                        .build(),
                    RESPONDENT_TWO_ORG_POLICY
                );
            }

            @Test
            void shouldApplyNoticeOfChange_whenInvokedByRespondent2For1v2DSOldOrgNullCopyExists() {
                executeTest(
                    CaseDataBuilder.builder().atStateClaimIssued()
                        .multiPartyClaimTwoDefendantSolicitors()
                        .changeOrganisationRequestField(false, true, "1234", null, REQUESTER_EMAIL)
                        .build(),
                    RESPONDENT_TWO_ORG_POLICY
                );
            }

            @Test
            void shouldApplyNoticeOfChange_whenInvokedByRespondent2For1v2DSLiP() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v2Respondent2LiP()
                    .changeOrganisationRequestField(false, true, "1234", null, REQUESTER_EMAIL)
                    .build().toBuilder().respondent2OrganisationIDCopy(null).build();

                executeTest(caseData, RESPONDENT_TWO_ORG_POLICY);
            }
        }

        @Test
        void shouldApplyNoticeOfChange_whenInvokedByApplicant1ForClaimantLip() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                .changeOrganisationRequestField(true, false, "1234", null, REQUESTER_EMAIL)
                .applicant1Represented(YesOrNo.NO)
                .build();

            executeTest(caseData, APPLICANT_ONE_ORG_POLICY, "APPLY_NOC_DECISION_LIP");
        }

        @Test
        void shouldApplyNoticeOfChange_whenInvokedByDefendant1ForDefendantLip() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                .changeOrganisationRequestField(false, false, "1234", null, REQUESTER_EMAIL)
                .applicant1Represented(YesOrNo.NO)
                .respondent1Represented(YesOrNo.NO)
                .build();

            executeTest(caseData, RESPONDENT_ONE_ORG_POLICY, "APPLY_NOC_DECISION_DEFENDANT_LIP");
        }

        @NotNull
        private CaseDetails caseDetailsAfterNoCApplied(CaseDetails caseDetails, String applicantOrRespondentOrgPolicy) {
            caseDetails.getData().put(CHANGE_ORGANISATION_REQUEST_FIELD,
                                      ChangeOrganisationRequest.builder().createdBy(REQUESTER_EMAIL).build());
            caseDetails.getData().put(applicantOrRespondentOrgPolicy, OrganisationPolicy.builder()
                .organisation(Organisation.builder().organisationID(NEW_ORG_ID).build()).build());
            return caseDetails;
        }

        private void executeTest(CaseData caseData, String applicantOrRespondentOrgPolicy) {
            executeTest(caseData, applicantOrRespondentOrgPolicy, "APPLY_NOC_DECISION");
        }

        private void executeTest(CaseData caseData, String applicantOrRespondentOrgPolicy, String camundaEvent) {
            CallbackParams params = callbackParamsOf(caseData,
                                                     CaseDetails.builder().data(caseData.toMap(mapper)).build(), ABOUT_TO_SUBMIT);

            CaseDetails caseDetailsAfterNoCApplied = caseDetailsAfterNoCApplied(
                CaseDetails.builder().data(caseData.toMap(mapper)).build(), applicantOrRespondentOrgPolicy);

            when(caseAssignmentApi.applyDecision(params.getParams().get(BEARER_TOKEN).toString(),
                                                 authTokenGenerator.generate(),
                                                 DecisionRequest.decisionRequest(params.getRequest().getCaseDetails())))
                .thenReturn(AboutToStartOrSubmitCallbackResponse.builder()
                                .data(caseDetailsAfterNoCApplied.getData()).build());

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertChangeOrganisationFieldIsUpdated(response);
            assertOrgIDIsUpdated(response, applicantOrRespondentOrgPolicy);
            assertCamundaEventIsReady(response, camundaEvent);
        }

        private void assertChangeOrganisationFieldIsUpdated(AboutToStartOrSubmitCallbackResponse response) {
            ChangeOrganisationRequest updatedCoR = mapper.convertValue(
                response.getData().get(CHANGE_ORGANISATION_REQUEST_FIELD), ChangeOrganisationRequest.class);

            assertThat(updatedCoR.getCreatedBy()).isEqualTo(REQUESTER_EMAIL);
            assertThat(updatedCoR.getApprovalStatus()).isNull();
            assertThat(updatedCoR.getRequestTimestamp()).isNull();
            assertThat(updatedCoR.getCaseRoleId()).isNull();
            assertThat(updatedCoR.getOrganisationToRemove()).isNull();
            assertThat(updatedCoR.getOrganisationToAdd().getOrganisationID())
                .isEqualTo(ORG_ID_FOR_AUTO_APPROVAL);
        }

        private void assertOrgIDIsUpdated(AboutToStartOrSubmitCallbackResponse response,
                                          String applicantOrRespondentOrgPolicy) {
            OrganisationPolicy updatedOrgPolicy = mapper.convertValue(
                response.getData().get(applicantOrRespondentOrgPolicy), OrganisationPolicy.class);

            assertThat(updatedOrgPolicy.getOrganisation().getOrganisationID()).isEqualTo(NEW_ORG_ID);
        }

        private void assertCamundaEventIsReady(AboutToStartOrSubmitCallbackResponse response, String camundaEvent) {
            assertThat(response.getData()).extracting("businessProcess")
                .extracting("status", "camundaEvent").contains("READY", camundaEvent);
        }
    }

    @Nested
    class ChangedOrgTest {

        private static final String ORG_ID = "123";

        @InjectMocks
        ApplyNoticeOfChangeDecisionCallbackHandler noticeOfChangeDecisionCallbackHandler;

        @Test
        void testGetChangedOrgReturnsOrganisationToRemoveId() {
            ChangeOrganisationRequest request = ChangeOrganisationRequest.builder()
                .caseRoleId(DynamicList.builder().value(DynamicListElement.builder()
                                                            .code(CaseRole.APPLICANTSOLICITORONE.getFormattedName()).build()).build())
                .organisationToRemove(Organisation.builder().organisationID(ORG_ID).build()).build();

            assertThat(noticeOfChangeDecisionCallbackHandler.getChangedOrg(CaseData.builder().build(), request))
                .isEqualTo(ORG_ID);
        }

        @Test
        void testGetChangedOrgReturnsRespondent1OrgIdCopy() {
            ChangeOrganisationRequest request = ChangeOrganisationRequest.builder()
                .caseRoleId(DynamicList.builder().value(DynamicListElement.builder()
                                                            .code(CaseRole.RESPONDENTSOLICITORONE.getFormattedName()).build()).build()).build();
            CaseData caseData = CaseData.builder().respondent1OrganisationIDCopy(ORG_ID).build();

            assertThat(noticeOfChangeDecisionCallbackHandler.getChangedOrg(caseData, request)).isEqualTo(ORG_ID);
        }

        @Test
        void testGetChangedOrgReturnsRespondent2OrgIdCopy() {
            ChangeOrganisationRequest request = ChangeOrganisationRequest.builder()
                .caseRoleId(DynamicList.builder().value(DynamicListElement.builder()
                                                            .code(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName()).build()).build()).build();
            CaseData caseData = CaseData.builder().respondent2OrganisationIDCopy(ORG_ID).build();

            assertThat(noticeOfChangeDecisionCallbackHandler.getChangedOrg(caseData, request)).isEqualTo(ORG_ID);
        }

        @Test
        void testGetChangedOrgReturnsRespondent1OrgIdCopyNull() {
            ChangeOrganisationRequest request = ChangeOrganisationRequest.builder()
                .caseRoleId(DynamicList.builder().value(DynamicListElement.builder()
                                                            .code(CaseRole.RESPONDENTSOLICITORONE.getFormattedName()).build()).build()).build();

            assertThat(noticeOfChangeDecisionCallbackHandler.getChangedOrg(CaseData.builder().build(), request)).isNull();
        }

        @Test
        void testGetChangedOrgReturnsRespondent2OrgIdCopyNull() {
            ChangeOrganisationRequest request = ChangeOrganisationRequest.builder()
                .caseRoleId(DynamicList.builder().value(DynamicListElement.builder()
                                                            .code(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName()).build()).build()).build();

            assertThat(noticeOfChangeDecisionCallbackHandler.getChangedOrg(CaseData.builder().build(), request)).isNull();
        }

        @Test
        void testGetChangedOrgApplicant() {
            ChangeOrganisationRequest request = ChangeOrganisationRequest.builder()
                .caseRoleId(DynamicList.builder().value(DynamicListElement.builder()
                                                            .code(CaseRole.APPLICANTSOLICITORONE.getFormattedName()).build()).build()).build();

            assertThat(noticeOfChangeDecisionCallbackHandler.getChangedOrg(CaseData.builder().build(), request)).isNull();
        }
    }

    private static ObjectMapper createObjectMapper() {
        return new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}

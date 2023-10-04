package uk.gov.hmcts.reform.civil.handler.callback.camunda.noticeofchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@SpringBootTest(classes = {
    UpdateCaseDetailsAfterNoCHandler.class,
    JacksonAutoConfiguration.class,
})
public class UpdateCaseDetailsAfterNoCHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private UpdateCaseDetailsAfterNoCHandler handler;

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    private static final String NEW_ORG_ID = "1234";

    @Nested
    class AboutToSubmit {

        @Test
        void shouldThrowError_whenOrgToAddIsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .changeOfRepresentation(true, false, null, "QWERTY A", null)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).containsExactly("Organisation to add is null");
        }

        @Test
        void shouldThrowError_whenChangeOfRepresentationIsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).containsExactly("No Notice of Change events recorded");
        }

        @Test
        void shouldThrowError_whenChangeOfRepresentationIsEmpty() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .build();

            caseData.toBuilder().changeOfRepresentation(null);

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).containsExactly("No Notice of Change events recorded");
        }

        @Test
        void shouldUpdateSolicitorDetails_afterNoCSubmittedByApplicantSolicitor1v1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .changeOfRepresentation(true, false, NEW_ORG_ID, "QWERTY A", null)
                .changeOrganisationRequestField(true, false, null, null, "requester@example.com")
                .updateOrgPolicyAfterNoC(true, false, NEW_ORG_ID)
                .setCaseListDisplayDefendantSolicitorReferences(true)
                .setUnassignedCaseListDisplayOrganisationReferences()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getApplicantSolicitor1ServiceAddress())
                .isEqualTo(null);
            assertThat(updatedCaseData.getApplicantSolicitor1ServiceAddressRequired())
                .isEqualTo(NO);

            assertThat(updatedCaseData.getApplicantSolicitor1PbaAccounts()).isNull();
            assertThat(updatedCaseData.getApplicantSolicitor1PbaAccountsIsEmpty()).isEqualTo(YES);
            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertSolicitorReferences(true, false, caseData, updatedCaseData);
            assertThat(updatedCaseData.getCaseListDisplayDefendantSolicitorReferences())
                .isEqualTo(updatedCaseData.getSolicitorReferences().getRespondentSolicitor1Reference());
            assertThat(updatedCaseData.getUnassignedCaseListDisplayOrganisationReferences()).isEmpty();
            assertThat(updatedCaseData.getApplicantSolicitor1UserDetails())
                .isEqualTo(IdamUserDetails.builder().email("requester@example.com").build());
        }

        @Test
        void shouldUpdateSolicitorDetails_afterNoCSubmittedByRespondentSolicitor1v1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .changeOfRepresentation(false, false, NEW_ORG_ID, "QWERTY R", null)
                .changeOrganisationRequestField(false, false, null, null, "requester@example.com")
                .updateOrgPolicyAfterNoC(true, false, NEW_ORG_ID)
                .setCaseListDisplayDefendantSolicitorReferences(true)
                .setUnassignedCaseListDisplayOrganisationReferences()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor1OrganisationDetails()).isEqualTo(null);
            assertThat(updatedCaseData.getRespondentSolicitor1ServiceAddress())
                .isEqualTo(null);
            assertThat(updatedCaseData.getRespondent1OrganisationIDCopy()).isEqualTo(NEW_ORG_ID);
            assertThat(updatedCaseData.getRespondent1Represented()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondent1OrgRegistered()).isEqualTo(YES);
            assertSolicitorReferences(false, false, caseData, updatedCaseData);
            assertThat(updatedCaseData.getCaseListDisplayDefendantSolicitorReferences())
                .isBlank();
            assertThat(updatedCaseData.getUnassignedCaseListDisplayOrganisationReferences()).isEmpty();
            assertThat(updatedCaseData.getRespondentSolicitor1EmailAddress())
                .isEqualTo("requester@example.com");
            assertThat(updatedCaseData.getDefendant1LIPAtClaimIssued()).isEqualTo(NO);
        }

        @Test
        void shouldUpdateSolicitorDetails_afterNoCSubmittedByRespondentSolicitor1v1LiP() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued1v1LiP()
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .changeOfRepresentation(false, false, NEW_ORG_ID, null, null)
                .changeOrganisationRequestField(true, false, null, null, "requester@example.com")
                .updateOrgPolicyAfterNoC(true, false, NEW_ORG_ID)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor1OrganisationDetails()).isEqualTo(null);
            assertThat(updatedCaseData.getRespondentSolicitor1ServiceAddress())
                .isEqualTo(null);
            assertSolicitorReferences(false, false, caseData, updatedCaseData);
            assertThat(updatedCaseData.getRespondent1OrganisationIDCopy()).isEqualTo(NEW_ORG_ID);
            assertThat(updatedCaseData.getRespondent1Represented()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondent1OrgRegistered()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondentSolicitor1EmailAddress())
                .isEqualTo("requester@example.com");
            assertThat(updatedCaseData.getDefendant1LIPAtClaimIssued()).isEqualTo(NO);
        }

        @Test
        void shouldUpdateSolicitorDetails_afterNoCSubmittedByRespondentSolicitor2In1v2DiffSolicitorToDiffSolicitor() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .multiPartyClaimTwoDefendantSolicitors()
                .changeOfRepresentation(false, true, NEW_ORG_ID, "QWERTY R2", null)
                .changeOrganisationRequestField(false, true, null, null, "requester@example.com")
                .updateOrgPolicyAfterNoC(false, true, NEW_ORG_ID)
                .setCaseListDisplayDefendantSolicitorReferences(false)
                .setUnassignedCaseListDisplayOrganisationReferences()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor2OrganisationDetails()).isEqualTo(null);
            assertThat(updatedCaseData.getRespondentSolicitor2ServiceAddress())
                .isEqualTo(null);
            assertThat(updatedCaseData.getRespondent2OrganisationIDCopy()).isEqualTo(NEW_ORG_ID);
            assertThat(updatedCaseData.getRespondent2Represented()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondent2OrgRegistered()).isEqualTo(YES);
            assertThat(getMultiPartyScenario(updatedCaseData)).isEqualTo(ONE_V_TWO_TWO_LEGAL_REP);

            assertSolicitorReferences(false, true, caseData, updatedCaseData);
            assertThat(updatedCaseData.getCaseListDisplayDefendantSolicitorReferences())
                .isEqualTo(updatedCaseData.getSolicitorReferences().getRespondentSolicitor1Reference());
            assertThat(updatedCaseData.getUnassignedCaseListDisplayOrganisationReferences()).isEmpty();
            assertThat(updatedCaseData.getRespondentSolicitor2EmailAddress())
                .isEqualTo("requester@example.com");
            assertThat(updatedCaseData.getDefendant2LIPAtClaimIssued()).isEqualTo(NO);
        }

        @Test
        void shouldUpdateSolicitorDetails_afterNoCSubmittedByRespondentSolicitor2In1v2DiffSolicitorToSameSolicitor() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .multiPartyClaimTwoDefendantSolicitors()
                .changeOfRepresentation(false, true, "QWERTY R", "QWERTY R2", null)
                .changeOrganisationRequestField(false, true, null, null, "requester@example.com")
                .updateOrgPolicyAfterNoC(false, true, "QWERTY R")
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor2OrganisationDetails()).isEqualTo(null);
            assertThat(updatedCaseData.getRespondentSolicitor2ServiceAddress())
                .isEqualTo(null);
            assertSolicitorReferences(false, true, caseData, updatedCaseData);
            assertThat(updatedCaseData.getRespondent2OrganisationIDCopy()).isEqualTo("QWERTY R");
            assertThat(updatedCaseData.getRespondent2Represented()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondent2OrgRegistered()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondentSolicitor2EmailAddress())
                .isEqualTo("requester@example.com");
            assertThat(getMultiPartyScenario(updatedCaseData)).isEqualTo(ONE_V_TWO_ONE_LEGAL_REP);
            assertThat(updatedCaseData.getDefendant2LIPAtClaimIssued()).isEqualTo(NO);
        }

        @Test
        void shouldUpdateSolicitorDetails_afterNoCSubmittedByRespondentSolicitor2In1v2SameSolicitorToDiffSolicitor() {
            CaseData caseData = CaseDataBuilder.builder()
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .atStateClaimIssued()
                .multiPartyClaimOneDefendantSolicitor()
                .changeOfRepresentation(false, true, NEW_ORG_ID, "QWERTY R", null)
                .changeOrganisationRequestField(false, true, null, null, "requester@example.com")
                .updateOrgPolicyAfterNoC(false, true, NEW_ORG_ID)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor2OrganisationDetails()).isEqualTo(null);
            assertThat(updatedCaseData.getRespondentSolicitor2ServiceAddress())
                .isEqualTo(null);
            assertSolicitorReferences(false, true, caseData, updatedCaseData);
            assertThat(updatedCaseData.getRespondent2OrganisationIDCopy()).isEqualTo(NEW_ORG_ID);
            assertThat(updatedCaseData.getRespondent2Represented()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondent2OrgRegistered()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondentSolicitor2EmailAddress())
                .isEqualTo("requester@example.com");
            assertThat(getMultiPartyScenario(updatedCaseData)).isEqualTo(ONE_V_TWO_TWO_LEGAL_REP);
            assertThat(updatedCaseData.getDefendant2LIPAtClaimIssued()).isEqualTo(NO);
        }

        @Test
        void shouldUpdateSolicitorDetails_afterNoCSubmittedByRespondentSolicitor2LiP() {
            CaseData caseData = CaseDataBuilder.builder()
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateClaimIssued1v2Respondent2LiP()
                .changeOfRepresentation(false, true, NEW_ORG_ID, null, null)
                .changeOrganisationRequestField(false, true, null, null, "requester@example.com")
                .updateOrgPolicyAfterNoC(false, true, NEW_ORG_ID)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor2OrganisationDetails()).isEqualTo(null);
            assertThat(updatedCaseData.getRespondentSolicitor2ServiceAddress())
                .isEqualTo(null);
            assertSolicitorReferences(false, true, caseData, updatedCaseData);
            assertThat(updatedCaseData.getRespondent2OrganisationIDCopy()).isEqualTo(NEW_ORG_ID);
            assertThat(updatedCaseData.getRespondent2Represented()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondent2OrgRegistered()).isEqualTo(YES);
            assertThat(getMultiPartyScenario(updatedCaseData)).isEqualTo(ONE_V_TWO_TWO_LEGAL_REP);
            assertThat(updatedCaseData.getRespondentSolicitor2EmailAddress())
                .isEqualTo("requester@example.com");
            assertThat(updatedCaseData.getDefendant2LIPAtClaimIssued()).isEqualTo(NO);
        }

        @Test
        void shouldUpdateSolicitorDetails_afterNoCSubmittedByRespondentSolicitor2BothRespondentsLiP() {
            CaseData caseData = CaseDataBuilder.builder()
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .multiPartyClaimTwoDefendantsLiP()
                .atStateClaimIssued1v2Respondent2LiP()
                .atStateClaimIssued1v1LiP()
                .changeOfRepresentation(false, true, NEW_ORG_ID, null, null)
                .changeOrganisationRequestField(false, true, null, null, "requester@example.com")
                .updateOrgPolicyAfterNoC(false, true, NEW_ORG_ID)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor2OrganisationDetails()).isEqualTo(null);
            assertThat(updatedCaseData.getRespondentSolicitor2ServiceAddress())
                .isEqualTo(null);
            assertSolicitorReferences(false, true, caseData, updatedCaseData);
            assertThat(updatedCaseData.getRespondent2OrganisationIDCopy()).isEqualTo(NEW_ORG_ID);
            assertThat(updatedCaseData.getRespondent2Represented()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondent2OrgRegistered()).isEqualTo(YES);
            assertThat(getMultiPartyScenario(updatedCaseData)).isEqualTo(ONE_V_TWO_TWO_LEGAL_REP);
            assertThat(updatedCaseData.getRespondentSolicitor2EmailAddress())
                .isEqualTo("requester@example.com");
            assertThat(updatedCaseData.getDefendant2LIPAtClaimIssued()).isEqualTo(NO);
        }

        @Test
        void shouldUpdateSolicitorDetails_afterNoCSubmittedByApplicantSolicitor1v1Spec() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .changeOfRepresentation(true, false, NEW_ORG_ID, "QWERTY A", null)
                .changeOrganisationRequestField(true, false, null, null, "requester@example.com")
                .updateOrgPolicyAfterNoC(true, false, NEW_ORG_ID)
                .setCaseListDisplayDefendantSolicitorReferences(true)
                .setUnassignedCaseListDisplayOrganisationReferences()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getApplicantSolicitor1PbaAccounts()).isNull();
            assertThat(updatedCaseData.getApplicantSolicitor1PbaAccountsIsEmpty()).isEqualTo(YES);
            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();

            assertSolicitorReferences(true, false, caseData, updatedCaseData);
            assertThat(updatedCaseData.getCaseListDisplayDefendantSolicitorReferences())
                .isEqualTo(updatedCaseData.getSolicitorReferences().getRespondentSolicitor1Reference());
            assertThat(updatedCaseData.getUnassignedCaseListDisplayOrganisationReferences()).isEmpty();

            assertThat(updatedCaseData.getApplicantSolicitor1UserDetails().getEmail())
                .isEqualTo("requester@example.com");
        }

        @Test
        void shouldUpdateSolicitorDetails_afterNoCSubmittedByRespondentSolicitor1v1Spec() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .changeOfRepresentation(false, false, NEW_ORG_ID, "QWERTY R", null)
                .changeOrganisationRequestField(false, false, null, null, "requester@example.com")
                .updateOrgPolicyAfterNoC(true, false, NEW_ORG_ID)
                .setCaseListDisplayDefendantSolicitorReferences(true)
                .setUnassignedCaseListDisplayOrganisationReferences()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getSpecAoSRespondentCorrespondenceAddressdetails()).isEqualTo(null);
            assertThat(updatedCaseData.getSpecRespondent1Represented()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondent1OrgRegistered()).isEqualTo(YES);

            assertSolicitorReferences(false, false, caseData, updatedCaseData);
            assertThat(updatedCaseData.getCaseListDisplayDefendantSolicitorReferences())
                .isBlank();
            assertThat(updatedCaseData.getUnassignedCaseListDisplayOrganisationReferences()).isEmpty();
            assertThat(updatedCaseData.getRespondentSolicitor1EmailAddress())
                .isEqualTo("requester@example.com");
        }

        @Test
        void shouldUpdateSolicitorDetails_afterNoCSubmittedByRespondentSolicitor1v1LiPSpec() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued1v1LiP()
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .changeOfRepresentation(false, false, NEW_ORG_ID, null, null)
                .changeOrganisationRequestField(false, false, null, null, "requester@example.com")
                .updateOrgPolicyAfterNoC(true, false, NEW_ORG_ID)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getSpecAoSRespondentCorrespondenceAddressdetails()).isEqualTo(null);
            assertThat(updatedCaseData.getSpecRespondent1Represented()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondentSolicitor1OrganisationDetails()).isEqualTo(null);
            assertSolicitorReferences(false, false, caseData, updatedCaseData);
            assertThat(updatedCaseData.getRespondent1OrgRegistered()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondentSolicitor1EmailAddress())
                .isEqualTo("requester@example.com");
        }

        @Test
        void shouldUpdateSolicitorDetails_afterNoCSubmittedByRespondentSolicitor2In1v2DiffSolicitorToDiffSolicitorSpec() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .multiPartyClaimTwoDefendantSolicitors()
                .changeOfRepresentation(false, true, NEW_ORG_ID, "QWERTY R2", null)
                .changeOrganisationRequestField(false, true, null, null, "requester@example.com")
                .updateOrgPolicyAfterNoC(false, true, NEW_ORG_ID)
                .setCaseListDisplayDefendantSolicitorReferences(false)
                .setUnassignedCaseListDisplayOrganisationReferences()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor2OrganisationDetails()).isEqualTo(null);
            assertThat(updatedCaseData.getSpecAoSRespondentCorrespondenceAddressdetails()).isEqualTo(null);
            assertThat(updatedCaseData.getSpecRespondent2Represented()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondent2OrgRegistered()).isEqualTo(YES);
            assertThat(getMultiPartyScenario(updatedCaseData)).isEqualTo(ONE_V_TWO_TWO_LEGAL_REP);

            assertSolicitorReferences(false, true, caseData, updatedCaseData);
            assertThat(updatedCaseData.getCaseListDisplayDefendantSolicitorReferences())
                .isEqualTo(updatedCaseData.getSolicitorReferences().getRespondentSolicitor1Reference());
            assertThat(updatedCaseData.getUnassignedCaseListDisplayOrganisationReferences()).isEmpty();
            assertThat(updatedCaseData.getRespondentSolicitor2EmailAddress())
                .isEqualTo("requester@example.com");
        }

        @Test
        void shouldUpdateSolicitorDetails_afterNoCSubmittedByRespondentSolicitor2In1v2DiffSolicitorToSameSolicitorSpec() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .multiPartyClaimTwoDefendantSolicitors()
                .changeOfRepresentation(false, true, "QWERTY R", "QWERTY R2", null)
                .changeOrganisationRequestField(false, true, null, null, "requester@example.com")
                .updateOrgPolicyAfterNoC(false, true, "QWERTY R")
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor2OrganisationDetails()).isEqualTo(null);
            assertThat(updatedCaseData.getSpecAoSRespondentCorrespondenceAddressdetails()).isEqualTo(null);
            assertThat(updatedCaseData.getSpecRespondent2Represented()).isEqualTo(YES);
            assertSolicitorReferences(false, true, caseData, updatedCaseData);
            assertThat(updatedCaseData.getRespondent2OrgRegistered()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondentSolicitor2EmailAddress())
                .isEqualTo("requester@example.com");
            assertThat(getMultiPartyScenario(updatedCaseData)).isEqualTo(ONE_V_TWO_ONE_LEGAL_REP);
        }

        @Test
        void shouldUpdateSolicitorDetails_afterNoCSubmittedByRespondentSolicitor2In1v2SameSolicitorToDiffSolicitorSpec() {
            CaseData caseData = CaseDataBuilder.builder()
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .atStateClaimIssued()
                .multiPartyClaimOneDefendantSolicitor()
                .changeOfRepresentation(false, true, NEW_ORG_ID, "QWERTY R", null)
                .changeOrganisationRequestField(false, true, null, null, "requester@example.com")
                .updateOrgPolicyAfterNoC(false, true, NEW_ORG_ID)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor2OrganisationDetails()).isEqualTo(null);
            assertThat(updatedCaseData.getSpecAoSRespondentCorrespondenceAddressdetails()).isEqualTo(null);
            assertThat(updatedCaseData.getSpecRespondent2Represented()).isEqualTo(YES);
            assertSolicitorReferences(false, true, caseData, updatedCaseData);
            assertThat(updatedCaseData.getRespondent2OrgRegistered()).isEqualTo(YES);
            assertThat(getMultiPartyScenario(updatedCaseData)).isEqualTo(ONE_V_TWO_TWO_LEGAL_REP);
            assertThat(updatedCaseData.getRespondentSolicitor2EmailAddress())
                .isEqualTo("requester@example.com");
        }

        @Test
        void shouldUpdateSolicitorDetails_afterNoCSubmittedByRespondentSolicitor2LiPSpec() {
            CaseData caseData = CaseDataBuilder.builder()
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateClaimIssued1v2Respondent2LiP()
                .changeOfRepresentation(false, true, NEW_ORG_ID, null, null)
                .changeOrganisationRequestField(false, true, null, null, "requester@example.com")
                .updateOrgPolicyAfterNoC(false, true, NEW_ORG_ID)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor2OrganisationDetails()).isEqualTo(null);
            assertThat(updatedCaseData.getSpecAoSRespondentCorrespondenceAddressdetails()).isEqualTo(null);
            assertThat(updatedCaseData.getSpecRespondent2Represented()).isEqualTo(YES);
            assertSolicitorReferences(false, true, caseData, updatedCaseData);
            assertThat(updatedCaseData.getRespondent2OrgRegistered()).isEqualTo(YES);
            assertThat(getMultiPartyScenario(updatedCaseData)).isEqualTo(ONE_V_TWO_TWO_LEGAL_REP);
            assertThat(updatedCaseData.getRespondentSolicitor2EmailAddress())
                .isEqualTo("requester@example.com");
        }

        @Test
        void shouldUpdateSolicitorDetails_afterNoCSubmittedByRespondentSolicitor2BothRespondentsLiPSpec() {
            CaseData caseData = CaseDataBuilder.builder()
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .multiPartyClaimTwoDefendantsLiP()
                .atStateClaimIssued1v2Respondent2LiP()
                .atStateClaimIssued1v1LiP()
                .changeOfRepresentation(false, true, NEW_ORG_ID, null, null)
                .changeOrganisationRequestField(false, true, null, null, "requester@example.com")
                .updateOrgPolicyAfterNoC(false, true, NEW_ORG_ID)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor2OrganisationDetails()).isEqualTo(null);
            assertThat(updatedCaseData.getSpecAoSRespondentCorrespondenceAddressdetails()).isEqualTo(null);
            assertThat(updatedCaseData.getSpecRespondent2Represented()).isEqualTo(YES);
            assertSolicitorReferences(false, true, caseData, updatedCaseData);
            assertThat(updatedCaseData.getRespondent2OrgRegistered()).isEqualTo(YES);
            assertThat(getMultiPartyScenario(updatedCaseData)).isEqualTo(ONE_V_TWO_TWO_LEGAL_REP);
            assertThat(updatedCaseData.getRespondentSolicitor2EmailAddress())
                .isEqualTo("requester@example.com");
        }

        private void assertSolicitorReferences(boolean isApplicant, boolean respondent2Exists,
                                               CaseData caseData, CaseData updatedCaseData) {
            if (isApplicant) {
                assertThat(updatedCaseData.getSolicitorReferences().getApplicantSolicitor1Reference()).isNull();
                assertThat(updatedCaseData.getSolicitorReferences().getRespondentSolicitor1Reference())
                    .isEqualTo(caseData.getSolicitorReferences().getRespondentSolicitor1Reference());

                if (caseData.getSolicitorReferencesCopy() != null) {
                    assertThat(updatedCaseData.getSolicitorReferencesCopy().getApplicantSolicitor1Reference()).isNull();
                    assertThat(updatedCaseData.getSolicitorReferencesCopy().getRespondentSolicitor1Reference())
                        .isEqualTo(caseData.getSolicitorReferencesCopy().getRespondentSolicitor1Reference());
                }
                if (respondent2Exists) {
                    assertThat(updatedCaseData.getSolicitorReferences().getRespondentSolicitor2Reference())
                        .isEqualTo(caseData.getSolicitorReferences().getRespondentSolicitor2Reference());
                    assertThat(updatedCaseData.getSolicitorReferencesCopy().getRespondentSolicitor2Reference())
                        .isEqualTo(caseData.getSolicitorReferencesCopy().getRespondentSolicitor2Reference());
                }
            } else {
                if (respondent2Exists) {
                    assertThat(updatedCaseData.getSolicitorReferences().getApplicantSolicitor1Reference())
                        .isEqualTo(caseData.getSolicitorReferences().getApplicantSolicitor1Reference());
                    assertThat(updatedCaseData.getSolicitorReferences().getRespondentSolicitor1Reference())
                        .isEqualTo(caseData.getSolicitorReferences().getRespondentSolicitor1Reference());
                    assertThat(updatedCaseData.getSolicitorReferences().getRespondentSolicitor2Reference()).isNull();

                    if (caseData.getSolicitorReferencesCopy() != null) {
                        assertThat(updatedCaseData.getSolicitorReferencesCopy().getApplicantSolicitor1Reference())
                            .isEqualTo(caseData.getSolicitorReferencesCopy().getApplicantSolicitor1Reference());
                        assertThat(updatedCaseData.getSolicitorReferencesCopy().getRespondentSolicitor1Reference())
                            .isEqualTo(caseData.getSolicitorReferencesCopy().getRespondentSolicitor1Reference());
                        assertThat(updatedCaseData.getSolicitorReferencesCopy().getRespondentSolicitor2Reference())
                            .isNull();
                    }
                } else {
                    assertThat(updatedCaseData.getSolicitorReferences().getApplicantSolicitor1Reference())
                        .isEqualTo(caseData.getSolicitorReferences().getApplicantSolicitor1Reference());
                    assertThat(updatedCaseData.getSolicitorReferences().getRespondentSolicitor1Reference()).isNull();

                    if (caseData.getSolicitorReferencesCopy() != null) {
                        assertThat(updatedCaseData.getSolicitorReferencesCopy().getApplicantSolicitor1Reference())
                            .isEqualTo(caseData.getSolicitorReferencesCopy().getApplicantSolicitor1Reference());
                        assertThat(updatedCaseData.getSolicitorReferencesCopy().getRespondentSolicitor1Reference())
                            .isNull();
                    }
                    if (respondent2Exists) {
                        assertThat(updatedCaseData.getSolicitorReferences().getRespondentSolicitor2Reference())
                            .isEqualTo(caseData.getSolicitorReferences().getRespondentSolicitor2Reference());
                        assertThat(updatedCaseData.getSolicitorReferencesCopy().getRespondentSolicitor2Reference())
                            .isEqualTo(caseData.getSolicitorReferencesCopy().getRespondentSolicitor2Reference());
                    }
                }
            }
        }
    }
}

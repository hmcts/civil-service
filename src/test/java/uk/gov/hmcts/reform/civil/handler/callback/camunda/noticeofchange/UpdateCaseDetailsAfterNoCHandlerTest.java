package uk.gov.hmcts.reform.civil.handler.callback.camunda.noticeofchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.BeforeEach;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.model.welshenhancements.PreferredLanguage;
import uk.gov.hmcts.reform.civil.prd.client.OrganisationApi;
import uk.gov.hmcts.reform.civil.prd.model.ContactInformation;
import uk.gov.hmcts.reform.civil.prd.model.DxAddress;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.CLAIMANT_CLAIM_FORM;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
public class UpdateCaseDetailsAfterNoCHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private UpdateCaseDetailsAfterNoCHandler handler;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private CoreCaseUserService coreCaseUserService;

    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private OrganisationService organisationService;
    @Mock
    OrganisationApi organisationApi;

    private static final String NEW_ORG_ID = "1234";
    private static final ContactInformation CONTACT_INFORMATION = ContactInformation.builder()
        .addressLine1("line 1")
        .addressLine2("line 2")
        .postCode("AB1 2XY")
        .county("My county")
        .dxAddress(List.of(DxAddress.builder()
                               .dxNumber("DX 12345")
                               .build()))
        .build();
    private static final Organisation ORGANISATION = Organisation.builder()
        .organisationIdentifier("QWERTY R")
        .name("Org Name")
        .contactInformation(List.of(CONTACT_INFORMATION))
        .build();

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        handler = new UpdateCaseDetailsAfterNoCHandler(mapper, coreCaseUserService, featureToggleService, organisationService);
        mapper.registerModule(new JavaTimeModule());
    }

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
                .addApplicantLRIndividual("Legal", "Rep")
                .addRespondent1LRIndividual("Legal", "Rep")
                .setUnassignedCaseListDisplayOrganisationReferences()
                .anyRepresented(NO)
                .build().toBuilder()
                .qmRespondentSolicitor1Queries(CaseQueriesCollection.builder().partyName("Defendant").build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getApplicantSolicitor1ServiceAddress())
                .isNull();
            assertThat(updatedCaseData.getApplicantSolicitor1ServiceAddressRequired())
                .isEqualTo(NO);

            assertThat(updatedCaseData.getApplicantSolicitor1PbaAccounts()).isNull();
            assertThat(updatedCaseData.getApplicantSolicitor1PbaAccountsIsEmpty()).isEqualTo(YES);
            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertSolicitorReferences(true, false, false, caseData, updatedCaseData);
            assertThat(updatedCaseData.getCaseListDisplayDefendantSolicitorReferences())
                .isEqualTo(updatedCaseData.getSolicitorReferences().getRespondentSolicitor1Reference());
            assertThat(updatedCaseData.getUnassignedCaseListDisplayOrganisationReferences()).isEmpty();
            assertThat(updatedCaseData.getApplicantSolicitor1UserDetails())
                .isEqualTo(IdamUserDetails.builder().email("requester@example.com").build());
            assertThat(updatedCaseData.getApplicant1LRIndividuals()).isNull();
            assertThat(updatedCaseData.getRespondent1LRIndividuals()).isNotNull();
            assertThat(updatedCaseData.getQmRespondentSolicitor1Queries().getPartyName()).isEqualTo("Defendant");
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
                .addApplicantLRIndividual("Legal", "Rep")
                .addRespondent1LRIndividual("Legal", "Rep")
                .anyRepresented(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor1OrganisationDetails()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor1ServiceAddress())
                .isNull();
            assertThat(updatedCaseData.getRespondent1OrganisationIDCopy()).isEqualTo(NEW_ORG_ID);
            assertThat(updatedCaseData.getRespondent1Represented()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondent1OrgRegistered()).isEqualTo(YES);
            assertSolicitorReferences(false, true, false, caseData, updatedCaseData);
            assertThat(updatedCaseData.getCaseListDisplayDefendantSolicitorReferences())
                .isBlank();
            assertThat(updatedCaseData.getUnassignedCaseListDisplayOrganisationReferences()).isEmpty();
            assertThat(updatedCaseData.getRespondentSolicitor1EmailAddress())
                .isEqualTo("requester@example.com");
            assertThat(updatedCaseData.getDefendant1LIPAtClaimIssued()).isEqualTo(NO);
            assertThat(updatedCaseData.getApplicant1LRIndividuals()).isNotNull();
            assertThat(updatedCaseData.getRespondent1LRIndividuals()).isNull();
        }

        @Test
        void shouldUpdateSolicitorDetails_afterNoCSubmittedByRespondentSolicitor1v1LiP() {

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued1v1LiP()
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .changeOfRepresentation(false, false, NEW_ORG_ID, null, null)
                .changeOrganisationRequestField(true, false, null, null, "requester@example.com")
                .updateOrgPolicyAfterNoC(true, false, NEW_ORG_ID)
                .addApplicantLRIndividual("Legal", "Rep")
                .anyRepresented(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor1OrganisationDetails()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor1ServiceAddress())
                .isNull();
            assertSolicitorReferences(false, true, false, caseData, updatedCaseData);
            assertThat(updatedCaseData.getRespondent1OrganisationIDCopy()).isEqualTo(NEW_ORG_ID);
            assertThat(updatedCaseData.getRespondent1Represented()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondent1OrgRegistered()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondentSolicitor1EmailAddress())
                .isEqualTo("requester@example.com");
            assertThat(updatedCaseData.getDefendant1LIPAtClaimIssued()).isEqualTo(NO);
            assertThat(updatedCaseData.getApplicant1LRIndividuals()).isNotNull();
            assertThat(updatedCaseData.getRespondent1LRIndividuals()).isNull();
            assertThat(updatedCaseData.getAnyRepresented()).isEqualTo(YES);
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
                .addApplicantLRIndividual("Legal", "Rep")
                .addRespondent1LRIndividual("Legal", "Rep")
                .addRespondent2LRIndividual("Legal", "Rep")
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor2OrganisationDetails()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor2ServiceAddress())
                .isNull();
            assertThat(updatedCaseData.getRespondent2OrganisationIDCopy()).isEqualTo(NEW_ORG_ID);
            assertThat(updatedCaseData.getRespondent2Represented()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondent2OrgRegistered()).isEqualTo(YES);
            assertThat(getMultiPartyScenario(updatedCaseData)).isEqualTo(ONE_V_TWO_TWO_LEGAL_REP);

            assertSolicitorReferences(false, false, true, caseData, updatedCaseData);
            assertThat(updatedCaseData.getCaseListDisplayDefendantSolicitorReferences())
                .isEqualTo(updatedCaseData.getSolicitorReferences().getRespondentSolicitor1Reference());
            assertThat(updatedCaseData.getUnassignedCaseListDisplayOrganisationReferences()).isEmpty();
            assertThat(updatedCaseData.getRespondentSolicitor2EmailAddress())
                .isEqualTo("requester@example.com");
            assertThat(updatedCaseData.getDefendant2LIPAtClaimIssued()).isEqualTo(NO);
            assertThat(updatedCaseData.getApplicant1LRIndividuals()).isNotNull();
            assertThat(updatedCaseData.getRespondent1LRIndividuals()).isNotNull();
            assertThat(updatedCaseData.getRespondent2LRIndividuals()).isNull();
        }

        @Test
        void shouldUpdateSolicitorDetails_afterNoCSubmittedByRespondentSolicitor1In1v2DiffSolicitorToDiffSolicitor() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .multiPartyClaimTwoDefendantSolicitors()
                .changeOfRepresentation(false, false, NEW_ORG_ID, "QWERTY R", null)
                .changeOrganisationRequestField(false, false, null, null, "requester@example.com")
                .updateOrgPolicyAfterNoC(false, false, NEW_ORG_ID)
                .setCaseListDisplayDefendantSolicitorReferences(false)
                .setUnassignedCaseListDisplayOrganisationReferences()
                .addApplicantLRIndividual("Legal", "Rep")
                .addRespondent1LRIndividual("Legal", "Rep")
                .addRespondent2LRIndividual("Legal", "Rep")
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor1OrganisationDetails()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor1ServiceAddress())
                .isNull();
            assertThat(updatedCaseData.getRespondent1OrganisationIDCopy()).isEqualTo(NEW_ORG_ID);
            assertThat(updatedCaseData.getRespondent1Represented()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondent1OrgRegistered()).isEqualTo(YES);
            assertThat(getMultiPartyScenario(updatedCaseData)).isEqualTo(ONE_V_TWO_TWO_LEGAL_REP);

            assertSolicitorReferences(false, true, false, caseData, updatedCaseData);
            assertThat(updatedCaseData.getCaseListDisplayDefendantSolicitorReferences())
                .isEqualTo(updatedCaseData.getSolicitorReferences().getRespondentSolicitor2Reference());
            assertThat(updatedCaseData.getUnassignedCaseListDisplayOrganisationReferences()).isEmpty();
            assertThat(updatedCaseData.getRespondentSolicitor1EmailAddress())
                .isEqualTo("requester@example.com");
            assertThat(updatedCaseData.getApplicant1LRIndividuals()).isNotNull();
            assertThat(updatedCaseData.getRespondent1LRIndividuals()).isNull();
            assertThat(updatedCaseData.getRespondent2LRIndividuals()).isNotNull();
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
                .addApplicantLRIndividual("Legal", "Rep")
                .addRespondent1LRIndividual("Legal", "Rep")
                .addRespondent2LRIndividual("Legal", "Rep")
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor2OrganisationDetails()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor2ServiceAddress())
                .isNull();
            assertSolicitorReferences(false, false, true, caseData, updatedCaseData);
            assertThat(updatedCaseData.getRespondent2OrganisationIDCopy()).isEqualTo("QWERTY R");
            assertThat(updatedCaseData.getRespondent2Represented()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondent2OrgRegistered()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondentSolicitor2EmailAddress())
                .isEqualTo("requester@example.com");
            assertThat(getMultiPartyScenario(updatedCaseData)).isEqualTo(ONE_V_TWO_ONE_LEGAL_REP);
            assertThat(updatedCaseData.getDefendant2LIPAtClaimIssued()).isEqualTo(NO);
            assertThat(updatedCaseData.getApplicant1LRIndividuals()).isNotNull();
            assertThat(updatedCaseData.getRespondent1LRIndividuals()).isNotNull();
            assertThat(updatedCaseData.getRespondent2LRIndividuals()).isNull();
        }

        @Test
        void shouldUpdateSolicitorDetails_afterNoCSubmittedByRespondentSolicitor1In1v2DiffSolicitorToSameSolicitor() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .multiPartyClaimTwoDefendantSolicitors()
                .changeOfRepresentation(false, false, "QWERTY R2", "QWERTY R", null)
                .changeOrganisationRequestField(false, false, null, null, "requester@example.com")
                .updateOrgPolicyAfterNoC(false, false, "QWERTY R2")
                .addApplicantLRIndividual("Legal", "Rep")
                .addRespondent1LRIndividual("Legal", "Rep")
                .addRespondent2LRIndividual("Legal", "Rep2")
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor1OrganisationDetails()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor1ServiceAddress())
                .isNull();
            assertSolicitorReferences(false, true, true, caseData, updatedCaseData);
            assertThat(updatedCaseData.getRespondent1OrganisationIDCopy()).isEqualTo("QWERTY R2");
            assertThat(updatedCaseData.getRespondent1Represented()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondent1OrgRegistered()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondentSolicitor1EmailAddress())
                .isEqualTo("requester@example.com");
            assertThat(getMultiPartyScenario(updatedCaseData)).isEqualTo(ONE_V_TWO_ONE_LEGAL_REP);
            assertThat(updatedCaseData.getApplicant1LRIndividuals()).isNotNull();
            assertThat(updatedCaseData.getRespondent1LRIndividuals()).isEqualTo(caseData.getRespondent2LRIndividuals());
            assertThat(updatedCaseData.getRespondent2LRIndividuals()).isNull();
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
                .addApplicantLRIndividual("Legal", "Rep")
                .addRespondent1LRIndividual("Legal", "Rep")
                .build().toBuilder()
                .qmRespondentSolicitor1Queries(CaseQueriesCollection.builder().partyName("Defendant").build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor2OrganisationDetails()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor2ServiceAddress())
                .isNull();
            assertSolicitorReferences(false, false, true, caseData, updatedCaseData);
            assertThat(updatedCaseData.getRespondent2OrganisationIDCopy()).isEqualTo(NEW_ORG_ID);
            assertThat(updatedCaseData.getRespondent2Represented()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondent2OrgRegistered()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondentSolicitor2EmailAddress())
                .isEqualTo("requester@example.com");
            assertThat(getMultiPartyScenario(updatedCaseData)).isEqualTo(ONE_V_TWO_TWO_LEGAL_REP);
            assertThat(updatedCaseData.getDefendant2LIPAtClaimIssued()).isEqualTo(NO);
            assertThat(updatedCaseData.getApplicant1LRIndividuals()).isNotNull();
            assertThat(updatedCaseData.getRespondent1LRIndividuals()).isNotNull();
            assertThat(updatedCaseData.getRespondent2LRIndividuals()).isNull();
            assertThat(updatedCaseData.getQmRespondentSolicitor1Queries().getPartyName()).isEqualTo("Defendant 1");
        }

        @Test
        void shouldUpdateSolicitorDetails_afterNoCSubmittedByRespondentSolicitor1In1v2SameSolicitorToDiffSolicitor() {
            CaseData caseData = CaseDataBuilder.builder()
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .atStateClaimIssued()
                .multiPartyClaimOneDefendantSolicitor()
                .changeOfRepresentation(false, false, NEW_ORG_ID, "QWERTY R", null)
                .changeOrganisationRequestField(false, false, null, null, "requester@example.com")
                .updateOrgPolicyAfterNoC(false, false, NEW_ORG_ID)
                .addApplicantLRIndividual("Legal", "Rep")
                .addRespondent1LRIndividual("Legal", "Rep1")
                .build().toBuilder()
                .qmRespondentSolicitor1Queries(CaseQueriesCollection.builder().partyName("Defendant").build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor1OrganisationDetails()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor1ServiceAddress())
                .isNull();
            assertSolicitorReferences(false, true, true, caseData, updatedCaseData);
            assertThat(updatedCaseData.getRespondent1OrganisationIDCopy()).isEqualTo(NEW_ORG_ID);
            assertThat(updatedCaseData.getRespondent1Represented()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondent1OrgRegistered()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondentSolicitor1EmailAddress())
                .isEqualTo("requester@example.com");
            assertThat(getMultiPartyScenario(updatedCaseData)).isEqualTo(ONE_V_TWO_TWO_LEGAL_REP);
            assertThat(updatedCaseData.getApplicant1LRIndividuals()).isNotNull();
            assertThat(updatedCaseData.getRespondent1LRIndividuals()).isNull();
            assertThat(updatedCaseData.getRespondent2LRIndividuals()).isEqualTo(caseData.getRespondent1LRIndividuals());
            assertThat(updatedCaseData.getQmRespondentSolicitor1Queries().getPartyName()).isEqualTo("Defendant 1");
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
                .addApplicantLRIndividual("Legal", "Rep")
                .addRespondent1LRIndividual("Legal", "Rep")
                .anyRepresented(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor2OrganisationDetails()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor2ServiceAddress())
                .isNull();
            assertSolicitorReferences(false, false, true, caseData, updatedCaseData);
            assertThat(updatedCaseData.getRespondent2OrganisationIDCopy()).isEqualTo(NEW_ORG_ID);
            assertThat(updatedCaseData.getRespondent2Represented()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondent2OrgRegistered()).isEqualTo(YES);
            assertThat(getMultiPartyScenario(updatedCaseData)).isEqualTo(ONE_V_TWO_TWO_LEGAL_REP);
            assertThat(updatedCaseData.getRespondentSolicitor2EmailAddress())
                .isEqualTo("requester@example.com");
            assertThat(updatedCaseData.getDefendant2LIPAtClaimIssued()).isEqualTo(NO);
            assertThat(updatedCaseData.getApplicant1LRIndividuals()).isNotNull();
            assertThat(updatedCaseData.getRespondent1LRIndividuals()).isNotNull();
            assertThat(updatedCaseData.getRespondent2LRIndividuals()).isNull();
            assertThat(updatedCaseData.getAnyRepresented()).isEqualTo(YES);
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
                .addApplicantLRIndividual("Legal", "Rep")
                .anyRepresented(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor2OrganisationDetails()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor2ServiceAddress())
                .isNull();
            assertSolicitorReferences(false, false, true, caseData, updatedCaseData);
            assertThat(updatedCaseData.getRespondent2OrganisationIDCopy()).isEqualTo(NEW_ORG_ID);
            assertThat(updatedCaseData.getRespondent2Represented()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondent2OrgRegistered()).isEqualTo(YES);
            assertThat(getMultiPartyScenario(updatedCaseData)).isEqualTo(ONE_V_TWO_TWO_LEGAL_REP);
            assertThat(updatedCaseData.getRespondentSolicitor2EmailAddress())
                .isEqualTo("requester@example.com");
            assertThat(updatedCaseData.getDefendant2LIPAtClaimIssued()).isEqualTo(NO);
            assertThat(updatedCaseData.getApplicant1LRIndividuals()).isNotNull();
            assertThat(updatedCaseData.getRespondent1LRIndividuals()).isNull();
            assertThat(updatedCaseData.getRespondent2LRIndividuals()).isNull();
            assertThat(updatedCaseData.getAnyRepresented()).isEqualTo(YES);
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
                .addApplicantLRIndividual("Legal", "Rep")
                .addRespondent1LRIndividual("Legal", "Rep")
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getApplicantSolicitor1PbaAccounts()).isNull();
            assertThat(updatedCaseData.getApplicantSolicitor1PbaAccountsIsEmpty()).isEqualTo(YES);
            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();

            assertSolicitorReferences(true, false, false, caseData, updatedCaseData);
            assertThat(updatedCaseData.getCaseListDisplayDefendantSolicitorReferences())
                .isEqualTo(updatedCaseData.getSolicitorReferences().getRespondentSolicitor1Reference());
            assertThat(updatedCaseData.getUnassignedCaseListDisplayOrganisationReferences()).isEmpty();

            assertThat(updatedCaseData.getApplicantSolicitor1UserDetails().getEmail())
                .isEqualTo("requester@example.com");
            assertThat(updatedCaseData.getApplicant1LRIndividuals()).isNull();
            assertThat(updatedCaseData.getRespondent1LRIndividuals()).isNotNull();
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
                .addApplicantLRIndividual("Legal", "Rep")
                .addRespondent1LRIndividual("Legal", "Rep")
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getSpecAoSRespondentCorrespondenceAddressdetails()).isNull();
            assertThat(updatedCaseData.getSpecRespondent1Represented()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondent1OrgRegistered()).isEqualTo(YES);

            assertSolicitorReferences(false, true, false, caseData, updatedCaseData);
            assertThat(updatedCaseData.getCaseListDisplayDefendantSolicitorReferences())
                .isBlank();
            assertThat(updatedCaseData.getUnassignedCaseListDisplayOrganisationReferences()).isEmpty();
            assertThat(updatedCaseData.getRespondentSolicitor1EmailAddress())
                .isEqualTo("requester@example.com");
            assertThat(updatedCaseData.getApplicant1LRIndividuals()).isNotNull();
            assertThat(updatedCaseData.getRespondent1LRIndividuals()).isNull();
        }

        @Test
        void shouldUpdateSolicitorDetails_afterNoCSubmittedByRespondentSolicitor1v1LiPSpec() {

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued1v1LiP()
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .changeOfRepresentation(false, false, NEW_ORG_ID, null, null)
                .changeOrganisationRequestField(false, false, null, null, "requester@example.com")
                .updateOrgPolicyAfterNoC(true, false, NEW_ORG_ID)
                .addApplicantLRIndividual("Legal", "Rep")
                .anyRepresented(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getSpecAoSRespondentCorrespondenceAddressdetails()).isNull();
            assertThat(updatedCaseData.getSpecRespondent1Represented()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondentSolicitor1OrganisationDetails()).isNull();
            assertSolicitorReferences(false, true, false, caseData, updatedCaseData);
            assertThat(updatedCaseData.getRespondent1OrgRegistered()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondentSolicitor1EmailAddress())
                .isEqualTo("requester@example.com");
            assertThat(updatedCaseData.getApplicant1LRIndividuals()).isNotNull();
            assertThat(updatedCaseData.getRespondent1LRIndividuals()).isNull();
            assertThat(updatedCaseData.getAnyRepresented()).isEqualTo(YES);
        }

        @Test
        void shouldUpdateSolicitorDetails_afterNoCSubmittedByRespondentSolicitor1v1LiPSpecForLiPNoC() {
            when(featureToggleService.isDefendantNoCOnlineForCase(any())).thenReturn(true);
            when(organisationService.findOrganisationById(any())).thenReturn(Optional.of(ORGANISATION));

            CaseData caseData = CaseDataBuilder.builder()
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .changeOfRepresentation(false, false, NEW_ORG_ID, null, null)
                .changeOrganisationRequestField(false, false, null, null, "requester@example.com")
                .updateOrgPolicyAfterNoC(true, false, NEW_ORG_ID)
                .anyRepresented(NO)
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                                    .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder().organisationID("QWERTY R").build())
                                                    .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITORONE]")
                                                    .build())
                .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder().documentType(SEALED_CLAIM).build(),
                                                           CaseDocument.builder().documentType(CLAIMANT_CLAIM_FORM).build()))
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getSpecAoSRespondentCorrespondenceAddressdetails()).isNull();
            assertThat(updatedCaseData.getSpecRespondent1Represented()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondentSolicitor1OrganisationDetails()).isNull();
            assertThat(updatedCaseData.getRespondent1OrgRegistered()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondentSolicitor1EmailAddress())
                .isEqualTo("requester@example.com");
            assertThat(updatedCaseData.getAnyRepresented()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondentSolicitorDetails().getOrgName()).isEqualTo(ORGANISATION.getName());
            assertThat(updatedCaseData.getRespondentSolicitorDetails().getAddress().getAddressLine1()).isEqualTo(ORGANISATION.getContactInformation().get(0).getAddressLine1());
            assertThat(updatedCaseData.getRespondentSolicitorDetails().getAddress().getAddressLine2()).isEqualTo(ORGANISATION.getContactInformation().get(0).getAddressLine2());
            assertThat(updatedCaseData.getRespondentSolicitorDetails().getAddress().getAddressLine2()).isEqualTo(ORGANISATION.getContactInformation().get(0).getAddressLine2());
            assertThat(updatedCaseData.getRespondentSolicitorDetails().getAddress().getPostCode()).isEqualTo(ORGANISATION.getContactInformation().get(0).getPostCode());
            assertThat(updatedCaseData.getRespondentSolicitorDetails().getAddress().getCounty()).isEqualTo(ORGANISATION.getContactInformation().get(0).getCounty());
            assertThat(updatedCaseData.getSystemGeneratedCaseDocuments().size()).isEqualTo(1);
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
                .addApplicantLRIndividual("Legal", "Rep")
                .addRespondent1LRIndividual("Legal", "Rep")
                .addRespondent2LRIndividual("Legal", "Rep")
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor2OrganisationDetails()).isNull();
            assertThat(updatedCaseData.getSpecAoSRespondentCorrespondenceAddressdetails()).isNull();
            assertThat(updatedCaseData.getSpecRespondent2Represented()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondent2OrgRegistered()).isEqualTo(YES);
            assertThat(getMultiPartyScenario(updatedCaseData)).isEqualTo(ONE_V_TWO_TWO_LEGAL_REP);

            assertSolicitorReferences(false, false, true, caseData, updatedCaseData);
            assertThat(updatedCaseData.getCaseListDisplayDefendantSolicitorReferences())
                .isEqualTo(updatedCaseData.getSolicitorReferences().getRespondentSolicitor1Reference());
            assertThat(updatedCaseData.getUnassignedCaseListDisplayOrganisationReferences()).isEmpty();
            assertThat(updatedCaseData.getRespondentSolicitor2EmailAddress())
                .isEqualTo("requester@example.com");
            assertThat(updatedCaseData.getApplicant1LRIndividuals()).isNotNull();
            assertThat(updatedCaseData.getRespondent1LRIndividuals()).isNotNull();
            assertThat(updatedCaseData.getRespondent2LRIndividuals()).isNull();
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
                .addApplicantLRIndividual("Legal", "Rep")
                .addRespondent1LRIndividual("Legal", "Rep")
                .addRespondent2LRIndividual("Legal", "Rep")
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor2OrganisationDetails()).isNull();
            assertThat(updatedCaseData.getSpecAoSRespondentCorrespondenceAddressdetails()).isNull();
            assertThat(updatedCaseData.getSpecRespondent2Represented()).isEqualTo(YES);
            assertSolicitorReferences(false, false, true, caseData, updatedCaseData);
            assertThat(updatedCaseData.getRespondent2OrgRegistered()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondentSolicitor2EmailAddress())
                .isEqualTo("requester@example.com");
            assertThat(getMultiPartyScenario(updatedCaseData)).isEqualTo(ONE_V_TWO_ONE_LEGAL_REP);
            assertThat(updatedCaseData.getApplicant1LRIndividuals()).isNotNull();
            assertThat(updatedCaseData.getRespondent1LRIndividuals()).isNotNull();
            assertThat(updatedCaseData.getRespondent2LRIndividuals()).isNull();
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
                .addApplicantLRIndividual("Legal", "Rep")
                .addRespondent1LRIndividual("Legal", "Rep")
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor2OrganisationDetails()).isNull();
            assertThat(updatedCaseData.getSpecAoSRespondentCorrespondenceAddressdetails()).isNull();
            assertThat(updatedCaseData.getSpecRespondent2Represented()).isEqualTo(YES);
            assertSolicitorReferences(false, false, true, caseData, updatedCaseData);
            assertThat(updatedCaseData.getRespondent2OrgRegistered()).isEqualTo(YES);
            assertThat(getMultiPartyScenario(updatedCaseData)).isEqualTo(ONE_V_TWO_TWO_LEGAL_REP);
            assertThat(updatedCaseData.getRespondentSolicitor2EmailAddress())
                .isEqualTo("requester@example.com");
            assertThat(updatedCaseData.getApplicant1LRIndividuals()).isNotNull();
            assertThat(updatedCaseData.getRespondent1LRIndividuals()).isNotNull();
            assertThat(updatedCaseData.getRespondent2LRIndividuals()).isNull();
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
                .addApplicantLRIndividual("Legal", "Rep")
                .addRespondent1LRIndividual("Legal", "Rep")
                .anyRepresented(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor2OrganisationDetails()).isNull();
            assertThat(updatedCaseData.getSpecAoSRespondentCorrespondenceAddressdetails()).isNull();
            assertThat(updatedCaseData.getSpecRespondent2Represented()).isEqualTo(YES);
            assertSolicitorReferences(false, false, true, caseData, updatedCaseData);
            assertThat(updatedCaseData.getRespondent2OrgRegistered()).isEqualTo(YES);
            assertThat(getMultiPartyScenario(updatedCaseData)).isEqualTo(ONE_V_TWO_TWO_LEGAL_REP);
            assertThat(updatedCaseData.getRespondentSolicitor2EmailAddress())
                .isEqualTo("requester@example.com");
            assertThat(updatedCaseData.getApplicant1LRIndividuals()).isNotNull();
            assertThat(updatedCaseData.getRespondent1LRIndividuals()).isNotNull();
            assertThat(updatedCaseData.getRespondent2LRIndividuals()).isNull();
            assertThat(updatedCaseData.getAnyRepresented()).isEqualTo(YES);
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
                .addApplicantLRIndividual("Legal", "Rep")
                .anyRepresented(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor2OrganisationDetails()).isNull();
            assertThat(updatedCaseData.getSpecAoSRespondentCorrespondenceAddressdetails()).isNull();
            assertThat(updatedCaseData.getSpecRespondent2Represented()).isEqualTo(YES);
            assertSolicitorReferences(false, false, true, caseData, updatedCaseData);
            assertThat(updatedCaseData.getRespondent2OrgRegistered()).isEqualTo(YES);
            assertThat(getMultiPartyScenario(updatedCaseData)).isEqualTo(ONE_V_TWO_TWO_LEGAL_REP);
            assertThat(updatedCaseData.getRespondentSolicitor2EmailAddress())
                .isEqualTo("requester@example.com");
            assertThat(updatedCaseData.getApplicant1LRIndividuals()).isNotNull();
            assertThat(updatedCaseData.getRespondent1LRIndividuals()).isNull();
            assertThat(updatedCaseData.getRespondent2LRIndividuals()).isNull();
            assertThat(updatedCaseData.getAnyRepresented()).isEqualTo(YES);
        }

        @Test
        void shouldUpdateSolicitorDetails_afterNocSubmittedByApplicantSolicitorForClaimantLip() {

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .changeOfRepresentation(true, false, NEW_ORG_ID, null, null)
                .changeOrganisationRequestField(true, false, null, null, "requester@example.com")
                .updateOrgPolicyAfterNoC(true, false, NEW_ORG_ID)
                .claimantUserDetails(IdamUserDetails.builder().email("xyz@hmcts.com").id("1234").build())
                .applicant1Represented(NO)
                .anyRepresented(NO)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedCaseData.getApplicantSolicitor1PbaAccounts()).isNull();
            assertThat(updatedCaseData.getApplicantSolicitor1PbaAccountsIsEmpty()).isEqualTo(YES);
            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getApplicantSolicitor1UserDetails().getEmail())
                .isEqualTo("requester@example.com");
            assertThat(updatedCaseData.getApplicant1Represented()).isEqualTo(YES);
            assertThat(updatedCaseData.getAnyRepresented()).isEqualTo(YES);
        }

        @Test
        void shouldResetLanguageFlag_afterNocSubmittedByApplicantSolicitorForClaimantLip() {
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                    .changeOfRepresentation(true, false, NEW_ORG_ID, null, null)
                    .changeOrganisationRequestField(true, false, null, null, "requester@example.com")
                    .updateOrgPolicyAfterNoC(true, false, NEW_ORG_ID)
                    .claimantUserDetails(IdamUserDetails.builder().email("xyz@hmcts.com").id("1234").build())
                    .applicant1Represented(NO)
                    .anyRepresented(NO)
                    .claimantBilingualLanguagePreference("WELSH")
                    .build();
            caseData = caseData.toBuilder().claimantLanguagePreferenceDisplay(PreferredLanguage.WELSH).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedCaseData.getClaimantBilingualLanguagePreference()).isNull();
            assertThat(updatedCaseData.getClaimantLanguagePreferenceDisplay()).isNull();
        }

        @Test
        void shouldResetLanguageFlag_afterNocSubmittedByDefendantSolicitorForDefendantLip() {
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                    .changeOfRepresentation(false, false, NEW_ORG_ID, null, null)
                    .changeOrganisationRequestField(true, false, null, null, "requester@example.com")
                    .updateOrgPolicyAfterNoC(true, false, NEW_ORG_ID)
                    .claimantUserDetails(IdamUserDetails.builder().email("xyz@hmcts.com").id("1234").build())
                    .applicant1Represented(NO)
                    .anyRepresented(NO)
                    .build();
            caseData = caseData.toBuilder()
                    .caseDataLiP(CaseDataLiP.builder()
                            .respondent1LiPResponse(
                                    RespondentLiPResponse.builder().respondent1ResponseLanguage("WELSH").build()
                            ).build())
                    .defendantLanguagePreferenceDisplay(PreferredLanguage.WELSH).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedCaseData.getCaseDataLiP().getRespondent1LiPResponse().getRespondent1ResponseLanguage()).isNull();
            assertThat(updatedCaseData.getDefendantLanguagePreferenceDisplay()).isNull();
        }

        @Test
        void shouldNotResetLanguageFlagIfWelshDisabled_afterNocSubmittedByApplicantSolicitorForClaimantLip() {
            when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
            CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimIssued()
                    .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                    .changeOfRepresentation(true, false, NEW_ORG_ID, null, null)
                    .changeOrganisationRequestField(true, false, null, null, "requester@example.com")
                    .updateOrgPolicyAfterNoC(true, false, NEW_ORG_ID)
                    .claimantUserDetails(IdamUserDetails.builder().email("xyz@hmcts.com").id("1234").build())
                    .applicant1Represented(NO)
                    .anyRepresented(NO)
                    .claimantBilingualLanguagePreference("WELSH")
                    .build();
            caseData = caseData.toBuilder().claimantLanguagePreferenceDisplay(PreferredLanguage.WELSH).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedCaseData.getClaimantBilingualLanguagePreference()).isEqualTo("WELSH");
            assertThat(updatedCaseData.getClaimantLanguagePreferenceDisplay()).isEqualTo(PreferredLanguage.WELSH);
        }

        private void assertSolicitorReferences(boolean isApplicant, boolean isRespondent1, boolean respondent2Exists,
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
            } else if (isRespondent1) {
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
                    if (caseData.getSolicitorReferencesCopy() != null) {
                        assertThat(updatedCaseData.getSolicitorReferencesCopy().getRespondentSolicitor2Reference())
                            .isEqualTo(caseData.getSolicitorReferencesCopy().getRespondentSolicitor2Reference());
                    }
                }
            } else {
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
            }
        }
    }
}

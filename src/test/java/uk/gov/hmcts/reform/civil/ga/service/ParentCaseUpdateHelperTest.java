package uk.gov.hmcts.reform.civil.ga.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLink;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GADetailsRespondentSol;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.time.LocalDateTime.now;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_CASE_WITH_GA_STATE;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.GENERAL_ORDER;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_ADD_PAYMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_ADDITIONAL_INFORMATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICATION_PAYMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_DIRECTIONS_ORDER_DOCS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_RESPONSE;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_WRITTEN_REPRESENTATIONS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.ORDER_MADE;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PENDING_APPLICATION_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.ga.service.ParentCaseUpdateHelper.DOCUMENT_STATES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class ParentCaseUpdateHelperTest {

    @InjectMocks
    private ParentCaseUpdateHelper parentCaseUpdateHelper;
    @Mock
    private GaCoreCaseDataService coreCaseDataService;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private FeatureToggleService featureToggleService;
    @Spy
    private ObjectMapper objectMapper = ObjectMapperFactory.instance();
    @Captor
    private ArgumentCaptor<Map<String, Object>> mapCaptor;

    @Test
    void updateParentApplicationVisibilityWithNewState() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(
                NO)
            .submittedOn(null).build();

        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(YES, NO));
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(caseData);

        parentCaseUpdateHelper.updateParentApplicationVisibilityWithNewState(caseData, ORDER_MADE.toString());
        verify(coreCaseDataService, times(1)).submitUpdate(any(), any());
    }

    @Test
    void updateParentApplicationVisibilityWithNewStateWithoutRespondentSol() {
        GeneralApplicationCaseData caseData = getCaseWithApplicationData(false);
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(YES, NO));
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(caseData);

        parentCaseUpdateHelper.updateParentApplicationVisibilityWithNewState(caseData, ORDER_MADE.toString());
        verify(coreCaseDataService, times(1)).submitUpdate(any(), any());

    }

    @Test
    void updateParentApplicationVisibilityWithNewStateWithRespondentSol() {
        GeneralApplicationCaseData caseData = getCaseWithApplicationData(true);
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(YES, NO));
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(caseData);

        parentCaseUpdateHelper.updateParentApplicationVisibilityWithNewState(caseData, ORDER_MADE.toString());
        verify(coreCaseDataService, times(1)).submitUpdate(any(), any());

    }

    @Test
    void updateCaseDocumentByType_null() {
        GeneralApplicationCaseData gaCase = getCaseWithApplicationDataAndGeneralOrder();
        GeneralApplicationCaseData civilCase = getCaseWithApplicationData(false);
        Map<String, Object> updateMap = new HashMap<>();
        try {
            parentCaseUpdateHelper.updateCaseDocumentByType(
                updateMap, "directionOrder", null,
                civilCase, gaCase
            );
            assertThat(updateMap.size()).isZero();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void updateCaseDocumentByType() {
        GeneralApplicationCaseData gaCase = getCaseWithApplicationDataAndGeneralOrder();
        GeneralApplicationCaseData civilCase = getCaseWithApplicationData(false);
        Map<String, Object> updateMap = new HashMap<>();
        try {
            parentCaseUpdateHelper.updateCaseDocumentByType(
                updateMap, "directionOrder", "RespondentSol",
                civilCase, gaCase
            );
            assertThat(updateMap).isNotNull();
            assertThat(updateMap.get("directionOrderDocRespondentSol")).isNotNull();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void updateCaseDocumentByRole() {
        GeneralApplicationCaseData gaCase = getCaseWithApplicationDataAndGeneralOrder();
        GeneralApplicationCaseData civilCase = getCaseWithApplicationData(false);
        Map<String, Object> updateMap = new HashMap<>();
        parentCaseUpdateHelper.updateCaseDocumentByRole(
            updateMap, "RespondentSol",
            civilCase, gaCase
        );
        assertThat(updateMap).isNotNull();
        assertThat(updateMap.get("directionOrderDocRespondentSol")).isNotNull();
    }

    @Test
    void updateCaseDocument() {
        GeneralApplicationCaseData gaCase = getCaseWithApplicationDataAndGeneralOrder();
        GeneralApplicationCaseData civilCase = getCaseWithApplicationData(false);
        Map<String, Object> updateMap = new HashMap<>();
        String[] roles = {"Claimant", "RespondentSol", null};
        parentCaseUpdateHelper.updateCaseDocument(
            updateMap,
            civilCase, gaCase, roles
        );
        assertThat(updateMap).isNotNull();
        assertThat(updateMap.get("directionOrderDocRespondentSol")).isNotNull();
        assertThat(updateMap.get("directionOrderDocClaimant")).isNotNull();
    }

    @Test
    void checkIfDocumentExists() {
        Element<?> same = Element.<CaseDocument>builder()
            .id(UUID.randomUUID())
            .value(new CaseDocument().setDocumentLink(new Document().setDocumentUrl("string"))).build();
        List<Element<?>> gaDocumentList = new ArrayList<>();
        List<Element<?>> civilCaseDocumentList = new ArrayList<>();
        gaDocumentList.add(same);
        assertThat(parentCaseUpdateHelper.checkIfDocumentExists(civilCaseDocumentList, gaDocumentList)).isEqualTo(0);
        civilCaseDocumentList.add(same);
        assertThat(parentCaseUpdateHelper.checkIfDocumentExists(civilCaseDocumentList, gaDocumentList)).isEqualTo(1);
    }

    @Test
    void checkIfDocumentExists_whenDocumentTypeIsDocumentClass() {
        Element<Document> documentElement = Element.<Document>builder()
            .id(UUID.randomUUID())
            .value(new Document().setDocumentUrl("string")).build();
        List<Element<?>> gaDocumentList = new ArrayList<>();
        List<Element<?>> civilCaseDocumentList = new ArrayList<>();
        gaDocumentList.add(documentElement);
        assertThat(parentCaseUpdateHelper.checkIfDocumentExists(civilCaseDocumentList, gaDocumentList)).isEqualTo(0);
        civilCaseDocumentList.add(documentElement);
        assertThat(parentCaseUpdateHelper.checkIfDocumentExists(civilCaseDocumentList, gaDocumentList)).isEqualTo(1);
    }

    @Test
    void updateParentWithGAState_Respond_Doc() {
        assertThat(DOCUMENT_STATES.size()).isEqualTo(5);
        assertThat(DOCUMENT_STATES.contains(AWAITING_ADDITIONAL_INFORMATION)).isTrue();
        assertThat(DOCUMENT_STATES.contains(AWAITING_WRITTEN_REPRESENTATIONS)).isTrue();
        assertThat(DOCUMENT_STATES.contains(AWAITING_DIRECTIONS_ORDER_DOCS)).isTrue();
        assertThat(DOCUMENT_STATES.contains(PENDING_APPLICATION_ISSUED)).isTrue();
        assertThat(DOCUMENT_STATES.contains(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)).isTrue();
    }

    @Test
    void findGaCreator() {
        String role = "Claimant";
        assertThat(parentCaseUpdateHelper
                       .findGaCreator(
                           getVaryMainCaseData(role),
                           getGaVaryCaseData(role, PENDING_APPLICATION_ISSUED, YES)
                       ))
            .isEqualTo(role);
        role = "RespondentSol";
        assertThat(parentCaseUpdateHelper
                       .findGaCreator(
                           getVaryMainCaseData(role),
                           getGaVaryCaseData(role, PENDING_APPLICATION_ISSUED, YES)
                       ))
            .isEqualTo(role);
        role = "RespondentSolTwo";
        assertThat(parentCaseUpdateHelper
                       .findGaCreator(
                           getVaryMainCaseData(role),
                           getGaVaryCaseData(role, PENDING_APPLICATION_ISSUED, YES)
                       ))
            .isEqualTo(role);
        assertThat(parentCaseUpdateHelper
                       .findGaCreator(
                           getVaryMainCaseData(role),
                           getGaVaryCaseData(role, PENDING_APPLICATION_ISSUED, YES).copy()
                               .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                                             .organisationIdentifier("Nothing").build()).build()
                       ))
            .isNull();

        role = "RespondentLip";
        assertThat(parentCaseUpdateHelper
                       .findGaCreator(
                           getVaryMainCaseData(role),
                           getGaVaryCaseData(role, PENDING_APPLICATION_ISSUED, NO)
                       ))
            .isEqualTo("RespondentSol");
    }

    @Test
    void updateSingleTypeByRoles() {
        GeneralApplicationCaseData gaCase = getCaseWithApplicationDataAndGeneralOrder();
        GeneralApplicationCaseData civilCase = getCaseWithApplicationData(false);
        Map<String, Object> updateMap = new HashMap<>();
        String[] roles = {"RespondentSol", "Staff"};
        try {
            parentCaseUpdateHelper.updateSingleTypeByRoles(
                updateMap, "directionOrder", roles,
                civilCase, gaCase
            );
            assertThat(updateMap).isNotNull();
            assertThat(updateMap.get("directionOrderDocRespondentSol")).isNotNull();
            assertThat(updateMap.get("directionOrderDocStaff")).isNotNull();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void updateN245_before() {
        Map<String, Object> updateMap = new HashMap<>();
        String role = "RespondentSolTwo";
        String[] docVisibilityRoles = {"Claimant", "RespondentSol", "RespondentSolTwo", "Staff"};
        parentCaseUpdateHelper.updateEvidence(
            updateMap, getVaryMainCaseData(role),
            getGaVaryCaseData(role, PENDING_APPLICATION_ISSUED, YES), docVisibilityRoles
        );
        assertThat(updateMap).isNotNull();
        assertThat(updateMap.get("gaEvidenceDocRespondentSolTwo")).isNotNull();
        assertThat(updateMap.get("gaEvidenceDocStaff")).isNull();
        assertThat(updateMap.get("gaEvidenceDocRespondentSol")).isNull();
        assertThat(updateMap.get("gaEvidenceDocClaimant")).isNull();
    }

    @Test
    void updateN245_after() {
        Map<String, Object> updateMap = new HashMap<>();
        String role = "RespondentSolTwo";
        String[] docVisibilityRoles = {"Claimant", "RespondentSol", "RespondentSolTwo", "Staff"};
        parentCaseUpdateHelper.updateEvidence(
            updateMap, getVaryMainCaseData(role),
            getGaVaryCaseData(role, AWAITING_APPLICATION_PAYMENT, YES), docVisibilityRoles
        );
        assertThat(updateMap).isNotNull();
        assertThat(updateMap.get("gaEvidenceDocRespondentSolTwo")).isNotNull();
        assertThat(updateMap.get("gaEvidenceDocStaff")).isNotNull();
        assertThat(updateMap.get("gaEvidenceDocRespondentSol")).isNotNull();
        assertThat(updateMap.get("gaEvidenceDocClaimant")).isNotNull();
    }

    @Test
    void updateParentWithGAState_with_n245_after_payment() {
        GeneralApplicationCaseData gaCase = getGaVaryCaseData("RespondentSolTwo", AWAITING_APPLICATION_PAYMENT, YES);
        GeneralApplicationCaseData civilCase = getVaryMainCaseData("RespondentSolTwo");
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(YES, NO));
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(civilCase);
        parentCaseUpdateHelper.updateParentWithGAState(gaCase, AWAITING_RESPONDENT_RESPONSE.toString());
        verify(coreCaseDataService, times(1))
            .caseDataContentFromStartEventResponse(any(), mapCaptor.capture());
        assertThat(mapCaptor.getValue().get("gaEvidenceDocRespondentSolTwo")).isNotNull();
        assertThat(mapCaptor.getValue().get("gaEvidenceDocStaff")).isNotNull();
        assertThat(mapCaptor.getValue().get("gaEvidenceDocRespondentSol")).isNotNull();
        assertThat(mapCaptor.getValue().get("gaEvidenceDocClaimant")).isNotNull();
        assertThat(mapCaptor.getValue().get("gaDetailsMasterCollection")).isNotNull();
        assertThat(mapCaptor.getValue().get("claimantGaAppDetails")).isNotNull();
        assertThat(mapCaptor.getValue().get("respondentSolGaAppDetails")).isNotNull();
        assertThat(mapCaptor.getValue().get("respondentSolTwoGaAppDetails")).isNotNull();
    }

    @Test
    void updateParentWithGAState_with_n245_before_payment() {
        GeneralApplicationCaseData gaCase = getGaVaryCaseDataForAddlDoc(
            "RespondentSol",
            PENDING_APPLICATION_ISSUED,
            YES
        );
        GeneralApplicationCaseData civilCase = getVaryMainCaseData("RespondentSol");
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(YES, NO));
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(civilCase);
        parentCaseUpdateHelper.updateParentWithGAState(gaCase, AWAITING_APPLICATION_PAYMENT.toString());
        verify(coreCaseDataService, times(1))
            .caseDataContentFromStartEventResponse(any(), mapCaptor.capture());
        assertThat(mapCaptor.getValue().get("gaAddlDocRespondentSol")).isNotNull();
    }

    @Test
    void shouldAddGA_to_Existing_collection_after_payment_1V2Diff_Solicitor_Def1() {

        GeneralApplicationCaseData gaCase = getGaVaryCaseData("RespondentSol", AWAITING_APPLICATION_PAYMENT, YES);

        GeneralApplicationCaseData civilCase = getCollectionsWithTwoExistingElements();

        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(YES, NO));
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(civilCase);
        parentCaseUpdateHelper.updateJudgeAndRespondentCollectionAfterPayment(gaCase);

        verify(coreCaseDataService, times(1))
            .caseDataContentFromStartEventResponse(any(), mapCaptor.capture());
        assertThat(mapCaptor.getValue().get("respondentSolTwoGaAppDetails")).isNotNull();
        assertThat(mapCaptor.getValue().get("gaDetailsMasterCollection")).isNotNull();
        assertThat(mapCaptor.getValue().get("respondentSolGaAppDetails")).isNotNull();
        assertThat(mapCaptor.getValue().get("claimantGaAppDetails")).isNotNull();
        assertThat(mapCaptor.getValue().get("respondentSolTwoGaAppDetails")).asList().hasSize(2);
        assertThat(mapCaptor.getValue().get("gaDetailsMasterCollection")).asList().hasSize(2);
        assertThat(mapCaptor.getValue().get("respondentSolGaAppDetails")).asList().hasSize(2);
        assertThat(mapCaptor.getValue().get("claimantGaAppDetails")).asList().hasSize(2);
    }

    @Test
    void addGA_to_judgeCollection_after_payment_1V2DifferntSolicitor_Def1() {

        GeneralApplicationCaseData gaCase = getGaVaryCaseData("RespondentSol", AWAITING_APPLICATION_PAYMENT, YES);

        GeneralApplicationCaseData civilCase = getVaryMainCaseDataForCollectionAfterPayment("RespondentSol");
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(YES, NO));
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(civilCase);
        parentCaseUpdateHelper.updateJudgeAndRespondentCollectionAfterPayment(gaCase);
        verify(coreCaseDataService, times(1))
            .caseDataContentFromStartEventResponse(any(), mapCaptor.capture());
        assertThat(mapCaptor.getValue().get("respondentSolTwoGaAppDetails")).isNotNull();
        assertThat(mapCaptor.getValue().get("gaDetailsMasterCollection")).isNotNull();
        assertThat(mapCaptor.getValue().get("respondentSolGaAppDetails")).isNotNull();
        assertThat(mapCaptor.getValue().get("claimantGaAppDetails")).isNotNull();
        assertThat(mapCaptor.getValue().get("respondentSolTwoGaAppDetails")).asList().hasSize(1);
        assertThat(mapCaptor.getValue().get("gaDetailsMasterCollection")).asList().hasSize(1);
        assertThat(mapCaptor.getValue().get("respondentSolGaAppDetails")).asList().hasSize(1);
        assertThat(mapCaptor.getValue().get("claimantGaAppDetails")).asList().hasSize(1);
    }

    @Test
    void addGA_to_judgeCollection_after_payment_1V2DifferntSolicitor_Def2() {

        GeneralApplicationCaseData gaCase = getGaVaryCaseData("RespondentSolTwo", AWAITING_APPLICATION_PAYMENT, YES);

        GeneralApplicationCaseData civilCase = getVaryMainCaseDataForCollectionAfterPayment("RespondentSolTwo");
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(YES, NO));
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(civilCase);
        parentCaseUpdateHelper.updateJudgeAndRespondentCollectionAfterPayment(gaCase);
        verify(coreCaseDataService, times(1))
            .caseDataContentFromStartEventResponse(any(), mapCaptor.capture());
        assertThat(mapCaptor.getValue().get("respondentSolTwoGaAppDetails")).isNotNull();
        assertThat(mapCaptor.getValue().get("gaDetailsMasterCollection")).isNotNull();
        assertThat(mapCaptor.getValue().get("respondentSolGaAppDetails")).isNotNull();
        assertThat(mapCaptor.getValue().get("claimantGaAppDetails")).isNotNull();
        assertThat(mapCaptor.getValue().get("respondentSolTwoGaAppDetails")).asList().hasSize(1);
        assertThat(mapCaptor.getValue().get("gaDetailsMasterCollection")).asList().hasSize(1);
        assertThat(mapCaptor.getValue().get("respondentSolGaAppDetails")).asList().hasSize(1);
        assertThat(mapCaptor.getValue().get("claimantGaAppDetails")).asList().hasSize(1);
    }

    @Test
    void updateParentWithGAState_with_collections_after_payment_1V2SameSolicitor() {

        GeneralApplicationCaseData gaCase = getGaVaryCaseData("RespondentSol", AWAITING_APPLICATION_PAYMENT, NO);

        GeneralApplicationCaseData civilCase = getVaryMainCaseDataFor1V2SameSol_DefendantInitiateGA("RespondentSol");
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(YES, NO));
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(civilCase);
        parentCaseUpdateHelper.updateJudgeAndRespondentCollectionAfterPayment(gaCase);
        verify(coreCaseDataService, times(1))
            .caseDataContentFromStartEventResponse(any(), mapCaptor.capture());
        assertThat(mapCaptor.getValue().get("respondentSolTwoGaAppDetails")).isEqualTo(newArrayList());
        assertThat(mapCaptor.getValue().get("gaDetailsMasterCollection")).isNotNull();
        assertThat(mapCaptor.getValue().get("respondentSolGaAppDetails")).isNotNull();
        assertThat(mapCaptor.getValue().get("claimantGaAppDetails")).isNotNull();
    }

    @Test
    void updateParentWithGAState_with_collections_after_payment_1V2SameSolicitor_claimantInitiate() {

        GeneralApplicationCaseData gaCase = getGaVaryCaseData("Claimant", AWAITING_APPLICATION_PAYMENT, NO);

        GeneralApplicationCaseData civilCase = getVaryMainCaseDataFor1V2SameSol_DefendantInitiateGA("Claimant");
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(YES, NO));
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(civilCase);
        parentCaseUpdateHelper.updateJudgeAndRespondentCollectionAfterPayment(gaCase);
        verify(coreCaseDataService, times(1))
            .caseDataContentFromStartEventResponse(any(), mapCaptor.capture());
        assertThat(mapCaptor.getValue().get("respondentSolTwoGaAppDetails")).isEqualTo(newArrayList());
        assertThat(mapCaptor.getValue().get("gaDetailsMasterCollection")).isNotNull();
        assertThat(mapCaptor.getValue().get("respondentSolGaAppDetails")).isNotNull();
        assertThat(mapCaptor.getValue().get("claimantGaAppDetails")).isNotNull();
    }

    @Test
    void updateParentWithGAState_with_collections_after_payment() {

        GeneralApplicationCaseData gaCase = getGaVaryCaseData("RespondentSolTwo", AWAITING_APPLICATION_PAYMENT, YES);
        GeneralApplicationCaseData civilCase = getVaryMainCaseDataForCollectionAfterPayment("RespondentSolTwo");
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(YES, NO));
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(civilCase);
        parentCaseUpdateHelper.updateJudgeAndRespondentCollectionAfterPayment(gaCase);
        verify(coreCaseDataService, times(1))
            .caseDataContentFromStartEventResponse(any(), mapCaptor.capture());
        assertThat(mapCaptor.getValue().get("respondentSolTwoGaAppDetails")).isNotNull();
        assertThat(mapCaptor.getValue().get("gaDetailsMasterCollection")).isNotNull();
        assertThat(mapCaptor.getValue().get("respondentSolGaAppDetails")).isNotNull();
        assertThat(mapCaptor.getValue().get("claimantGaAppDetails")).isNotNull();
    }

    @Test
    void updateParentWithGAState_with_collections_after_payment_ClaimantRole_withNotice() {

        GeneralApplicationCaseData gaCase = getGaVaryCaseData("Claimant", AWAITING_APPLICATION_PAYMENT, YES);
        GeneralApplicationCaseData civilCase = getVaryMainCaseDataForCollectionAfterPayment("Claimant");
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(YES, NO));
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(civilCase);
        parentCaseUpdateHelper.updateJudgeAndRespondentCollectionAfterPayment(gaCase);
        verify(coreCaseDataService, times(1))
            .caseDataContentFromStartEventResponse(any(), mapCaptor.capture());
        assertThat(mapCaptor.getValue().get("respondentSolTwoGaAppDetails")).isNotNull();
        assertThat(mapCaptor.getValue().get("gaDetailsMasterCollection")).isNotNull();
        assertThat(mapCaptor.getValue().get("respondentSolGaAppDetails")).isNotNull();
        assertThat(mapCaptor.getValue().get("claimantGaAppDetails")).isNotNull();
    }

    @Test
    void updateParentWithGAState_with_collections_after_payment_ClaimantRole_withNoticeWithOutMultiparty() {

        GeneralApplicationCaseData gaCase = getGaVaryCaseDataForCollection(
            "Claimant",
            AWAITING_APPLICATION_PAYMENT,
            NO,
            YES,
            NO
        );
        GeneralApplicationCaseData civilCase = getVaryMainCaseDataForCollectionAfterPayment("Claimant");
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(YES, NO));
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(civilCase);
        parentCaseUpdateHelper.updateJudgeAndRespondentCollectionAfterPayment(gaCase);
        verify(coreCaseDataService, times(1))
            .caseDataContentFromStartEventResponse(any(), mapCaptor.capture());
        assertThat(mapCaptor.getValue().get("gaDetailsMasterCollection")).isNotNull();
        assertThat(mapCaptor.getValue().get("respondentSolGaAppDetails")).isNotNull();
        assertThat(mapCaptor.getValue().get("claimantGaAppDetails")).isNotNull();
    }

    @Test
    void updateParentWithGAState_with_collections_after_payment_ClaimantRole_withOutNotice() {

        GeneralApplicationCaseData gaCase = getGaVaryCaseDataForCollection(
            "Claimant",
            AWAITING_APPLICATION_PAYMENT,
            NO,
            NO,
            NO
        );
        GeneralApplicationCaseData civilCase = getVaryMainCaseDataForCollectionAfterPayment("Claimant");
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(NO, NO));
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(civilCase);
        parentCaseUpdateHelper.updateJudgeAndRespondentCollectionAfterPayment(gaCase);
        verify(coreCaseDataService, times(1))
            .caseDataContentFromStartEventResponse(any(), mapCaptor.capture());
        assertThat(mapCaptor.getValue().get("gaDetailsMasterCollection")).isNotNull();
        assertThat(mapCaptor.getValue().get("respondentSolGaAppDetails")).isNotSameAs(mapCaptor.getValue().get(
            "claimantGaAppDetails"));
        assertThat(mapCaptor.getValue().get("claimantGaAppDetails")).isNotNull();
    }

    @Test
    void updateParentWithGAState_with_collections_after_payment_RespondentRole_withOutNotice() {

        GeneralApplicationCaseData gaCase = getGaVaryCaseDataForCollection(
            "RespondentSol",
            AWAITING_APPLICATION_PAYMENT,
            NO,
            NO,
            NO
        );
        GeneralApplicationCaseData civilCase = getVaryMainCaseDataForCollectionAfterPayment("RespondentSol");
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(NO, NO));
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(civilCase);
        parentCaseUpdateHelper.updateJudgeAndRespondentCollectionAfterPayment(gaCase);
        verify(coreCaseDataService, times(1))
            .caseDataContentFromStartEventResponse(any(), mapCaptor.capture());
        assertThat(mapCaptor.getValue().get("gaDetailsMasterCollection")).isNotNull();
        assertThat(mapCaptor.getValue().get("respondentSolGaAppDetails")).isNotNull();
        assertThat(mapCaptor.getValue().get("claimantGaAppDetails")).isNotSameAs(mapCaptor.getValue().get(
            "respondentSolGaAppDetails"));
    }

    @Test
    void updateParentWithGAState_with_collections_after_payment_Respondent2Role_withOutNotice() {

        GeneralApplicationCaseData gaCase = getGaVaryCaseDataForCollection(
            "RespondentSolTwo",
            AWAITING_APPLICATION_PAYMENT,
            YES,
            NO,
            NO
        );
        GeneralApplicationCaseData civilCase = getVaryMainCaseDataForCollectionAfterPayment("RespondentSolTwo");
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(NO, NO));
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(civilCase);
        parentCaseUpdateHelper.updateJudgeAndRespondentCollectionAfterPayment(gaCase);
        verify(coreCaseDataService, times(1))
            .caseDataContentFromStartEventResponse(any(), mapCaptor.capture());
        assertThat(mapCaptor.getValue().get("gaDetailsMasterCollection")).isNotNull();
        assertThat(mapCaptor.getValue().get("respondentSolGaAppDetails"))
            .isNotEqualTo(mapCaptor.getValue().get("respondentSolTwoGaAppDetails"));

    }

    @Test
    void updateParentWithGAState_with_collections_after_payment_Resp1Role_WithNotice() {

        GeneralApplicationCaseData gaCase = getGaVaryCaseData("RespondentSol", AWAITING_APPLICATION_PAYMENT, YES);
        GeneralApplicationCaseData civilCase = getVaryMainCaseDataForCollectionAfterPayment("RespondentSol");
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(YES, NO));
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(civilCase);
        parentCaseUpdateHelper.updateJudgeAndRespondentCollectionAfterPayment(gaCase);
        verify(coreCaseDataService, times(1))
            .caseDataContentFromStartEventResponse(any(), mapCaptor.capture());
        assertThat(mapCaptor.getValue().get("respondentSolTwoGaAppDetails")).isNotNull();
        assertThat(mapCaptor.getValue().get("gaDetailsMasterCollection")).isNotNull();
        assertThat(mapCaptor.getValue().get("respondentSolGaAppDetails")).isNotNull();
        assertThat(mapCaptor.getValue().get("claimantGaAppDetails")).isNotNull();
    }

    @Test
    void updateParentWithGAState_with_collections_after_payment_Resp1Role_WithNotice_WithOutMultiParty() {

        GeneralApplicationCaseData gaCase = getGaVaryCaseDataForCollection(
            "RespondentSol",
            AWAITING_APPLICATION_PAYMENT,
            NO,
            NO,
            YES
        );
        GeneralApplicationCaseData civilCase = getVaryMainCaseDataForCollectionAfterPayment("RespondentSol");
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(YES, NO));
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(civilCase);
        parentCaseUpdateHelper.updateJudgeAndRespondentCollectionAfterPayment(gaCase);
        verify(coreCaseDataService, times(1))
            .caseDataContentFromStartEventResponse(any(), mapCaptor.capture());
        assertThat(mapCaptor.getValue().get("gaDetailsMasterCollection")).isNotNull();
        assertThat(mapCaptor.getValue().get("respondentSolGaAppDetails")).isNotNull();
        assertThat(mapCaptor.getValue().get("claimantGaAppDetails")).isNotNull();
    }

    @Test
    void updateParentWithGAState_with_collections_WithHwf_WithNotice_WithOutMultiParty() {

        GeneralApplicationCaseData gaCase = getGaVaryCaseDataForCollection(
            "Claimant",
            AWAITING_APPLICATION_PAYMENT,
            NO,
            NO,
            YES
        );
        GeneralApplicationCaseData civilCase = getVaryMainCaseDataFor1V2SameSol_DefendantInitiateGA("Claimant");
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(YES, NO));
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(civilCase);
        parentCaseUpdateHelper.updateMasterCollectionForHwf(gaCase);
        verify(coreCaseDataService, times(1))
            .caseDataContentFromStartEventResponse(any(), mapCaptor.capture());
        assertThat(mapCaptor.getValue().get("gaDetailsMasterCollection")).isNotNull();
        assertThat(mapCaptor.getValue().get("respondentSolGaAppDetails")).isNotNull();
        assertThat(mapCaptor.getValue().get("claimantGaAppDetails")).isNotNull();
    }

    @Test
    void updateParentWithGAState_without_n245() {
        GeneralApplicationCaseData gaCase = getCaseWithApplicationDataAndGeneralOrder();
        GeneralApplicationCaseData civilCase = getCaseWithApplicationData(false);
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(YES, NO));
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(civilCase);
        parentCaseUpdateHelper.updateParentWithGAState(gaCase, AWAITING_APPLICATION_PAYMENT.toString());
        verify(coreCaseDataService, times(1))
            .caseDataContentFromStartEventResponse(any(), mapCaptor.capture());
        assertThat(mapCaptor.getValue().get("gaEvidenceDocStaff")).isNull();
    }

    @Test
    void should_addClaimantApplicationDetails_and_submit_when_parentClaimantIsApplicantIsYes() {

        GeneralApplicationCaseData civilCase = getVaryMainCaseDataForCollectionAfterPayment("Claimant");
        civilCase.getClaimantGaAppDetails().add(Element.<GeneralApplicationsDetails>builder()
                                                    .value(GeneralApplicationsDetails.builder()
                                                               .caseLink(CaseLink.builder()
                                                                             .caseReference(
                                                                                 GeneralApplicationCaseDataBuilder.CASE_ID.toString())
                                                                             .build()).build()).build());

        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(YES, NO));
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(civilCase);
        GeneralApplicationCaseData gaCase = getGaVaryCaseDataForCollection(
            "Claimant",
            AWAITING_APPLICATION_PAYMENT,
            NO,
            YES,
            NO
        );
        parentCaseUpdateHelper.updateCollectionForWelshApplication(gaCase);
        verify(coreCaseDataService, times(1))
            .caseDataContentFromStartEventResponse(any(), mapCaptor.capture());
        assertThat(mapCaptor.getValue().get("gaDetailsTranslationCollection")).isNotNull();
    }

    @Test
    void should_updateJudgeOrClaimantFromRespCollection_and_submit_when_parentClaimantIsApplicantIsNo() {

        GeneralApplicationCaseData civilCase = getVaryMainCaseDataForCollectionAfterPayment("RespondentSol");
        civilCase.getRespondentSolGaAppDetails().add(Element.<GADetailsRespondentSol>builder()
                                                         .value(GADetailsRespondentSol.builder()
                                                                    .caseLink(CaseLink.builder()
                                                                                  .caseReference(
                                                                                      GeneralApplicationCaseDataBuilder.CASE_ID.toString())
                                                                                  .build()).build()).build());
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(YES, NO));
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(civilCase);
        GeneralApplicationCaseData gaCase = getGaVaryCaseDataForCollection(
            "RespondentSol",
            AWAITING_APPLICATION_PAYMENT,
            NO,
            YES,
            NO
        );
        parentCaseUpdateHelper.updateCollectionForWelshApplication(gaCase);
        verify(coreCaseDataService, times(1))
            .caseDataContentFromStartEventResponse(any(), mapCaptor.capture());
        assertThat(mapCaptor.getValue().get("gaDetailsTranslationCollection")).isNotNull();
    }

    @Test
    void should_removeTheApplicationFromTranslationCollectionAfterPaymentAndUnpause() {

        GeneralApplicationCaseData civilCase = getVaryMainCaseDataForCollectionAfterPayment("Claimant");
        List<Element<GeneralApplicationsDetails>> translationList = new ArrayList<>();
        translationList.add(Element.<GeneralApplicationsDetails>builder()
                                .value(GeneralApplicationsDetails.builder()
                                           .caseLink(CaseLink.builder()
                                                         .caseReference(GeneralApplicationCaseDataBuilder.CASE_ID.toString())
                                                         .build()).build()).build());
        GeneralApplicationCaseData updatedCaseData = civilCase.copy().gaDetailsTranslationCollection(
            translationList).build();
        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(YES, NO));
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(updatedCaseData);
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        GeneralApplicationCaseData gaCase = getGaVaryCaseDataForCollection(
            "Claimant",
            AWAITING_APPLICATION_PAYMENT,
            NO,
            YES,
            NO
        );
        parentCaseUpdateHelper.updateJudgeAndRespondentCollectionAfterPayment(gaCase);
        verify(coreCaseDataService, times(1))
            .caseDataContentFromStartEventResponse(any(), mapCaptor.capture());
        assertThat(mapCaptor.getValue())
            .extracting("gaDetailsTranslationCollection")
            .isEqualTo(" ");
        assertThat(mapCaptor.getValue().get("gaDetailsMasterCollection")).asList().hasSize(1);
    }

    @Test
    void should_removeTheApplicationFromTranslationCollectionAfterHwfApprovalAndUnpause() {

        GeneralApplicationCaseData civilCase = getVaryMainCaseDataForCollectionAfterPayment("RespondentSol");
        List<Element<GeneralApplicationsDetails>> translationList = new ArrayList<>();
        translationList.add(Element.<GeneralApplicationsDetails>builder()
                                .value(GeneralApplicationsDetails.builder()
                                           .caseLink(CaseLink.builder()
                                                         .caseReference(GeneralApplicationCaseDataBuilder.CASE_ID.toString())
                                                         .build()).build()).build());
        GeneralApplicationCaseData updatedCaseData = civilCase.copy().gaDetailsTranslationCollection(
            translationList).build();

        when(coreCaseDataService.startUpdate(any(), any())).thenReturn(getStartEventResponse(YES, NO));
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(updatedCaseData);
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        GeneralApplicationCaseData gaCase = getGaVaryCaseDataForCollection(
            "RespondentSol",
            AWAITING_APPLICATION_PAYMENT,
            NO,
            YES,
            NO
        );
        parentCaseUpdateHelper.updateMasterCollectionForHwf(gaCase);
        verify(coreCaseDataService, times(1))
            .caseDataContentFromStartEventResponse(any(), mapCaptor.capture());

        assertThat(mapCaptor.getValue())
            .extracting("gaDetailsTranslationCollection")
            .isEqualTo(" ");
        assertThat(mapCaptor.getValue().get("gaDetailsMasterCollection")).asList().hasSize(1);
    }

    private StartEventResponse getStartEventResponse(YesOrNo isConsented, YesOrNo isTobeNotified) {
        CaseDetails caseDetails = CaseDetailsBuilder.builder().data(
                GeneralApplicationCaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(NO).build())
            .id(1645779506193000L)
            .state(APPLICATION_ADD_PAYMENT)
            .build();
        StartEventResponse.StartEventResponseBuilder startEventResponseBuilder = StartEventResponse.builder();
        startEventResponseBuilder.eventId(UPDATE_CASE_WITH_GA_STATE.toString())
            .token("BEARER_TOKEN")
            .caseDetails(caseDetails);

        return startEventResponseBuilder.build();
    }

    private GeneralApplicationCaseData getCaseWithApplicationData(Boolean withRespondentSol) {
        GeneralApplication generalApplication = GeneralApplication
            .builder()
            .caseLink(CaseLink.builder().caseReference(GeneralApplicationCaseDataBuilder.CASE_ID.toString()).build())
            .build();

        List<Element<GeneralApplicationsDetails>> generalApplicationsDetailsList = Lists.newArrayList();

        GeneralApplicationsDetails generalApplicationsDetails = GeneralApplicationsDetails.builder()
            .generalApplicationType("Summary judgment")
            .generalAppSubmittedDateGAspec(generalApplication.getGeneralAppSubmittedDateGAspec())
            .caseLink(generalApplication.getCaseLink())
            .caseState("pending").build();
        generalApplicationsDetailsList.add(element(generalApplicationsDetails));

        List<Element<GeneralApplicationsDetails>> gaDetailsMasterCollection = Lists.newArrayList();
        GeneralApplicationsDetails gaDetailsMasterColl = GeneralApplicationsDetails.builder()
            .generalApplicationType("Summary judgment")
            .generalAppSubmittedDateGAspec(generalApplication.getGeneralAppSubmittedDateGAspec())
            .caseLink(generalApplication.getCaseLink())
            .caseState("pending").build();
        gaDetailsMasterCollection.add(element(gaDetailsMasterColl));

        List<Element<GADetailsRespondentSol>> gaDetailsRespondentSolList = Lists.newArrayList();
        GADetailsRespondentSol gaDetailsRespondentSol = GADetailsRespondentSol.builder()
            .generalApplicationType("Summary judgment")
            .generalAppSubmittedDateGAspec(generalApplication.getGeneralAppSubmittedDateGAspec())
            .caseLink(generalApplication.getCaseLink())
            .caseState("pending").build();
        gaDetailsRespondentSolList.add(element(gaDetailsRespondentSol));

        List<Element<GADetailsRespondentSol>> gaDetailsRespondentSolListTwo = Lists.newArrayList();
        GADetailsRespondentSol gaDetailsRespondentSolTwo = GADetailsRespondentSol.builder()
            .generalApplicationType("Summary judgment")
            .generalAppSubmittedDateGAspec(generalApplication.getGeneralAppSubmittedDateGAspec())
            .caseLink(generalApplication.getCaseLink())
            .caseState("pending").build();
        gaDetailsRespondentSolListTwo.add(element(gaDetailsRespondentSolTwo));

        List<Element<GeneralApplication>> generalApplications = wrapElements(generalApplication);

        return GeneralApplicationCaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(NO)
            .generalApplications(generalApplications)
            .claimantGaAppDetails(generalApplicationsDetailsList)
            .gaDetailsMasterCollection(gaDetailsMasterCollection)
            .respondentSolGaAppDetails(withRespondentSol ? gaDetailsRespondentSolList : null)
            .respondentSolTwoGaAppDetails(withRespondentSol ? gaDetailsRespondentSolListTwo : null)
            .submittedOn(null).build();
    }

    private GeneralApplicationCaseData getCaseWithApplicationDataAndGeneralOrder() {
        String uid = "f000aa01-0451-4000-b000-000000000000";
        CaseDocument pdfDocument = new CaseDocument()
            .setCreatedBy("John")
            .setDocumentName("documentName")
            .setDocumentSize(0L)
            .setDocumentType(GENERAL_ORDER)
            .setCreatedDatetime(now())
            .setDocumentLink(new Document()
                              .setDocumentUrl("fake-url")
                              .setDocumentFileName("file-name")
                              .setDocumentBinaryUrl("binary-url"));
        return getCaseWithApplicationData(false)
            .copy().ccdState(PENDING_APPLICATION_ISSUED)
            .directionOrderDocument(singletonList(Element.<CaseDocument>builder()
                                                      .id(UUID.fromString(uid))
                                                      .value(pdfDocument).build())).build();
    }

    private GeneralApplicationCaseData getGaVaryCaseData(String role, CaseState state, YesOrNo isMultiparty) {
        GeneralApplicationCaseData builder = new GeneralApplicationCaseData();
        builder.generalAppType(GAApplicationType.builder()
                                   .types(List.of(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT)).build())
            .ccdCaseReference(GeneralApplicationCaseDataBuilder.CASE_ID)
            .generalAppParentCaseLink(new GeneralAppParentCaseLink()
                                          .setCaseReference(GeneralApplicationCaseDataBuilder.CASE_ID.toString()))
            .ccdState(state);
        Document pdfDocument = new Document()
            .setDocumentUrl("fake-url")
            .setDocumentFileName("file-name")
            .setDocumentBinaryUrl("binary-url");
        String uid = "f000aa01-0451-4000-b000-000000000000";
        builder.generalAppEvidenceDocument(singletonList(Element.<Document>builder()
                                                             .id(UUID.fromString(uid))
                                                             .value(pdfDocument).build()));
        switch (role) {
            case "Claimant":
                builder.parentClaimantIsApplicant(YES)
                    .isMultiParty(isMultiparty)
                    .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YES).build());
                break;
            case "RespondentSol":
                builder.parentClaimantIsApplicant(NO)
                    .isMultiParty(isMultiparty).generalAppApplnSolicitor(
                        GASolicitorDetailsGAspec.builder()
                            .organisationIdentifier("RespondentSol").build())
                    .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YES).build());
                break;
            case "RespondentSolTwo":
                builder.parentClaimantIsApplicant(NO)
                    .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YES).build())
                    .isMultiParty(isMultiparty).generalAppApplnSolicitor(
                        GASolicitorDetailsGAspec.builder()
                            .organisationIdentifier("RespondentSolTwo").build());
                break;
            case "RespondentLip":
                builder.parentClaimantIsApplicant(NO)
                    .isGaApplicantLip(YES)
                    .isMultiParty(isMultiparty)
                    .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().build());
                break;
            default:
                break;
        }
        return builder.build();
    }

    private GeneralApplicationCaseData getGaVaryCaseDataForAddlDoc(String role, CaseState state, YesOrNo isMultiparty) {
        GeneralApplicationCaseData builder = new GeneralApplicationCaseData();
        builder.generalAppType(GAApplicationType.builder()
                                   .types(List.of(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT)).build())
            .ccdCaseReference(GeneralApplicationCaseDataBuilder.CASE_ID)
            .generalAppParentCaseLink(new GeneralAppParentCaseLink()
                                          .setCaseReference(GeneralApplicationCaseDataBuilder.CASE_ID.toString()))
            .ccdState(state);
        CaseDocument pdfDocument = new CaseDocument()
            .setCreatedBy("John")
            .setDocumentName("documentName")
            .setDocumentSize(0L)
            .setCreatedDatetime(now())
            .setDocumentLink(new Document()
                              .setDocumentUrl("fake-url")
                              .setDocumentFileName("file-name")
                              .setDocumentBinaryUrl("binary-url"));
        String uid = "f000aa01-0451-4000-b000-000000000000";
        builder.gaAddlDoc(singletonList(Element.<CaseDocument>builder()
                                            .id(UUID.fromString(uid))
                                            .value(pdfDocument).build()));
        switch (role) {
            case "Claimant":
                builder.parentClaimantIsApplicant(YES)
                    .isMultiParty(isMultiparty)
                    .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YES).build());
                break;
            case "RespondentSol":
                builder.parentClaimantIsApplicant(NO)
                    .isMultiParty(isMultiparty).generalAppApplnSolicitor(
                        GASolicitorDetailsGAspec.builder()
                            .organisationIdentifier("RespondentSol").build())
                    .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YES).build());
                break;
            case "RespondentSolTwo":
                builder.parentClaimantIsApplicant(NO)
                    .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YES).build())
                    .isMultiParty(isMultiparty).generalAppApplnSolicitor(
                        GASolicitorDetailsGAspec.builder()
                            .organisationIdentifier("RespondentSolTwo").build());
                break;
            case "RespondentLip":
                builder.parentClaimantIsApplicant(NO)
                    .isGaApplicantLip(YES)
                    .isMultiParty(isMultiparty)
                    .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().build());
                break;
            default:
                break;
        }
        return builder.build();
    }

    private GeneralApplicationCaseData getGaVaryCaseDataForCollection(String role, CaseState state, YesOrNo isMultiparty,
                                                                      YesOrNo hasAgreed, YesOrNo isWithNotice) {
        GeneralApplicationCaseData builder = new GeneralApplicationCaseData();
        builder.generalAppType(GAApplicationType.builder()
                                   .types(List.of(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT)).build())
            .ccdCaseReference(GeneralApplicationCaseDataBuilder.CASE_ID)
            .generalAppParentCaseLink(new GeneralAppParentCaseLink()
                                          .setCaseReference(GeneralApplicationCaseDataBuilder.CASE_ID.toString()))
            .ccdState(state);
        Document pdfDocument = new Document()
            .setDocumentUrl("fake-url")
            .setDocumentFileName("file-name")
            .setDocumentBinaryUrl("binary-url");
        String uid = "f000aa01-0451-4000-b000-000000000000";
        builder.generalAppEvidenceDocument(singletonList(Element.<Document>builder()
                                                             .id(UUID.fromString(uid))
                                                             .value(pdfDocument).build()));
        switch (role) {
            case "Claimant":
                builder.parentClaimantIsApplicant(YES)
                    .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isWithNotice).build())
                    .isMultiParty(isMultiparty)
                    .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(hasAgreed).build());
                break;
            case "RespondentSol":
                builder.parentClaimantIsApplicant(NO)
                    .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isWithNotice).build())
                    .isMultiParty(isMultiparty).generalAppApplnSolicitor(
                        GASolicitorDetailsGAspec.builder()
                            .organisationIdentifier("RespondentSol").build())
                    .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(hasAgreed).build());
                break;
            case "RespondentSolTwo":
                builder.parentClaimantIsApplicant(NO)
                    .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isWithNotice).build())
                    .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(hasAgreed).build())
                    .isMultiParty(isMultiparty).generalAppApplnSolicitor(
                        GASolicitorDetailsGAspec.builder()
                            .organisationIdentifier("RespondentSolTwo").build());
                break;
            default:
                break;
        }
        return builder.build();
    }

    private GeneralApplicationCaseData getVaryMainCaseData(String role) {
        GeneralApplicationCaseData builder = new GeneralApplicationCaseData();

        GeneralApplication generalApplication = GeneralApplication
            .builder()
            .caseLink(CaseLink.builder().caseReference(GeneralApplicationCaseDataBuilder.CASE_ID.toString()).build())
            .build();

        List<Element<GeneralApplicationsDetails>> generalApplicationsDetailsList = Lists.newArrayList();

        GeneralApplicationsDetails generalApplicationsDetails = GeneralApplicationsDetails.builder()
            .generalApplicationType(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT.toString())
            .generalAppSubmittedDateGAspec(generalApplication.getGeneralAppSubmittedDateGAspec())
            .caseLink(generalApplication.getCaseLink())
            .caseState(PENDING_APPLICATION_ISSUED.toString()).build();
        generalApplicationsDetailsList.add(element(generalApplicationsDetails));

        List<Element<GeneralApplicationsDetails>> gaDetailsMasterCollection = Lists.newArrayList();
        GeneralApplicationsDetails gaDetailsMasterColl = GeneralApplicationsDetails.builder()
            .generalApplicationType(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT.toString())
            .generalAppSubmittedDateGAspec(generalApplication.getGeneralAppSubmittedDateGAspec())
            .caseLink(generalApplication.getCaseLink())
            .caseState(PENDING_APPLICATION_ISSUED.toString()).build();
        gaDetailsMasterCollection.add(element(gaDetailsMasterColl));

        List<Element<GADetailsRespondentSol>> gaDetailsRespondentSolList = Lists.newArrayList();
        GADetailsRespondentSol gaDetailsRespondentSol = GADetailsRespondentSol.builder()
            .generalApplicationType(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT.toString())
            .generalAppSubmittedDateGAspec(generalApplication.getGeneralAppSubmittedDateGAspec())
            .caseLink(generalApplication.getCaseLink())
            .caseState(PENDING_APPLICATION_ISSUED.toString()).build();
        gaDetailsRespondentSolList.add(element(gaDetailsRespondentSol));

        List<Element<GADetailsRespondentSol>> gaDetailsRespondentSolListTwo = Lists.newArrayList();
        GADetailsRespondentSol gaDetailsRespondentSolTwo = GADetailsRespondentSol.builder()
            .generalApplicationType(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT.toString())
            .generalAppSubmittedDateGAspec(generalApplication.getGeneralAppSubmittedDateGAspec())
            .caseLink(generalApplication.getCaseLink())
            .caseState(PENDING_APPLICATION_ISSUED.toString()).build();
        gaDetailsRespondentSolListTwo.add(element(gaDetailsRespondentSolTwo));

        List<Element<GeneralApplication>> generalApplications = wrapElements(generalApplication);
        builder.generalApplications(generalApplications);
        builder.gaDetailsMasterCollection(gaDetailsMasterCollection);
        switch (role) {
            case "Claimant":
                builder.claimantGaAppDetails(generalApplicationsDetailsList);
                break;
            case "RespondentSol":
                builder.respondent1OrganisationPolicy(new OrganisationPolicy())
                    .respondent1OrganisationIDCopy("RespondentSol");
                builder.claimantGaAppDetails(generalApplicationsDetailsList);
                builder.respondentSolGaAppDetails(gaDetailsRespondentSolList);
                break;
            case "RespondentSolTwo":
                builder.respondent1OrganisationPolicy(new OrganisationPolicy());
                builder.respondent2OrganisationPolicy(new OrganisationPolicy())
                    .respondent2OrganisationIDCopy("RespondentSolTwo")
                    .respondent2SameLegalRepresentative(NO)
                    .addApplicant2(NO);
                builder.claimantGaAppDetails(generalApplicationsDetailsList);
                builder.respondentSolGaAppDetails(gaDetailsRespondentSolList);
                builder.respondentSolTwoGaAppDetails(gaDetailsRespondentSolListTwo);
                break;
            default:
                break;
        }
        return builder.build();
    }

    private GeneralApplicationCaseData getVaryMainCaseDataForCollectionBeforePayment(String role) {
        GeneralApplicationCaseData builder = new GeneralApplicationCaseData();

        GeneralApplication generalApplication = GeneralApplication
            .builder()
            .caseLink(CaseLink.builder().caseReference(GeneralApplicationCaseDataBuilder.CASE_ID.toString()).build())
            .build();

        List<Element<GeneralApplicationsDetails>> generalApplicationsDetailsList = Lists.newArrayList();

        GeneralApplicationsDetails generalApplicationsDetails = GeneralApplicationsDetails.builder()
            .generalApplicationType(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT.toString())
            .generalAppSubmittedDateGAspec(generalApplication.getGeneralAppSubmittedDateGAspec())
            .caseLink(generalApplication.getCaseLink())
            .caseState(PENDING_APPLICATION_ISSUED.toString()).build();
        generalApplicationsDetailsList.add(element(generalApplicationsDetails));
        List<Element<GeneralApplication>> generalApplications = wrapElements(generalApplication);
        builder.generalApplications(generalApplications);

        List<Element<GeneralApplicationsDetails>> gaDetailsMasterCollection = Lists.newArrayList();
        GeneralApplicationsDetails gaDetailsMasterColl = GeneralApplicationsDetails.builder()
            .generalApplicationType(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT.toString())
            .generalAppSubmittedDateGAspec(generalApplication.getGeneralAppSubmittedDateGAspec())
            .caseLink(generalApplication.getCaseLink())
            .caseState(PENDING_APPLICATION_ISSUED.toString()).build();
        gaDetailsMasterCollection.add(element(gaDetailsMasterColl));

        List<Element<GADetailsRespondentSol>> gaDetailsRespondentSolList = Lists.newArrayList();
        GADetailsRespondentSol gaDetailsRespondentSol = GADetailsRespondentSol.builder()
            .generalApplicationType(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT.toString())
            .generalAppSubmittedDateGAspec(generalApplication.getGeneralAppSubmittedDateGAspec())
            .caseLink(generalApplication.getCaseLink())
            .caseState(PENDING_APPLICATION_ISSUED.toString()).build();
        gaDetailsRespondentSolList.add(element(gaDetailsRespondentSol));

        List<Element<GADetailsRespondentSol>> gaDetailsRespondentSolListTwo = Lists.newArrayList();
        GADetailsRespondentSol gaDetailsRespondentSolTwo = GADetailsRespondentSol.builder()
            .generalApplicationType(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT.toString())
            .generalAppSubmittedDateGAspec(generalApplication.getGeneralAppSubmittedDateGAspec())
            .caseLink(generalApplication.getCaseLink())
            .caseState(PENDING_APPLICATION_ISSUED.toString()).build();
        gaDetailsRespondentSolListTwo.add(element(gaDetailsRespondentSolTwo));

        switch (role) {
            case "Claimant":
                builder.claimantGaAppDetails(generalApplicationsDetailsList);
                break;
            case "RespondentSol":
                builder.respondent1OrganisationPolicy(new OrganisationPolicy())
                    .respondent1OrganisationIDCopy("RespondentSol");
                builder.respondentSolGaAppDetails(gaDetailsRespondentSolList);

                break;
            case "RespondentSolTwo":
                builder.respondent1OrganisationPolicy(new OrganisationPolicy());
                builder.respondent2OrganisationPolicy(new OrganisationPolicy())
                    .respondent2OrganisationIDCopy("RespondentSolTwo")
                    .respondent2SameLegalRepresentative(NO)
                    .addApplicant2(NO);
                builder.respondentSolTwoGaAppDetails(gaDetailsRespondentSolListTwo);
                break;
            default:
                break;
        }
        return builder.build();
    }

    private GeneralApplicationCaseData getVaryMainCaseDataForCollectionAfterPayment(String role) {
        GeneralApplicationCaseData builder = new GeneralApplicationCaseData();

        GeneralApplication generalApplication = GeneralApplication
            .builder()
            .caseLink(CaseLink.builder().caseReference(GeneralApplicationCaseDataBuilder.CASE_ID.toString()).build())
            .build();

        List<Element<GeneralApplicationsDetails>> generalApplicationsDetailsList = Lists.newArrayList();

        GeneralApplicationsDetails generalApplicationsDetails = GeneralApplicationsDetails.builder()
            .generalApplicationType(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT.toString())
            .generalAppSubmittedDateGAspec(generalApplication.getGeneralAppSubmittedDateGAspec())
            .caseLink(generalApplication.getCaseLink())
            .caseState(AWAITING_APPLICATION_PAYMENT.toString()).build();
        generalApplicationsDetailsList.add(element(generalApplicationsDetails));
        List<Element<GeneralApplication>> generalApplications = wrapElements(generalApplication);
        builder.generalApplications(generalApplications);

        List<Element<GeneralApplicationsDetails>> gaDetailsMasterCollection = Lists.newArrayList();
        GeneralApplicationsDetails gaDetailsMasterColl = GeneralApplicationsDetails.builder()
            .generalApplicationType(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT.toString())
            .generalAppSubmittedDateGAspec(generalApplication.getGeneralAppSubmittedDateGAspec())
            .caseLink(generalApplication.getCaseLink())
            .caseState(AWAITING_APPLICATION_PAYMENT.toString()).build();
        gaDetailsMasterCollection.add(element(gaDetailsMasterColl));

        List<Element<GADetailsRespondentSol>> gaDetailsRespondentSolList = Lists.newArrayList();
        GADetailsRespondentSol gaDetailsRespondentSol = GADetailsRespondentSol.builder()
            .generalApplicationType(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT.toString())
            .generalAppSubmittedDateGAspec(generalApplication.getGeneralAppSubmittedDateGAspec())
            .caseLink(generalApplication.getCaseLink())
            .caseState(AWAITING_APPLICATION_PAYMENT.toString()).build();
        gaDetailsRespondentSolList.add(element(gaDetailsRespondentSol));

        List<Element<GADetailsRespondentSol>> gaDetailsRespondentSolListTwo = Lists.newArrayList();
        GADetailsRespondentSol gaDetailsRespondentSolTwo = GADetailsRespondentSol.builder()
            .generalApplicationType(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT.toString())
            .generalAppSubmittedDateGAspec(generalApplication.getGeneralAppSubmittedDateGAspec())
            .caseLink(generalApplication.getCaseLink())
            .caseState(AWAITING_APPLICATION_PAYMENT.toString()).build();
        gaDetailsRespondentSolListTwo.add(element(gaDetailsRespondentSolTwo));

        switch (role) {
            case "Claimant":
                builder.claimantGaAppDetails(generalApplicationsDetailsList);
                break;
            case "RespondentSol":
                builder.respondent1OrganisationPolicy(new OrganisationPolicy())
                    .respondent1OrganisationIDCopy("RespondentSol");
                builder.respondentSolGaAppDetails(gaDetailsRespondentSolList);

                break;
            case "RespondentSolTwo":
                builder.respondent1OrganisationPolicy(new OrganisationPolicy());
                builder.respondent2OrganisationPolicy(new OrganisationPolicy())
                    .respondent2OrganisationIDCopy("RespondentSolTwo")
                    .respondent2SameLegalRepresentative(NO)
                    .addApplicant2(NO);
                builder.respondentSolTwoGaAppDetails(gaDetailsRespondentSolListTwo);
                break;
            default:
                break;
        }
        return builder.build();
    }

    private GeneralApplicationCaseData getVaryMainCaseDataFor1V2SameSol_DefendantInitiateGA(String role) {
        GeneralApplicationCaseData builder = new GeneralApplicationCaseData();

        List<Element<GeneralApplicationsDetails>> generalApplicationsDetailsList = Lists.newArrayList();

        GeneralApplication generalApplication = GeneralApplication
            .builder()
            .caseLink(CaseLink.builder().caseReference(GeneralApplicationCaseDataBuilder.CASE_ID.toString()).build())
            .build();

        GeneralApplicationsDetails generalApplicationsDetails = GeneralApplicationsDetails.builder()
            .generalApplicationType(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT.toString())
            .generalAppSubmittedDateGAspec(generalApplication.getGeneralAppSubmittedDateGAspec())
            .caseLink(generalApplication.getCaseLink())
            .caseState(AWAITING_APPLICATION_PAYMENT.toString()).build();
        generalApplicationsDetailsList.add(element(generalApplicationsDetails));

        List<Element<GADetailsRespondentSol>> gaDetailsRespondentSolList = Lists.newArrayList();
        GADetailsRespondentSol gaDetailsRespondentSol = GADetailsRespondentSol.builder()
            .generalApplicationType(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT.toString())
            .generalAppSubmittedDateGAspec(generalApplication.getGeneralAppSubmittedDateGAspec())
            .caseLink(generalApplication.getCaseLink())
            .caseState(AWAITING_APPLICATION_PAYMENT.toString()).build();
        gaDetailsRespondentSolList.add(element(gaDetailsRespondentSol));

        switch (role) {
            case "Claimant":
                builder.claimantGaAppDetails(generalApplicationsDetailsList);
                break;
            case "RespondentSol":
                builder.respondent1OrganisationPolicy(new OrganisationPolicy())
                    .respondent1OrganisationIDCopy("RespondentSol");
                builder.respondentSolGaAppDetails(gaDetailsRespondentSolList);
                break;
            default:
                break;
        }
        return builder.build();
    }

    private GeneralApplicationCaseData getCollectionsWithTwoExistingElements() {
        GeneralApplicationCaseData builder = new GeneralApplicationCaseData();

        // Claimant Collection with one element
        GeneralApplication generalApplication01 = GeneralApplication
            .builder()
            .caseLink(CaseLink.builder().caseReference(String.valueOf(3451956117501L)).build())
            .build();

        GeneralApplicationsDetails claimantCollectionElement01 = GeneralApplicationsDetails.builder()
            .generalApplicationType(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT.toString())
            .generalAppSubmittedDateGAspec(generalApplication01.getGeneralAppSubmittedDateGAspec())
            .caseLink(generalApplication01.getCaseLink())
            .caseState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION.toString()).build();

        List<Element<GeneralApplicationsDetails>> claimantCollection
            = Lists.newArrayList((element(claimantCollectionElement01)));

        builder.claimantGaAppDetails(claimantCollection);

        // Master Collection with One element
        GeneralApplication gaMaster001 = GeneralApplication
            .builder()
            .caseLink(CaseLink.builder().caseReference(String.valueOf(3451956117501L)).build())
            .build();

        GeneralApplicationsDetails masterElement001 = GeneralApplicationsDetails.builder()
            .generalApplicationType(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT.toString())
            .generalAppSubmittedDateGAspec(gaMaster001.getGeneralAppSubmittedDateGAspec())
            .caseLink(gaMaster001.getCaseLink())
            .caseState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION.toString()).build();

        List<Element<GeneralApplicationsDetails>> masterCollection
            = Lists.newArrayList((element(masterElement001)));

        builder.gaDetailsMasterCollection(masterCollection);

        // Construct respondent One collection with 2 GAs
        GeneralApplication respondentOne01 = GeneralApplication
            .builder()
            .caseLink(CaseLink.builder().caseReference(String.valueOf(3451956117501L)).build())
            .build();

        GeneralApplication respondentOne02 = GeneralApplication
            .builder()
            .caseLink(CaseLink.builder().caseReference(GeneralApplicationCaseDataBuilder.CASE_ID.toString()).build())
            .build();

        GADetailsRespondentSol respondentSolOneElement01 = GADetailsRespondentSol.builder()
            .generalApplicationType(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT.toString())
            .generalAppSubmittedDateGAspec(respondentOne01.getGeneralAppSubmittedDateGAspec())
            .caseLink(respondentOne01.getCaseLink())
            .caseState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION.toString()).build();

        GADetailsRespondentSol respondentSolOneElement02 = GADetailsRespondentSol.builder()
            .generalApplicationType(GeneralApplicationTypes.STAY_THE_CLAIM.toString())
            .generalAppSubmittedDateGAspec(respondentOne02.getGeneralAppSubmittedDateGAspec())
            .caseLink(respondentOne02.getCaseLink())
            .caseState(AWAITING_APPLICATION_PAYMENT.toString()).build();

        List<Element<GADetailsRespondentSol>> gaDetailsRespondentSolList =
            Lists.newArrayList(element(respondentSolOneElement01), element(respondentSolOneElement02));

        builder.respondent1OrganisationPolicy(new OrganisationPolicy())
            .respondent1OrganisationIDCopy("RespondentSol");
        builder.respondentSolGaAppDetails(gaDetailsRespondentSolList);

        // Build Respondent Two Collection with One element
        GeneralApplication generalApplication3 = GeneralApplication
            .builder()
            .caseLink(CaseLink.builder().caseReference(String.valueOf(3451956117501L)).build())
            .build();

        GADetailsRespondentSol respondentSolTwoGA1 = GADetailsRespondentSol.builder()
            .generalApplicationType(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT.toString())
            .generalAppSubmittedDateGAspec(generalApplication3.getGeneralAppSubmittedDateGAspec())
            .caseLink(generalApplication3.getCaseLink())
            .caseState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION.toString()).build();

        List<Element<GADetailsRespondentSol>> gaDetailsRespondentSolList2 =
            Lists.newArrayList(element(respondentSolTwoGA1));

        builder.respondent2OrganisationPolicy(new OrganisationPolicy())
            .respondent2OrganisationIDCopy("RespondentSolTwo")
            .respondent2SameLegalRepresentative(NO)
            .addApplicant2(NO);
        builder.respondentSolTwoGaAppDetails(gaDetailsRespondentSolList2);

        return builder.build();
    }
}

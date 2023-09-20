package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.UnsecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderSelection;
import uk.gov.hmcts.reform.civil.enums.finalorders.AppealList;
import uk.gov.hmcts.reform.civil.enums.finalorders.AssistedCostTypesList;
import uk.gov.hmcts.reform.civil.enums.finalorders.HearingLengthFinalOrderList;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrderToggle;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrderRepresentationList;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersJudgePapers;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersClaimantRepresentationList;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersClaimantDefendantNotAttending;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersDefendantRepresentationList;
import uk.gov.hmcts.reform.civil.enums.finalorders.OrderMadeOnTypes;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.FreeFormOrderValues;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;

import uk.gov.hmcts.reform.civil.model.finalorders.*;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.JUDGE_FINAL_ORDER;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.ASSISTED_ORDER_PDF;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.FREE_FORM_ORDER_PDF;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JudgeFinalOrderGenerator.class,
    JacksonAutoConfiguration.class
})
public class JudgeFinalOrderGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String fileFreeForm = format(FREE_FORM_ORDER_PDF.getDocumentTitle(), LocalDate.now());
    private static final String assistedForm = format(ASSISTED_ORDER_PDF.getDocumentTitle(), LocalDate.now());
    List<FinalOrderToggle> toggleList = new ArrayList<FinalOrderToggle>(Arrays.asList(FinalOrderToggle.SHOW));
    private static final CaseDocument FREE_FROM_ORDER = CaseDocumentBuilder.builder()
        .documentName(fileFreeForm)
        .documentType(JUDGE_FINAL_ORDER)
        .build();
    private static final CaseDocument ASSISTED_FROM_ORDER = CaseDocumentBuilder.builder()
        .documentName(assistedForm)
        .documentType(JUDGE_FINAL_ORDER)
        .build();
    @MockBean
    private UnsecuredDocumentManagementService documentManagementService;

    @MockBean
    private DocumentGeneratorService documentGeneratorService;

    @MockBean
    private IdamClient idamClient;
    @MockBean
    private LocationRefDataService locationRefDataService;
    @MockBean
    private DocumentHearingLocationHelper locationHelper;
    @Autowired
    private JudgeFinalOrderGenerator generator;

    private static LocationRefData locationRefData =   LocationRefData.builder().siteName("SiteName")
        .courtAddress("1").postcode("1")
        .courtName("Court Name").region("Region").regionId("4").courtVenueId("000")
        .courtTypeId("10").courtLocationCode("121")
        .epimmsId("000000").build();

    @BeforeEach
    public void setUp() throws JsonProcessingException {

        when(idamClient.getUserDetails(any()))
            .thenReturn(new UserDetails("1", "test@email.com", "Test", "User", null));
        when(idamClient.getUserDetails(any()))
            .thenReturn(new UserDetails("1", "test@email.com", "Test", "User", null));

        when(locationHelper.getHearingLocation(any(), any(), any())).thenReturn(locationRefData);
        when(locationRefDataService.getCcmccLocation(any())).thenReturn(locationRefData);
    }

    @Test
    void shouldGenerateFreeFormOrder_whenNoneSelected() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(FREE_FORM_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(FREE_FORM_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(FREE_FROM_ORDER);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertNotNull(caseDocument);
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void shouldGenerateFreeFormOrder_whenClaimantAndDefendantReferenceNotAddedToCase() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(FREE_FORM_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(FREE_FORM_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(FREE_FROM_ORDER);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .solicitorReferences(null)
            .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertNotNull(caseDocument);
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void shouldGenerateFreeFormOrder_whenOrderOnCourtInitiativeSelected() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(FREE_FORM_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(FREE_FORM_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(FREE_FROM_ORDER);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
            .orderOnCourtInitiative(FreeFormOrderValues.builder().onInitiativeSelectionTextArea("test").onInitiativeSelectionDate(
                LocalDate.now()).build())
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertNotNull(caseDocument);
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void shouldGenerateFreeFormOrder_whenOrderWithoutNoticeIsSelected() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(FREE_FORM_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(FREE_FORM_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(FREE_FROM_ORDER);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderSelection(FinalOrderSelection.FREE_FORM_ORDER)
            .ccdState(CaseState.JUDICIAL_REFERRAL)
            .orderWithoutNotice(FreeFormOrderValues.builder().withoutNoticeSelectionTextArea("test without notice")
                                    .withoutNoticeSelectionDate(LocalDate.now()).build())
            .respondent2(PartyBuilder.builder().individual().build().toBuilder()
                             .partyID("app-2-party-id")
                             .partyName("Applicant2")
                             .build())
            .applicant2(PartyBuilder.builder().soleTrader().build().toBuilder()
                            .partyID("res-2-party-id")
                            .partyName("Respondent2")
                            .build())
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertNotNull(caseDocument);
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileFreeForm, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void shouldGenerateAssistedFormOrder_whenRecitalsNotSelected() {
        //Given: case data without recitals selected
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(ASSISTED_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(ASSISTED_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(assistedForm, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(ASSISTED_FROM_ORDER);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRecitals(null)
            .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
            .finalOrderDateHeardComplex(OrderMade.builder().singleDateSelection(DatesFinalOrders.builder().singleDate(LocalDate.now()).build()).build())
            .assistedOrderCostList(AssistedCostTypesList.NO_ORDER_TO_COST)
            .orderMadeOnDetailsList(OrderMadeOnTypes.COURTS_INITIATIVE)
            .orderMadeOnDetailsOrderCourt(OrderMadeOnDetails.builder().ownInitiativeDate(LocalDate.now()).build())
            .build();
        //When: Assisted order document generation called
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);
        //Then: It should generate assisted order document
        assertNotNull(caseDocument);
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(assistedForm, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void shouldGenerateAssistedFormOrder_withAllDetails() {
        //Given: Case data with all fields for docmosis
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(ASSISTED_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(ASSISTED_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(assistedForm, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(ASSISTED_FROM_ORDER);
        DynamicListElement dynamicListElement = DynamicListElement.builder().label("test_label").build();
        DynamicList dynamicList = DynamicList.builder()
            .listItems(Collections.singletonList(dynamicListElement))
            .value(dynamicListElement)
            .build();
        List<FinalOrdersJudgePapers> finalOrdersJudgePapersList =
            new ArrayList<>(Arrays.asList(FinalOrdersJudgePapers.CONSIDERED));
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRecitals(null)
            .finalOrderSelection(FinalOrderSelection.ASSISTED_ORDER)
            .assistedOrderCostList(AssistedCostTypesList.NO_ORDER_TO_COST)
            .orderMadeOnDetailsList(OrderMadeOnTypes.COURTS_INITIATIVE)
            .orderMadeOnDetailsOrderCourt(OrderMadeOnDetails.builder().ownInitiativeDate(LocalDate.now()).build())
            .finalOrderDateHeardComplex(OrderMade.builder().singleDateSelection(DatesFinalOrders.builder().singleDate(LocalDate.now()).build()).build())
            .finalOrderRepresentation(FinalOrderRepresentation.builder().typeRepresentationJudgePapersList(finalOrdersJudgePapersList)
                                          .typeRepresentationList(FinalOrderRepresentationList.CLAIMANT_AND_DEFENDANT).typeRepresentationOtherComplex(
                ClaimantAndDefendantHeard.builder().detailsRepresentationText("Test").build()).build())
            .finalOrderRecitals(toggleList)
            .finalOrderRecitalsRecorded(FinalOrderRecitalsRecorded.builder().text("Test").build())
            .assistedOrderCostsReserved(AssistedOrderCostDetails.builder().detailsRepresentationText("Test").build())
            .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder().alternativeHearingList(dynamicList).build())
            .finalOrderGiveReasonsComplex(AssistedOrderReasons.builder().reasonsText("Test").build())
            .assistedOrderCostsBespoke(AssistedOrderCostDetails.builder().besPokeCostDetailsText("Test").build())
            .finalOrderAppealToggle(toggleList)
            .build();
        //When: Assisted order document generation called
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        //Then: It should generate assisted order document
        assertNotNull(caseDocument);
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(assistedForm, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void testDefendantOneAttendsOrRepresentedTextBuilder() {
        for (FinalOrdersDefendantRepresentationList finalOrdersDefendantRepresentationList : List.of(FinalOrdersDefendantRepresentationList.values())) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderRecitals(null)
                .finalOrderRepresentation(FinalOrderRepresentation.builder().typeRepresentationComplex(
                    ClaimantAndDefendantHeard.builder().typeRepresentationDefendantList(
                        finalOrdersDefendantRepresentationList).build()).build())
                .build();
            String name = caseData.getRespondent1().getPartyName();
            String response = generator.defendantAttendsOrRepresentedTextBuilder(caseData, false);
            switch (finalOrdersDefendantRepresentationList) {
                case COUNSEL_FOR_DEFENDANT:
                    assertEquals(format("Counsel for %s, the defendant.", name), response);
                    break;
                case SOLICITOR_FOR_DEFENDANT:
                    assertEquals(format("Solicitor for %s, the defendant.", name), response);
                    break;
                case COST_DRAFTSMAN_FOR_THE_DEFENDANT:
                    assertEquals(format("Costs draftsman for %s, the defendant.", name), response);
                    break;
                case THE_DEFENDANT_IN_PERSON:
                    assertEquals(format("%s, the defendant, in person.", name), response);
                    break;
                case LAY_REPRESENTATIVE_FOR_THE_DEFENDANT:
                    assertEquals(format("A lay representative for %s, the defendant.", name), response);
                    break;
                default:
                    break;
            }
        }
    }

    @Test
    void testDefendantTwoAttendsOrRepresentedTextBuilder() {
        for (FinalOrdersDefendantRepresentationList finalOrdersDefendantRepresentationList : List.of(FinalOrdersDefendantRepresentationList.values())) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .finalOrderRecitals(null)
                .finalOrderRepresentation(FinalOrderRepresentation.builder().typeRepresentationComplex(
                    ClaimantAndDefendantHeard.builder().typeRepresentationDefendantTwoList(
                        finalOrdersDefendantRepresentationList).build()).build())
                .build();
            String name = caseData.getRespondent2().getPartyName();
            String response = generator.defendantAttendsOrRepresentedTextBuilder(caseData, true);
            switch (finalOrdersDefendantRepresentationList) {
                case COUNSEL_FOR_DEFENDANT:
                    assertEquals(format("Counsel for %s, the defendant.", name), response);
                    break;
                case SOLICITOR_FOR_DEFENDANT:
                    assertEquals(format("Solicitor for %s, the defendant.", name), response);
                    break;
                case COST_DRAFTSMAN_FOR_THE_DEFENDANT:
                    assertEquals(format("Costs draftsman for %s, the defendant.", name), response);
                    break;
                case THE_DEFENDANT_IN_PERSON:
                    assertEquals(format("%s, the defendant, in person.", name), response);
                    break;
                case LAY_REPRESENTATIVE_FOR_THE_DEFENDANT:
                    assertEquals(format("A lay representative for %s, the defendant.", name), response);
                    break;
                default:
                    break;
            }
        }
    }

    @Test
    void testClaimantOneAttendsOrRepresentedTextBuilder() {
        for (FinalOrdersClaimantRepresentationList finalOrdersClaimantRepresentationList : List.of(FinalOrdersClaimantRepresentationList.values())) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderRecitals(null)
                .finalOrderRepresentation(FinalOrderRepresentation.builder().typeRepresentationComplex(
                    ClaimantAndDefendantHeard.builder().typeRepresentationClaimantList(
                        finalOrdersClaimantRepresentationList).build()).build())
                .build();
            String name = caseData.getApplicant1().getPartyName();
            String response = generator.claimantAttendsOrRepresentedTextBuilder(caseData, false);
            switch (finalOrdersClaimantRepresentationList) {
                case COUNSEL_FOR_CLAIMANT:
                    assertEquals(format("Counsel for %s, the claimant.", name), response);
                    break;
                case SOLICITOR_FOR_CLAIMANT:
                    assertEquals(format("Solicitor for %s, the claimant.", name), response);
                    break;
                case COST_DRAFTSMAN_FOR_THE_CLAIMANT:
                    assertEquals(format("Costs draftsman for %s, the claimant.", name),  response);
                    break;
                case THE_CLAIMANT_IN_PERSON:
                    assertEquals(format("%s, the claimant, in person.", name), response);
                    break;
                case LAY_REPRESENTATIVE_FOR_THE_CLAIMANT:
                    assertEquals(format("A lay representative for %s, the claimant.", name), response);
                    break;
                default:
                    break;
            }
        }
    }

    @Test
    void testClaimantTwoAttendsOrRepresentedTextBuilder() {
        for (FinalOrdersClaimantRepresentationList finalOrdersClaimantRepresentationList : List.of(FinalOrdersClaimantRepresentationList.values())) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .addApplicant2(YesOrNo.YES)
                .applicant2(PartyBuilder.builder().individual().build())
                .finalOrderRecitals(null)
                .finalOrderRepresentation(FinalOrderRepresentation.builder().typeRepresentationComplex(
                    ClaimantAndDefendantHeard.builder().typeRepresentationClaimantListTwo(
                        finalOrdersClaimantRepresentationList).build()).build())
                .build();
            String name = caseData.getApplicant2().getPartyName();
            String response = generator.claimantAttendsOrRepresentedTextBuilder(caseData, true);
            switch (finalOrdersClaimantRepresentationList) {
                case COUNSEL_FOR_CLAIMANT:
                    assertEquals(format("Counsel for %s, the claimant.", name), response);
                    break;
                case SOLICITOR_FOR_CLAIMANT:
                    assertEquals(format("Solicitor for %s, the claimant.", name), response);
                    break;
                case COST_DRAFTSMAN_FOR_THE_CLAIMANT:
                    assertEquals(format("Costs draftsman for %s, the claimant.", name),  response);
                    break;
                case THE_CLAIMANT_IN_PERSON:
                    assertEquals(format("%s, the claimant, in person.", name), response);
                    break;
                case LAY_REPRESENTATIVE_FOR_THE_CLAIMANT:
                    assertEquals(format("A lay representative for %s, the claimant.", name), response);
                    break;
                default:
                    break;
            }
        }
    }

    @Test
    void testDefendantOneNotAttendingText() {
        for (FinalOrdersClaimantDefendantNotAttending finalOrdersClaimantDefendantNotAttending : List.of(FinalOrdersClaimantDefendantNotAttending.values())) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderRecitals(null)
                .finalOrderRepresentation(FinalOrderRepresentation.builder().typeRepresentationComplex(
                    ClaimantAndDefendantHeard.builder().trialProcedureComplex(TrialNoticeProcedure.builder().listDef(
                        finalOrdersClaimantDefendantNotAttending).build()).build()).build())
                .build();
            String name = caseData.getRespondent1().getPartyName();
            String response = generator.defendantNotAttendingText(caseData, false, name);
            switch (finalOrdersClaimantDefendantNotAttending) {
                case NOT_SATISFIED_NOTICE_OF_TRIAL:
                    assertEquals(format("%s, the defendant, did not attend the trial. "
                                            + "The Judge was not satisfied that they had received notice of the hearing "
                                            + "and it was not reasonable to proceed in their absence.",
                                        name), response);
                    break;
                case SATISFIED_NOTICE_OF_TRIAL:
                    assertEquals(format("%s, the defendant, did not attend the trial and, whilst the Judge was satisfied that they had "
                                            + "received notice of the trial, the Judge was not satisfied that it was reasonable to proceed in their absence.",
                                        name), response);
                    break;
                case SATISFIED_REASONABLE_TO_PROCEED:
                    assertEquals(
                        format("%s, the defendant, did not attend the trial. The Judge was satisfied that they had "
                                   + "received notice of the trial and determined that it was reasonable to proceed in their absence.",
                               name), response
                    );
                    break;
                default:
                    assertEquals("", response);
                    break;
            }
        }
    }

    @Test
    void testDefendantTwoNotAttendingText() {
        for (FinalOrdersClaimantDefendantNotAttending finalOrdersClaimantDefendantNotAttending : List.of(FinalOrdersClaimantDefendantNotAttending.values())) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .finalOrderRecitals(null)
                .finalOrderRepresentation(FinalOrderRepresentation.builder().typeRepresentationComplex(
                    ClaimantAndDefendantHeard.builder().trialProcedureDefTwoComplex(TrialNoticeProcedure.builder().listDefTwo(
                        finalOrdersClaimantDefendantNotAttending).build()).build()).build())
                .build();
            String name = caseData.getRespondent2().getPartyName();
            String response = generator.defendantNotAttendingText(caseData, true, name);
            switch (finalOrdersClaimantDefendantNotAttending) {
                case NOT_SATISFIED_NOTICE_OF_TRIAL:
                    assertEquals(format("%s, the defendant, did not attend the trial. "
                                            + "The Judge was not satisfied that they had received notice of the hearing "
                                            + "and it was not reasonable to proceed in their absence.",
                                        name), response);
                    break;
                case SATISFIED_NOTICE_OF_TRIAL:
                    assertEquals(format("%s, the defendant, did not attend the trial and, whilst the Judge was satisfied that they had "
                                            + "received notice of the trial, the Judge was not satisfied that it was reasonable to proceed in their absence.",
                                        name), response);
                    break;
                case SATISFIED_REASONABLE_TO_PROCEED:
                    assertEquals(
                        format("%s, the defendant, did not attend the trial. The Judge was satisfied that they had "
                                   + "received notice of the trial and determined that it was reasonable to proceed in their absence.",
                               name), response
                    );
                    break;
                default:
                    assertEquals("", response);
                    break;
            }
        }
    }

    @Test
    void testGetClaimantOneNotAttendedText() {
        for (FinalOrdersClaimantDefendantNotAttending finalOrdersClaimantDefendantNotAttending : List.of(FinalOrdersClaimantDefendantNotAttending.values())) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderRecitals(null)
                .finalOrderRepresentation(FinalOrderRepresentation.builder().typeRepresentationComplex(
                    ClaimantAndDefendantHeard.builder().trialProcedureClaimantComplex(TrialNoticeProcedure.builder().list(
                        finalOrdersClaimantDefendantNotAttending).build()).build()).build())
                .build();
            String name = caseData.getApplicant1().getPartyName();
            String response = generator.claimantNotAttendingText(caseData, false, name);
            switch (finalOrdersClaimantDefendantNotAttending) {
                case NOT_SATISFIED_NOTICE_OF_TRIAL:
                    assertEquals(format("%s, the claimant, did not attend the trial. "
                                            + "The Judge was not satisfied that they had received notice of the hearing "
                                            + "and it was not reasonable to proceed in their absence.",
                                        name), response);
                    break;
                case SATISFIED_NOTICE_OF_TRIAL:
                    assertEquals(format("%s, the claimant, did not attend the trial and, whilst the Judge was satisfied that they had "
                                            + "received notice of the trial, the Judge was not satisfied that it was reasonable to proceed in their absence.",
                                        name), response);
                    break;
                case SATISFIED_REASONABLE_TO_PROCEED:
                    assertEquals(format("%s, the claimant, did not attend the trial. The Judge was satisfied that they had "
                                            + "received notice of the trial and determined that it was reasonable to proceed in their absence.",
                                        name), response);
                    break;
                default:
                    break;
            }
        }
    }

    @Test
    void testGetClaimantTwoNotAttendedText() {
        for (FinalOrdersClaimantDefendantNotAttending finalOrdersClaimantDefendantNotAttending : List.of(FinalOrdersClaimantDefendantNotAttending.values())) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .addApplicant2(YesOrNo.YES)
                .applicant2(PartyBuilder.builder().individual().build())
                .finalOrderRecitals(null)
                .finalOrderRepresentation(FinalOrderRepresentation.builder().typeRepresentationComplex(
                    ClaimantAndDefendantHeard.builder().trialProcedClaimTwoComplex(TrialNoticeProcedure.builder().listClaimTwo(
                        finalOrdersClaimantDefendantNotAttending).build()).build()).build())
                .build();
            String name = caseData.getApplicant2().getPartyName();
            String response = generator.claimantNotAttendingText(caseData, true, name);
            switch (finalOrdersClaimantDefendantNotAttending) {
                case NOT_SATISFIED_NOTICE_OF_TRIAL:
                    assertEquals(format("%s, the claimant, did not attend the trial. "
                                            + "The Judge was not satisfied that they had received notice of the hearing "
                                            + "and it was not reasonable to proceed in their absence.",
                                        name), response);
                    break;
                case SATISFIED_NOTICE_OF_TRIAL:
                    assertEquals(format("%s, the claimant, did not attend the trial and, whilst the Judge was satisfied that they had "
                                            + "received notice of the trial, the Judge was not satisfied that it was reasonable to proceed in their absence.",
                                        name), response);
                    break;
                case SATISFIED_REASONABLE_TO_PROCEED:
                    assertEquals(format("%s, the claimant, did not attend the trial. The Judge was satisfied that they had "
                                            + "received notice of the trial and determined that it was reasonable to proceed in their absence.",
                                        name), response);
                    break;
                default:
                    break;
            }
        }
    }

    @Test
    void testGetFurtherHearingLength() {
        for (HearingLengthFinalOrderList hearingLengthFinalOrderList : List.of(HearingLengthFinalOrderList.values())) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderRecitals(null)
                .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder().lengthList(
                    hearingLengthFinalOrderList).build()).build();
            String response = generator.getFurtherHearingLength(caseData);
            switch (hearingLengthFinalOrderList) {
                case MINUTES_15:
                    assertEquals("15 minutes", response);
                    break;
                case MINUTES_30:
                    assertEquals("30 minutes", response);
                    break;
                case HOUR_1:
                    assertEquals("1 hour", response);
                    break;
                case HOUR_1_5:
                    assertEquals("1.5 hours", response);
                    break;
                case HOUR_2:
                    assertEquals("2 hours", response);
                    break;
                default:
                    break;
            }
        }
    }

//    @Test
//    void testGetFurtherHearingLengthForOther() {
//        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
//            .finalOrderRecitals(null)
//            .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder()
//                                                 .lengthListOther(CaseHearingLengthElement.builder()
//                                                                      .lengthListOtherDays("12")
//                                                                      .lengthListOtherHours("1")
//                                                                      .lengthListOtherMinutes("30").build()).build()).build();
//        String response = generator.getFurtherHearingLength(caseData);
//        assertEquals("12 days 1 hours 30 minutes", response);
//    }

    @Test
    void testGetFurtherHearingFromDate() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRecitals(null)
            .finalOrderFurtherHearingToggle(toggleList)
            .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder().listFromDate(LocalDate.of(2022, 12,
                                                                                                          12)).build()).build();
        LocalDate response = generator.getFurtherHearingDate(caseData, true);
        assertEquals(LocalDate.of(2022, 12,
                                  12), response);
    }

    @Test
    void testGetFurtherHearingToDate() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRecitals(null)
            .finalOrderFurtherHearingToggle(toggleList)
            .finalOrderFurtherHearingComplex(FinalOrderFurtherHearing.builder().dateToDate(LocalDate.of(2022, 12,
                                                                                                          12)).build()).build();
        LocalDate response = generator.getFurtherHearingDate(caseData, false);
        assertEquals(LocalDate.of(2022, 12,
                                  12), response);
    }

    @Test
    void testGetAppealFor() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRecitals(null)
            .finalOrderAppealComplex(FinalOrderAppeal.builder().list(AppealList.CLAIMANT).build()).build();
        String response = generator.getAppealFor(caseData);
        assertEquals(AppealList.CLAIMANT.name().toLowerCase(), response);
    }

    @Test
    void testGetAppealForOthers() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRecitals(null)
            .finalOrderAppealComplex(FinalOrderAppeal.builder().otherText("test").list(AppealList.OTHER).build()).build();
        String response = generator.getAppealFor(caseData);
        assertEquals("test", response);
    }

    @ParameterizedTest
    @MethodSource("testData")
    void orderMadeDateBuilder(CaseData caseData, String expectedResponse) {
        String response = generator.orderMadeDateBuilder(caseData);
        assertEquals(expectedResponse, response);
    }

    static Stream<Arguments> testData() {
        return Stream.of(
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderDateHeardComplex(OrderMade.builder().singleDateSelection(DatesFinalOrders.builder()
                                                                                            .singleDate(LocalDate.of(2023, 9, 15))
                                                                                            .build()).build()).build(),
                "on 15 September 2023"
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderDateHeardComplex(OrderMade.builder().dateRangeSelection(DatesFinalOrders.builder()
                                                                                           .dateRangeFrom(LocalDate.of(2023, 9, 13))
                                                                                           .dateRangeTo(LocalDate.of(2023, 9, 14))
                                                                                           .build()).build()).build(),
                "between 13 September 2023 and 14 September 2023"
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderDateHeardComplex(OrderMade.builder().bespokeRangeSelection(DatesFinalOrders.builder()
                                                                                              .bespokeRangeTextArea("date between 12 feb 2023, and 14 feb 2023")
                                                                                              .build()).build()).build(),
                "on date between 12 feb 2023, and 14 feb 2023"
            )
        );
    }

}


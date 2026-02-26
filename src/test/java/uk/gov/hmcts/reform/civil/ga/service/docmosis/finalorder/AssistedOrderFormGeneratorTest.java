package uk.gov.hmcts.reform.civil.ga.service.docmosis.finalorder;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.enums.GAJudicialHearingType;
import uk.gov.hmcts.reform.civil.ga.enums.dq.AppealOriginTypes;
import uk.gov.hmcts.reform.civil.ga.enums.dq.AssistedCostTypesList;
import uk.gov.hmcts.reform.civil.ga.enums.dq.AssistedOrderCostDropdownList;
import uk.gov.hmcts.reform.civil.ga.enums.dq.ClaimantDefendantNotAttendingType;
import uk.gov.hmcts.reform.civil.ga.enums.dq.ClaimantRepresentationType;
import uk.gov.hmcts.reform.civil.ga.enums.dq.DefendantRepresentationType;
import uk.gov.hmcts.reform.civil.ga.enums.dq.FinalOrderConsideredToggle;
import uk.gov.hmcts.reform.civil.ga.enums.dq.FinalOrderShowToggle;
import uk.gov.hmcts.reform.civil.ga.enums.dq.HeardFromRepresentationTypes;
import uk.gov.hmcts.reform.civil.ga.enums.dq.LengthOfHearing;
import uk.gov.hmcts.reform.civil.ga.enums.dq.OrderMadeOnTypes;
import uk.gov.hmcts.reform.civil.ga.enums.dq.PermissionToAppealTypes;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.docmosis.AssistedOrderForm;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.HearingLength;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AppealTypeChoiceList;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AppealTypeChoices;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AssistedOrderAppealDetails;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AssistedOrderCost;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AssistedOrderDateHeard;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AssistedOrderFurtherHearingDetails;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AssistedOrderGiveReasonsDetails;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AssistedOrderHeardRepresentation;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AssistedOrderMadeDateHeardDetails;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.AssistedOrderRecitalRecord;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.BeSpokeCostDetailText;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.ClaimantDefendantRepresentation;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.DetailText;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.DetailTextWithDate;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.HeardClaimantNotAttend;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.HeardDefendantNotAttend;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder.HeardDefendantTwoNotAttend;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.DocmosisService;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.ASSISTED_ORDER_FORM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.POST_JUDGE_ASSISTED_ORDER_FORM_LIP;

@ExtendWith(MockitoExtension.class)
class AssistedOrderFormGeneratorTest {

    private static final String RECITAL_RECORDED_TEXT = "It is recorded that";
    private static final String BEARER_TOKEN = "Bearer Token";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String CLAIMANT_SUMMARILY_ASSESSED_TEXT = "The claimant shall pay the defendant's costs (both fixed and summarily assessed as appropriate) " +
        "in the sum of £789.00. " +
        "Such sum shall be paid by 4pm on";
    private static final String DEFENDANT_SUMMARILY_ASSESSED_TEXT = "The defendant shall pay the claimant's costs (both fixed and summarily assessed as appropriate) " +
        "in the sum of £789.00. " +
        "Such sum shall be paid by 4pm on";
    private static final String CLAIMANT_DETAILED_INDEMNITY_ASSESSED_TEXT = "The claimant shall pay the defendant's costs to be subject to a detailed assessment " +
        "on the indemnity basis if not agreed";
    private static final String CLAIMANT_DETAILED_STANDARD_ASSESSED_TEXT = "The claimant shall pay the defendant's costs to be subject to a detailed assessment " +
        "on the standard basis if not agreed";
    private static final String DEFENDANT_DETAILED_INDEMNITY_ASSESSED_TEXT = "The defendant shall pay the claimant's costs to be subject to a detailed assessment " +
        "on the indemnity basis if not agreed";
    private static final String DEFENDANT_DETAILED_STANDARD_ASSESSED_TEXT = "The defendant shall pay the claimant's costs to be subject to a detailed assessment " +
        "on the standard basis if not agreed";
    private static final String TEST_TEXT = "Test 123";
    private static final String OTHER_ORIGIN_TEXT = "test other origin text";

    private static final String INTERIM_PAYMENT_TEXT = "An interim payment of £500.00 on account of costs shall be paid by 4pm on";

    @Mock
    private DocumentManagementService documentManagementService;

    @Mock
    private DocumentGeneratorService documentGeneratorService;

    @Mock
    private DocmosisService docmosisService;

    @InjectMocks
    private AssistedOrderFormGenerator generator;

    @Test
    void test_getCaseNumberFormatted() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().ccdCaseReference(
            1644495739087775L).build();
        String formattedCaseNumber = generator.getCaseNumberFormatted(caseData);
        assertThat(formattedCaseNumber).isEqualTo("1644-4957-3908-7775");
    }

    @Test
    void test_getFileName() {
        String name = generator.getFileName(DocmosisTemplates.ASSISTED_ORDER_FORM);
        assertThat(name).startsWith("General_order_for_application_");
        assertThat(name).endsWith(".pdf");
    }

    @Test
    void test_getTemplate() {
        assertThat(generator.getTemplate(FlowFlag.ONE_RESPONDENT_REPRESENTATIVE)).isEqualTo(DocmosisTemplates.ASSISTED_ORDER_FORM);
    }

    @Test
    void shouldThrowExceptionWhenNoLocationMatch() {
        GeneralApplicationCaseData caseData = getSampleGeneralApplicationGeneralApplicationCaseData(NO).copy()
            .caseManagementLocation(CaseLocationCivil.builder().siteName("testing")
                                        .address("london court")
                                        .baseLocation("9")
                                        .postcode("BA 117").build())
            .claimant2PartyName(null).build();

        doThrow(new IllegalArgumentException("Court Name is not found in location data"))
            .when(docmosisService).getCaseManagementLocationVenueName(any(), any());
        Exception exception =
            assertThrows(
                IllegalArgumentException.class, ()
                    -> generator.generate(caseData, BEARER_TOKEN)
        );
        String expectedMessage = "Court Name is not found in location data";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void shouldGenerateAssistedOrderDocument() {
        when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
            .thenReturn(
                new LocationRefData()
                    .setEpimmsId("2")
                    .setExternalShortName("Reading")
            );
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(ASSISTED_ORDER_FORM)))
            .thenReturn(new DocmosisDocument(ASSISTED_ORDER_FORM.getDocumentTitle(), bytes));

        GeneralApplicationCaseData caseData = getSampleGeneralApplicationGeneralApplicationCaseData(NO).copy()
            .caseManagementLocation(CaseLocationCivil.builder().siteName("testing")
                                        .address("london court")
                                        .baseLocation("1")
                                        .postcode("BA 117").build())
            .judgeTitle("John Doe")
            .claimant2PartyName(null).build();

        generator.generate(caseData, BEARER_TOKEN);

        verify(documentGeneratorService).generateDocmosisDocument(
            any(AssistedOrderForm.class),
            eq(ASSISTED_ORDER_FORM)
        );
        verify(documentManagementService).uploadDocument(
            BEARER_TOKEN,
            new PDF(any(), any(), DocumentType.GENERAL_ORDER)
        );

        var templateData = generator.getTemplateData(
            null,
            caseData,
            BEARER_TOKEN,
            FlowFlag.ONE_RESPONDENT_REPRESENTATIVE
        );
        assertThat(templateData.getCostsProtection()).isEqualTo(YesOrNo.YES);
        assertThat(templateData.getAddress()).isEqualTo("london court");
        assertThat(templateData.getSiteName()).isEqualTo("testing");
        assertThat(templateData.getPostcode()).isEqualTo("BA 117");
        assertThat(templateData.getCourtLocation()).isEqualTo("Reading");
        assertThat(templateData.getClaimant1Name()).isEqualTo(caseData.getClaimant1PartyName());
        assertThat(templateData.getClaimant2Name()).isNull();
        assertThat(templateData.getDefendant1Name()).isEqualTo(caseData.getDefendant1PartyName());
        assertThat(templateData.getDefendant2Name()).isNull();
        assertThat(templateData.getJudgeNameTitle()).isEqualTo("John Doe");
    }

    @Test
    void shouldGenerateAssistedOrderDocument_1v2() {

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(ASSISTED_ORDER_FORM)))
            .thenReturn(new DocmosisDocument(ASSISTED_ORDER_FORM.getDocumentTitle(), bytes));

        when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
            .thenReturn(
                new LocationRefData()
                    .setEpimmsId("2")
                    .setExternalShortName("London")
            );
        GeneralApplicationCaseData caseData = getSampleGeneralApplicationGeneralApplicationCaseData(YES).copy()
            .caseManagementLocation(CaseLocationCivil.builder().siteName("testing")
                                        .address("london court")
                                        .baseLocation("2")
                                        .postcode("BA 117").build())
            .judgeTitle("John Doe")
            .claimant2PartyName(null).build();
        generator.generate(caseData, BEARER_TOKEN);

        verify(documentGeneratorService).generateDocmosisDocument(
            any(AssistedOrderForm.class),
            eq(ASSISTED_ORDER_FORM)
        );
        verify(documentManagementService).uploadDocument(
            BEARER_TOKEN,
            new PDF(any(), any(), DocumentType.GENERAL_ORDER)
        );

        var templateData = generator.getTemplateData(
            null,
            caseData,
            BEARER_TOKEN,
            FlowFlag.ONE_RESPONDENT_REPRESENTATIVE
        );
        assertThat(templateData.getCostsProtection()).isEqualTo(YesOrNo.YES);
        assertThat(templateData.getClaimant1Name()).isEqualTo(caseData.getClaimant1PartyName());
        assertThat(templateData.getClaimant2Name()).isEqualTo(caseData.getClaimant2PartyName());
        assertThat(templateData.getIsMultiParty()).isEqualTo(YES);
        assertThat(templateData.getCourtLocation()).isEqualTo("London");
        assertThat(templateData.getJudgeNameTitle()).isEqualTo(caseData.getJudgeTitle());
        assertThat(templateData.getDefendant1Name()).isEqualTo(caseData.getDefendant1PartyName());
        assertThat(templateData.getDefendant2Name()).isEqualTo(caseData.getDefendant2PartyName());
    }

    private GeneralApplicationCaseData getSampleGeneralApplicationGeneralApplicationCaseData(YesOrNo isMultiparty) {
        List<FinalOrderShowToggle> judgeHeardFromShowOption = new ArrayList<>();
        judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
        List<FinalOrderShowToggle> recitalsOrderShowOption = new ArrayList<>();
        recitalsOrderShowOption.add(FinalOrderShowToggle.SHOW);
        List<FinalOrderShowToggle> furtherHearingShowOption = new ArrayList<>();
        furtherHearingShowOption.add(FinalOrderShowToggle.SHOW);
        List<FinalOrderShowToggle> appealShowOption = new ArrayList<>();
        appealShowOption.add(FinalOrderShowToggle.SHOW);
        return new GeneralApplicationCaseData()
            .ccdCaseReference(1644495739087775L)
            .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference("1644495739087775"))
            .claimant1PartyName("ClaimantName")
            .defendant1PartyName("defendant1PartyName")
            .claimant2PartyName("Test Claimant2 Name")
            .defendant2PartyName("Test Defendant2 Name")
            .isMultiParty(isMultiparty)
            .locationName("ccmcc")
            .caseManagementLocation(CaseLocationCivil.builder().siteName("testing")
                                        .address("london court")
                                        .postcode("BA 117").build())
            .assistedOrderMadeSelection(YES)
            .assistedOrderMadeDateHeardDetails(new AssistedOrderMadeDateHeardDetails().setSingleDateSelection(
                new AssistedOrderDateHeard().setSingleDate(LocalDate.now())))
            .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
            .assistedOrderRepresentation(new AssistedOrderHeardRepresentation()
                                             .setRepresentationType(HeardFromRepresentationTypes.OTHER_REPRESENTATION)
                                             .setOtherRepresentation(new DetailText().setDetailText(OTHER_ORIGIN_TEXT)))
            .assistedOrderRecitals(recitalsOrderShowOption)
            .assistedOrderRecitalsRecorded(new AssistedOrderRecitalRecord().setText(RECITAL_RECORDED_TEXT))
            .assistedOrderOrderedThatText(TEST_TEXT)
            .assistedCostTypes(AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
            .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()
                                                  .setMakeAnOrderForCostsList(AssistedOrderCostDropdownList.CLAIMANT)
                                                  .setAssistedOrderCostsMakeAnOrderTopList(
                                                      AssistedOrderCostDropdownList.COSTS)
                                                  .setAssistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900)))
            .publicFundingCostsProtection(YES)
            .assistedOrderFurtherHearingToggle(furtherHearingShowOption)
            .assistedOrderFurtherHearingDetails(new AssistedOrderFurtherHearingDetails()
                                                    .setListFromDate(LocalDate.now().minusDays(5))
                                                    .setLengthOfNewHearing(LengthOfHearing.OTHER)
                                                    .setLengthOfHearingOther(new HearingLength().setLengthListOtherDays(2)
                                                                              .setLengthListOtherHours(5)
                                                                              .setLengthListOtherMinutes(30))
                                                    .setDatesToAvoid(NO)
                                                    .setHearingLocationList(DynamicList.builder().value(
                                                        DynamicListElement.builder().label("Other location").build()).build())
                                                    .setAlternativeHearingLocation(DynamicList.builder().value(
                                                        DynamicListElement.builder().label(
                                                            "Site Name 2 - Address2 - 28000").build()).build())
                                                    .setHearingMethods(GAJudicialHearingType.TELEPHONE))
            .assistedOrderAppealToggle(appealShowOption)
            .assistedOrderAppealDetails(new AssistedOrderAppealDetails()
                                            .setAppealOrigin(AppealOriginTypes.DEFENDANT)
                                            .setPermissionToAppeal(PermissionToAppealTypes.GRANTED)
                                            .setAppealTypeChoicesForGranted(
                                                new AppealTypeChoices()
                                                    .setAssistedOrderAppealJudgeSelection(PermissionToAppealTypes.CIRCUIT_COURT_JUDGE)
                                                    .setAppealChoiceOptionA(
                                                        new AppealTypeChoiceList()
                                                            .setAppealGrantedRefusedDate(LocalDate.now().plusDays(14))
                                                            )))
            .orderMadeOnOption(OrderMadeOnTypes.COURTS_INITIATIVE)
            .orderMadeOnOwnInitiative(new DetailTextWithDate()
                                          .setDetailText(TEST_TEXT)
                                          .setDate(LocalDate.now())
                                          )
            .assistedOrderGiveReasonsYesNo(YES)
            .assistedOrderGiveReasonsDetails(new AssistedOrderGiveReasonsDetails()
                                                 .setReasonsText(TEST_TEXT))
            .build();

    }

    @Nested
    class CostTextValues {

        @Test
        void shouldReturnText_WhenSelected_CostsReserved() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                    AssistedCostTypesList.COSTS_RESERVED)
                .costReservedDetails(new DetailText().setDetailText(TEST_TEXT)).build();
            String assistedOrderString = generator.getCostsReservedText(caseData);

            assertThat(assistedOrderString).contains(TEST_TEXT);
        }

        @Test
        void shouldReturnNull_WhenSelected_CostsReserved_detailTextIsEmpty() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                    AssistedCostTypesList.COSTS_RESERVED)
                .build();
            String assistedOrderString = generator.getCostsReservedText(caseData);

            assertThat(assistedOrderString).isNull();
        }

        @Test
        void shouldReturnValue_WhenSelected_BeSpokeCostOrder() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                    AssistedCostTypesList.BESPOKE_COSTS_ORDER)
                .assistedOrderCostsBespoke(new BeSpokeCostDetailText().setDetailText("test"))
                .build();
            String assistedOrderString = generator.getBespokeCostOrderText(caseData);

            assertThat(assistedOrderString).isEqualTo("\n\n" + "test");
        }

        @Test
        void shouldReturnNull_WhenSelected_NotCostsReserved() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                    AssistedCostTypesList.NO_ORDER_TO_COST)
                .build();
            String assistedOrderString = generator.getCostsReservedText(caseData);

            assertThat(assistedOrderString).isNull();
        }

        @Test
        void shouldReturnText_WhenSelected_Claimant_SummarilyAssessed() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                    AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()
                                                      .setMakeAnOrderForCostsList(AssistedOrderCostDropdownList.CLAIMANT)
                                                      .setAssistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.COSTS)
                                                      .setAssistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900))).build();
            String assistedOrderString = generator.getSummarilyAssessed(caseData);

            assertThat(assistedOrderString).contains(CLAIMANT_SUMMARILY_ASSESSED_TEXT);
        }

        @Test
        void shouldReturnText_WhenSelected_Defendant_SummarilyAssessed() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                    AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()
                                                      .setMakeAnOrderForCostsList(AssistedOrderCostDropdownList.DEFENDANT)
                                                      .setAssistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.COSTS)
                                                      .setAssistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900))).build();
            String assistedOrderString = generator.getSummarilyAssessed(caseData);

            assertThat(assistedOrderString).contains(DEFENDANT_SUMMARILY_ASSESSED_TEXT);
        }

        @Test
        void shouldReturnNull_WhenMakeAnOrderForCostsEmpty() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                    AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()).build();
            String assistedOrderString = generator.getSummarilyAssessed(caseData);

            assertThat(assistedOrderString).isNull();
        }

        @Test
        void shouldReturnNull_WhenMakeAnOrderForCostsListIsEmpty() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS).build();
            String assistedOrderString = generator.getSummarilyAssessed(caseData);

            assertThat(assistedOrderString).isNull();
        }

        @Test
        void shouldReturnDifferentText_WhenSelected_Defendant_SubjectSummarilyAssessed() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                    AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()
                                                      .setMakeAnOrderForCostsList(AssistedOrderCostDropdownList.DEFENDANT)
                                                      .setAssistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.SUBJECT_DETAILED_ASSESSMENT)
                                                      .setAssistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900))).build();
            String assistedOrderString = generator.getSummarilyAssessed(caseData);

            assertThat(assistedOrderString).isNull();
        }

        @Test
        void shouldReturnText_WhenSelected_Claimant_SummarilyAssessedDate() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                    AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()
                                                      .setMakeAnOrderForCostsList(AssistedOrderCostDropdownList.DEFENDANT)
                                                      .setAssistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.COSTS)
                                                      .setAssistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900))
                                                      .setAssistedOrderCostsFirstDropdownDate(LocalDate.now().minusDays(5))).build();
            LocalDate assistedOrderDropdownDate = generator.getSummarilyAssessedDate(caseData);

            assertThat(assistedOrderDropdownDate).isEqualTo(LocalDate.now().minusDays(5));
        }

        @Test
        void shouldReturnNull_When_MakeAnOrderForCostsIsNull_SummarilyAssessedDate() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                    AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()).build();
            LocalDate assistedOrderDropdownDate = generator.getSummarilyAssessedDate(caseData);

            assertThat(assistedOrderDropdownDate).isNull();
        }

        @Test
        void shouldReturnNull_When_MakeAnOrderForCostsListIsNull_SummarilyAssessedDate() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                    AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()
                                                      .setAssistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.COSTS)
                                                      .setAssistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900))
                                                      .setAssistedOrderCostsFirstDropdownDate(LocalDate.now().minusDays(5))).build();
            LocalDate assistedOrderDropdownDate = generator.getSummarilyAssessedDate(caseData);

            assertThat(assistedOrderDropdownDate).isNull();
        }

        @Test
        void shouldReturnNull_When_MakeAnOrderForCostsListDropdownIsNotCosts_SummarilyAssessedDate() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                    AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()
                                                      .setAssistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.SUBJECT_DETAILED_ASSESSMENT)
                                                      .setAssistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900))
                                                      .setAssistedOrderCostsFirstDropdownDate(LocalDate.now().minusDays(5))).build();
            LocalDate assistedOrderDropdownDate = generator.getSummarilyAssessedDate(caseData);

            assertThat(assistedOrderDropdownDate).isNull();
        }

        @Test
        void shouldReturnText_WhenSelected_Claimant_DetailedAssessed() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                    AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()
                                                      .setMakeAnOrderForCostsList(AssistedOrderCostDropdownList.CLAIMANT)
                                                      .setAssistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.SUBJECT_DETAILED_ASSESSMENT)
                                                      .setAssistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900))
                                                      .setAssistedOrderAssessmentSecondDropdownList1(
                                                          AssistedOrderCostDropdownList.INDEMNITY_BASIS)).build();
            String assistedOrderString = generator.getDetailedAssessment(caseData);

            assertThat(assistedOrderString).contains(CLAIMANT_DETAILED_INDEMNITY_ASSESSED_TEXT);
        }

        @Test
        void shouldReturnText_WhenSelected_Claimant_StandardDetailedAssessed() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                    AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()
                                                      .setMakeAnOrderForCostsList(AssistedOrderCostDropdownList.CLAIMANT)
                                                      .setAssistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.SUBJECT_DETAILED_ASSESSMENT)
                                                      .setAssistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900))
                                                      .setAssistedOrderAssessmentSecondDropdownList1(
                                                          AssistedOrderCostDropdownList.STANDARD_BASIS)).build();
            String assistedOrderString = generator.getDetailedAssessment(caseData);

            assertThat(assistedOrderString).contains(CLAIMANT_DETAILED_STANDARD_ASSESSED_TEXT);
        }

        @Test
        void shouldReturnText_WhenSelected_Defendant_StandardDetailedAssessed() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                    AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()
                                                      .setMakeAnOrderForCostsList(AssistedOrderCostDropdownList.DEFENDANT)
                                                      .setAssistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.SUBJECT_DETAILED_ASSESSMENT)
                                                      .setAssistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900))
                                                      .setAssistedOrderAssessmentSecondDropdownList1(
                                                          AssistedOrderCostDropdownList.STANDARD_BASIS)).build();
            String assistedOrderString = generator.getDetailedAssessment(caseData);

            assertThat(assistedOrderString).contains(DEFENDANT_DETAILED_STANDARD_ASSESSED_TEXT);
        }

        @Test
        void shouldReturnText_WhenSelected_Defendant_IndemnityDetailedAssessed() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                    AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()
                                                      .setMakeAnOrderForCostsList(AssistedOrderCostDropdownList.DEFENDANT)
                                                      .setAssistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.SUBJECT_DETAILED_ASSESSMENT)
                                                      .setAssistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900))
                                                      .setAssistedOrderAssessmentSecondDropdownList1(
                                                          AssistedOrderCostDropdownList.INDEMNITY_BASIS)).build();
            String assistedOrderString = generator.getDetailedAssessment(caseData);

            assertThat(assistedOrderString).contains(DEFENDANT_DETAILED_INDEMNITY_ASSESSED_TEXT);
        }

        @Test
        void shouldReturnNull_WhenMakeAnOrderForCostsEmpty_Detailed_Assessment() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                    AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()).build();
            String assistedOrderString = generator.getDetailedAssessment(caseData);

            assertThat(assistedOrderString).isNull();
        }

        @Test
        void shouldReturnNull_WhenMakeAnOrderForCostsListIsEmpty_Detailed_Assessment() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS).build();
            String assistedOrderString = generator.getDetailedAssessment(caseData);

            assertThat(assistedOrderString).isNull();
        }

        @Test
        void shouldReturnDifferentText_WhenSelected_Defendant_CostsDetailed_Assessment() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                    AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()
                                                      .setMakeAnOrderForCostsList(AssistedOrderCostDropdownList.DEFENDANT)
                                                      .setAssistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.COSTS)
                                                      .setAssistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900))).build();
            String assistedOrderString = generator.getDetailedAssessment(caseData);

            assertThat(assistedOrderString).isNull();
        }

        @Test
        void shouldReturnNull_WhenMakeAnOrderForCostsEmpty_InterimPayment() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                    AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()).build();
            String assistedOrderString = generator.getInterimPayment(caseData);

            assertThat(assistedOrderString).isNull();
        }

        @Test
        void shouldReturnNull_WhenMakeAnOrderForCostsListIsEmpty_InterimPayment() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS).build();
            String assistedOrderString = generator.getInterimPayment(caseData);

            assertThat(assistedOrderString).isNull();
        }

        @Test
        void shouldReturnDifferentText_WhenSelected_Defendant_CostsInterimPayment() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                    AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()
                                                      .setMakeAnOrderForCostsList(AssistedOrderCostDropdownList.DEFENDANT)
                                                      .setAssistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.COSTS)
                                                      .setAssistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900))).build();
            String assistedOrderString = generator.getInterimPayment(caseData);

            assertThat(assistedOrderString).isNull();
        }

        @Test
        void shouldReturnDifferentText_WhenSelected_Defendant_CostsInterimPayment_No() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                    AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()
                                                      .setMakeAnOrderForCostsList(AssistedOrderCostDropdownList.DEFENDANT)
                                                      .setAssistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.COSTS)
                                                      .setAssistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900))
                                                      .setAssistedOrderAssessmentSecondDropdownList2(
                                                          AssistedOrderCostDropdownList.NO)).build();
            String assistedOrderString = generator.getInterimPayment(caseData);

            assertThat(assistedOrderString).isNull();
        }

        @Test
        void shouldReturnText_WhenSelected_Defendant_InterimPayment() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                    AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()
                                                      .setMakeAnOrderForCostsList(AssistedOrderCostDropdownList.DEFENDANT)
                                                      .setAssistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.SUBJECT_DETAILED_ASSESSMENT)
                                                      .setAssistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900))
                                                      .setAssistedOrderAssessmentSecondDropdownList1(
                                                          AssistedOrderCostDropdownList.INDEMNITY_BASIS)
                                                      .setAssistedOrderAssessmentSecondDropdownList2(
                                                          AssistedOrderCostDropdownList.YES)
                                                      .setAssistedOrderAssessmentThirdDropdownAmount(BigDecimal.valueOf(
                                                          50000))).build();
            String assistedOrderString = generator.getInterimPayment(caseData);

            assertThat(assistedOrderString).contains(INTERIM_PAYMENT_TEXT);
        }

        @Test
        void shouldReturnDate_WhenSelected_Defendant_InterimPaymentDate() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                    AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()
                                                      .setMakeAnOrderForCostsList(AssistedOrderCostDropdownList.DEFENDANT)
                                                      .setAssistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.SUBJECT_DETAILED_ASSESSMENT)
                                                      .setAssistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900))
                                                      .setAssistedOrderAssessmentSecondDropdownList1(
                                                          AssistedOrderCostDropdownList.INDEMNITY_BASIS)
                                                      .setAssistedOrderAssessmentSecondDropdownList2(
                                                          AssistedOrderCostDropdownList.YES)
                                                      .setAssistedOrderAssessmentThirdDropdownAmount(BigDecimal.valueOf(
                                                          50000))
                                                      .setAssistedOrderAssessmentThirdDropdownDate(LocalDate.now().plusDays(
                                                          10))).build();
            LocalDate assistedOrderDate = generator.getInterimPaymentDate(caseData);

            assertThat(assistedOrderDate).isEqualTo(LocalDate.now().plusDays(10));
        }

        @Test
        void shouldReturnNull_WhenMakeAnOrderForCostsEmpty_InterimPaymentDate() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                    AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()).build();
            LocalDate assistedOrderDate = generator.getInterimPaymentDate(caseData);

            assertThat(assistedOrderDate).isNull();
        }

        @Test
        void shouldReturnNull_WhenMakeAnOrderForCostsListIsEmpty_InterimPaymentDate() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS).build();
            LocalDate assistedOrderDate = generator.getInterimPaymentDate(caseData);

            assertThat(assistedOrderDate).isNull();
        }

        @Test
        void shouldReturnDifferentText_WhenSelected_Defendant_CostsInterimPaymentDate() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                    AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()
                                                      .setMakeAnOrderForCostsList(AssistedOrderCostDropdownList.DEFENDANT)
                                                      .setAssistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.COSTS)
                                                      .setAssistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900))).build();
            LocalDate assistedOrderDate = generator.getInterimPaymentDate(caseData);

            assertThat(assistedOrderDate).isNull();
        }

        @Test
        void shouldReturnTrueWhenQocsProtectionEnabled() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                    AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()
                                                      .setMakeAnOrderForCostsList(AssistedOrderCostDropdownList.DEFENDANT)
                                                      .setAssistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.COSTS)
                                                      .setMakeAnOrderForCostsYesOrNo(YesOrNo.YES)
                                                      .setAssistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900))).build();
            Boolean checkQocsFlag = generator.checkIsQocsProtectionEnabled(caseData);

            assertThat(checkQocsFlag).isTrue();
        }

        @Test
        void shouldReturnFalseWhenQocsProtectionDisabled() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                    AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()
                                                      .setMakeAnOrderForCostsList(AssistedOrderCostDropdownList.DEFENDANT)
                                                      .setAssistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.COSTS)
                                                      .setMakeAnOrderForCostsYesOrNo(YesOrNo.NO)
                                                      .setAssistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900))).build();
            Boolean checkQocsFlag = generator.checkIsQocsProtectionEnabled(caseData);

            assertThat(checkQocsFlag).isFalse();
        }

        @Test
        void shouldReturnFalseWhenQocsProtectionDisabled_YesOrNoIsNull() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                    AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()
                                                      .setMakeAnOrderForCostsList(AssistedOrderCostDropdownList.DEFENDANT)
                                                      .setAssistedOrderCostsMakeAnOrderTopList(
                                                          AssistedOrderCostDropdownList.COSTS)
                                                      .setAssistedOrderCostsFirstDropdownAmount(BigDecimal.valueOf(78900))).build();
            Boolean checkQocsFlag = generator.checkIsQocsProtectionEnabled(caseData);

            assertThat(checkQocsFlag).isFalse();
        }

        @Test
        void shouldReturnFalseWhenQocsProtectionDisabled_MakeAnOrderForCostsIsNull() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedCostTypes(
                    AssistedCostTypesList.MAKE_AN_ORDER_FOR_DETAILED_COSTS)
                .assistedOrderMakeAnOrderForCosts(new AssistedOrderCost()).build();
            Boolean checkQocsFlag = generator.checkIsQocsProtectionEnabled(caseData);

            assertThat(checkQocsFlag).isFalse();
        }
    }

    @Nested
    class FurtherHearing {

        private final List<FinalOrderShowToggle> furtherHearingShowOption = new ArrayList<>();

        @Test
        void shouldReturnNull_When_FurtherHearing_NotSelected() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderFurtherHearingToggle(null)
                .build();
            Boolean checkToggle = generator.checkFurtherHearingToggle(caseData);
            assertThat(checkToggle).isFalse();
        }

        @Test
        void shouldReturnNull_FurtherHearingOption_Null() {
            furtherHearingShowOption.add(null);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderFurtherHearingToggle(furtherHearingShowOption)
                .build();
            Boolean checkToggle = generator.checkFurtherHearingToggle(caseData);
            assertThat(checkToggle).isFalse();
        }

        @Test
        void shouldReturnNull_When_FurtherHearingOption_NotShow() {
            furtherHearingShowOption.add(FinalOrderShowToggle.HIDE);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderFurtherHearingToggle(furtherHearingShowOption)
                .build();
            Boolean checkToggle = generator.checkFurtherHearingToggle(caseData);
            assertThat(checkToggle).isFalse();
        }

        @Test
        void shouldNotReturnNull_When_FurtherHearingOption_Show() {
            furtherHearingShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderFurtherHearingToggle(furtherHearingShowOption)
                .build();
            Boolean checkToggle = generator.checkFurtherHearingToggle(caseData);
            assertThat(checkToggle).isTrue();
        }

        @Test
        void shouldReturnYes_When_FurtherHearing_CheckListToDateExists() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderFurtherHearingDetails(new AssistedOrderFurtherHearingDetails().setListToDate(LocalDate.now().minusDays(
                    5)))
                .build();
            YesOrNo checkListToDate = generator.checkListToDate(caseData);
            assertThat(checkListToDate).isEqualTo(YesOrNo.YES);
        }

        @Test
        void shouldReturnNo_When_FurtherHearing_CheckListToDateDoesNotExists() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderFurtherHearingDetails(new AssistedOrderFurtherHearingDetails().setListFromDate(LocalDate.now().minusDays(
                    5)))
                .build();
            YesOrNo checkListToDate = generator.checkListToDate(caseData);
            assertThat(checkListToDate).isEqualTo(YesOrNo.NO);
        }

        @Test
        void shouldReturnNo_When_FurtherHearing_ListToDateDetailsNotFound() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderFurtherHearingDetails(new AssistedOrderFurtherHearingDetails())
                .build();
            YesOrNo checkListToDate = generator.checkListToDate(caseData);
            assertThat(checkListToDate).isEqualTo(YesOrNo.NO);
        }

        @Test
        void shouldReturnYes_When_FurtherHearing_ListToDateExists() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderFurtherHearingDetails(new AssistedOrderFurtherHearingDetails().setListToDate(LocalDate.now().minusDays(
                    5)))
                .build();
            LocalDate getListToDate = generator.getFurtherHearingListToDate(caseData);
            assertThat(getListToDate).isEqualTo(LocalDate.now().minusDays(5));
        }

        @Test
        void shouldReturnListToDate_When_FurtherHearing_ListToDateDoesNotExists() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderFurtherHearingDetails(new AssistedOrderFurtherHearingDetails().setListFromDate(LocalDate.now().minusDays(
                    5)))
                .build();
            LocalDate getListToDate = generator.getFurtherHearingListToDate(caseData);
            assertThat(getListToDate).isNull();
        }

        @Test
        void shouldReturnNo_When_FurtherHearing_DetailsNotFound() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderFurtherHearingDetails(new AssistedOrderFurtherHearingDetails())
                .build();
            LocalDate getListToDate = generator.getFurtherHearingListToDate(caseData);
            assertThat(getListToDate).isNull();
        }

        @Test
        void shouldReturnDate_When_FurtherHearing_ListFromDate() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderFurtherHearingDetails(new AssistedOrderFurtherHearingDetails().setListFromDate(LocalDate.now().minusDays(
                    5)))
                .build();
            LocalDate getListFromDate = generator.getFurtherHearingListFromDate(caseData);
            assertThat(getListFromDate).isEqualTo(LocalDate.now().minusDays(5));
        }

        @Test
        void shouldReturnDate_When_FurtherHearing_ListFromDateDetailsNotFound() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderFurtherHearingDetails(null).build();
            LocalDate getListFromDate = generator.getFurtherHearingListFromDate(caseData);
            assertThat(getListFromDate).isNull();
        }

        @Test
        void shouldReturnText_When_FurtherHearing_HearingMethodExists() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderFurtherHearingDetails(new AssistedOrderFurtherHearingDetails()
                                                        .setListFromDate(LocalDate.now().minusDays(5))
                                                        .setHearingMethods(GAJudicialHearingType.TELEPHONE))
                .build();
            String hearingMethodText = generator.getFurtherHearingMethod(caseData);
            assertThat(hearingMethodText).contains("TELEPHONE");
        }

        @Test
        void shouldReturnDate_When_FurtherHearing_HearingMethodDetailsNotFound() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderFurtherHearingDetails(null).build();
            String hearingMethodText = generator.getFurtherHearingMethod(caseData);
            assertThat(hearingMethodText).isNull();
        }

        @Test
        void shouldReturnText_When_FurtherHearing_HearingDurationExists_Hours() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderFurtherHearingDetails(new AssistedOrderFurtherHearingDetails()
                                                        .setListFromDate(LocalDate.now().minusDays(5))
                                                        .setLengthOfNewHearing(LengthOfHearing.HOURS_2)
                                                        .setHearingMethods(GAJudicialHearingType.TELEPHONE))
                .build();
            String hearingDurationText = generator.getFurtherHearingDuration(caseData);
            assertThat(hearingDurationText).contains("2 hours");
        }

        @Test
        void shouldReturnText_When_FurtherHearing_HearingDurationExists_Other() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderFurtherHearingDetails(new AssistedOrderFurtherHearingDetails()
                                                        .setListFromDate(LocalDate.now().minusDays(5))
                                                        .setLengthOfNewHearing(LengthOfHearing.OTHER)
                                                        .setLengthOfHearingOther(new HearingLength().setLengthListOtherDays(
                                                                2)
                                                                                  .setLengthListOtherHours(5)
                                                                                  .setLengthListOtherMinutes(30))
                                                        .setHearingMethods(GAJudicialHearingType.TELEPHONE))
                .build();
            String hearingDurationText = generator.getFurtherHearingDuration(caseData);
            assertThat(hearingDurationText).isEqualTo("2 days 5 hours 30 minutes");

            caseData = new GeneralApplicationCaseData()
                .assistedOrderFurtherHearingDetails(new AssistedOrderFurtherHearingDetails()
                                                        .setListFromDate(LocalDate.now().minusDays(5))
                                                        .setLengthOfNewHearing(LengthOfHearing.OTHER)
                                                        .setLengthOfHearingOther(new HearingLength().setLengthListOtherDays(
                                                                0)
                                                                                  .setLengthListOtherHours(5)
                                                                                  .setLengthListOtherMinutes(30))
                                                        .setHearingMethods(GAJudicialHearingType.TELEPHONE))
                .build();
            hearingDurationText = generator.getFurtherHearingDuration(caseData);
            assertThat(hearingDurationText).isEqualTo("5 hours 30 minutes");

            caseData = new GeneralApplicationCaseData()
                .assistedOrderFurtherHearingDetails(new AssistedOrderFurtherHearingDetails()
                                                        .setListFromDate(LocalDate.now().minusDays(5))
                                                        .setLengthOfNewHearing(LengthOfHearing.OTHER)
                                                        .setLengthOfHearingOther(new HearingLength().setLengthListOtherDays(
                                                                0)
                                                                                  .setLengthListOtherHours(5)
                                                                                  .setLengthListOtherMinutes(0))
                                                        .setHearingMethods(GAJudicialHearingType.TELEPHONE))
                .build();
            hearingDurationText = generator.getFurtherHearingDuration(caseData);
            assertThat(hearingDurationText).isEqualTo("5 hours");

            caseData = new GeneralApplicationCaseData()
                .assistedOrderFurtherHearingDetails(new AssistedOrderFurtherHearingDetails()
                                                        .setListFromDate(LocalDate.now().minusDays(5))
                                                        .setLengthOfNewHearing(LengthOfHearing.OTHER)
                                                        .setLengthOfHearingOther(new HearingLength().setLengthListOtherDays(
                                                                5)
                                                                                  .setLengthListOtherHours(0)
                                                                                  .setLengthListOtherMinutes(0))
                                                        .setHearingMethods(GAJudicialHearingType.TELEPHONE))
                .build();
            hearingDurationText = generator.getFurtherHearingDuration(caseData);
            assertThat(hearingDurationText).isEqualTo("5 days");

            caseData = new GeneralApplicationCaseData()
                .assistedOrderFurtherHearingDetails(new AssistedOrderFurtherHearingDetails()
                                                        .setListFromDate(LocalDate.now().minusDays(5))
                                                        .setLengthOfNewHearing(LengthOfHearing.OTHER)
                                                        .setLengthOfHearingOther(new HearingLength().setLengthListOtherDays(
                                                                0)
                                                                                  .setLengthListOtherHours(0)
                                                                                  .setLengthListOtherMinutes(30))
                                                        .setHearingMethods(GAJudicialHearingType.TELEPHONE))
                .build();
            hearingDurationText = generator.getFurtherHearingDuration(caseData);
            assertThat(hearingDurationText).isEqualTo("30 minutes");
        }

        @Test
        void shouldReturnDate_When_FurtherHearing_HearingDurationDetailsNotFound() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderFurtherHearingDetails(null).build();
            String hearingDurationText = generator.getFurtherHearingDuration(caseData);
            assertThat(hearingDurationText).isNull();
        }

        @Test
        void shouldReturnTrue_When_FurtherHearing_checkDatesToAvoidIsYes() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderFurtherHearingDetails(new AssistedOrderFurtherHearingDetails()
                                                        .setListFromDate(LocalDate.now().minusDays(5))
                                                        .setLengthOfNewHearing(LengthOfHearing.OTHER)
                                                        .setLengthOfHearingOther(new HearingLength().setLengthListOtherDays(
                                                                2)
                                                                                  .setLengthListOtherHours(5)
                                                                                  .setLengthListOtherMinutes(30))
                                                        .setDatesToAvoid(YesOrNo.YES)
                                                        .setHearingMethods(GAJudicialHearingType.TELEPHONE))
                .build();
            Boolean datesToAvoid = generator.checkDatesToAvoid(caseData);
            assertThat(datesToAvoid).isTrue();
        }

        @Test
        void shouldReturnTrue_When_FurtherHearing_checkDatesToAvoidIsNo() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderFurtherHearingDetails(new AssistedOrderFurtherHearingDetails()
                                                        .setListFromDate(LocalDate.now().minusDays(5))
                                                        .setLengthOfNewHearing(LengthOfHearing.OTHER)
                                                        .setLengthOfHearingOther(new HearingLength().setLengthListOtherDays(
                                                                2)
                                                                                  .setLengthListOtherHours(5)
                                                                                  .setLengthListOtherMinutes(30))
                                                        .setDatesToAvoid(YesOrNo.NO)
                                                        .setHearingMethods(GAJudicialHearingType.TELEPHONE))
                .build();
            Boolean datesToAvoid = generator.checkDatesToAvoid(caseData);
            assertThat(datesToAvoid).isFalse();
        }

        @Test
        void shouldReturnNo_When_FurtherHearing_checkDatesToAvoidDetailsNotFound() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderFurtherHearingDetails(null).build();
            Boolean datesToAvoid = generator.checkDatesToAvoid(caseData);
            assertThat(datesToAvoid).isFalse();
        }

        @Test
        void shouldReturnDate_When_FurtherHearing_FurtherHearingDatesToAvoidIsYes() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderFurtherHearingDetails(new AssistedOrderFurtherHearingDetails()
                                                        .setListFromDate(LocalDate.now().minusDays(5))
                                                        .setLengthOfNewHearing(LengthOfHearing.OTHER)
                                                        .setLengthOfHearingOther(new HearingLength().setLengthListOtherDays(
                                                                2)
                                                                                  .setLengthListOtherHours(5)
                                                                                  .setLengthListOtherMinutes(30))
                                                        .setDatesToAvoid(YesOrNo.YES)
                                                        .setDatesToAvoidDateDropdown(new AssistedOrderDateHeard().setDatesToAvoidDates(
                                                                LocalDate.now().plusDays(7))
                                                                                      )
                                                        .setHearingMethods(GAJudicialHearingType.TELEPHONE))
                .build();
            LocalDate datesToAvoid = generator.getFurtherHearingDatesToAvoid(caseData);
            assertThat(datesToAvoid).isEqualTo(LocalDate.now().plusDays(7));
        }

        @Test
        void shouldReturnDate_When_FurtherHearing_FurtherHearingDatesToAvoidIsNo() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderFurtherHearingDetails(new AssistedOrderFurtherHearingDetails()
                                                        .setListFromDate(LocalDate.now().minusDays(5))
                                                        .setLengthOfNewHearing(LengthOfHearing.OTHER)
                                                        .setLengthOfHearingOther(new HearingLength().setLengthListOtherDays(
                                                                2)
                                                                                  .setLengthListOtherHours(5)
                                                                                  .setLengthListOtherMinutes(30))
                                                        .setDatesToAvoid(YesOrNo.NO)
                                                        .setHearingMethods(GAJudicialHearingType.TELEPHONE))
                .build();
            LocalDate datesToAvoid = generator.getFurtherHearingDatesToAvoid(caseData);
            assertThat(datesToAvoid).isNull();
        }

        @Test
        void shouldReturnNull_When_FurtherHearing_DatesToAvoidDetailsNotFound() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderFurtherHearingDetails(null).build();
            LocalDate datesToAvoid = generator.getFurtherHearingDatesToAvoid(caseData);
            assertThat(datesToAvoid).isNull();
        }

        @Test
        void shouldReturnDate_When_FurtherHearing_FurtherHearingLocationIsOtherLocation() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderFurtherHearingDetails(new AssistedOrderFurtherHearingDetails()
                                                        .setListFromDate(LocalDate.now().minusDays(5))
                                                        .setLengthOfNewHearing(LengthOfHearing.OTHER)
                                                        .setLengthOfHearingOther(new HearingLength().setLengthListOtherDays(
                                                                2)
                                                                                  .setLengthListOtherHours(5)
                                                                                  .setLengthListOtherMinutes(30))
                                                        .setDatesToAvoid(YesOrNo.NO)
                                                        .setHearingLocationList(DynamicList.builder().value(
                                                            DynamicListElement.builder().label("Other location").build()).build())
                                                        .setAlternativeHearingLocation(DynamicList.builder().value(
                                                            DynamicListElement.builder().label(
                                                                "Site Name 2 - Address2 - 28000").build()).build())
                                                        .setHearingMethods(GAJudicialHearingType.TELEPHONE))
                .build();
            String hearingLocation = generator.getFurtherHearingLocation(caseData);
            assertThat(hearingLocation).contains("Site Name 2 - Address2 - 28000");
        }

        @Test
        void shouldReturnDate_When_FurtherHearing_FurtherHearingLocationIsCcmcc() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .locationName("ccmcc location")
                .assistedOrderFurtherHearingDetails(new AssistedOrderFurtherHearingDetails()
                                                        .setListFromDate(LocalDate.now().minusDays(5))
                                                        .setLengthOfNewHearing(LengthOfHearing.OTHER)
                                                        .setLengthOfHearingOther(new HearingLength().setLengthListOtherDays(
                                                                2)
                                                                                  .setLengthListOtherHours(5)
                                                                                  .setLengthListOtherMinutes(30))
                                                        .setDatesToAvoid(YesOrNo.NO)
                                                        .setHearingLocationList(DynamicList.builder().value(
                                                            DynamicListElement.builder().label("ccmcc location").build()).build())
                                                        .setHearingMethods(GAJudicialHearingType.TELEPHONE))
                .build();
            String hearingLocation = generator.getFurtherHearingLocation(caseData);
            assertThat(hearingLocation).contains("ccmcc location");
        }

        @Test
        void shouldReturnNull_When_FurtherHearing_LocationDetailsNotFound() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderFurtherHearingDetails(null).build();
            String furtherHearingLocation = generator.getFurtherHearingLocation(caseData);
            assertThat(furtherHearingLocation).isNull();
        }
    }

    @Nested
    class Recitals {
        @Test
        void shouldReturnNull_When_Recitals_NotSelected() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderRecitals(null)
                .build();
            Boolean checkToggle = generator.checkRecitalsToggle(caseData);
            assertThat(checkToggle).isFalse();
        }

        @Test
        void shouldReturnNull_RecitalOption_Null() {
            List<FinalOrderShowToggle> recitalsOrderShowOption = new ArrayList<>();
            recitalsOrderShowOption.add(null);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderRecitals(recitalsOrderShowOption)
                .build();
            Boolean checkToggle = generator.checkRecitalsToggle(caseData);
            assertThat(checkToggle).isFalse();
        }

        @Test
        void shouldReturnNull_When_RecitalOption_NotShow() {
            List<FinalOrderShowToggle> recitalsOrderShowOption = new ArrayList<>();
            recitalsOrderShowOption.add(FinalOrderShowToggle.HIDE);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderRecitals(recitalsOrderShowOption)
                .build();
            Boolean checkToggle = generator.checkRecitalsToggle(caseData);
            assertThat(checkToggle).isFalse();
        }

        @Test
        void shouldNotReturnNull_When_RecitalOption_Show() {
            List<FinalOrderShowToggle> recitalsOrderShowOption = new ArrayList<>();
            recitalsOrderShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderRecitals(recitalsOrderShowOption)
                .build();
            Boolean checkToggle = generator.checkRecitalsToggle(caseData);
            assertThat(checkToggle).isTrue();
        }

        @Test
        void shouldReturnNull_When_recitalsRecordedIsNull() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderRecitalsRecorded(null)
                .build();
            String assistedOrderString = generator.getRecitalRecordedText(caseData);
            assertNull(assistedOrderString);
        }

        @Test
        void shouldReturnText_When_recitalsRecordedIsNotNull() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderRecitalsRecorded(new AssistedOrderRecitalRecord().setText(RECITAL_RECORDED_TEXT))
                .build();
            String assistedOrderString = generator.getRecitalRecordedText(caseData);
            assertThat(assistedOrderString).contains(RECITAL_RECORDED_TEXT);
        }
    }

    @Nested
    class JudgeHeardFrom {

        private final List<FinalOrderShowToggle> judgeHeardFromShowOption = new ArrayList<>();

        @Test
        void shouldReturnNull_When_judgeHeardFromShowOption_NotSelected() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderJudgeHeardFrom(null)
                .build();
            Boolean checkToggle = generator.checkJudgeHeardFromToggle(caseData);
            assertThat(checkToggle).isFalse();
        }

        @Test
        void shouldReturnNull_judgeHeardFromShowOptionOption_Null() {
            judgeHeardFromShowOption.add(null);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .build();
            Boolean checkToggle = generator.checkJudgeHeardFromToggle(caseData);
            assertThat(checkToggle).isFalse();
        }

        @Test
        void shouldReturnNull_When_judgeHeardFromShowOption_NotShow() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.HIDE);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .build();
            Boolean checkToggle = generator.checkJudgeHeardFromToggle(caseData);
            assertThat(checkToggle).isFalse();
        }

        @Test
        void shouldNotReturnNull_When_judgeHeardFromShowOption_Show() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .build();
            Boolean checkToggle = generator.checkJudgeHeardFromToggle(caseData);
            assertThat(checkToggle).isTrue();
        }

        @Test
        void shouldReturnValue_When_RepresentationTypeIsNotNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(new AssistedOrderHeardRepresentation()
                                                 .setRepresentationType(HeardFromRepresentationTypes.CLAIMANT_AND_DEFENDANT)
                                                 )
                .build();
            String representationType = generator.getJudgeHeardFromRepresentation(caseData);
            assertThat(representationType).contains("Claimant and Defendant");
        }

        @Test
        void shouldReturnNull_When_RepresentationTypeIsNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .build();
            String representationType = generator.getJudgeHeardFromRepresentation(caseData);
            assertThat(representationType).isNull();
        }

        @Test
        void shouldReturnValue_When_ClaimantRepresentationIsNotNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(new AssistedOrderHeardRepresentation()
                                                 .setRepresentationType(HeardFromRepresentationTypes.CLAIMANT_AND_DEFENDANT)
                                                 .setClaimantDefendantRepresentation(new ClaimantDefendantRepresentation()
                                                                                      .setDefendantRepresentation(
                                                                                          DefendantRepresentationType.SOLICITOR_FOR_DEFENDANT)
                                                                                      .setClaimantRepresentation(
                                                                                          ClaimantRepresentationType.COUNSEL_FOR_CLAIMANT))
                                                 )
                .build();
            String claimantRepresentationType = generator.getClaimantRepresentation(caseData);
            assertThat(claimantRepresentationType).contains("Counsel for claimant");
        }

        @Test
        void shouldReturnNull_When_ClaimantRepresentationTypeDetailsIsNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .build();
            String representationType = generator.getClaimantRepresentation(caseData);
            assertThat(representationType).isNull();
        }

        @Test
        void shouldReturnValue_When_DefendantRepresentationIsNotNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(new AssistedOrderHeardRepresentation()
                                                 .setRepresentationType(HeardFromRepresentationTypes.CLAIMANT_AND_DEFENDANT)
                                                 .setClaimantDefendantRepresentation(new ClaimantDefendantRepresentation()
                                                                                      .setDefendantRepresentation(
                                                                                          DefendantRepresentationType.SOLICITOR_FOR_DEFENDANT)
                                                                                      .setClaimantRepresentation(
                                                                                          ClaimantRepresentationType.COUNSEL_FOR_CLAIMANT))
                                                 )
                .build();
            String defendantRepresentationType = generator.getDefendantRepresentation(caseData);
            assertThat(defendantRepresentationType).contains("Solicitor for defendant");
        }

        @Test
        void shouldReturnNull_When_DefendantRepresentationTypeDetailsIsNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .build();
            String representationType = generator.getDefendantRepresentation(caseData);
            assertThat(representationType).isNull();
        }

        @Test
        void shouldReturnValue_When_DefendantTwoRepresentationIsNotNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .isMultiParty(YesOrNo.YES)
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(new AssistedOrderHeardRepresentation()
                                                 .setRepresentationType(HeardFromRepresentationTypes.CLAIMANT_AND_DEFENDANT)
                                                 .setClaimantDefendantRepresentation(new ClaimantDefendantRepresentation()
                                                                                      .setDefendantRepresentation(
                                                                                          DefendantRepresentationType.SOLICITOR_FOR_DEFENDANT)
                                                                                      .setDefendantTwoRepresentation(
                                                                                          DefendantRepresentationType.COST_DRAFTSMAN_FOR_THE_DEFENDANT)
                                                                                      .setClaimantRepresentation(
                                                                                          ClaimantRepresentationType.COUNSEL_FOR_CLAIMANT))
                                                 )
                .build();
            String defendantRepresentationType = generator.getDefendantTwoRepresentation(caseData);
            assertThat(defendantRepresentationType).contains("Cost draftsman for the defendant");
        }

        @Test
        void shouldReturnValue_When_DefendantTwoRepresentationIsNotNull_1v1() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .isMultiParty(YesOrNo.NO)
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(new AssistedOrderHeardRepresentation()
                                                 .setRepresentationType(HeardFromRepresentationTypes.CLAIMANT_AND_DEFENDANT)
                                                 .setClaimantDefendantRepresentation(new ClaimantDefendantRepresentation()
                                                                                      .setDefendantRepresentation(
                                                                                          DefendantRepresentationType.SOLICITOR_FOR_DEFENDANT)
                                                                                      .setClaimantRepresentation(
                                                                                          ClaimantRepresentationType.COUNSEL_FOR_CLAIMANT))
                                                 )
                .build();
            String defendantRepresentationType = generator.getDefendantTwoRepresentation(caseData);
            assertThat(defendantRepresentationType).isNull();
        }

        @Test
        void shouldReturnNull_When_DefendantTwoRepresentationTypeDetailsIsNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .isMultiParty(YesOrNo.YES)
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .build();
            String representationType = generator.getDefendantTwoRepresentation(caseData);
            assertThat(representationType).isNull();
        }

        @Test
        void shouldReturnTrue_When_applicationIs_1v2() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .isMultiParty(YesOrNo.YES)
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(new AssistedOrderHeardRepresentation()
                                                 .setRepresentationType(HeardFromRepresentationTypes.CLAIMANT_AND_DEFENDANT)
                                                 .setClaimantDefendantRepresentation(new ClaimantDefendantRepresentation()
                                                                                      .setDefendantRepresentation(
                                                                                          DefendantRepresentationType.SOLICITOR_FOR_DEFENDANT)
                                                                                      .setClaimantRepresentation(
                                                                                          ClaimantRepresentationType.COUNSEL_FOR_CLAIMANT))
                                                 )
                .build();
            Boolean multipartyFlag = generator.checkIsMultiparty(caseData);
            assertThat(multipartyFlag).isTrue();
        }

        @Test
        void shouldReturnFalse_When_applicationIs_1v1() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .isMultiParty(YesOrNo.NO)
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(new AssistedOrderHeardRepresentation()
                                                 .setRepresentationType(HeardFromRepresentationTypes.CLAIMANT_AND_DEFENDANT)
                                                 .setClaimantDefendantRepresentation(new ClaimantDefendantRepresentation()
                                                                                      .setDefendantRepresentation(
                                                                                          DefendantRepresentationType.SOLICITOR_FOR_DEFENDANT)
                                                                                      .setClaimantRepresentation(
                                                                                          ClaimantRepresentationType.COUNSEL_FOR_CLAIMANT))
                                                 )
                .build();
            Boolean multipartyFlag = generator.checkIsMultiparty(caseData);
            assertThat(multipartyFlag).isFalse();
        }

        @Test
        void shouldReturnValue_When_DefendantNotAttendedIsNotNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(new AssistedOrderHeardRepresentation()
                                                 .setRepresentationType(HeardFromRepresentationTypes.CLAIMANT_AND_DEFENDANT)
                                                 .setClaimantDefendantRepresentation(new ClaimantDefendantRepresentation()
                                                                                      .setDefendantRepresentation(
                                                                                          DefendantRepresentationType.DEFENDANT_NOT_ATTENDING)
                                                                                      .setClaimantRepresentation(
                                                                                          ClaimantRepresentationType.SOLICITOR_FOR_CLAIMANT)
                                                                                      .setHeardFromDefendantNotAttend(
                                                                                          new HeardDefendantNotAttend()
                                                                                              .setListDef(
                                                                                                  ClaimantDefendantNotAttendingType.NOT_GIVEN_NOTICE_OF_APPLICATION)
                                                                                              ))
                                                 )
                .build();
            String heardDefendantNotAttended = generator.getHeardDefendantNotAttend(caseData);
            assertThat(heardDefendantNotAttended).contains("The defendant was not given notice of this application");
        }

        @Test
        void shouldReturnNull_When_DefendantNotAttending_OtherRepresentation() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(new AssistedOrderHeardRepresentation()
                                                 .setRepresentationType(HeardFromRepresentationTypes.OTHER_REPRESENTATION))
                .build();
            String heardDefendantNotAttended = generator.getHeardDefendantNotAttend(caseData);
            assertThat(heardDefendantNotAttended).isNull();
        }

        @Test
        void shouldReturnNull_When_DefendantNotAttendDetailsAreNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(null)
                .build();
            String heardDefendantNotAttended = generator.getHeardDefendantNotAttend(caseData);
            assertThat(heardDefendantNotAttended).isNull();
        }

        @Test
        void shouldReturnValue_When_DefendantTwoNotAttendedIsNotNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .isMultiParty(YesOrNo.YES)
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(new AssistedOrderHeardRepresentation()
                                                 .setRepresentationType(HeardFromRepresentationTypes.CLAIMANT_AND_DEFENDANT)
                                                 .setClaimantDefendantRepresentation(new ClaimantDefendantRepresentation()
                                                                                      .setDefendantTwoRepresentation(
                                                                                          DefendantRepresentationType.DEFENDANT_NOT_ATTENDING)
                                                                                      .setDefendantRepresentation(
                                                                                          DefendantRepresentationType.SOLICITOR_FOR_DEFENDANT)
                                                                                      .setClaimantRepresentation(
                                                                                          ClaimantRepresentationType.COUNSEL_FOR_CLAIMANT)
                                                                                      .setHeardFromDefendantTwoNotAttend(
                                                                                          new HeardDefendantTwoNotAttend()
                                                                                              .setListDefTwo(
                                                                                                  ClaimantDefendantNotAttendingType.NOT_GIVEN_NOTICE_OF_APPLICATION)
                                                                                              ))
                                                 )
                .build();
            String heardDefendantNotAttended = generator.getHeardDefendantTwoNotAttend(caseData);
            assertThat(heardDefendantNotAttended).contains("The defendant was not given notice of this application");
        }

        @Test
        void shouldReturnNull_When_DefendantTwoNotAttending_OtherRepresentation() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .isMultiParty(YesOrNo.YES)
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(new AssistedOrderHeardRepresentation()
                                                 .setRepresentationType(HeardFromRepresentationTypes.OTHER_REPRESENTATION))
                .build();
            String heardDefendantNotAttended = generator.getHeardDefendantTwoNotAttend(caseData);
            assertThat(heardDefendantNotAttended).isNull();
        }

        @Test
        void shouldReturnNull_When_DefendantTwoNotAttending_OtherRepresentation_1v1() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .isMultiParty(YesOrNo.NO)
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(new AssistedOrderHeardRepresentation()
                                                 .setRepresentationType(HeardFromRepresentationTypes.OTHER_REPRESENTATION))
                .build();
            String heardDefendantNotAttended = generator.getHeardDefendantTwoNotAttend(caseData);
            assertThat(heardDefendantNotAttended).isNull();
        }

        @Test
        void shouldReturnNull_When_DefendantTwoNotAttendDetailsAreNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .isMultiParty(YesOrNo.YES)
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(null)
                .build();
            String heardDefendantNotAttended = generator.getHeardDefendantTwoNotAttend(caseData);
            assertThat(heardDefendantNotAttended).isNull();
        }

        @Test
        void shouldReturnValue_When_ClaimantNotAttendedIsNotNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(new AssistedOrderHeardRepresentation()
                                                 .setRepresentationType(HeardFromRepresentationTypes.CLAIMANT_AND_DEFENDANT)
                                                 .setClaimantDefendantRepresentation(new ClaimantDefendantRepresentation()
                                                                                      .setDefendantRepresentation(
                                                                                          DefendantRepresentationType.SOLICITOR_FOR_DEFENDANT)
                                                                                      .setClaimantRepresentation(
                                                                                          ClaimantRepresentationType.CLAIMANT_NOT_ATTENDING)
                                                                                      .setHeardFromClaimantNotAttend(
                                                                                          new HeardClaimantNotAttend()
                                                                                              .setListClaim(
                                                                                                  ClaimantDefendantNotAttendingType
                                                                                                      .NOT_GIVEN_NOTICE_OF_APPLICATION_CLAIMANT)
                                                                                              ))
                                                 )
                .build();
            String heardClaimantNotAttended = generator.getHeardClaimantNotAttend(caseData);
            assertThat(heardClaimantNotAttended).contains("The claimant was not given notice of this application");
        }

        @Test
        void shouldReturnNull_When_OtherRepresentation() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(new AssistedOrderHeardRepresentation()
                                                 .setRepresentationType(HeardFromRepresentationTypes.OTHER_REPRESENTATION))
                .build();
            String heardClaimantNotAttended = generator.getHeardClaimantNotAttend(caseData);
            assertThat(heardClaimantNotAttended).isNull();
        }

        @Test
        void shouldReturnNull_When_ClaimantNotAttendDetailsAreNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(null)
                .build();
            String heardClaimantNotAttended = generator.getHeardClaimantNotAttend(caseData);
            assertThat(heardClaimantNotAttended).isNull();
        }

        @Test
        void shouldReturnTrue_When_SelectionIs_OtherRepresentation() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(new AssistedOrderHeardRepresentation()
                                                 .setRepresentationType(HeardFromRepresentationTypes.OTHER_REPRESENTATION))
                .build();
            Boolean selection = generator.checkIsOtherRepresentation(caseData);
            assertThat(selection).isTrue();
        }

        @Test
        void shouldReturnFalse_When_SelectionIs_ClaimantOrDefendantRepresentation() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(new AssistedOrderHeardRepresentation()
                                                 .setRepresentationType(HeardFromRepresentationTypes.CLAIMANT_AND_DEFENDANT))
                .build();
            Boolean selection = generator.checkIsOtherRepresentation(caseData);
            assertThat(selection).isFalse();
        }

        @Test
        void shouldReturnFalse_When_SelectionDetailsAreNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(null)
                .build();
            Boolean selection = generator.checkIsOtherRepresentation(caseData);
            assertThat(selection).isFalse();
        }

        @Test
        void shouldReturnText_When_SelectionIs_OtherRepresentation() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(new AssistedOrderHeardRepresentation()
                                                 .setRepresentationType(HeardFromRepresentationTypes.OTHER_REPRESENTATION)
                                                 .setOtherRepresentation(new DetailText().setDetailText(OTHER_ORIGIN_TEXT)))
                .build();
            String detailText = generator.getOtherRepresentationText(caseData);
            assertThat(detailText).contains(OTHER_ORIGIN_TEXT);
        }

        @Test
        void shouldReturnNull_When_SelectionIs_ClaimantOrDefendantRepresentation() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(new AssistedOrderHeardRepresentation()
                                                 .setRepresentationType(HeardFromRepresentationTypes.CLAIMANT_AND_DEFENDANT))
                .build();
            String detailText = generator.getOtherRepresentationText(caseData);
            assertThat(detailText).isNull();
        }

        @Test
        void shouldReturnNull_When_SelectionDetailsAreNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(null)
                .build();
            String detailText = generator.getOtherRepresentationText(caseData);
            assertThat(detailText).isNull();
        }

        @Test
        void shouldReturnTrue_When_JudgeConsideredPapers() {
            List<FinalOrderConsideredToggle> judgeConsideredPapers = new ArrayList<>();
            judgeConsideredPapers.add(FinalOrderConsideredToggle.CONSIDERED);
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .typeRepresentationJudgePapersList(judgeConsideredPapers)
                .assistedOrderRepresentation(new AssistedOrderHeardRepresentation()

                                                 .setRepresentationType(HeardFromRepresentationTypes.OTHER_REPRESENTATION)
                                                 .setOtherRepresentation(new DetailText().setDetailText(OTHER_ORIGIN_TEXT)))
                .build();
            Boolean judgeConsidered = generator.checkIsJudgeConsidered(caseData);
            assertThat(judgeConsidered).isTrue();
        }

        @Test
        void shouldReturnFalse_When_JudgeNotConsideredPapers() {
            List<FinalOrderConsideredToggle> judgeConsideredPapers = new ArrayList<>();
            judgeConsideredPapers.add(FinalOrderConsideredToggle.NOT_CONSIDERED);
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .typeRepresentationJudgePapersList(judgeConsideredPapers)
                .assistedOrderRepresentation(new AssistedOrderHeardRepresentation()
                                                 .setRepresentationType(HeardFromRepresentationTypes.OTHER_REPRESENTATION)
                                                 .setOtherRepresentation(new DetailText().setDetailText(OTHER_ORIGIN_TEXT)))
                .build();
            Boolean judgeConsidered = generator.checkIsJudgeConsidered(caseData);
            assertThat(judgeConsidered).isFalse();
        }

        @Test
        void shouldReturnFalse_When_JudgePapersListIsNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(new AssistedOrderHeardRepresentation()
                                                 .setRepresentationType(HeardFromRepresentationTypes.OTHER_REPRESENTATION)
                                                 .setOtherRepresentation(new DetailText().setDetailText(OTHER_ORIGIN_TEXT)))
                .build();
            Boolean judgeConsidered = generator.checkIsJudgeConsidered(caseData);
            assertThat(judgeConsidered).isFalse();
        }

        @Test
        void shouldReturnFalse_When_JudgePapersListDetailsAreNull() {
            judgeHeardFromShowOption.add(FinalOrderShowToggle.SHOW);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderJudgeHeardFrom(judgeHeardFromShowOption)
                .assistedOrderRepresentation(null)
                .build();
            Boolean judgeConsidered = generator.checkIsJudgeConsidered(caseData);
            assertThat(judgeConsidered).isFalse();
        }
    }

    @Nested
    class OrderMadeOn {

        @Test
        void shouldReturnTrueWhenOrderMadeWithInitiative() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .orderMadeOnOption(OrderMadeOnTypes.COURTS_INITIATIVE)
                .orderMadeOnOwnInitiative(new DetailTextWithDate()
                                              .setDetailText(TEST_TEXT)
                                              .setDate(LocalDate.now())
                                              )
                .build();
            Boolean checkInitiativeOrWithoutNotice = generator.checkInitiativeOrWithoutNotice(caseData);
            assertThat(checkInitiativeOrWithoutNotice).isTrue();
        }

        @Test
        void shouldReturnTrueWhenOrderMadeWithWithoutNotice() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .orderMadeOnOption(OrderMadeOnTypes.WITHOUT_NOTICE)
                .build();
            Boolean checkInitiativeOrWithoutNotice = generator.checkInitiativeOrWithoutNotice(caseData);
            assertThat(checkInitiativeOrWithoutNotice).isTrue();
        }

        @Test
        void shouldReturnFalseWhenOrderMadeWithOherTypeExceptWithOutNoticeOrInitiative() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .orderMadeOnOption(OrderMadeOnTypes.NONE)
                .build();
            Boolean checkInitiativeOrWithoutNotice = generator.checkInitiativeOrWithoutNotice(caseData);
            assertThat(checkInitiativeOrWithoutNotice).isFalse();
        }

        @Test
        void shouldReturnTrueWhen_OrderMadeWithInitiative() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .orderMadeOnOption(OrderMadeOnTypes.COURTS_INITIATIVE)
                .orderMadeOnOwnInitiative(new DetailTextWithDate()
                                              .setDetailText(TEST_TEXT)
                                              .setDate(LocalDate.now())
                                              )
                .build();
            Boolean checkInitiative = generator.checkInitiative(caseData);
            assertThat(checkInitiative).isTrue();
        }

        @Test
        void shouldReturnFalseWhenOrderMadeWithOherTypeExceptInitiative() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .orderMadeOnOption(OrderMadeOnTypes.WITHOUT_NOTICE)
                .build();
            Boolean checkInitiativeOrWithoutNotice = generator.checkInitiative(caseData);
            assertThat(checkInitiativeOrWithoutNotice).isFalse();
        }

        @Test
        void shouldReturnTextWhen_OrderMadeWithInitiative() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .orderMadeOnOption(OrderMadeOnTypes.COURTS_INITIATIVE)
                .orderMadeOnOwnInitiative(new DetailTextWithDate()
                                              .setDetailText(TEST_TEXT)
                                              .setDate(LocalDate.now())
                                              )
                .build();
            String orderMadeOnText = generator.getOrderMadeOnText(caseData);
            assertThat(orderMadeOnText).contains(TEST_TEXT);
        }

        @Test
        void shouldReturnTextWhen_OrderMadeWithWithoutNotice() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .orderMadeOnOption(OrderMadeOnTypes.WITHOUT_NOTICE)
                .orderMadeOnWithOutNotice(new DetailTextWithDate()
                                              .setDetailText(TEST_TEXT)
                                              .setDate(LocalDate.now())
                                              )
                .build();
            String orderMadeOnText = generator.getOrderMadeOnText(caseData);
            assertThat(orderMadeOnText).contains(TEST_TEXT);
        }

        @Test
        void shouldReturnNullWhen_OrderMadeWithNone() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .orderMadeOnOption(OrderMadeOnTypes.NONE)
                .build();
            String orderMadeOnText = generator.getOrderMadeOnText(caseData);
            assertThat(orderMadeOnText).contains("");
        }

        @Test
        void shouldReturnInitiativeDateWhen_OrderMadeWithInitiative() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .orderMadeOnOption(OrderMadeOnTypes.COURTS_INITIATIVE)
                .orderMadeOnOwnInitiative(new DetailTextWithDate()
                                              .setDetailText(TEST_TEXT)
                                              .setDate(LocalDate.now())
                                              )
                .build();
            LocalDate orderMadeDate = generator.getOrderMadeCourtInitiativeDate(caseData);
            assertThat(orderMadeDate).isEqualTo(LocalDate.now());
        }

        @Test
        void shouldReturnInitiativeDateNullWhen_OrderMadeWithoutNotice() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .orderMadeOnOption(OrderMadeOnTypes.WITHOUT_NOTICE)
                .orderMadeOnWithOutNotice(new DetailTextWithDate()
                                              .setDetailText(TEST_TEXT)
                                              .setDate(LocalDate.now())
                                              )
                .build();
            LocalDate orderMadeDate = generator.getOrderMadeCourtInitiativeDate(caseData);
            assertThat(orderMadeDate).isNull();
        }

        @Test
        void shouldReturnWithoutNoticeDateWhen_OrderMadeWithoutNotice() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .orderMadeOnOption(OrderMadeOnTypes.WITHOUT_NOTICE)
                .orderMadeOnWithOutNotice(new DetailTextWithDate()
                                              .setDetailText(TEST_TEXT)
                                              .setDate(LocalDate.now())
                                              )
                .build();
            LocalDate orderMadeDate = generator.getOrderMadeCourtWithOutNoticeDate(caseData);
            assertThat(orderMadeDate).isEqualTo(LocalDate.now());
        }

        @Test
        void shouldReturnWithoutNoticeDateNull_When_OrderMadeWithInitiative() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .orderMadeOnOption(OrderMadeOnTypes.COURTS_INITIATIVE)
                .orderMadeOnOwnInitiative(new DetailTextWithDate()
                                              .setDetailText(TEST_TEXT)
                                              .setDate(LocalDate.now())
                                              )
                .build();
            LocalDate orderMadeDate = generator.getOrderMadeCourtWithOutNoticeDate(caseData);
            assertThat(orderMadeDate).isNull();
        }
    }

    @Nested
    class AppealSection {

        @Test
        void shouldReturnNull_When_OrderAppeal_NotSelected() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderAppealToggle(null)
                .build();
            Boolean checkToggle = generator.checkAppealToggle(caseData);
            assertThat(checkToggle).isFalse();
        }

        @Test
        void shouldReturnNull_When_OrderAppealOption_Null() {
            List<FinalOrderShowToggle> recitalsOrderShowOption = new ArrayList<>();
            recitalsOrderShowOption.add(null);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderAppealToggle(recitalsOrderShowOption)
                .build();
            Boolean checkToggle = generator.checkAppealToggle(caseData);
            assertThat(checkToggle).isFalse();
        }

        @Test
        void shouldReturnNull_When_OrderAppealOption_NotShow() {
            List<FinalOrderShowToggle> recitalsOrderShowOption = new ArrayList<>();
            recitalsOrderShowOption.add(FinalOrderShowToggle.HIDE);
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderAppealToggle(recitalsOrderShowOption)
                .build();
            Boolean checkToggle = generator.checkAppealToggle(caseData);
            assertThat(checkToggle).isFalse();
        }

        @Test
        void shouldReturnText_WhenSelected_ClaimantOrDefendantAppeal_Claimant() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedOrderAppealDetails(
                new AssistedOrderAppealDetails()
                    .setAppealOrigin(AppealOriginTypes.CLAIMANT)).build();
            String assistedOrderString = generator.getClaimantOrDefendantAppeal(caseData);

            assertThat(assistedOrderString).contains("claimant");
        }

        @Test
        void shouldReturnText_WhenSelected_ClaimantOrDefendantAppeal_Defendant() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedOrderAppealDetails(
                new AssistedOrderAppealDetails()
                    .setAppealOrigin(AppealOriginTypes.DEFENDANT)).build();
            String assistedOrderString = generator.getClaimantOrDefendantAppeal(caseData);

            assertThat(assistedOrderString).contains("defendant");
        }

        @Test
        void shouldReturnText_WhenSelected_ClaimantOrDefendantAppeal_Other() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedOrderAppealDetails(
                new AssistedOrderAppealDetails()
                    .setAppealOrigin(AppealOriginTypes.OTHER)
                    .setOtherOriginText("test other origin text")).build();
            String assistedOrderString = generator.getClaimantOrDefendantAppeal(caseData);

            assertThat(assistedOrderString).contains(OTHER_ORIGIN_TEXT);
        }

        @Test
        void shouldReturnNull_When_AppealDetails_Null() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().build();
            String assistedOrderString = generator.getClaimantOrDefendantAppeal(caseData);

            assertThat(assistedOrderString).contains("");
        }

        @Test
        void shouldReturnNull_When_AppealOrigin_isNull() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedOrderAppealDetails(
                new AssistedOrderAppealDetails()
                    ).build();
            String assistedOrderString = generator.getClaimantOrDefendantAppeal(caseData);

            assertThat(assistedOrderString).contains("");
        }

        @Test
        void shouldReturnTrueWhenIsAppealGranted() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedOrderAppealDetails(
                new AssistedOrderAppealDetails()
                    .setAppealOrigin(AppealOriginTypes.DEFENDANT)
                    .setPermissionToAppeal(PermissionToAppealTypes.GRANTED)).build();
            Boolean isAppealGranted = generator.isAppealGranted(caseData);

            assertThat(isAppealGranted).isTrue();
        }

        @Test
        void shouldReturnFalseWhenAppealIsRefused() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedOrderAppealDetails(
                new AssistedOrderAppealDetails()
                    .setAppealOrigin(AppealOriginTypes.DEFENDANT)
                    .setPermissionToAppeal(PermissionToAppealTypes.REFUSED)).build();
            Boolean isAppealGranted = generator.isAppealGranted(caseData);

            assertThat(isAppealGranted).isFalse();
        }

        @Test
        void shouldReturnFalseWhenIsAppealNotGranted() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedOrderAppealDetails(
                new AssistedOrderAppealDetails()
                    .setPermissionToAppeal(PermissionToAppealTypes.REFUSED)
                    ).build();
            Boolean isAppealGranted = generator.isAppealGranted(caseData);

            assertThat(isAppealGranted).isFalse();
        }

        @Test
        void shouldReturnText_WhentableAorBIsSelected_Granted() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedOrderAppealDetails(
                new AssistedOrderAppealDetails()
                    .setAppealOrigin(AppealOriginTypes.DEFENDANT)
                    .setPermissionToAppeal(PermissionToAppealTypes.GRANTED)
                    .setAppealTypeChoicesForGranted(
                        new AppealTypeChoices()
                            .setAssistedOrderAppealJudgeSelection(PermissionToAppealTypes.CIRCUIT_COURT_JUDGE)
                            )).build();
            String assistedOrderString = generator.checkCircuitOrHighCourtJudge(caseData);

            assertThat(assistedOrderString).contains("A");
        }

        @Test
        void shouldReturnText_WhentableAorBIsSelected_Refused() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedOrderAppealDetails(
                new AssistedOrderAppealDetails()
                    .setAppealOrigin(AppealOriginTypes.DEFENDANT)
                    .setPermissionToAppeal(PermissionToAppealTypes.REFUSED)
                    .setAppealTypeChoicesForRefused(
                        new AppealTypeChoices()
                            .setAssistedOrderAppealJudgeSelectionRefuse(
                                PermissionToAppealTypes.CIRCUIT_COURT_JUDGE))).build();
            String assistedOrderString = generator.checkCircuitOrHighCourtJudge(caseData);

            assertThat(assistedOrderString).contains("A");
        }

        @Test
        void shouldReturnText_WhentableBIsSelected_Refused() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedOrderAppealDetails(
                new AssistedOrderAppealDetails()
                    .setAppealOrigin(AppealOriginTypes.DEFENDANT)
                    .setPermissionToAppeal(PermissionToAppealTypes.REFUSED)
                    .setAppealTypeChoicesForRefused(
                        new AppealTypeChoices()
                            .setAssistedOrderAppealJudgeSelectionRefuse(
                                PermissionToAppealTypes.HIGH_COURT_JUDGE)
                            )).build();
            String assistedOrderString = generator.checkCircuitOrHighCourtJudge(caseData);

            assertThat(assistedOrderString).contains("B");
        }

        @Test
        void shouldReturnText_WhentableBIsSelected_Granted() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedOrderAppealDetails(
                new AssistedOrderAppealDetails()
                    .setAppealOrigin(AppealOriginTypes.DEFENDANT)
                    .setPermissionToAppeal(PermissionToAppealTypes.GRANTED)
                    .setAppealTypeChoicesForGranted(
                        new AppealTypeChoices()
                            .setAssistedOrderAppealJudgeSelection(PermissionToAppealTypes.HIGH_COURT_JUDGE)
                            )).build();
            String assistedOrderString = generator.checkCircuitOrHighCourtJudge(caseData);

            assertThat(assistedOrderString).contains("B");
        }

        @Test
        void shouldReturnAppealDate_WhentableA_Granted() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedOrderAppealDetails(
                new AssistedOrderAppealDetails()
                    .setAppealOrigin(AppealOriginTypes.DEFENDANT)
                    .setPermissionToAppeal(PermissionToAppealTypes.GRANTED)
                    .setAppealTypeChoicesForGranted(
                        new AppealTypeChoices()
                            .setAssistedOrderAppealJudgeSelection(PermissionToAppealTypes.CIRCUIT_COURT_JUDGE)
                            .setAppealChoiceOptionA(
                                new AppealTypeChoiceList()
                                    .setAppealGrantedRefusedDate(LocalDate.now().plusDays(14))
                                    ))).build();
            LocalDate assistedOrderAppealDate = generator.getAppealDate(caseData);

            assertThat(assistedOrderAppealDate).isEqualTo(LocalDate.now().plusDays(14));
        }

        @Test
        void shouldReturnAppealDate_WhentableB_Granted() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedOrderAppealDetails(
                new AssistedOrderAppealDetails()
                    .setAppealOrigin(AppealOriginTypes.DEFENDANT)
                    .setPermissionToAppeal(PermissionToAppealTypes.GRANTED)
                    .setAppealTypeChoicesForGranted(
                        new AppealTypeChoices()
                            .setAssistedOrderAppealJudgeSelection(PermissionToAppealTypes.HIGH_COURT_JUDGE)
                            .setAppealChoiceOptionB(
                                new AppealTypeChoiceList()
                                    .setAppealGrantedRefusedDate(LocalDate.now().plusDays(14))
                                    ))).build();
            LocalDate assistedOrderAppealDate = generator.getAppealDate(caseData);

            assertThat(assistedOrderAppealDate).isEqualTo(LocalDate.now().plusDays(14));
        }

        @Test
        void shouldReturnAppealDate_WhentableA_Refused() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedOrderAppealDetails(
                new AssistedOrderAppealDetails()
                    .setAppealOrigin(AppealOriginTypes.DEFENDANT)
                    .setPermissionToAppeal(PermissionToAppealTypes.REFUSED)
                    .setAppealTypeChoicesForRefused(
                        new AppealTypeChoices()
                            .setAssistedOrderAppealJudgeSelectionRefuse(PermissionToAppealTypes.CIRCUIT_COURT_JUDGE)
                            .setAppealChoiceOptionA(
                                new AppealTypeChoiceList()
                                    .setAppealGrantedRefusedDate(LocalDate.now().plusDays(14)))
                            )).build();
            LocalDate assistedOrderAppealDate = generator.getAppealDate(caseData);

            assertThat(assistedOrderAppealDate).isEqualTo(LocalDate.now().plusDays(14));
        }

        @Test
        void shouldReturnAppealDate_WhentableB_Refused() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().assistedOrderAppealDetails(
                new AssistedOrderAppealDetails()
                    .setAppealOrigin(AppealOriginTypes.DEFENDANT)
                    .setPermissionToAppeal(PermissionToAppealTypes.REFUSED)
                    .setAppealTypeChoicesForRefused(
                        new AppealTypeChoices()
                            .setAssistedOrderAppealJudgeSelectionRefuse(PermissionToAppealTypes.HIGH_COURT_JUDGE)
                            .setAppealChoiceOptionB(
                                new AppealTypeChoiceList()
                                    .setAppealGrantedRefusedDate(LocalDate.now().plusDays(14))
                                    )
                            )).build();
            LocalDate assistedOrderAppealDate = generator.getAppealDate(caseData);

            assertThat(assistedOrderAppealDate).isEqualTo(LocalDate.now().plusDays(14));
        }

        @Test
        void shouldReturnNullAppealDate_WhenAssistedOrderAppealDetailsAreNull() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().build();
            LocalDate assistedOrderAppealDate = generator.getAppealDate(caseData);

            assertThat(assistedOrderAppealDate).isNull();
        }
    }

    @Nested
    class OrderMadeDate {

        @Test
        void shouldReturnTrueWhenOrderMadeSelectionIsSingleDate() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderMadeSelection(YesOrNo.YES)
                .assistedOrderMadeDateHeardDetails(new AssistedOrderMadeDateHeardDetails().setSingleDateSelection(
                    new AssistedOrderDateHeard().setSingleDate(LocalDate.now())))
                .build();
            Boolean isSingleDate = generator.checkIsSingleDate(caseData);
            assertThat(isSingleDate).isTrue();
        }

        @Test
        void shouldReturnFalseWhenOrderMadeSelectionIsNotSingleDate() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderMadeSelection(YesOrNo.YES)
                .assistedOrderMadeDateHeardDetails(new AssistedOrderMadeDateHeardDetails().setDateRangeSelection(
                    new AssistedOrderDateHeard().setDateRangeFrom(LocalDate.now().minusDays(10))))
                .build();
            Boolean isSingleDate = generator.checkIsSingleDate(caseData);
            assertThat(isSingleDate).isFalse();
        }

        @Test
        void shouldReturnFalseWhenOrderMadeSelectionIsNotExist() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderMadeSelection(YesOrNo.NO)
                .build();
            Boolean isSingleDate = generator.checkIsSingleDate(caseData);
            assertThat(isSingleDate).isFalse();
        }

        @Test
        void shouldReturnSingleDateWhenOrderMadeSelectionIsSingleDate() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderMadeSelection(YesOrNo.YES)
                .assistedOrderMadeDateHeardDetails(new AssistedOrderMadeDateHeardDetails().setSingleDateSelection(
                    new AssistedOrderDateHeard().setSingleDate(LocalDate.now())))
                .build();
            LocalDate assistedOrderDate = generator.getOrderMadeSingleDate(caseData);
            assertThat(assistedOrderDate).isEqualTo(LocalDate.now());
        }

        @Test
        void shouldReturnNullWhenOrderMadeSelectionIsNo() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderMadeSelection(YesOrNo.NO)
                .build();
            LocalDate assistedOrderDate = generator.getOrderMadeSingleDate(caseData);
            assertThat(assistedOrderDate).isNull();
        }

        @Test
        void shouldReturnNullWhenOrderMadeHeardsDetailsAreNull() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderMadeSelection(YesOrNo.YES)
                .assistedOrderMadeDateHeardDetails(new AssistedOrderMadeDateHeardDetails())
                .build();
            LocalDate assistedOrderDate = generator.getOrderMadeSingleDate(caseData);
            assertThat(assistedOrderDate).isNull();
        }

        @Test
        void shouldReturnTrueWhenOrderMadeSelectionIsDateRange() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderMadeSelection(YesOrNo.YES)
                .assistedOrderMadeDateHeardDetails(new AssistedOrderMadeDateHeardDetails().setDateRangeSelection(
                    new AssistedOrderDateHeard().setDateRangeFrom(LocalDate.now().minusDays(10))
                        .setDateRangeTo(LocalDate.now().minusDays(5))))
                .build();
            Boolean isSingleDate = generator.checkIsDateRange(caseData);
            assertThat(isSingleDate).isTrue();
        }

        @Test
        void shouldReturnFalseWhenOrderMadeSelectionIsNotDateRange() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderMadeSelection(YesOrNo.YES)
                .assistedOrderMadeDateHeardDetails(new AssistedOrderMadeDateHeardDetails().setSingleDateSelection(
                    new AssistedOrderDateHeard().setSingleDate(LocalDate.now().minusDays(10))))
                .build();
            Boolean isSingleDate = generator.checkIsDateRange(caseData);
            assertThat(isSingleDate).isFalse();
        }

        @Test
        void shouldReturnFalseWhenOrderMadeSelectionIsNotExistForDateRange() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderMadeSelection(YesOrNo.NO)
                .build();
            Boolean isSingleDate = generator.checkIsDateRange(caseData);
            assertThat(isSingleDate).isFalse();
        }

        @Test
        void shouldReturnDateRangeFrom_WhenOrderMadeSelectionIsDateRange() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderMadeSelection(YesOrNo.YES)
                .assistedOrderMadeDateHeardDetails(new AssistedOrderMadeDateHeardDetails().setDateRangeSelection(
                    new AssistedOrderDateHeard().setDateRangeFrom(LocalDate.now().minusDays(10))
                        .setDateRangeTo(LocalDate.now().minusDays(5))))
                .build();
            LocalDate dateRange = generator.getOrderMadeDateRangeFrom(caseData);
            assertThat(dateRange).isEqualTo(LocalDate.now().minusDays(10));
        }

        @Test
        void shouldNotReturnDateRangeFrom_WhenOrderMadeSelectionIsDateRange() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderMadeSelection(YesOrNo.YES)
                .assistedOrderMadeDateHeardDetails(new AssistedOrderMadeDateHeardDetails().setDateRangeSelection(
                    new AssistedOrderDateHeard()
                        .setDateRangeTo(LocalDate.now().minusDays(5))))
                .build();
            LocalDate dateRange = generator.getOrderMadeDateRangeFrom(caseData);
            assertThat(dateRange).isNull();
        }

        @Test
        void shouldNotReturnDateRangeFrom_WhenOrderMadeSelectionIsNull() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderMadeSelection(YesOrNo.NO)
                .build();
            LocalDate dateRange = generator.getOrderMadeDateRangeFrom(caseData);
            assertThat(dateRange).isNull();
        }

        @Test
        void shouldReturnDateRangeTo_WhenOrderMadeSelectionIsDateRange() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderMadeSelection(YesOrNo.YES)
                .assistedOrderMadeDateHeardDetails(new AssistedOrderMadeDateHeardDetails().setDateRangeSelection(
                    new AssistedOrderDateHeard().setDateRangeFrom(LocalDate.now().minusDays(10))
                        .setDateRangeTo(LocalDate.now().minusDays(5))))
                .build();
            LocalDate dateRange = generator.getOrderMadeDateRangeTo(caseData);
            assertThat(dateRange).isEqualTo(LocalDate.now().minusDays(5));
        }

        @Test
        void shouldNotReturnDateRangeTo_WhenOrderMadeSelectionIsDateRange() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderMadeSelection(YesOrNo.YES)
                .assistedOrderMadeDateHeardDetails(new AssistedOrderMadeDateHeardDetails().setDateRangeSelection(
                    new AssistedOrderDateHeard()
                        .setDateRangeFrom(LocalDate.now().minusDays(5))))
                .build();
            LocalDate dateRange = generator.getOrderMadeDateRangeTo(caseData);
            assertThat(dateRange).isNull();
        }

        @Test
        void shouldNotReturnDateRangeTo_WhenOrderMadeSelectionIsNull() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderMadeSelection(YesOrNo.NO)
                .build();
            LocalDate dateRange = generator.getOrderMadeDateRangeTo(caseData);
            assertThat(dateRange).isNull();
        }

        @Test
        void shouldReturnTrueWhenOrderMadeSelectionIsBeSpokeRange() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderMadeSelection(YesOrNo.YES)
                .assistedOrderMadeDateHeardDetails(new AssistedOrderMadeDateHeardDetails().setBeSpokeRangeSelection(
                    new AssistedOrderDateHeard().setBeSpokeRangeText("beSpoke text")))
                .build();
            Boolean isBeSpokeRange = generator.checkIsBeSpokeRange(caseData);
            assertThat(isBeSpokeRange).isTrue();
        }

        @Test
        void shouldReturnFalseWhenOrderMadeSelectionIsNotBeSpokeRange() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderMadeSelection(YesOrNo.YES)
                .assistedOrderMadeDateHeardDetails(new AssistedOrderMadeDateHeardDetails().setSingleDateSelection(
                    new AssistedOrderDateHeard().setSingleDate(LocalDate.now().minusDays(10))))
                .build();
            Boolean isBeSpokeRange = generator.checkIsBeSpokeRange(caseData);
            assertThat(isBeSpokeRange).isFalse();
        }

        @Test
        void shouldReturnFalseWhenOrderMadeSelectionIsNotExistForBeSpokeRange() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderMadeSelection(YesOrNo.NO)
                .build();
            Boolean isBeSpokeRange = generator.checkIsBeSpokeRange(caseData);
            assertThat(isBeSpokeRange).isFalse();
        }

        @Test
        void shouldReturnBeSpokeTextWhenOrderMadeSelectionIsBeSpokeRange() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderMadeSelection(YesOrNo.YES)
                .assistedOrderMadeDateHeardDetails(new AssistedOrderMadeDateHeardDetails().setBeSpokeRangeSelection(
                    new AssistedOrderDateHeard().setBeSpokeRangeText("beSpoke text")))
                .build();
            String beSpokeRangeText = generator.getOrderMadeBeSpokeText(caseData);
            assertThat(beSpokeRangeText).contains("beSpoke text");
        }

        @Test
        void shouldNotReturnBeSpokeTextWhenOrderMadeSelectionIsNo() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderMadeSelection(YesOrNo.NO)
                .build();
            String beSpokeRangeText = generator.getOrderMadeBeSpokeText(caseData);
            assertThat(beSpokeRangeText).isNull();
        }

        @Test
        void shouldReturnNullWhenOrderMadeSelectionIsBeSpokeRangeAndIsNull() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderMadeSelection(YesOrNo.YES)
                .assistedOrderMadeDateHeardDetails(new AssistedOrderMadeDateHeardDetails().setSingleDateSelection(
                    new AssistedOrderDateHeard().setSingleDate(LocalDate.now())))
                .build();
            String beSpokeRangeText = generator.getOrderMadeBeSpokeText(caseData);
            assertThat(beSpokeRangeText).isNull();
        }
    }

    @Nested
    class ReasonText {

        @Test
        void shouldReturnNull_When_GiveReasons_Null() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderGiveReasonsYesNo(null)
                .build();
            String assistedOrderString = generator.getReasonText(caseData);
            assertNull(assistedOrderString);
        }

        @Test
        void shouldReturnNull_When_GiveReasons_SelectedOption_No() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderGiveReasonsYesNo(YesOrNo.NO)
                .build();
            String assistedOrderString = generator.getReasonText(caseData);
            assertNull(assistedOrderString);
        }

        @Test
        void shouldReturnNull_When_GiveReasonsText_Null() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderGiveReasonsYesNo(YesOrNo.YES)
                .assistedOrderGiveReasonsDetails(new AssistedOrderGiveReasonsDetails())
                .build();
            String assistedOrderString = generator.getReasonText(caseData);
            assertNull(assistedOrderString);
        }

        @Test
        void shouldReturnText_When_GiveReasonsText() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .assistedOrderGiveReasonsYesNo(YesOrNo.YES)
                .assistedOrderGiveReasonsDetails(new AssistedOrderGiveReasonsDetails()
                                                     .setReasonsText(TEST_TEXT))
                .build();
            String assistedOrderString = generator.getReasonText(caseData);
            assertNotNull(assistedOrderString);
        }
    }

    @Nested
    class GetTemplateDataLip {

        @Test
        void test_getTemplate() {
            assertThat(generator.getTemplate(FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT)).isEqualTo(DocmosisTemplates.POST_JUDGE_ASSISTED_ORDER_FORM_LIP);
        }

        @Test
        void shouldGenerateAssistedOrderDocument() {
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(
                    new LocationRefData()
                        .setEpimmsId("2")
                        .setExternalShortName("Reading")
                );
            when(documentGeneratorService.generateDocmosisDocument(
                any(MappableObject.class),
                eq(POST_JUDGE_ASSISTED_ORDER_FORM_LIP)
            ))
                .thenReturn(new DocmosisDocument(POST_JUDGE_ASSISTED_ORDER_FORM_LIP.getDocumentTitle(), bytes));

            GeneralApplicationCaseData caseData = getSampleGeneralApplicationGeneralApplicationCaseData(NO).copy()
                .parentClaimantIsApplicant(YesOrNo.YES)
                .caseManagementLocation(CaseLocationCivil.builder().siteName("testing")
                                            .address("london court")
                                            .baseLocation("1")
                                            .postcode("BA 117").build())
                .judgeTitle("John Doe")
                .claimant2PartyName(null).build();

            generator.generate(
                GeneralApplicationCaseDataBuilder.builder().getCivilCaseData(),
                caseData,
                BEARER_TOKEN,
                FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT
            );

            verify(documentGeneratorService).generateDocmosisDocument(
                any(AssistedOrderForm.class),
                eq(POST_JUDGE_ASSISTED_ORDER_FORM_LIP)
            );
            verify(documentManagementService).uploadDocument(
                BEARER_TOKEN,
                new PDF(any(), any(), DocumentType.GENERAL_ORDER)
            );

            var templateData = generator
                .getTemplateData(
                    GeneralApplicationCaseDataBuilder.builder().getCivilCaseData(),
                    caseData,
                    BEARER_TOKEN,
                    FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT
                );

            assertThat(templateData.getCostsProtection()).isEqualTo(YesOrNo.YES);
            assertThat(templateData.getAddress()).isEqualTo("london court");
            assertThat(templateData.getSiteName()).isEqualTo("testing");
            assertThat(templateData.getPostcode()).isEqualTo("BA 117");
            assertThat(templateData.getCourtLocation()).isEqualTo("Reading");
            assertThat(templateData.getClaimant1Name()).isEqualTo(caseData.getClaimant1PartyName());
            assertThat(templateData.getClaimant2Name()).isNull();
            assertThat(templateData.getDefendant1Name()).isEqualTo(caseData.getDefendant1PartyName());
            assertThat(templateData.getDefendant2Name()).isNull();
            assertThat(templateData.getJudgeNameTitle()).isEqualTo("John Doe");
            assertEquals("respondent1partyname", templateData.getPartyName());
            assertEquals("respondent1address1", templateData.getPartyAddressAddressLine1());
            assertEquals("respondent1address2", templateData.getPartyAddressAddressLine2());
            assertEquals("respondent1address3", templateData.getPartyAddressAddressLine3());
            assertEquals("respondent1posttown", templateData.getPartyAddressPostTown());
            assertEquals("respondent1postcode", templateData.getPartyAddressPostCode());
        }
    }
}

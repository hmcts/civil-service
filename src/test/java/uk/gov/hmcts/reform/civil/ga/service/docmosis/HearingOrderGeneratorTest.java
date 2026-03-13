package uk.gov.hmcts.reform.civil.ga.service.docmosis;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.ga.enums.GAJudicialHearingType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAByCourtsInitiativeGAspec;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingDuration;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.ga.model.docmosis.JudgeDecisionPdfDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudgesHearingListGAspec;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAOrderCourtOwnInitiativeGAspec;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAOrderWithoutNoticeGAspec;
import uk.gov.hmcts.reform.civil.ga.service.JudicialTimeEstimateHelper;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.documentmanagement.SecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_ORDER;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.POST_JUDGE_HEARING_ORDER_LIP;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService.DATE_FORMATTER;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    HearingOrderGenerator.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class HearingOrderGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};

    @MockBean
    private SecuredDocumentManagementService documentManagementService;
    @MockBean
    private DocumentGeneratorService documentGeneratorService;
    @Autowired
    private HearingOrderGenerator hearingOrderGenerator;
    @MockBean
    private DocmosisService docmosisService;
    @MockBean
    private JudicialTimeEstimateHelper timeEstimateHelper;

    @Test
    void shouldGenerateHearingOrderDocument() {

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(HEARING_ORDER)))
            .thenReturn(new DocmosisDocument(HEARING_ORDER.getDocumentTitle(), bytes));
        when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
            .thenReturn(new LocationRefData().setEpimmsId("2").setExternalShortName("London"));
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().hearingOrderApplication(YesOrNo.NO, YesOrNo.NO).build();

        hearingOrderGenerator.generate(caseData, BEARER_TOKEN);

        verify(documentManagementService).uploadDocument(
            BEARER_TOKEN,
            new PDF(any(), any(), DocumentType.HEARING_ORDER)
        );
        verify(documentGeneratorService).generateDocmosisDocument(any(JudgeDecisionPdfDocument.class),
                                                                  eq(HEARING_ORDER));
    }

    @Test
    void shouldThrowExceptionWhenNoLocationMatch() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .hearingOrderApplication(YesOrNo.NO, YesOrNo.NO)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("8").build())
            .build();

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class),
                                                               eq(HEARING_ORDER)))
            .thenReturn(new DocmosisDocument(HEARING_ORDER.getDocumentTitle(), bytes));
        doThrow(new IllegalArgumentException("Court Name is not found in location data"))
            .when(docmosisService).getCaseManagementLocationVenueName(any(), any());

        Exception exception =
            assertThrows(IllegalArgumentException.class, () -> hearingOrderGenerator.generate(caseData, BEARER_TOKEN));
        String expectedMessage = "Court Name is not found in location data";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Nested
    class GetTemplateDataLip {
        @Test
        void shouldGenerateHearingOrderDocument() {

            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(POST_JUDGE_HEARING_ORDER_LIP)))
                .thenReturn(new DocmosisDocument(POST_JUDGE_HEARING_ORDER_LIP.getDocumentTitle(), bytes));
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(new LocationRefData().setEpimmsId("2").setExternalShortName("London"));
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().hearingOrderApplication(YesOrNo.NO, YesOrNo.NO).build();

            hearingOrderGenerator.generate(GeneralApplicationCaseDataBuilder.builder().getCivilCaseData(),
                                           caseData,
                                           BEARER_TOKEN, FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT);

            verify(documentManagementService).uploadDocument(
                BEARER_TOKEN,
                new PDF(any(), any(), DocumentType.HEARING_ORDER)
            );
            verify(documentGeneratorService).generateDocmosisDocument(any(JudgeDecisionPdfDocument.class),
                                                                      eq(POST_JUDGE_HEARING_ORDER_LIP));
        }

        @Test
        void whenJudgeMakeDecision_ShouldGetHearingOrderData() {
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(new LocationRefData().setEpimmsId("2").setExternalShortName("Reading"));
            when(timeEstimateHelper.getEstimatedHearingLength(any())).thenReturn("15 minutes");
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .hearingOrderApplication(YesOrNo.NO, YesOrNo.YES).build().copy()
                .isMultiParty(YES)
                .parentClaimantIsApplicant(NO)
                .build();

            var templateData = hearingOrderGenerator.getTemplateData(GeneralApplicationCaseDataBuilder.builder().getCivilCaseData(),
                                                                     caseData,
                                                                     "auth",
                                                                     FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT);

            assertThatFieldsAreCorrect_HearingOrder(templateData, caseData);
        }

        private void assertThatFieldsAreCorrect_HearingOrder(JudgeDecisionPdfDocument templateData, GeneralApplicationCaseData caseData) {
            Assertions.assertAll(
                "Hearing Order Document data should be as expected",
                () -> assertEquals(templateData.getClaimNumber(), caseData.getGeneralAppParentCaseLink().getCaseReference()),
                () -> assertEquals("sitename - location name - D12 8997", templateData.getJudgeHearingLocation()),
                () -> assertEquals(templateData.getJudgeNameTitle(), caseData.getJudgeTitle()),
                () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                () -> assertEquals(templateData.getClaimant2Name(), caseData.getClaimant2PartyName()),
                () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
                () -> assertEquals(YES, templateData.getIsMultiParty()),
                () -> assertEquals("Reading", templateData.getCourtName()),
                () -> assertEquals(templateData.getDefendant2Name(), caseData.getDefendant2PartyName()),
                () -> assertEquals(templateData.getHearingPrefType(), caseData.getJudicialListForHearing()
                    .getHearingPreferencesPreferredType().getDisplayedValue()),
                () -> assertEquals(templateData.getJudicialByCourtsInitiativeListForHearing(), caseData
                    .getOrderCourtOwnInitiativeListForHearing().getOrderCourtOwnInitiative()
                    + " ".concat(caseData.getOrderCourtOwnInitiativeListForHearing()
                                     .getOrderCourtOwnInitiativeDate().format(DATE_FORMATTER))),
                () -> assertEquals(templateData.getEstimatedHearingLength(),
                                   caseData.getJudicialListForHearing().getJudicialTimeEstimate().getDisplayedValue()),
                () -> assertEquals(templateData.getJudgeRecital(), caseData.getJudicialGeneralHearingOrderRecital()),
                () -> assertEquals(templateData.getHearingOrder(), caseData.getJudicialGOHearingDirections()),
                () -> assertEquals(templateData.getAddress(), caseData.getCaseManagementLocation().getAddress()),
                () -> assertEquals(templateData.getSiteName(), caseData.getCaseManagementLocation().getSiteName()),
                () -> assertEquals(templateData.getPostcode(), caseData.getCaseManagementLocation().getPostcode()),
                () -> assertEquals("applicant1partyname", templateData.getPartyName()),
                () -> assertEquals("address1", templateData.getPartyAddressAddressLine1()),
                () -> assertEquals("address2", templateData.getPartyAddressAddressLine2()),
                () -> assertEquals("address3", templateData.getPartyAddressAddressLine3()),
                () -> assertEquals("posttown", templateData.getPartyAddressPostTown()),
                () -> assertEquals("postcode", templateData.getPartyAddressPostCode()));
        }
    }

    @Nested
    class GetTemplateData {

        @Test
        void whenJudgeMakeDecision_ShouldGetHearingOrderData() {
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(new LocationRefData().setEpimmsId("2").setExternalShortName("Reading"));
            when(timeEstimateHelper.getEstimatedHearingLength(any())).thenReturn("15 minutes");
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .hearingOrderApplication(YesOrNo.NO, YesOrNo.YES).build().copy()
                .isMultiParty(YES)
                .build();

            var templateData = hearingOrderGenerator.getTemplateData(null, caseData, "auth", FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);

            assertThatFieldsAreCorrect_HearingOrder(templateData, caseData);
        }

        private void assertThatFieldsAreCorrect_HearingOrder(JudgeDecisionPdfDocument templateData, GeneralApplicationCaseData caseData) {
            Assertions.assertAll(
                "Hearing Order Document data should be as expected",
                () -> assertEquals(templateData.getClaimNumber(), caseData.getGeneralAppParentCaseLink().getCaseReference()),
                () -> assertEquals("sitename - location name - D12 8997", templateData.getJudgeHearingLocation()),
                () -> assertEquals(templateData.getJudgeNameTitle(), caseData.getJudgeTitle()),
                () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                () -> assertEquals(templateData.getClaimant2Name(), caseData.getClaimant2PartyName()),
                () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
                () -> assertEquals(YES, templateData.getIsMultiParty()),
                () -> assertEquals("Reading", templateData.getCourtName()),
                () -> assertEquals(templateData.getDefendant2Name(), caseData.getDefendant2PartyName()),
                () -> assertEquals(templateData.getHearingPrefType(), caseData.getJudicialListForHearing()
                    .getHearingPreferencesPreferredType().getDisplayedValue()),
                () -> assertEquals(templateData.getJudicialByCourtsInitiativeListForHearing(), caseData
                    .getOrderCourtOwnInitiativeListForHearing().getOrderCourtOwnInitiative()
                    + " ".concat(caseData.getOrderCourtOwnInitiativeListForHearing()
                                     .getOrderCourtOwnInitiativeDate().format(DATE_FORMATTER))),
                () -> assertEquals(templateData.getEstimatedHearingLength(),
                                   caseData.getJudicialListForHearing().getJudicialTimeEstimate().getDisplayedValue()),
                () -> assertEquals(templateData.getJudgeRecital(), caseData.getJudicialGeneralHearingOrderRecital()),
                () -> assertEquals(templateData.getHearingOrder(), caseData.getJudicialGOHearingDirections()),
                () -> assertEquals(templateData.getAddress(), caseData.getCaseManagementLocation().getAddress()),
                () -> assertEquals(templateData.getSiteName(), caseData.getCaseManagementLocation().getSiteName()),
                () -> assertEquals(templateData.getPostcode(), caseData.getCaseManagementLocation().getPostcode())
            );
        }

        @Test
        void whenJudgeMakeDecision_ShouldGetHearingOrderData_Option2() {
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(new LocationRefData().setEpimmsId("2").setExternalShortName("Manchester"));
            when(timeEstimateHelper.getEstimatedHearingLength(any())).thenReturn("15 minutes");
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .hearingOrderApplication(YesOrNo.NO, YesOrNo.YES).build().copy()
                .isMultiParty(NO)
                .judicialListForHearing(new GAJudgesHearingListGAspec()
                                            .setHearingPreferencesPreferredType(GAJudicialHearingType.PAPER_HEARING)
                                            .setJudicialTimeEstimate(GAHearingDuration.MINUTES_15))
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("3").build())
                .build();

            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.judicialByCourtsInitiativeListForHearing(GAByCourtsInitiativeGAspec.OPTION_2)
                .orderCourtOwnInitiativeListForHearing(new GAOrderCourtOwnInitiativeGAspec())
                .orderWithoutNoticeListForHearing(new GAOrderWithoutNoticeGAspec()
                                                      .setOrderWithoutNotice("abcd")
                                                      .setOrderWithoutNoticeDate(LocalDate.now())).build();

            GeneralApplicationCaseData updateData = caseDataBuilder.build();
            var templateData = hearingOrderGenerator.getTemplateData(null, updateData, "auth", FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);

            assertThatFieldsAreCorrect_HearingOrder_Option2(templateData, updateData);
        }

        private void assertThatFieldsAreCorrect_HearingOrder_Option2(JudgeDecisionPdfDocument templateData,
                                                                     GeneralApplicationCaseData caseData) {
            Assertions.assertAll(
                "Hearing Order Document data should be as expected",
                () -> assertEquals(templateData.getClaimNumber(), caseData.getGeneralAppParentCaseLink().getCaseReference()),
                () -> assertEquals(templateData.getJudgeNameTitle(), caseData.getJudgeTitle()),
                () -> assertNull(templateData.getJudgeHearingLocation()),
                () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                () -> assertEquals(templateData.getClaimant2Name(), caseData.getClaimant2PartyName()),
                () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
                () -> assertEquals(templateData.getDefendant2Name(), caseData.getDefendant2PartyName()),
                () -> assertEquals(NO, templateData.getIsMultiParty()),
                () -> assertEquals("Manchester", templateData.getCourtName()),
                () -> assertEquals(templateData.getHearingPrefType(), caseData.getJudicialListForHearing()
                    .getHearingPreferencesPreferredType().getDisplayedValue()),
                () -> assertEquals(templateData.getJudicialByCourtsInitiativeListForHearing(), caseData
                    .getOrderWithoutNoticeListForHearing().getOrderWithoutNotice()
                    + " ".concat(caseData.getOrderWithoutNoticeListForHearing()
                                     .getOrderWithoutNoticeDate().format(DATE_FORMATTER))),
                () -> assertEquals(templateData.getEstimatedHearingLength(),
                                   caseData.getJudicialListForHearing().getJudicialTimeEstimate().getDisplayedValue()),
                () -> assertEquals(templateData.getJudgeRecital(), caseData.getJudicialGeneralHearingOrderRecital()),
                () -> assertEquals(templateData.getHearingOrder(), caseData.getJudicialGOHearingDirections())
            );
        }

        @Test
        void whenJudgeMakeDecision_ShouldGetHearingOrderData_Option3() {
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(new LocationRefData().setEpimmsId("2").setExternalShortName("London"));
            when(timeEstimateHelper.getEstimatedHearingLength(any())).thenReturn("15 minutes");
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .hearingOrderApplication(YesOrNo.NO, YesOrNo.YES).build().copy()
                .isMultiParty(NO)
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("2").build())
                .build();

            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.judicialByCourtsInitiativeListForHearing(GAByCourtsInitiativeGAspec.OPTION_3)
                .orderCourtOwnInitiativeListForHearing(new GAOrderCourtOwnInitiativeGAspec())
                .orderWithoutNoticeListForHearing(new GAOrderWithoutNoticeGAspec()).build();

            GeneralApplicationCaseData updateData = caseDataBuilder.build();

            var templateData = hearingOrderGenerator.getTemplateData(null, updateData, "auth", FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);

            assertThatFieldsAreCorrect_HearingOrder_Option3(templateData, updateData);
        }

        private void assertThatFieldsAreCorrect_HearingOrder_Option3(JudgeDecisionPdfDocument templateData,
                                                                     GeneralApplicationCaseData caseData) {
            Assertions.assertAll(
                "Hearing Order Document data should be as expected",
                () -> assertEquals(templateData.getClaimNumber(), caseData.getGeneralAppParentCaseLink().getCaseReference()),
                () -> assertEquals(templateData.getJudgeNameTitle(), caseData.getJudgeTitle()),
                () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                () -> assertEquals(templateData.getClaimant2Name(), caseData.getClaimant2PartyName()),
                () -> assertEquals(NO, templateData.getIsMultiParty()),
                () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
                () -> assertEquals("London", templateData.getCourtName()),
                () -> assertEquals(templateData.getDefendant2Name(), caseData.getDefendant2PartyName()),
                () -> assertEquals(templateData.getHearingPrefType(), caseData.getJudicialListForHearing()
                    .getHearingPreferencesPreferredType().getDisplayedValue()),
                () -> assertEquals(StringUtils.EMPTY, templateData.getJudicialByCourtsInitiativeListForHearing()),
                () -> assertEquals(templateData.getEstimatedHearingLength(),
                                   caseData.getJudicialListForHearing().getJudicialTimeEstimate().getDisplayedValue()),
                () -> assertEquals(templateData.getJudgeRecital(), caseData.getJudicialGeneralHearingOrderRecital()),
                () -> assertEquals(templateData.getHearingOrder(), caseData.getJudicialGOHearingDirections())
            );
        }

        @Test
        void whenJudgeMakeDecision_ShouldGetHearingOrderData_Option3_1v1() {
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(new LocationRefData().setEpimmsId("2").setExternalShortName("Reading"));
            when(timeEstimateHelper.getEstimatedHearingLength(any())).thenReturn("15 minutes");
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .hearingOrderApplication(YesOrNo.NO, YesOrNo.YES).build().copy()
                .isMultiParty(YES)
                .defendant2PartyName(null)
                .claimant2PartyName(null)
                .build();

            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.judicialByCourtsInitiativeListForHearing(GAByCourtsInitiativeGAspec.OPTION_3)
                .orderCourtOwnInitiativeListForHearing(new GAOrderCourtOwnInitiativeGAspec())
                .orderWithoutNoticeListForHearing(new GAOrderWithoutNoticeGAspec()).build();

            GeneralApplicationCaseData updateData = caseDataBuilder.build();

            var templateData = hearingOrderGenerator.getTemplateData(null, updateData, "auth", FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);

            assertThatFieldsAreCorrect_HearingOrder_Option3_1v1(templateData, updateData);
        }

        private void assertThatFieldsAreCorrect_HearingOrder_Option3_1v1(JudgeDecisionPdfDocument templateData,
                                                                     GeneralApplicationCaseData caseData) {
            Assertions.assertAll(
                "Hearing Order Document data should be as expected",
                () -> assertEquals(templateData.getClaimNumber(), caseData.getGeneralAppParentCaseLink().getCaseReference()),
                () -> assertEquals(templateData.getJudgeNameTitle(), caseData.getJudgeTitle()),
                () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                () -> assertEquals(templateData.getClaimant2Name(), caseData.getClaimant2PartyName()),
                () -> assertEquals(YES, templateData.getIsMultiParty()),
                () -> assertEquals("Reading", templateData.getCourtName()),
                () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
                () -> assertEquals(templateData.getDefendant2Name(), caseData.getDefendant2PartyName()),
                () -> assertEquals(templateData.getHearingPrefType(), caseData.getJudicialListForHearing()
                    .getHearingPreferencesPreferredType().getDisplayedValue()),
                () -> assertEquals(StringUtils.EMPTY, templateData.getJudicialByCourtsInitiativeListForHearing()),
                () -> assertEquals(templateData.getEstimatedHearingLength(),
                                   caseData.getJudicialListForHearing().getJudicialTimeEstimate().getDisplayedValue()),
                () -> assertEquals(templateData.getJudgeRecital(), caseData.getJudicialGeneralHearingOrderRecital()),
                () -> assertEquals(templateData.getHearingOrder(), caseData.getJudicialGOHearingDirections())
            );
        }
    }
}

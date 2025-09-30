package uk.gov.hmcts.reform.civil.service.docmosis.directionorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.SecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.FinalOrderShowToggle;
import uk.gov.hmcts.reform.civil.enums.dq.GAByCourtsInitiativeGAspec;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.judgedecisionpdfdocument.JudgeDecisionPdfDocument;
import uk.gov.hmcts.reform.civil.model.genapplication.GACaseLocation;
import uk.gov.hmcts.reform.civil.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.GeneralAppLocationRefDataService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

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
import static uk.gov.hmcts.reform.civil.enums.dq.GAJudgeMakeAnOrderOption.GIVE_DIRECTIONS_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DIRECTION_ORDER;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.POST_JUDGE_DIRECTION_ORDER_LIP;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService.DATE_FORMATTER;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    DirectionOrderGenerator.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class DirectionOrderGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};

    @MockBean
    private SecuredDocumentManagementService documentManagementService;
    @MockBean
    private DocumentGeneratorService documentGeneratorService;
    @Autowired
    private DirectionOrderGenerator directionOrderGenerator;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private DocmosisService docmosisService;
    @MockBean
    private GeneralAppLocationRefDataService generalAppLocationRefDataService;

    private static List<LocationRefData> locationRefData = Arrays
        .asList(LocationRefData.builder().epimmsId("1").externalShortName("Reading").build(),
                LocationRefData.builder().epimmsId("2").externalShortName("London").build(),
                LocationRefData.builder().epimmsId("3").externalShortName("Manchester").build());

    @BeforeEach
    public void setUp() {

        when(generalAppLocationRefDataService.getCourtLocations(any())).thenReturn(locationRefData);
    }

    @Test
    void shouldGenerateDirectionOrderDocument() {

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DIRECTION_ORDER)))
            .thenReturn(new DocmosisDocument(DIRECTION_ORDER.getDocumentTitle(), bytes));
        when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
            .thenReturn(LocationRefData.builder().epimmsId("2").externalShortName("London").build());
        CaseData caseData = CaseDataBuilder.builder().directionOrderApplication().build();

        directionOrderGenerator.generate(caseData, BEARER_TOKEN);

        verify(documentManagementService).uploadDocument(
            BEARER_TOKEN,
            new PDF(any(), any(), DocumentType.DIRECTION_ORDER)
        );
        verify(documentGeneratorService).generateDocmosisDocument(any(JudgeDecisionPdfDocument.class),
                                                                  eq(DIRECTION_ORDER));
    }

    @Test
    void shouldThrowExceptionWhenNoLocationMatch() {
        CaseData caseData = CaseDataBuilder.builder().directionOrderApplication()
            .gaCaseManagementLocation(GACaseLocation.builder().baseLocation("8").build())
            .build();

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DIRECTION_ORDER)))
            .thenReturn(new DocmosisDocument(DIRECTION_ORDER.getDocumentTitle(), bytes));
        doThrow(new IllegalArgumentException("Court Name is not found in location data"))
            .when(docmosisService).getCaseManagementLocationVenueName(any(), any());

        Exception exception =
            assertThrows(IllegalArgumentException.class, ()
                -> directionOrderGenerator.generate(caseData, BEARER_TOKEN));

        String expectedMessage = "Court Name is not found in location data";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Nested
    class GetTemplateData {

        @Test
        void whenJudgeMakeDecision_ShouldGetHearingOrderData() {
            CaseData caseData = CaseDataBuilder.builder().directionOrderApplication().build().toBuilder()
                .isMultiParty(YES)
                .build();

            when(docmosisService.reasonAvailable(any())).thenReturn(YesOrNo.NO);
            when(docmosisService.populateJudgeReason(any())).thenReturn("");
            when(docmosisService.populateJudicialByCourtsInitiative(any()))
                .thenReturn("abcd ".concat(LocalDate.now().format(DATE_FORMATTER)));
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(LocationRefData.builder().epimmsId("2").externalShortName("Reading").build());

            var templateData = directionOrderGenerator.getTemplateData(null, caseData, "auth", FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);

            assertThatFieldsAreCorrect_DirectionOrder(templateData, caseData);
        }

        private void assertThatFieldsAreCorrect_DirectionOrder(JudgeDecisionPdfDocument templateData,
                                                               CaseData caseData) {
            Assertions.assertAll(
                "Direction Order Document data should be as expected",
                () -> assertEquals(templateData.getClaimNumber(), caseData.getCcdCaseReference().toString()),
                () -> assertEquals(templateData.getJudgeNameTitle(), caseData.getJudgeTitle()),
                () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                () -> assertEquals(templateData.getClaimant2Name(), caseData.getClaimant2PartyName()),
                () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
                () -> assertEquals(templateData.getCourtName(), "Reading"),
                () -> assertEquals(templateData.getDefendant2Name(), caseData.getDefendant2PartyName()),
                () -> assertEquals(YES, templateData.getIsMultiParty()),
                () -> assertEquals(templateData.getJudgeDirection(),
                                   caseData.getJudicialDecisionMakeOrder().getDirectionsText()),
                () -> assertEquals(templateData.getLocationName(), caseData.getLocationName()),
                () -> assertEquals(templateData.getJudicialByCourtsInitiative(), caseData
                    .getJudicialDecisionMakeOrder().getOrderCourtOwnInitiative()
                    + " ".concat(LocalDate.now().format(DATE_FORMATTER))),
                () -> assertEquals(templateData.getJudgeRecital(),
                                   caseData.getJudicialDecisionMakeOrder().getJudgeRecitalText()),
                () -> assertEquals(templateData.getAddress(), caseData.getGaCaseManagementLocation().getAddress()),
                () -> assertEquals(templateData.getSiteName(), caseData.getGaCaseManagementLocation().getSiteName()),
                () -> assertEquals(templateData.getPostcode(), caseData.getGaCaseManagementLocation().getPostcode())
            );
        }

        @Test
        void whenJudgeMakeDecision_ShouldGetHearingOrderData_1v1() {
            CaseData caseData = CaseDataBuilder.builder().directionOrderApplication().build().toBuilder()
                .defendant2PartyName(null)
                .claimant2PartyName(null)
                .gaCaseManagementLocation(GACaseLocation.builder().baseLocation("3").build())
                .isMultiParty(NO)
                .build();

            when(docmosisService.reasonAvailable(any())).thenReturn(YesOrNo.NO);
            when(docmosisService.populateJudgeReason(any())).thenReturn("");
            when(docmosisService.populateJudicialByCourtsInitiative(any()))
                .thenReturn("abcd ".concat(LocalDate.now().format(DATE_FORMATTER)));
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(LocationRefData.builder().epimmsId("2").externalShortName("Manchester").build());

            var templateData = directionOrderGenerator.getTemplateData(null, caseData, "auth", FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);

            assertThatFieldsAreCorrect_DirectionOrder_1v1(templateData, caseData);
        }

        private void assertThatFieldsAreCorrect_DirectionOrder_1v1(JudgeDecisionPdfDocument templateData,
                                                               CaseData caseData) {
            Assertions.assertAll(
                "Direction Order Document data should be as expected",
                () -> assertEquals(templateData.getClaimNumber(), caseData.getCcdCaseReference().toString()),
                () -> assertEquals(templateData.getJudgeNameTitle(), caseData.getJudgeTitle()),
                () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                () -> assertNull(templateData.getClaimant2Name()),
                () -> assertEquals(templateData.getCourtName(), "Manchester"),
                () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
                () -> assertNull(templateData.getDefendant2Name()),
                () -> assertEquals(NO, templateData.getIsMultiParty()));
        }

        @Test
        void whenJudgeMakeDecision_ShouldGetHearingOrderData_Option2() {
            CaseData caseData = CaseDataBuilder.builder().directionOrderApplication().build().toBuilder()
                .isMultiParty(YES)
                .gaCaseManagementLocation(GACaseLocation.builder().baseLocation("2").build())
                .build();

            CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
            caseDataBuilder.judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                                           .directionsText("Test Direction")
                                                           .judicialByCourtsInitiative(
                                                               GAByCourtsInitiativeGAspec.OPTION_2)
                                                           .orderWithoutNotice("abcdef")
                                                           .orderWithoutNoticeDate(LocalDate.now())
                                                           .reasonForDecisionText("Test Reason")
                                                           .showReasonForDecision(YesOrNo.YES)
                                                           .makeAnOrder(GIVE_DIRECTIONS_WITHOUT_HEARING)
                                                           .directionsResponseByDate(LocalDate.now())
                                                           .showJudgeRecitalText(List.of(FinalOrderShowToggle.SHOW))
                                                           .judgeRecitalText("Test Judge's recital")
                                                           .build()).build();
            CaseData updateCaseData = caseDataBuilder.build();

            when(docmosisService.reasonAvailable(any())).thenReturn(YesOrNo.NO);
            when(docmosisService.populateJudgeReason(any())).thenReturn("Test Reason");
            when(docmosisService.populateJudicialByCourtsInitiative(any()))
                .thenReturn("abcdef ".concat(LocalDate.now().format(DATE_FORMATTER)));
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(LocationRefData.builder().epimmsId("2").externalShortName("London").build());

            var templateData = directionOrderGenerator.getTemplateData(null, updateCaseData, "auth", FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);

            assertJudicialByCourtsInitiative_Option2(templateData, updateCaseData);
        }

        private void assertJudicialByCourtsInitiative_Option2(JudgeDecisionPdfDocument templateData,
                                                               CaseData caseData) {
            Assertions.assertAll(
                "Direction Order Document data should be as expected",
                () -> assertEquals(templateData.getClaimNumber(), caseData.getCcdCaseReference().toString()),
                () -> assertEquals(templateData.getJudgeNameTitle(), caseData.getJudgeTitle()),
                () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                () -> assertEquals(templateData.getClaimant2Name(), caseData.getClaimant2PartyName()),
                () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
                () -> assertEquals(templateData.getCourtName(), "London"),
                () -> assertEquals(templateData.getDefendant2Name(), caseData.getDefendant2PartyName()),
                () -> assertEquals(YES, templateData.getIsMultiParty()),
                () -> assertEquals(templateData.getJudgeDirection(),
                                   caseData.getJudicialDecisionMakeOrder().getDirectionsText()),
                () -> assertEquals(templateData.getLocationName(), caseData.getLocationName()),
                () -> assertEquals(templateData.getJudicialByCourtsInitiative(), caseData
                    .getJudicialDecisionMakeOrder().getOrderWithoutNotice()
                    + " ".concat(LocalDate.now().format(DATE_FORMATTER))),
                () -> assertEquals(templateData.getJudgeRecital(),
                                   caseData.getJudicialDecisionMakeOrder().getJudgeRecitalText()),
                () -> assertEquals(templateData.getReasonForDecision(),
                            caseData.getJudicialDecisionMakeOrder().getReasonForDecisionText())
            );
        }

        @Test
        void whenJudgeMakeDecision_ShouldGetHearingOrderData_Option3() {
            CaseData caseData = CaseDataBuilder.builder().directionOrderApplication().build().toBuilder()
                .isMultiParty(YES)
                .build();

            CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
            caseDataBuilder.judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                                          .directionsText("Test Direction")
                                                          .judicialByCourtsInitiative(
                                                              GAByCourtsInitiativeGAspec.OPTION_3)
                                                          .showReasonForDecision(YesOrNo.YES)
                                                          .reasonForDecisionText("Test Reason")
                                                          .makeAnOrder(GIVE_DIRECTIONS_WITHOUT_HEARING)
                                                          .directionsResponseByDate(LocalDate.now())
                                                          .showJudgeRecitalText(List.of(FinalOrderShowToggle.SHOW))
                                                          .judgeRecitalText("Test Judge's recital")
                                                          .build()).build();

            CaseData updateCaseData = caseDataBuilder.build();

            when(docmosisService.reasonAvailable(any())).thenReturn(YesOrNo.YES);
            when(docmosisService.populateJudgeReason(any())).thenReturn("Test Reason");
            when(docmosisService.populateJudicialByCourtsInitiative(any()))
                .thenReturn(StringUtils.EMPTY);
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(LocationRefData.builder().epimmsId("2").externalShortName("Reading").build());

            var templateData = directionOrderGenerator.getTemplateData(null, updateCaseData, "auth", FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);

            assertJudicialByCourtsInitiative_Option3(templateData, updateCaseData);
        }

        private void assertJudicialByCourtsInitiative_Option3(JudgeDecisionPdfDocument templateData,
                                                      CaseData caseData) {
            Assertions.assertAll(
                "Direction Order Document data should be as expected",
                () -> assertEquals(templateData.getClaimNumber(), caseData.getCcdCaseReference().toString()),
                () -> assertEquals(templateData.getJudgeNameTitle(), caseData.getJudgeTitle()),
                () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                () -> assertEquals(templateData.getClaimant2Name(), caseData.getClaimant2PartyName()),
                () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
                () -> assertEquals(templateData.getDefendant2Name(), caseData.getDefendant2PartyName()),
                () -> assertEquals(YES, templateData.getIsMultiParty()),
                () -> assertEquals(templateData.getCourtName(), "Reading"),
                () -> assertEquals(templateData.getJudgeDirection(),
                                   caseData.getJudicialDecisionMakeOrder().getDirectionsText()),
                () -> assertEquals(templateData.getLocationName(), caseData.getLocationName()),
                () -> assertEquals(StringUtils.EMPTY, templateData.getJudicialByCourtsInitiative()),
                () -> assertEquals(templateData.getJudgeRecital(),
                                   caseData.getJudicialDecisionMakeOrder().getJudgeRecitalText()),
                () -> assertEquals(templateData.getReasonForDecision(),
                            caseData.getJudicialDecisionMakeOrder().getReasonForDecisionText())
            );
        }

        @Test
        void whenJudgeMakeDecision_ShouldHideRecital_whileUnchecked() {
            CaseData caseData = CaseDataBuilder.builder().directionOrderApplication().build().toBuilder()
                    .build();

            CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
            caseDataBuilder.judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                    .directionsText("Test Direction")
                    .judicialByCourtsInitiative(
                            GAByCourtsInitiativeGAspec.OPTION_3)
                    .showReasonForDecision(YesOrNo.NO)
                    .reasonForDecisionText("Test Reason")
                    .makeAnOrder(GIVE_DIRECTIONS_WITHOUT_HEARING)
                    .directionsResponseByDate(LocalDate.now())
                    .judgeRecitalText("Test Judge's recital")
                    .build()).build();

            CaseData updateCaseData = caseDataBuilder.build();

            when(docmosisService.populateJudgeReason(any())).thenReturn(StringUtils.EMPTY);
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(LocationRefData.builder().epimmsId("2").externalShortName("London").build());
            var templateData = directionOrderGenerator.getTemplateData(null, updateCaseData, "auth", FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);

            assertNull(templateData.getJudgeRecital());
            assertEquals("", templateData.getReasonForDecision());
        }
    }

    @Nested
    class GetTemplateDateLip {

        @Test
        void shouldGeneratePostJudgeDirectionOrderDocument() {

            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(POST_JUDGE_DIRECTION_ORDER_LIP)))
                .thenReturn(new DocmosisDocument(DIRECTION_ORDER.getDocumentTitle(), bytes));
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(LocationRefData.builder().epimmsId("2").externalShortName("London").build());
            CaseData caseData = CaseDataBuilder.builder().directionOrderApplication().build();

            directionOrderGenerator.generate(CaseDataBuilder.builder().getCivilCaseData(), caseData, BEARER_TOKEN,
                                             FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT);

            verify(documentManagementService).uploadDocument(
                BEARER_TOKEN,
                new PDF(any(), any(), DocumentType.DIRECTION_ORDER)
            );
            verify(documentGeneratorService).generateDocmosisDocument(any(JudgeDecisionPdfDocument.class),
                                                                      eq(POST_JUDGE_DIRECTION_ORDER_LIP));
        }

        @Test
        void whenJudgeMakeDecision_ShouldGetHearingOrderData_1v1_LipRespondent() {

            CaseData caseData = CaseDataBuilder.builder().directionOrderApplication().build().toBuilder()
                .defendant2PartyName(null)
                .claimant2PartyName(null)
                .parentClaimantIsApplicant(YES)
                .gaCaseManagementLocation(GACaseLocation.builder().baseLocation("3").build())
                .isMultiParty(NO)
                .build();

            when(docmosisService.reasonAvailable(any())).thenReturn(YesOrNo.NO);
            when(docmosisService.populateJudgeReason(any())).thenReturn("");
            when(docmosisService.populateJudicialByCourtsInitiative(any()))
                .thenReturn("abcd ".concat(LocalDate.now().format(DATE_FORMATTER)));
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(LocationRefData.builder().epimmsId("2").externalShortName("Manchester").build());

            var templateData = directionOrderGenerator
                .getTemplateData(CaseDataBuilder.builder().getCivilCaseData(), caseData, "auth",
                                 FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT);

            assertThatFieldsAreCorrect_DirectionOrder_LipRespondent(templateData, caseData);
        }

        private void assertThatFieldsAreCorrect_DirectionOrder_LipRespondent(JudgeDecisionPdfDocument templateData,
                                                                   CaseData caseData) {
            Assertions.assertAll(
                "Direction Order Document data should be as expected",
                () -> assertEquals(templateData.getClaimNumber(), caseData.getCcdCaseReference().toString()),
                () -> assertEquals(templateData.getJudgeNameTitle(), caseData.getJudgeTitle()),
                () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                () -> assertNull(templateData.getClaimant2Name()),
                () -> assertEquals(templateData.getCourtName(), "Manchester"),
                () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
                () -> assertNull(templateData.getDefendant2Name()),
                () -> assertEquals(NO, templateData.getIsMultiParty()),
                () -> assertEquals("respondent1 partyname", templateData.getPartyName()),
                () -> assertEquals("respondent1address1", templateData.getPartyAddressAddressLine1()),
                () -> assertEquals("respondent1address2", templateData.getPartyAddressAddressLine2()),
                () -> assertEquals("respondent1address3", templateData.getPartyAddressAddressLine3()),
                () -> assertEquals("respondent1posttown", templateData.getPartyAddressPostTown()),
                () -> assertEquals("respondent1postcode", templateData.getPartyAddressPostCode()));
        }

        @Test
        void whenJudgeMakeDecision_ShouldGetHearingOrderData_1v1_Lip() {

            CaseData caseData = CaseDataBuilder.builder().directionOrderApplication().build().toBuilder()
                .defendant2PartyName(null)
                .claimant2PartyName(null)
                .parentClaimantIsApplicant(YES)
                .gaCaseManagementLocation(GACaseLocation.builder().baseLocation("3").build())
                .isMultiParty(NO)
                .build();

            when(docmosisService.reasonAvailable(any())).thenReturn(YesOrNo.NO);
            when(docmosisService.populateJudgeReason(any())).thenReturn("");
            when(docmosisService.populateJudicialByCourtsInitiative(any()))
                .thenReturn("abcd ".concat(LocalDate.now().format(DATE_FORMATTER)));
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(LocationRefData.builder().epimmsId("2").externalShortName("Manchester").build());

            var templateData = directionOrderGenerator
                .getTemplateData(CaseDataBuilder.builder().getCivilCaseData(), caseData, "auth", FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT);

            assertThatFieldsAreCorrect_DirectionOrder_1v1(templateData, caseData);
        }

        private void assertThatFieldsAreCorrect_DirectionOrder_1v1(JudgeDecisionPdfDocument templateData,
                                                                   CaseData caseData) {
            Assertions.assertAll(
                "Direction Order Document data should be as expected",
                () -> assertEquals(templateData.getClaimNumber(), caseData.getCcdCaseReference().toString()),
                () -> assertEquals(templateData.getJudgeNameTitle(), caseData.getJudgeTitle()),
                () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                () -> assertNull(templateData.getClaimant2Name()),
                () -> assertEquals(templateData.getCourtName(), "Manchester"),
                () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
                () -> assertNull(templateData.getDefendant2Name()),
                () -> assertEquals(NO, templateData.getIsMultiParty()),
                () -> assertEquals("applicant1 partyname", templateData.getPartyName()),
                () -> assertEquals("address1", templateData.getPartyAddressAddressLine1()),
                () -> assertEquals("address2", templateData.getPartyAddressAddressLine2()),
                () -> assertEquals("address3", templateData.getPartyAddressAddressLine3()),
                () -> assertEquals("posttown", templateData.getPartyAddressPostTown()),
                () -> assertEquals("postcode", templateData.getPartyAddressPostCode()));
        }
    }
}

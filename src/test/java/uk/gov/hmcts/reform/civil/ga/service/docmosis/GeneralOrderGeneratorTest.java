package uk.gov.hmcts.reform.civil.ga.service.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.enums.dq.FinalOrderShowToggle;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAByCourtsInitiativeGAspec;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.ga.model.docmosis.JudgeDecisionPdfDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.documentmanagement.SecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;

import java.time.LocalDate;
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
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.GENERAL_ORDER;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.POST_JUDGE_GENERAL_ORDER_LIP;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService.DATE_FORMATTER;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    GeneralOrderGenerator.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class GeneralOrderGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};

    @MockBean
    private SecuredDocumentManagementService documentManagementService;
    @MockBean
    private DocumentGeneratorService documentGeneratorService;
    @Autowired
    private GeneralOrderGenerator generalOrderGenerator;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private DocmosisService docmosisService;

    @Test
    void shouldGenerateGeneralOrderDocument() {

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(GENERAL_ORDER)))
            .thenReturn(new DocmosisDocument(GENERAL_ORDER.getDocumentTitle(), bytes));
        when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
            .thenReturn(new LocationRefData().setEpimmsId("2").setExternalShortName("London"));
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication().build();
        generalOrderGenerator.generate(caseData, BEARER_TOKEN);

        verify(documentManagementService).uploadDocument(
            BEARER_TOKEN,
            new PDF(any(), any(), DocumentType.GENERAL_ORDER)
        );
        verify(documentGeneratorService).generateDocmosisDocument(any(JudgeDecisionPdfDocument.class),
                                                                  eq(GENERAL_ORDER));
    }

    @Test
    void shouldThrowExceptionWhenNoLocationMatch() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("8").build()).build();

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(GENERAL_ORDER)))
            .thenReturn(new DocmosisDocument(GENERAL_ORDER.getDocumentTitle(), bytes));
        doThrow(new IllegalArgumentException("Court Name is not found in location data"))
            .when(docmosisService).getCaseManagementLocationVenueName(any(), any());

        Exception exception =
            assertThrows(IllegalArgumentException.class, () -> generalOrderGenerator.generate(caseData, BEARER_TOKEN));
        String expectedMessage = "Court Name is not found in location data";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));

    }

    @Nested
    class GetTemplateDataLip {
        @Test
        void shouldGenerateGeneralOrderDocument() {

            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(POST_JUDGE_GENERAL_ORDER_LIP)))
                .thenReturn(new DocmosisDocument(POST_JUDGE_GENERAL_ORDER_LIP.getDocumentTitle(), bytes));
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(new LocationRefData().setEpimmsId("2").setExternalShortName("London"));

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication().build();
            generalOrderGenerator.generate(GeneralApplicationCaseDataBuilder.builder().getCivilCaseData(),
                                           caseData,
                                           BEARER_TOKEN,
                                           FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT);

            verify(documentManagementService).uploadDocument(
                BEARER_TOKEN,
                new PDF(any(), any(), DocumentType.GENERAL_ORDER)
            );
            verify(documentGeneratorService).generateDocmosisDocument(any(JudgeDecisionPdfDocument.class),
                                                                      eq(POST_JUDGE_GENERAL_ORDER_LIP));
        }

        @Test
        void whenJudgeMakeDecision_ShouldGetGeneralOrderData() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication().build().copy()
                .build();

            when(docmosisService.reasonAvailable(any())).thenReturn(YesOrNo.YES);
            when(docmosisService.populateJudgeReason(any())).thenReturn("Test Reason");
            when(docmosisService.populateJudicialByCourtsInitiative(any()))
                .thenReturn("abcd ".concat(LocalDate.now().format(DATE_FORMATTER)));
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(new LocationRefData().setEpimmsId("2").setExternalShortName("London"));

            var templateData = generalOrderGenerator.getTemplateData(GeneralApplicationCaseDataBuilder.builder().getCivilCaseData(),
                                                                     caseData,
                                                                     "auth",
                                                                     FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT);

            assertThatFieldsAreCorrect_GeneralOrder(templateData, caseData);
        }

        private void assertThatFieldsAreCorrect_GeneralOrder(JudgeDecisionPdfDocument templateData, GeneralApplicationCaseData caseData) {
            Assertions.assertAll(
                "GeneralOrderDocument data should be as expected",
                () -> assertEquals(templateData.getClaimNumber(), caseData.getGeneralAppParentCaseLink().getCaseReference()),
                () -> assertEquals(templateData.getJudgeNameTitle(), caseData.getJudgeTitle()),
                () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                () -> assertEquals(templateData.getClaimant2Name(), caseData.getClaimant2PartyName()),
                () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
                () -> assertEquals(templateData.getDefendant2Name(), caseData.getDefendant2PartyName()),
                () -> assertEquals(templateData.getGeneralOrder(),
                                   caseData.getJudicialDecisionMakeOrder().getOrderText()),
                () -> assertEquals(YesOrNo.YES, templateData.getReasonAvailable()),
                () -> assertEquals(templateData.getLocationName(), caseData.getLocationName()),
                () -> assertEquals("London", templateData.getCourtName()),
                () -> assertEquals(templateData.getJudicialByCourtsInitiative(), caseData
                    .getJudicialDecisionMakeOrder().getOrderCourtOwnInitiative()
                    + " ".concat(LocalDate.now().format(DATE_FORMATTER))),
                () -> assertEquals(templateData.getJudgeRecital(),
                                   caseData.getJudicialDecisionMakeOrder().getJudgeRecitalText()),
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
        void whenJudgeMakeDecision_ShouldGetGeneralOrderData() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication().build().copy()
                .build();

            when(docmosisService.reasonAvailable(any())).thenReturn(YesOrNo.YES);
            when(docmosisService.populateJudgeReason(any())).thenReturn("Test Reason");
            when(docmosisService.populateJudicialByCourtsInitiative(any()))
                .thenReturn("abcd ".concat(LocalDate.now().format(DATE_FORMATTER)));
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(new LocationRefData().setEpimmsId("2").setExternalShortName("London"));

            var templateData = generalOrderGenerator.getTemplateData(null, caseData, "auth", FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);

            assertThatFieldsAreCorrect_GeneralOrder(templateData, caseData);
        }

        private void assertThatFieldsAreCorrect_GeneralOrder(JudgeDecisionPdfDocument templateData, GeneralApplicationCaseData caseData) {
            Assertions.assertAll(
                "GeneralOrderDocument data should be as expected",
                () -> assertEquals(templateData.getClaimNumber(), caseData.getGeneralAppParentCaseLink().getCaseReference()),
                () -> assertEquals(templateData.getJudgeNameTitle(), caseData.getJudgeTitle()),
                () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                () -> assertEquals(templateData.getClaimant2Name(), caseData.getClaimant2PartyName()),
                () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
                () -> assertEquals(templateData.getDefendant2Name(), caseData.getDefendant2PartyName()),
                () -> assertEquals(templateData.getGeneralOrder(),
                                   caseData.getJudicialDecisionMakeOrder().getOrderText()),
                () -> assertEquals(YesOrNo.YES, templateData.getReasonAvailable()),
                () -> assertEquals(templateData.getLocationName(), caseData.getLocationName()),
                () -> assertEquals("London", templateData.getCourtName()),
                () -> assertEquals(templateData.getJudicialByCourtsInitiative(), caseData
                    .getJudicialDecisionMakeOrder().getOrderCourtOwnInitiative()
                    + " ".concat(LocalDate.now().format(DATE_FORMATTER))),
                () -> assertEquals(templateData.getJudgeRecital(),
                                   caseData.getJudicialDecisionMakeOrder().getJudgeRecitalText()),
                () -> assertEquals(templateData.getAddress(), caseData.getCaseManagementLocation().getAddress()),
                () -> assertEquals(templateData.getSiteName(), caseData.getCaseManagementLocation().getSiteName()),
                () -> assertEquals(templateData.getPostcode(), caseData.getCaseManagementLocation().getPostcode())
            );
        }

        @Test
        void whenJudgeMakeDecision_ShouldGetGeneralOrderData_1v1() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication().build().copy()
                .defendant2PartyName(null)
                .claimant2PartyName(null)
                .isMultiParty(NO)
                .build();

            when(docmosisService.reasonAvailable(any())).thenReturn(YesOrNo.YES);
            when(docmosisService.populateJudgeReason(any())).thenReturn("Test Reason");
            when(docmosisService.populateJudicialByCourtsInitiative(any()))
                .thenReturn("abcd ".concat(LocalDate.now().format(DATE_FORMATTER)));
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(new LocationRefData().setEpimmsId("2").setExternalShortName("London"));

            var templateData = generalOrderGenerator.getTemplateData(null, caseData, "auth", FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);

            assertThatFieldsAreCorrect_GeneralOrder_1v1(templateData, caseData);
        }

        private void assertThatFieldsAreCorrect_GeneralOrder_1v1(JudgeDecisionPdfDocument templateData, GeneralApplicationCaseData caseData) {
            Assertions.assertAll(
                "GeneralOrderDocument data should be as expected",
                () -> assertEquals(templateData.getClaimNumber(), caseData.getGeneralAppParentCaseLink().getCaseReference()),
                () -> assertEquals(templateData.getJudgeNameTitle(), caseData.getJudgeTitle()),
                () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                () -> assertNull(templateData.getClaimant2Name()),
                () -> assertEquals("London", templateData.getCourtName()),
                () -> assertEquals(NO, templateData.getIsMultiParty()),
                () -> assertNull(templateData.getDefendant2Name()),
                () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()));
        }

        @Test
        void whenJudgeMakeDecision_ShouldGetGeneralOrderData_Option2() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication().build().copy()
                .isMultiParty(YES)
                .build();

            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                                          .setOrderText("Test Order")
                                                          .setOrderWithoutNotice("abcdef")
                                                          .setOrderWithoutNoticeDate(LocalDate.now())
                                                          .setJudicialByCourtsInitiative(
                                                              GAByCourtsInitiativeGAspec.OPTION_2)
                                                          .setShowReasonForDecision(YesOrNo.YES)
                                                          .setReasonForDecisionText("Test Reason")
                                                          .setMakeAnOrder(
                                                              GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT)
                                                          .setShowJudgeRecitalText(List.of(FinalOrderShowToggle.SHOW))
                                                          .setJudgeRecitalText("Test Judge's recital")).build();
            GeneralApplicationCaseData updateData = caseDataBuilder.build();

            when(docmosisService.reasonAvailable(any())).thenReturn(YesOrNo.YES);
            when(docmosisService.populateJudgeReason(any())).thenReturn("Test Reason");
            when(docmosisService.populateJudicialByCourtsInitiative(any()))
                .thenReturn("abcdef ".concat(LocalDate.now().format(DATE_FORMATTER)));
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(new LocationRefData().setEpimmsId("2").setExternalShortName("Reading"));

            var templateData = generalOrderGenerator.getTemplateData(null, updateData, "auth", FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);

            assertThatFieldsAreCorrect_GeneralOrder_Option2(templateData, updateData);
        }

        private void assertThatFieldsAreCorrect_GeneralOrder_Option2(JudgeDecisionPdfDocument templateData,
                                                                     GeneralApplicationCaseData caseData) {
            Assertions.assertAll(
                "GeneralOrderDocument data should be as expected",
                () -> assertEquals(templateData.getClaimNumber(), caseData.getGeneralAppParentCaseLink().getCaseReference()),
                () -> assertEquals(templateData.getJudgeNameTitle(), caseData.getJudgeTitle()),
                () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                () -> assertEquals(templateData.getClaimant2Name(), caseData.getClaimant2PartyName()),
                () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
                () -> assertEquals(templateData.getDefendant2Name(), caseData.getDefendant2PartyName()),
                () -> assertEquals("Reading", templateData.getCourtName()),
                () -> assertEquals(templateData.getGeneralOrder(),
                                   caseData.getJudicialDecisionMakeOrder().getOrderText()),
                () -> assertEquals(templateData.getLocationName(), caseData.getLocationName()),
                () -> assertEquals(templateData.getJudicialByCourtsInitiative(), caseData
                    .getJudicialDecisionMakeOrder().getOrderWithoutNotice()
                    + " ".concat(LocalDate.now().format(DATE_FORMATTER))),
                () -> assertEquals(YES, templateData.getIsMultiParty()),
                () -> assertEquals(YesOrNo.YES, templateData.getReasonAvailable()),
                () -> assertEquals(templateData.getJudgeRecital(),
                                   caseData.getJudicialDecisionMakeOrder().getJudgeRecitalText()),
                () -> assertEquals(templateData.getReasonForDecision(),
                        caseData.getJudicialDecisionMakeOrder().getReasonForDecisionText())
            );
        }

        @Test
        void whenJudgeMakeDecision_ShouldGetGeneralOrderData_Option3() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication().build().copy()
                .isMultiParty(YES)
                .build();

            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                                          .setOrderText("Test Order")
                                                          .setJudicialByCourtsInitiative(
                                                              GAByCourtsInitiativeGAspec.OPTION_3)
                                                          .setShowReasonForDecision(YesOrNo.YES)
                                                          .setReasonForDecisionText("Test Reason")
                                                          .setMakeAnOrder(
                                                              GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT)
                                                          .setShowJudgeRecitalText(List.of(FinalOrderShowToggle.SHOW))
                                                          .setJudgeRecitalText("Test Judge's recital")).build();
            GeneralApplicationCaseData updateData = caseDataBuilder.build();

            when(docmosisService.reasonAvailable(any())).thenReturn(YesOrNo.YES);
            when(docmosisService.populateJudgeReason(any())).thenReturn("Test Reason");
            when(docmosisService.populateJudicialByCourtsInitiative(any()))
                .thenReturn(StringUtils.EMPTY);
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(new LocationRefData().setEpimmsId("2").setExternalShortName("Manchester"));

            var templateData = generalOrderGenerator.getTemplateData(null, updateData, "auth", FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);

            assertThatFieldsAreCorrect_GeneralOrder_Option3(templateData, updateData);
        }

        private void assertThatFieldsAreCorrect_GeneralOrder_Option3(JudgeDecisionPdfDocument templateData,
                                                                     GeneralApplicationCaseData caseData) {
            Assertions.assertAll(
                "GeneralOrderDocument data should be as expected",
                () -> assertEquals(templateData.getClaimNumber(), caseData.getGeneralAppParentCaseLink().getCaseReference()),
                () -> assertEquals(templateData.getJudgeNameTitle(), caseData.getJudgeTitle()),
                () -> assertEquals("Manchester", templateData.getCourtName()),
                () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
                () -> assertEquals(templateData.getClaimant2Name(), caseData.getClaimant2PartyName()),
                () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
                () -> assertEquals(templateData.getDefendant2Name(), caseData.getDefendant2PartyName()),
                () -> assertEquals(YES, templateData.getIsMultiParty()),
                () -> assertEquals(templateData.getGeneralOrder(),
                                   caseData.getJudicialDecisionMakeOrder().getOrderText()),
                () -> assertEquals(YesOrNo.YES, templateData.getReasonAvailable()),
                () -> assertEquals(templateData.getLocationName(), caseData.getLocationName()),
                () -> assertEquals(StringUtils.EMPTY, templateData.getJudicialByCourtsInitiative()),
                () -> assertEquals(templateData.getJudgeRecital(),
                                   caseData.getJudicialDecisionMakeOrder().getJudgeRecitalText()),
                () -> assertEquals(templateData.getReasonForDecision(),
                                   caseData.getJudicialDecisionMakeOrder().getReasonForDecisionText())
            );
        }

        @Test
        void whenJudgeMakeDecision_ShouldHideText_whileUnchecked() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication().build().copy()
                    .build();

            GeneralApplicationCaseData caseDataBuilder = caseData.copy();
            caseDataBuilder.judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                    .setOrderText("Test Order")
                    .setJudicialByCourtsInitiative(
                            GAByCourtsInitiativeGAspec.OPTION_3)
                    .setReasonForDecisionText("Test Reason")
                    .setShowReasonForDecision(YesOrNo.NO)
                    .setMakeAnOrder(
                            GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT)
                    .setJudgeRecitalText("Test Judge's recital")).build();
            GeneralApplicationCaseData updateData = caseDataBuilder.build();

            when(docmosisService.populateJudgeReason(any())).thenReturn(StringUtils.EMPTY);
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(new LocationRefData().setEpimmsId("2").setExternalShortName("Manchester"));

            var templateData = generalOrderGenerator.getTemplateData(null, updateData, "auth", FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);

            assertNull(templateData.getJudgeRecital());
            assertEquals("", templateData.getReasonForDecision());
        }
    }
}

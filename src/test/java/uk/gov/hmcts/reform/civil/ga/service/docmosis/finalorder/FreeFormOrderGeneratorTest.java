package uk.gov.hmcts.reform.civil.ga.service.docmosis.finalorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.SecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.caseprogression.OrderOnCourtsList;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.docmosis.FreeFormOrder;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.DocmosisService;
import uk.gov.hmcts.reform.civil.model.caseprogression.FreeFormOrderValues;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.GENERAL_ORDER;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.OrderOnCourtsList.ORDER_ON_COURT_INITIATIVE;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.OrderOnCourtsList.ORDER_WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.POST_JUDGE_FREE_FORM_ORDER_LIP;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    FreeFormOrderGenerator.class,
    JacksonAutoConfiguration.class
})
class FreeFormOrderGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};

    private static final String TEMPLATE_NAME = "Free_form_order_%s.pdf";
    private static final String FILE_NAME_APPLICATION = String.format(
        TEMPLATE_NAME,
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    );
    private static final CaseDocument CASE_DOCUMENT = CaseDocumentBuilder.builder()
        .documentName(FILE_NAME_APPLICATION)
        .documentType(GENERAL_ORDER)
        .build();

    @MockBean
    private SecuredDocumentManagementService documentManagementService;

    @MockBean
    private DocumentGeneratorService documentGeneratorService;

    @Autowired
    private FreeFormOrderGenerator generator;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private DocmosisService docmosisService;

    @Test
    void shouldHearingFormGeneratorOneForm_whenValidDataIsProvided() {
        when(documentGeneratorService
                 .generateDocmosisDocument(
                     any(MappableObject.class), eq(DocmosisTemplates.FREE_FORM_ORDER)))
            .thenReturn(new DocmosisDocument(
                DocmosisTemplates.FREE_FORM_ORDER.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(any(String.class), any(PDF.class)))
            .thenReturn(CASE_DOCUMENT);
        when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
            .thenReturn(new LocationRefData().setEpimmsId("2").setVenueName("London"));

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().hearingScheduledApplication(
                YES).build()
            .copy()
            .freeFormRecitalText("RecitalText")
            .freeFormOrderedText("OrderedText")
            .orderOnCourtsList(OrderOnCourtsList.NOT_APPLICABLE)
            .build();
        CaseDocument caseDocuments = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocuments).isNotNull();

        verify(documentManagementService)
            .uploadDocument(any(String.class), any(PDF.class));
    }

    @Test
    void shouldThrowExceptionWhenNoLocationMatch() {
        when(documentGeneratorService
                 .generateDocmosisDocument(
                     any(MappableObject.class), eq(DocmosisTemplates.FREE_FORM_ORDER)))
            .thenReturn(new DocmosisDocument(
                DocmosisTemplates.FREE_FORM_ORDER.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(any(String.class), any(PDF.class)))
            .thenReturn(CASE_DOCUMENT);
        doThrow(new IllegalArgumentException("Court Name is not found in location data"))
            .when(docmosisService).getCaseManagementLocationVenueName(any(), any());

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().hearingScheduledApplication(
                YES).build()
            .copy()
            .freeFormRecitalText("RecitalText")
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("8").build())
            .freeFormOrderedText("OrderedText")
            .orderOnCourtsList(OrderOnCourtsList.NOT_APPLICABLE)
            .build();
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
    void test_getFreeFormOrderValueOnCourt() {
        FreeFormOrderValues values = new FreeFormOrderValues()
            .setOnInitiativeSelectionTextArea("test")
            .setOnInitiativeSelectionDate(LocalDate.now());
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().orderOnCourtsList(
                ORDER_ON_COURT_INITIATIVE)
            .orderOnCourtInitiative(values).build();
        String orderString = generator.getFreeFormOrderValue(caseData);
        assertThat(orderString).contains("test");
    }

    @Test
    void test_getFreeFormOrderValueWithoutNotice() {
        FreeFormOrderValues values = new FreeFormOrderValues()
            .setWithoutNoticeSelectionTextArea("test")
            .setWithoutNoticeSelectionDate(LocalDate.now());
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().orderOnCourtsList(
                ORDER_WITHOUT_NOTICE)
            .orderWithoutNotice(values).build();
        String orderString = generator.getFreeFormOrderValue(caseData);
        assertThat(orderString).contains("test");

    }

    @Test
    void test_getFileName() {
        String name = generator.getFileName(DocmosisTemplates.FREE_FORM_ORDER);
        assertThat(name).startsWith("General_order_for_application_");
        assertThat(name).endsWith(".pdf");
    }

    @Test
    void test_getDateFormatted() {
        String dateString = generator.getDateFormatted(LocalDate.EPOCH);
        assertThat(dateString).isEqualTo(" 1 January 1970");
    }

    @Test
    void test_getTemplate() {
        assertThat(generator.getTemplate(FlowFlag.ONE_RESPONDENT_REPRESENTATIVE)).isEqualTo(DocmosisTemplates.FREE_FORM_ORDER);
    }

    @Test
    void whenJudgeMakeDecision_ShouldGetFreeFormOrderData() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .finalOrderFreeForm().isMultiParty(YES).build().copy()
            .build();
        when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
            .thenReturn(
                new LocationRefData()
                    .setEpimmsId("2")
                    .setExternalShortName("London")
            );
        FreeFormOrder templateDate = generator.getTemplateData(
            null,
            caseData,
            "auth",
            FlowFlag.ONE_RESPONDENT_REPRESENTATIVE
            );
        assertThatFieldsAreCorrect_FreeFormOrder(templateDate, caseData);
    }

    private void assertThatFieldsAreCorrect_FreeFormOrder(FreeFormOrder freeFormOrder,
                                                          GeneralApplicationCaseData caseData) {
        Assertions.assertAll(
            "GeneralOrderDocument data should be as expected",
            () -> assertEquals(freeFormOrder.getClaimant1Name(), caseData.getClaimant1PartyName()),
            () -> assertEquals(freeFormOrder.getJudgeNameTitle(), caseData.getJudgeTitle()),
            () -> assertEquals(freeFormOrder.getClaimant2Name(), caseData.getClaimant2PartyName()),
            () -> assertEquals("London", freeFormOrder.getCourtName()),
            () -> assertEquals(freeFormOrder.getDefendant1Name(), caseData.getDefendant1PartyName()),
            () -> assertEquals(freeFormOrder.getDefendant2Name(), caseData.getDefendant2PartyName()),
            () -> assertEquals(YES, freeFormOrder.getIsMultiParty())
        );
    }

    @Test
    void whenJudgeMakeDecision_ShouldGetFreeFormOrderData_1V1() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .finalOrderFreeForm().build().copy()
            .defendant2PartyName(null)
            .claimant2PartyName(null)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("3").build())
            .isMultiParty(NO)
            .build();

        when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
            .thenReturn(
                new LocationRefData()
                    .setEpimmsId("2")
                    .setExternalShortName("Manchester")
            );
        FreeFormOrder templateDate = generator.getTemplateData(
            null,
            caseData,
            "auth",
            FlowFlag.ONE_RESPONDENT_REPRESENTATIVE
            );
        assertThatFieldsAreCorrect_FreeFormOrder_1V1(templateDate, caseData);
    }

    private void assertThatFieldsAreCorrect_FreeFormOrder_1V1(FreeFormOrder freeFormOrder,
                                                              GeneralApplicationCaseData caseData) {
        Assertions.assertAll(
            "GeneralOrderDocument data should be as expected",
            () -> assertEquals(freeFormOrder.getClaimant1Name(), caseData.getClaimant1PartyName()),
            () -> assertEquals(freeFormOrder.getJudgeNameTitle(), caseData.getJudgeTitle()),
            () -> assertNull(freeFormOrder.getClaimant2Name()),
            () -> assertEquals("Manchester", freeFormOrder.getCourtName()),
            () -> assertEquals(freeFormOrder.getDefendant1Name(), caseData.getDefendant1PartyName()),
            () -> assertNull(freeFormOrder.getDefendant2Name()),
            () -> assertEquals(NO, freeFormOrder.getIsMultiParty())
        );
    }

    @Nested
    class GetTemplateDataLip {
        @Test
        void whenJudgeMakeDecision_ShouldGetFreeFormOrderData_1V1() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .finalOrderFreeForm().build().copy()
                .defendant2PartyName(null)
                .claimant2PartyName(null)
                .parentClaimantIsApplicant(NO)
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("3").build())
                .isMultiParty(NO)
                .build();

            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(
                    new LocationRefData()
                        .setEpimmsId("2")
                        .setExternalShortName("Manchester")
                );
            FreeFormOrder templateDate = generator.getTemplateData(
                GeneralApplicationCaseDataBuilder.builder().getCivilCaseData(),
                caseData,
                "auth",
                FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT
            );
            assertThatFieldsAreCorrect_FreeFormOrder_1V1(templateDate, caseData);
        }

        @Test
        void shouldHearingFormGeneratorOneForm_whenValidDataIsProvided() {
            when(documentGeneratorService
                     .generateDocmosisDocument(
                         any(MappableObject.class), eq(DocmosisTemplates.POST_JUDGE_FREE_FORM_ORDER_LIP)))
                .thenReturn(new DocmosisDocument(
                    DocmosisTemplates.POST_JUDGE_FREE_FORM_ORDER_LIP.getDocumentTitle(), bytes));
            when(documentManagementService.uploadDocument(any(String.class), any(PDF.class)))
                .thenReturn(CASE_DOCUMENT);
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(new LocationRefData().setEpimmsId("2").setVenueName("London"));

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().hearingScheduledApplication(
                    YES).build()
                .copy()
                .freeFormRecitalText("RecitalText")
                .freeFormOrderedText("OrderedText")
                .orderOnCourtsList(OrderOnCourtsList.NOT_APPLICABLE)
                .build();
            CaseDocument caseDocuments = generator.generate(
                GeneralApplicationCaseDataBuilder.builder().getCivilCaseData(),
                caseData,
                BEARER_TOKEN,
                FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT
            );

            assertThat(caseDocuments).isNotNull();

            verify(documentGeneratorService)
                .generateDocmosisDocument(any(FreeFormOrder.class), eq(POST_JUDGE_FREE_FORM_ORDER_LIP));
            verify(documentManagementService).uploadDocument(any(String.class), any(PDF.class));
        }

        private void assertThatFieldsAreCorrect_FreeFormOrder_1V1(FreeFormOrder freeFormOrder,
                                                                  GeneralApplicationCaseData caseData) {
            Assertions.assertAll(
                "GeneralOrderDocument data should be as expected",
                () -> assertEquals(freeFormOrder.getClaimant1Name(), caseData.getClaimant1PartyName()),
                () -> assertEquals(freeFormOrder.getJudgeNameTitle(), caseData.getJudgeTitle()),
                () -> assertNull(freeFormOrder.getClaimant2Name()),
                () -> assertEquals("Manchester", freeFormOrder.getCourtName()),
                () -> assertEquals(freeFormOrder.getDefendant1Name(), caseData.getDefendant1PartyName()),
                () -> assertNull(freeFormOrder.getDefendant2Name()),
                () -> assertEquals(NO, freeFormOrder.getIsMultiParty()),
                () -> assertEquals("applicant1partyname", freeFormOrder.getPartyName()),
                () -> assertEquals("address1", freeFormOrder.getPartyAddressAddressLine1()),
                () -> assertEquals("address2", freeFormOrder.getPartyAddressAddressLine2()),
                () -> assertEquals("address3", freeFormOrder.getPartyAddressAddressLine3()),
                () -> assertEquals("posttown", freeFormOrder.getPartyAddressPostTown()),
                () -> assertEquals("postcode", freeFormOrder.getPartyAddressPostCode())
            );
        }
    }
}

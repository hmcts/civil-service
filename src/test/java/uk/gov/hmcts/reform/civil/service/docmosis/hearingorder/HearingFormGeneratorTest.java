package uk.gov.hmcts.reform.civil.service.docmosis.hearingorder;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.documentmanagement.SecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.GAJudicialHearingType;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingDuration;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.genapplication.GACaseLocation;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingNoticeDetail;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.HEARING_NOTICE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_APPLICATION;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.POST_JUDGE_HEARING_APPLICATION_LIP;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    HearingFormGeneratorGeneralApplication.class,
    JacksonAutoConfiguration.class
})
class HearingFormGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    List<DynamicListElement> listItems = Arrays.asList(DynamicListElement.builder()
                                                           .code("code").label("label").build());

    DynamicListElement selectedLocation = DynamicListElement
        .builder().label("sitename - location name - D12 8997").build();

    private static final String templateName = "Application_Hearing_Notice_%s.pdf";
    private static final String fileName_application = String.format(templateName,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    private static final CaseDocument CASE_DOCUMENT = CaseDocumentBuilder.builder()
            .documentName(fileName_application)
            .documentType(HEARING_NOTICE)
            .build();

    @MockBean
    private SecuredDocumentManagementService documentManagementService;

    @MockBean
    private DocumentGeneratorService documentGeneratorService;

    @MockBean
    private CoreCaseDataService coreCaseDataService;
    @MockBean
    private CaseDetailsConverter caseDetailsConverter;

    @Autowired
    private HearingFormGeneratorGeneralApplication generator;
    @MockBean
    private DocmosisService docmosisService;

    @Test
    void shouldHearingFormGeneratorOneForm_whenValidDataIsProvided() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(HEARING_APPLICATION)))
                .thenReturn(new DocmosisDocument(HEARING_APPLICATION.getDocumentTitle(), bytes));

        when(documentManagementService
                .uploadDocument(any(), (PDF) any()))
                .thenReturn(CASE_DOCUMENT);
        when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
            .thenReturn(LocationRefData.builder().epimmsId("2").venueName("London").build());

        Map<String, String> refMap = new HashMap<>();
        refMap.put("applicantSolicitor1Reference", "app1ref");
        refMap.put("respondentSolicitor1Reference", "resp1ref");
        refMap.put("respondentSolicitor2Reference", "resp2ref");
        Map<String, Object> caseDataContent = new HashMap<>();
        caseDataContent.put("solicitorReferences", refMap);
        CaseData mainCaseData = CaseDataBuilder.builder().getMainCaseDataWithDetails(
                true,
                true,
                true, true).build();
        when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(mainCaseData);
        CaseDetails caseDetails = CaseDetails.builder().data(caseDataContent).build();
        when(coreCaseDataService.getCase(
                anyLong()
        )).thenReturn(caseDetails);

        CaseData caseData = CaseDataBuilder.builder().hearingScheduledApplication(YES).build();
        CaseDocument caseDocuments = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocuments).isNotNull();

        verify(documentManagementService)
                .uploadDocument(any(), (PDF) any());
    }

    @Test
    void whenJudgeMakeDecision_shouldGetHearingFormData() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(HEARING_APPLICATION)))
            .thenReturn(new DocmosisDocument(HEARING_APPLICATION.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(any(), (PDF) any()))
            .thenReturn(CASE_DOCUMENT);
        when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
            .thenReturn(LocationRefData.builder().epimmsId("2").externalShortName("London").build());

        Map<String, String> refMap = new HashMap<>();
        refMap.put("applicantSolicitor1Reference", "app1ref");
        refMap.put("respondentSolicitor1Reference", "resp1ref");
        refMap.put("respondentSolicitor2Reference", "resp2ref");
        Map<String, Object> caseDataContent = new HashMap<>();
        caseDataContent.put("solicitorReferences", refMap);
        CaseData mainCaseData = CaseDataBuilder.builder().getMainCaseDataWithDetails(
            true,
            true,
            true, true).build();
        when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(mainCaseData);
        CaseDetails caseDetails = CaseDetails.builder().data(caseDataContent).build();
        when(coreCaseDataService.getCase(
            anyLong()
        )).thenReturn(caseDetails);
        CaseData caseData = CaseDataBuilder.builder().hearingScheduledApplication(YES)
            .gaHearingNoticeDetail(GAHearingNoticeDetail
                                       .builder()
                                       .hearingDuration(GAHearingDuration.OTHER)
                                       .channel(GAJudicialHearingType.IN_PERSON)
                                       .hearingLocation(
                                           DynamicList.builder()
                                               .value(selectedLocation).listItems(listItems)
                                               .build()).build())
            .build();

        var templateData = generator.getTemplateData(null, caseData, "auth", FlowFlag.ONE_RESPONDENT_REPRESENTATIVE);
        assertThat(templateData.getCourt()).isEqualTo("London");
        assertThat(templateData.getJudgeHearingLocation()).isEqualTo("sitename - location name - D12 8997");
    }

    @Test
    void shouldThrowExceptionWhenNoLocationMatch() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(HEARING_APPLICATION)))
            .thenReturn(new DocmosisDocument(HEARING_APPLICATION.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(any(), (PDF) any()))
            .thenReturn(CASE_DOCUMENT);
        doThrow(new IllegalArgumentException("Court Name is not found in location data"))
            .when(docmosisService).getCaseManagementLocationVenueName(any(), any());

        Map<String, String> refMap = new HashMap<>();
        refMap.put("applicantSolicitor1Reference", "app1ref");
        refMap.put("respondentSolicitor1Reference", "resp1ref");
        refMap.put("respondentSolicitor2Reference", "resp2ref");
        Map<String, Object> caseDataContent = new HashMap<>();
        caseDataContent.put("solicitorReferences", refMap);
        CaseData mainCaseData = CaseDataBuilder.builder().getMainCaseDataWithDetails(
                true,
                true,
                true, true).build();
        when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(mainCaseData);
        CaseDetails caseDetails = CaseDetails.builder().data(caseDataContent).build();
        when(coreCaseDataService.getCase(
            anyLong()
        )).thenReturn(caseDetails);

        CaseData caseData = CaseDataBuilder.builder().hearingScheduledApplication(YES)
            .gaCaseManagementLocation(GACaseLocation.builder().baseLocation("8").build())
            .build();

        Exception exception =
            assertThrows(IllegalArgumentException.class, ()
                -> generator.generate(caseData, BEARER_TOKEN));
        String expectedMessage = "Court Name is not found in location data";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void test_getCaseNumberFormatted() {
        CaseData caseData = CaseDataBuilder.builder().ccdCaseReference(1644495739087775L)
            .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("1644495739087775").build()).build();
        String formattedCaseNumber = generator.getCaseNumberFormatted(caseData);
        assertThat(formattedCaseNumber).isEqualTo("1644-4957-3908-7775");
    }

    @Test
    void test_getFileName() {
        String name = generator.getFileName(HEARING_APPLICATION);
        assertThat(name).startsWith("Application_Hearing_Notice_");
        assertThat(name).endsWith(".pdf");
    }

    @Test
    void test_getDateFormatted() {
        String dateString = generator.getDateFormatted(LocalDate.EPOCH);
        assertThat(dateString).isEqualTo("1 January 1970");
    }

    @Test
    void test_getReference() {
        Map<String, String> refMap = new HashMap<>();
        refMap.put("applicantSolicitor1Reference", "app1ref");
        refMap.put("respondentSolicitor1Reference", "resp1ref");
        refMap.put("respondentSolicitor2Reference", "resp2ref");
        Map<String, Object> caseDataContent = new HashMap<>();
        caseDataContent.put("solicitorReferences", refMap);
        CaseDetails caseDetails = CaseDetails.builder().data(caseDataContent).build();

        assertThat(generator.getReference(caseDetails, "applicantSolicitor1Reference")).isEqualTo("app1ref");
        assertThat(generator.getReference(caseDetails, "notExist")).isNull();
    }

    @Test
    void test_getHearingTimeFormatted() {
        assertThat(HearingFormGeneratorGeneralApplication.getHearingTimeFormatted("error")).isNull();
        assertThat(HearingFormGeneratorGeneralApplication.getHearingTimeFormatted("0800")).isEqualTo("08:00");
    }

    @Test
    void test_getHearingDurationString() {
        CaseData caseData = CaseDataBuilder.builder().hearingScheduledApplication(YES).build();
        String durationString = HearingFormGeneratorGeneralApplication.getHearingDurationString(caseData);
        assertThat(durationString).isEqualTo(GAHearingDuration.HOUR_1.getDisplayedValue());
    }

    @Test
    void test_getHearingDurationStringOther() {
        CaseData caseData = CaseData.builder().gaHearingNoticeDetail(GAHearingNoticeDetail.builder()
                .hearingDuration(GAHearingDuration.OTHER)
                .hearingDurationOther("One year").build()).build();
        String durationString = HearingFormGeneratorGeneralApplication.getHearingDurationString(caseData);
        assertThat(durationString).isEqualTo("One year");
    }

    @Test
    void testCanViewWithoutNoticeGaCreatedByResp2() {
        CaseData caseData = CaseDataBuilder.builder().getMainCaseDataWithDetails(
                        false,
                        false,
                        true, true).build();
        CaseData generalAppCaseData = CaseData.builder().ccdCaseReference(CaseDataBuilder.CASE_ID).build();
        when(caseDetailsConverter.toCaseDataGA(any()))
                .thenReturn(caseData);
        AssertionsForClassTypes
                .assertThat(HearingFormGeneratorGeneralApplication.canViewResp(caseData, generalAppCaseData, "2")).isTrue();
        AssertionsForClassTypes
                .assertThat(HearingFormGeneratorGeneralApplication.canViewResp(caseData, generalAppCaseData, "1")).isFalse();
        AssertionsForClassTypes
                .assertThat(HearingFormGeneratorGeneralApplication.canViewClaimant(caseData, generalAppCaseData)).isFalse();
    }

    @Test
    void testCanViewWithoutNoticeGaCreatedByResp1() {
        CaseData caseData = CaseDataBuilder.builder().getMainCaseDataWithDetails(
                        false,
                        true,
                        false, true).build();
        CaseData generalAppCaseData = CaseData.builder().ccdCaseReference(CaseDataBuilder.CASE_ID).build();
        when(caseDetailsConverter.toCaseDataGA(any()))
                .thenReturn(caseData);
        AssertionsForClassTypes
                .assertThat(HearingFormGeneratorGeneralApplication.canViewResp(caseData, generalAppCaseData, "2")).isFalse();
        AssertionsForClassTypes
                .assertThat(HearingFormGeneratorGeneralApplication.canViewResp(caseData, generalAppCaseData, "1")).isTrue();
        AssertionsForClassTypes
                .assertThat(HearingFormGeneratorGeneralApplication.canViewClaimant(caseData, generalAppCaseData)).isFalse();
    }

    @Test
    void testCanViewWithoutNoticeGaCreatedByClaimant() {
        CaseData caseData = CaseDataBuilder.builder().getMainCaseDataWithDetails(
                        true,
                        false,
                        false, true).build();
        CaseData generalAppCaseData = CaseData.builder().ccdCaseReference(CaseDataBuilder.CASE_ID).build();
        when(caseDetailsConverter.toCaseDataGA(any()))
                .thenReturn(caseData);
        AssertionsForClassTypes
                .assertThat(HearingFormGeneratorGeneralApplication.canViewResp(caseData, generalAppCaseData, "2")).isFalse();
        AssertionsForClassTypes
                .assertThat(HearingFormGeneratorGeneralApplication.canViewResp(caseData, generalAppCaseData, "1")).isFalse();
        AssertionsForClassTypes
                .assertThat(HearingFormGeneratorGeneralApplication.canViewClaimant(caseData, generalAppCaseData)).isTrue();
    }

    @Test
    void testCanViewWithNoticeGaCreatedByResp2() {
        CaseData caseData = CaseDataBuilder.builder().getMainCaseDataWithDetails(
                        true,
                        true,
                        true, true).build();
        CaseData generalAppCaseData = CaseData.builder().ccdCaseReference(CaseDataBuilder.CASE_ID).build();
        when(caseDetailsConverter.toCaseDataGA(any()))
                .thenReturn(caseData);
        AssertionsForClassTypes
                .assertThat(HearingFormGeneratorGeneralApplication.canViewResp(caseData, generalAppCaseData, "2")).isTrue();
        AssertionsForClassTypes
                .assertThat(HearingFormGeneratorGeneralApplication.canViewResp(caseData, generalAppCaseData, "1")).isTrue();
        AssertionsForClassTypes
                .assertThat(HearingFormGeneratorGeneralApplication.canViewClaimant(caseData, generalAppCaseData)).isTrue();
    }

    @Test
    void test_getTemplate() {
        CaseData caseData = CaseDataBuilder.builder().build();
        assertThat(generator.getTemplate(FlowFlag.ONE_RESPONDENT_REPRESENTATIVE)).isEqualTo(HEARING_APPLICATION);
    }

    @Nested
    class GetTemplateDataLip {

        @Test
        void test_getTemplate() {
            assertThat(generator.getTemplate(FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT)).isEqualTo(POST_JUDGE_HEARING_APPLICATION_LIP);
        }

        @Test
        void whenJudgeMakeDecision_shouldGetHearingFormData() {
            when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(POST_JUDGE_HEARING_APPLICATION_LIP)))
                .thenReturn(new DocmosisDocument(POST_JUDGE_HEARING_APPLICATION_LIP.getDocumentTitle(), bytes));

            when(documentManagementService
                     .uploadDocument(any(), (PDF) any()))
                .thenReturn(CASE_DOCUMENT);
            when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
                .thenReturn(LocationRefData.builder().epimmsId("2").externalShortName("London").build());

            Map<String, String> refMap = new HashMap<>();
            refMap.put("applicantSolicitor1Reference", "app1ref");
            refMap.put("respondentSolicitor1Reference", "resp1ref");
            refMap.put("respondentSolicitor2Reference", "resp2ref");
            Map<String, Object> caseDataContent = new HashMap<>();
            caseDataContent.put("solicitorReferences", refMap);
            CaseData mainCaseData = CaseDataBuilder.builder().getMainCaseDataWithDetails(
                true,
                true,
                true, true).build();
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(mainCaseData);

            CaseDetails caseDetails = CaseDetails.builder().data(caseDataContent).build();
            when(coreCaseDataService.getCase(
                anyLong()
            )).thenReturn(caseDetails);

            CaseData caseData = CaseDataBuilder.builder().hearingScheduledApplication(YES)
                .parentClaimantIsApplicant(YES)
                .gaHearingNoticeDetail(GAHearingNoticeDetail
                                           .builder()
                                           .hearingDuration(GAHearingDuration.OTHER)
                                           .channel(GAJudicialHearingType.IN_PERSON)
                                           .hearingLocation(
                                               DynamicList.builder()
                                                   .value(selectedLocation).listItems(listItems)
                                                   .build()).build())
                .build();

            var templateData = generator.getTemplateData(CaseDataBuilder.builder().getCivilCaseData(), caseData, "auth", FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT);
            assertThat(templateData.getCourt()).isEqualTo("London");
            assertThat(templateData.getJudgeHearingLocation()).isEqualTo("sitename - location name - D12 8997");
            assertEquals("respondent1 partyname", templateData.getPartyName());
            assertEquals("respondent1address1", templateData.getPartyAddressAddressLine1());
            assertEquals("respondent1address2", templateData.getPartyAddressAddressLine2());
            assertEquals("respondent1address3", templateData.getPartyAddressAddressLine3());
            assertEquals("respondent1posttown", templateData.getPartyAddressPostTown());
            assertEquals("respondent1postcode", templateData.getPartyAddressPostCode());
        }
    }
}

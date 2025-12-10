package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.civil.documentmanagement.SecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.ComplexityBand;
import uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderSelection;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrdersComplexityBand;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderAfterHearingDate;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderAfterHearingDateType;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.JUDGE_FINAL_ORDER;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.INTERMEDIATE_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.MULTI_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.BLANK_TEMPLATE_AFTER_HEARING_DOCX;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.BLANK_TEMPLATE_BEFORE_HEARING_DOCX;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.FIX_DATE_CCMC_DOCX;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.FIX_DATE_CMC_DOCX;
import static uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.JudgeOrderDownloadGenerator.BLANK_TEMPLATE_TO_BE_USED_AFTER_A_HEARING;
import static uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.JudgeOrderDownloadGenerator.BLANK_TEMPLATE_TO_BE_USED_BEFORE_A_HEARING_BOX_WORK;
import static uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.JudgeOrderDownloadGenerator.FIX_A_DATE_FOR_CCMC;
import static uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.JudgeOrderDownloadGenerator.FIX_A_DATE_FOR_CMC;
import static uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.JudgeOrderDownloadGenerator.INTERMEDIATE_NO_BAND_NO_REASON;
import static uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.JudgeOrderDownloadGenerator.INTERMEDIATE_NO_BAND_WITH_REASON;
import static uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.JudgeOrderDownloadGenerator.INTERMEDIATE_WITH_BAND_NO_REASON;
import static uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.JudgeOrderDownloadGenerator.INTERMEDIATE_WITH_BAND_WITH_REASON;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JudgeOrderDownloadGeneratorTest {

    private static final CaseLocationCivil caseManagementLocation = CaseLocationCivil.builder().baseLocation("000000").build();
    private static final String BEARER_TOKEN = "Bearer Token";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String FILE_BLANK_BEFORE_HEARING = format(BLANK_TEMPLATE_BEFORE_HEARING_DOCX.getDocumentTitle(),  formatLocalDate(LocalDate.now(), DATE_FORMAT));
    private static final String FILE_BLANK_AFTER_HEARING = format(BLANK_TEMPLATE_AFTER_HEARING_DOCX.getDocumentTitle(),  formatLocalDate(LocalDate.now(), DATE_FORMAT));
    private static final String FILE_FIX_DATE_CMC = format(FIX_DATE_CMC_DOCX.getDocumentTitle(),  formatLocalDate(LocalDate.now(), DATE_FORMAT));
    private static final String FILE_FIX_DATE_CCMC = format(FIX_DATE_CCMC_DOCX.getDocumentTitle(),  formatLocalDate(LocalDate.now(), DATE_FORMAT));
    private static final CaseDocument BLANK_TEMPLATE_BEFORE_HEARING = CaseDocumentBuilder.builder()
        .documentName(FILE_BLANK_BEFORE_HEARING)
        .documentType(JUDGE_FINAL_ORDER)
        .build();

    private static final CaseDocument BLANK_TEMPLATE_AFTER_HEARING = CaseDocumentBuilder.builder()
        .documentName(FILE_BLANK_AFTER_HEARING)
        .documentType(JUDGE_FINAL_ORDER)
        .build();

    private static final CaseDocument FIX_DATE_CMC = CaseDocumentBuilder.builder()
        .documentName(FILE_FIX_DATE_CMC)
        .documentType(JUDGE_FINAL_ORDER)
        .build();

    private static final CaseDocument FIX_DATE_CCMC = CaseDocumentBuilder.builder()
        .documentName(FILE_FIX_DATE_CCMC)
        .documentType(JUDGE_FINAL_ORDER)
        .build();

    private static LocationRefData locationRefData = LocationRefData.builder().siteName("SiteName")
        .courtAddress("1").postcode("1")
        .courtName("Court Name").region("Region").regionId("4").courtVenueId("000")
        .externalShortName("ExternalShortName")
        .courtTypeId("10").courtLocationCode("121")
        .epimmsId("000000").build();

    @InjectMocks
    JudgeOrderDownloadGenerator judgeOrderDownloadGenerator;
    @Mock
    private DocumentGeneratorService documentGeneratorService;
    @Mock
    private SecuredDocumentManagementService documentManagementService;
    @Mock
    private UserService userService;
    @Mock
    private DocumentHearingLocationHelper documentHearingLocationHelper;

    @BeforeEach
    public void setUp() {
        when(userService.getUserDetails(any())).thenReturn(new UserDetails("1", "test@email.com", "Test", "User", null));
        when(documentHearingLocationHelper.getCaseManagementLocationDetailsNro(any(), any(), any())).thenReturn(locationRefData);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        BLANK_TEMPLATE_TO_BE_USED_AFTER_A_HEARING,
        BLANK_TEMPLATE_TO_BE_USED_BEFORE_A_HEARING_BOX_WORK,
        FIX_A_DATE_FOR_CCMC,
        FIX_A_DATE_FOR_CMC
    })
    void getDownloadTemplate(String templateToUse) {
        if (templateToUse.equals(BLANK_TEMPLATE_TO_BE_USED_AFTER_A_HEARING)) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderDownloadTemplateOptions(DynamicList.builder()
                                                       .value(DynamicListElement.dynamicElement(templateToUse))
                                                       .build())
                .orderAfterHearingDate(OrderAfterHearingDate.builder()
                                           .dateType(OrderAfterHearingDateType.BESPOKE_RANGE)
                                           .bespokeDates("12-01-2025, 14-12-2024 to 19-12-2024")
                                           .build())
                .build();
            judgeOrderDownloadGenerator.getDownloadTemplate(caseData, "auth");
        } else {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderDownloadTemplateOptions(DynamicList.builder()
                                                       .value(DynamicListElement.dynamicElement(templateToUse))
                                                       .build())
                .build();
            judgeOrderDownloadGenerator.getDownloadTemplate(caseData, "auth");
        }

        switch (templateToUse) {
            case BLANK_TEMPLATE_TO_BE_USED_AFTER_A_HEARING:
                assertEquals(BLANK_TEMPLATE_AFTER_HEARING_DOCX, judgeOrderDownloadGenerator.docmosisTemplate);
                break;
            case BLANK_TEMPLATE_TO_BE_USED_BEFORE_A_HEARING_BOX_WORK:
                assertEquals(BLANK_TEMPLATE_BEFORE_HEARING_DOCX, judgeOrderDownloadGenerator.docmosisTemplate);
                break;
            case FIX_A_DATE_FOR_CCMC:
                assertEquals(FIX_DATE_CCMC_DOCX, judgeOrderDownloadGenerator.docmosisTemplate);
                break;
            case FIX_A_DATE_FOR_CMC:
                assertEquals(FIX_DATE_CMC_DOCX, judgeOrderDownloadGenerator.docmosisTemplate);
                break;
            default: // do nothing
        }

    }

    @ParameterizedTest
    @MethodSource("testData")
    void getTrackAndComplexityText(CaseData caseData, String expectedText) {
        String response = judgeOrderDownloadGenerator.getTrackAndComplexityText(caseData);
        assertEquals(expectedText, response);
    }

    static Stream<Arguments> testData() {
        return Stream.of(
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderAllocateToTrack(YES)
                    .finalOrderTrackAllocation(MULTI_CLAIM)
                    .build(),
                "This case is allocated to the multi-track."
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderAllocateToTrack(YES)
                    .finalOrderTrackAllocation(INTERMEDIATE_CLAIM)
                    .finalOrderIntermediateTrackComplexityBand(FinalOrdersComplexityBand.builder()
                                                                   .assignComplexityBand(YES)
                                                                   .band(ComplexityBand.BAND_2)
                                                                   .reasons("reasons").build())
                    .build(),
                format(INTERMEDIATE_WITH_BAND_WITH_REASON, "2", "reasons")
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderAllocateToTrack(YES)
                    .finalOrderTrackAllocation(INTERMEDIATE_CLAIM)
                    .finalOrderIntermediateTrackComplexityBand(FinalOrdersComplexityBand.builder()
                                                                   .assignComplexityBand(YES)
                                                                   .band(ComplexityBand.BAND_1).build())
                    .build(),
                format(INTERMEDIATE_WITH_BAND_NO_REASON, "1")
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderAllocateToTrack(YES)
                    .finalOrderTrackAllocation(INTERMEDIATE_CLAIM)
                    .finalOrderIntermediateTrackComplexityBand(FinalOrdersComplexityBand.builder()
                                                                   .assignComplexityBand(NO)
                                                                   .build())
                    .build(),
                INTERMEDIATE_NO_BAND_NO_REASON
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderAllocateToTrack(YES)
                    .finalOrderTrackAllocation(INTERMEDIATE_CLAIM)
                    .finalOrderIntermediateTrackComplexityBand(FinalOrdersComplexityBand.builder()
                                                                   .assignComplexityBand(NO)
                                                                   .reasons("reasons").build())
                    .build(),
                format(INTERMEDIATE_NO_BAND_WITH_REASON, "reasons")
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderAllocateToTrack(NO)
                    .build(),
                null
            )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "BAND_1",
        "BAND_2",
        "BAND_3",
        "BAND_4"
    })
    void getComplexityBandIntermediate(String bandToUse) {
        ComplexityBand band = ComplexityBand.valueOf(bandToUse);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderIntermediateTrackComplexityBand(FinalOrdersComplexityBand.builder()
                                                           .assignComplexityBand(YES)
                                                           .band(band).build())
            .build();
        String response = judgeOrderDownloadGenerator.getComplexityBand(caseData);

        switch (band) {
            case BAND_1:
                assertEquals("1", response);
                break;
            case BAND_2:
                assertEquals("2", response);
                break;
            case BAND_3:
                assertEquals("3", response);
                break;
            case BAND_4:
                assertEquals("4", response);
                break;
            default: // do nothing
        }
    }

    @Test
    void shouldGenerateBlankTemplateBeforeHearing_whenOptionSelected_intermediateClaim() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(BLANK_TEMPLATE_BEFORE_HEARING_DOCX), eq("docx")))
            .thenReturn(new DocmosisDocument(BLANK_TEMPLATE_BEFORE_HEARING_DOCX.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(FILE_BLANK_BEFORE_HEARING, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(BLANK_TEMPLATE_BEFORE_HEARING);
        when(documentHearingLocationHelper.getCaseManagementLocationDetailsNro(any(), any(), any())).thenReturn(locationRefData);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderSelection(FinalOrderSelection.DOWNLOAD_ORDER_TEMPLATE)
            .finalOrderAllocateToTrack(YES)
            .finalOrderTrackAllocation(INTERMEDIATE_CLAIM)
            .finalOrderIntermediateTrackComplexityBand(FinalOrdersComplexityBand.builder()
                                                           .assignComplexityBand(YES)
                                                           .band(ComplexityBand.BAND_2)
                                                           .reasons("important reasons")
                                                           .build())
            .finalOrderDownloadTemplateOptions(DynamicList.builder()
                                                   .value(DynamicListElement
                                                              .dynamicElement("Blank template to be used before a hearing/box work"))
                                                   .build())
            .caseManagementLocation(caseManagementLocation)
            .build();
        CaseDocument caseDocument = judgeOrderDownloadGenerator.generate(caseData, BEARER_TOKEN);

        assertNotNull(caseDocument);
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(FILE_BLANK_BEFORE_HEARING, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void shouldGenerateBlankTemplateBeforeHearing_whenOptionSelected_smallClaim() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(BLANK_TEMPLATE_BEFORE_HEARING_DOCX), eq("docx")))
            .thenReturn(new DocmosisDocument(BLANK_TEMPLATE_BEFORE_HEARING_DOCX.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(FILE_BLANK_BEFORE_HEARING, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(BLANK_TEMPLATE_BEFORE_HEARING);
        when(documentHearingLocationHelper.getCaseManagementLocationDetailsNro(any(), any(), any())).thenReturn(locationRefData);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderSelection(FinalOrderSelection.DOWNLOAD_ORDER_TEMPLATE)
            .finalOrderAllocateToTrack(YES)
            .finalOrderTrackAllocation(SMALL_CLAIM)
            .finalOrderDownloadTemplateOptions(DynamicList.builder()
                                                   .value(DynamicListElement
                                                              .dynamicElement("Blank template to be used before a hearing/box work"))
                                                   .build())
            .caseManagementLocation(caseManagementLocation)
            .build();
        CaseDocument caseDocument = judgeOrderDownloadGenerator.generate(caseData, BEARER_TOKEN);

        assertNotNull(caseDocument);
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(FILE_BLANK_BEFORE_HEARING, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void shouldGenerateBlankTemplateAfterHearing_whenOptionSelected() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(BLANK_TEMPLATE_AFTER_HEARING_DOCX), eq("docx")))
            .thenReturn(new DocmosisDocument(BLANK_TEMPLATE_AFTER_HEARING_DOCX.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(FILE_BLANK_AFTER_HEARING, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(BLANK_TEMPLATE_AFTER_HEARING);
        when(documentHearingLocationHelper.getCaseManagementLocationDetailsNro(any(), any(), any())).thenReturn(locationRefData);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderSelection(FinalOrderSelection.DOWNLOAD_ORDER_TEMPLATE)
            .finalOrderAllocateToTrack(YES)
            .finalOrderTrackAllocation(SMALL_CLAIM)
            .finalOrderDownloadTemplateOptions(DynamicList.builder()
                                                   .value(DynamicListElement
                                                              .dynamicElement("Blank template to be used after a hearing"))
                                                   .build())
            .orderAfterHearingDate(OrderAfterHearingDate.builder()
                                       .dateType(OrderAfterHearingDateType.BESPOKE_RANGE)
                                       .bespokeDates("12-01-2025, 14-12-2024 to 19-12-2024")
                                       .build())
            .caseManagementLocation(caseManagementLocation)
            .build();
        CaseDocument caseDocument = judgeOrderDownloadGenerator.generate(caseData, BEARER_TOKEN);

        assertNotNull(caseDocument);
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(FILE_BLANK_AFTER_HEARING, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void shouldGenerateFixDateCMC_whenOptionSelected() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(FIX_DATE_CMC_DOCX), eq("docx")))
            .thenReturn(new DocmosisDocument(FIX_DATE_CMC_DOCX.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(FILE_FIX_DATE_CMC, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(FIX_DATE_CMC);
        when(documentHearingLocationHelper.getCaseManagementLocationDetailsNro(any(), any(), any())).thenReturn(locationRefData);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderSelection(FinalOrderSelection.DOWNLOAD_ORDER_TEMPLATE)
            .finalOrderAllocateToTrack(YES)
            .finalOrderTrackAllocation(INTERMEDIATE_CLAIM)
            .finalOrderIntermediateTrackComplexityBand(FinalOrdersComplexityBand.builder()
                                                           .assignComplexityBand(YES)
                                                           .band(ComplexityBand.BAND_2)
                                                           .reasons("important reasons")
                                                           .build())
            .finalOrderDownloadTemplateOptions(DynamicList.builder()
                                                   .value(DynamicListElement
                                                              .dynamicElement("Fix a date for CMC"))
                                                   .build())
            .caseManagementLocation(caseManagementLocation)
            .build();
        CaseDocument caseDocument = judgeOrderDownloadGenerator.generate(caseData, BEARER_TOKEN);

        assertNotNull(caseDocument);
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(FILE_FIX_DATE_CMC, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void shouldGenerateFixDateCCMC_whenOptionSelected() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(FIX_DATE_CCMC_DOCX), eq("docx")))
            .thenReturn(new DocmosisDocument(FIX_DATE_CCMC_DOCX.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(FILE_FIX_DATE_CCMC, bytes, JUDGE_FINAL_ORDER)))
            .thenReturn(FIX_DATE_CCMC);
        when(documentHearingLocationHelper.getCaseManagementLocationDetailsNro(any(), any(), any())).thenReturn(locationRefData);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderSelection(FinalOrderSelection.DOWNLOAD_ORDER_TEMPLATE)
            .finalOrderAllocateToTrack(YES)
            .finalOrderTrackAllocation(MULTI_CLAIM)
            .finalOrderDownloadTemplateOptions(DynamicList.builder()
                                                   .value(DynamicListElement
                                                              .dynamicElement("Fix a date for CCMC"))
                                                   .build())
            .caseManagementLocation(caseManagementLocation)
            .build();
        CaseDocument caseDocument = judgeOrderDownloadGenerator.generate(caseData, BEARER_TOKEN);

        assertNotNull(caseDocument);
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(FILE_FIX_DATE_CCMC, bytes, JUDGE_FINAL_ORDER));
    }
}

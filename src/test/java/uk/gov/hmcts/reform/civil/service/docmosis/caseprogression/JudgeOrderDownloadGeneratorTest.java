package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import uk.gov.hmcts.reform.civil.documentmanagement.UnsecuredDocumentManagementService;
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
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrdersComplexityBand;
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
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
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
import static uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.JudgeOrderDownloadGenerator.FAST_NO_BAND_NO_REASON;
import static uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.JudgeOrderDownloadGenerator.FAST_NO_BAND_WITH_REASON;
import static uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.JudgeOrderDownloadGenerator.FAST_WITH_BAND_NO_REASON;
import static uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.JudgeOrderDownloadGenerator.FAST_WITH_BAND_WITH_REASON;
import static uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.JudgeOrderDownloadGenerator.FIX_A_DATE_FOR_CCMC;
import static uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.JudgeOrderDownloadGenerator.FIX_A_DATE_FOR_CMC;
import static uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.JudgeOrderDownloadGenerator.INTERMEDIATE_NO_BAND_NO_REASON;
import static uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.JudgeOrderDownloadGenerator.INTERMEDIATE_NO_BAND_WITH_REASON;
import static uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.JudgeOrderDownloadGenerator.INTERMEDIATE_WITH_BAND_NO_REASON;
import static uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.JudgeOrderDownloadGenerator.INTERMEDIATE_WITH_BAND_WITH_REASON;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class JudgeOrderDownloadGeneratorTest {

    private static final CaseLocationCivil caseManagementLocation = CaseLocationCivil.builder().baseLocation("000000").build();
    private static final String BEARER_TOKEN = "Bearer Token";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String DATE_FORMAT = "dd/MM/yyyy";
    private static final String fileBlankBeforeHearing = format(BLANK_TEMPLATE_BEFORE_HEARING_DOCX.getDocumentTitle(),  formatLocalDate(LocalDate.now(), DATE_FORMAT));
    private static final CaseDocument BLANK_TEMPLATE_BEFORE_HEARING = CaseDocumentBuilder.builder()
        .documentName(fileBlankBeforeHearing)
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
    private UnsecuredDocumentManagementService documentManagementService;
    @Mock
    private UserService userService;
    @Mock
    private DocumentHearingLocationHelper documentHearingLocationHelper;

    @BeforeEach
    public void setUp() throws JsonProcessingException {
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
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderDownloadTemplateOptions(DynamicList.builder()
                                                   .value(DynamicListElement.dynamicElement(templateToUse))
                                                   .build())
            .build();
        JudgeFinalOrderForm response = judgeOrderDownloadGenerator.getDownloadTemplate(caseData, "auth");

        switch (templateToUse) {
            case BLANK_TEMPLATE_TO_BE_USED_AFTER_A_HEARING:
                assertEquals(judgeOrderDownloadGenerator.docmosisTemplate, BLANK_TEMPLATE_AFTER_HEARING_DOCX);
                break;
            case BLANK_TEMPLATE_TO_BE_USED_BEFORE_A_HEARING_BOX_WORK:
                assertEquals(judgeOrderDownloadGenerator.docmosisTemplate, BLANK_TEMPLATE_BEFORE_HEARING_DOCX);
                break;
            case FIX_A_DATE_FOR_CCMC:
                assertEquals(judgeOrderDownloadGenerator.docmosisTemplate, FIX_DATE_CCMC_DOCX);
                break;
            case FIX_A_DATE_FOR_CMC:
                assertEquals(judgeOrderDownloadGenerator.docmosisTemplate, FIX_DATE_CMC_DOCX);
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
                    .finalOrderTrackAllocation(SMALL_CLAIM)
                    .build(),
                "This case is allocated to the Small Claims."
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderAllocateToTrack(YES)
                    .finalOrderTrackAllocation(MULTI_CLAIM)
                    .build(),
                "This case is allocated to the Multi Track."
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
                    .finalOrderAllocateToTrack(YES)
                    .finalOrderTrackAllocation(FAST_CLAIM)
                    .finalOrderFastTrackComplexityBand(FinalOrdersComplexityBand.builder()
                                                           .assignComplexityBand(NO)
                                                           .build())
                    .build(),
                format(FAST_NO_BAND_NO_REASON)
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderAllocateToTrack(YES)
                    .finalOrderTrackAllocation(FAST_CLAIM)
                    .finalOrderFastTrackComplexityBand(FinalOrdersComplexityBand.builder()
                                                           .assignComplexityBand(NO)
                                                           .reasons("reasons").build())
                    .build(),
                format(FAST_NO_BAND_WITH_REASON, "reasons")
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderAllocateToTrack(YES)
                    .finalOrderTrackAllocation(FAST_CLAIM)
                    .finalOrderFastTrackComplexityBand(FinalOrdersComplexityBand.builder()
                                                           .assignComplexityBand(YES)
                                                           .band(ComplexityBand.BAND_1).build())
                    .build(),
                format(FAST_WITH_BAND_NO_REASON, "1")
            ),
            Arguments.of(
                CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .finalOrderAllocateToTrack(YES)
                    .finalOrderTrackAllocation(FAST_CLAIM)
                    .finalOrderFastTrackComplexityBand(FinalOrdersComplexityBand.builder()
                                                           .assignComplexityBand(YES)
                                                           .band(ComplexityBand.BAND_2)
                                                           .reasons("reasons").build())
                    .build(),
                format(FAST_WITH_BAND_WITH_REASON, "2", "reasons")
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
                assertEquals(response, "1");
                break;
            case BAND_2:
                assertEquals(response, "2");
                break;
            case BAND_3:
                assertEquals(response, "3");
                break;
            case BAND_4:
                assertEquals(response, "4");
                break;
            default: // do nothing
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "BAND_1",
        "BAND_2",
        "BAND_3",
        "BAND_4"
    })
    void getComplexityBandFast(String bandToUse) {
        ComplexityBand band = ComplexityBand.valueOf(bandToUse);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderFastTrackComplexityBand(FinalOrdersComplexityBand.builder()
                                                           .assignComplexityBand(YES)
                                                           .band(band).build())
            .build();
        String response = judgeOrderDownloadGenerator.getComplexityBand(caseData);

        switch (band) {
            case BAND_1:
                assertEquals(response, "1");
                break;
            case BAND_2:
                assertEquals(response, "2");
                break;
            case BAND_3:
                assertEquals(response, "3");
                break;
            case BAND_4:
                assertEquals(response, "4");
                break;
            default: // do nothing
        }
    }

    @Test
    void shouldGenerateBlankTemplateBeforeHearing_whenOptionSelected_intermediateClaim() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(BLANK_TEMPLATE_BEFORE_HEARING_DOCX), eq("docx")))
            .thenReturn(new DocmosisDocument(BLANK_TEMPLATE_BEFORE_HEARING_DOCX.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileBlankBeforeHearing, bytes, JUDGE_FINAL_ORDER)))
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
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(fileBlankBeforeHearing, bytes, JUDGE_FINAL_ORDER));
    }

    @Test
    void shouldGenerateBlankTemplateBeforeHearing_whenOptionSelected_smallClaim() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(BLANK_TEMPLATE_BEFORE_HEARING_DOCX), eq("docx")))
            .thenReturn(new DocmosisDocument(BLANK_TEMPLATE_BEFORE_HEARING_DOCX.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileBlankBeforeHearing, bytes, JUDGE_FINAL_ORDER)))
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
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(fileBlankBeforeHearing, bytes, JUDGE_FINAL_ORDER));
    }

}

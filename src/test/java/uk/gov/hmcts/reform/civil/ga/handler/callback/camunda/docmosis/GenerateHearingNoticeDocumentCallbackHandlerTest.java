package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.ga.service.SendFinalOrderPrintService;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.GaHearingFormGenerator;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.ga.enums.welshenhancements.PreTranslationGaDocumentType.HEARING_NOTICE_DOC;

@ExtendWith(MockitoExtension.class)
class GenerateHearingNoticeDocumentCallbackHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    @InjectMocks
    private GenerateHearingNoticeDocumentCallbackHandler handler;
    @Mock
    private GaHearingFormGenerator hearingFormGenerator;
    @Spy
    private ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private GaForLipService gaForLipService;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private SendFinalOrderPrintService sendFinalOrderPrintService;

    @Spy
    private AssignCategoryId assignCategoryId = new AssignCategoryId();

    @Mock
    private FeatureToggleService featureToggleService;

    @Test
    void shouldReturnCorrectActivityId_whenRequested() {
        assertThat(handler.camundaActivityId(CallbackParams.builder().build())).isEqualTo("GenerateHearingNoticeDocument");
    }

    @Test
    void shouldGenerateHearingNoticeDocument_whenAndWelseToggleEnabledAboutToSubmitEventIsCalled() {
        CaseDocument caseDocument = CaseDocument.builder()
            .documentLink(Document.builder().documentUrl("doc").build()).build();

        when(hearingFormGenerator.generate(any(), any())).thenReturn(caseDocument);
        when(gaForLipService.isGaForLip(any())).thenReturn(false);
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .isGaApplicantLip(YesOrNo.YES)
            .applicantBilingualLanguagePreference(YesOrNo.YES)
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        assertThat(updatedData.getHearingNoticeDocument().size()).isEqualTo(0);
        assertThat(updatedData.getPreTranslationGaDocuments().size()).isEqualTo(1);
        assertThat(updatedData.getPreTranslationGaDocumentType()).isEqualTo(HEARING_NOTICE_DOC);
    }

    @Test
    void shouldGenerateHearingNoticeDocument_whenAboutToSubmitEventIsCalled() {
        CaseDocument caseDocument = CaseDocument.builder()
            .documentLink(Document.builder().documentUrl("doc").build()).build();

        when(hearingFormGenerator.generate(any(), any())).thenReturn(caseDocument);
        when(gaForLipService.isGaForLip(any())).thenReturn(false);
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(false);
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        assertThat(updatedData.getHearingNoticeDocument().size()).isEqualTo(1);
    }

    @Test
    void shouldGenerateHearingNoticeDocumentWithCoverLetterTwice() {
        CaseDocument caseDocument = CaseDocument.builder()
            .documentLink(Document.builder().documentUrl("doc").build()).build();

        when(hearingFormGenerator.generate(any(), any())).thenReturn(caseDocument);
        when(gaForLipService.isLipApp(any())).thenReturn(true);
        when(gaForLipService.isLipResp(any())).thenReturn(true);
        when(hearingFormGenerator.generate(any(), any(), any(), any())).thenReturn(caseDocument);

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .applicationIsUncloakedOnce(YesOrNo.YES)
            .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("1234").build())
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        assertThat(updatedData.getHearingNoticeDocument().size()).isEqualTo(1);
        verify(hearingFormGenerator, times(1)).generate(any(), any());
        verify(hearingFormGenerator, times(2)).generate(any(), any(), any(), any());
        verify(sendFinalOrderPrintService, times(2)).sendJudgeFinalOrderToPrintForLIP(any(), any(), any(), any(), any());
    }

    @Test
    void shouldGenerateHearingNoticeDocumentWithCoverLetterTwiceWhenWithoutNotice() {
        CaseDocument caseDocument = CaseDocument.builder()
            .documentLink(Document.builder().documentUrl("doc").build()).build();

        when(hearingFormGenerator.generate(any(), any())).thenReturn(caseDocument);
        when(gaForLipService.isLipApp(any())).thenReturn(true);
        when(gaForLipService.isLipResp(any())).thenReturn(true);
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(false);
        when(hearingFormGenerator.generate(any(), any(), any(), any())).thenReturn(caseDocument);

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.NO).build())
            .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("1234").build())
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        assertThat(updatedData.getHearingNoticeDocument().size()).isEqualTo(1);
        verify(hearingFormGenerator, times(2)).generate(any(), any(), any(), any());
        verify(sendFinalOrderPrintService, times(2)).sendJudgeFinalOrderToPrintForLIP(any(), any(), any(), any(), any());
    }

    @Test
    void shouldNotGenerateHearingNoticeDocumentWithCoverLetterWhenLanguagePreference() {
        CaseDocument caseDocument = CaseDocument.builder()
            .documentLink(Document.builder().documentUrl("doc").build()).build();

        when(hearingFormGenerator.generate(any(), any())).thenReturn(caseDocument);
        when(gaForLipService.isLipApp(any())).thenReturn(true);
        when(gaForLipService.isLipResp(any())).thenReturn(true);
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().generalOrderApplication()
            .isGaApplicantLip(YesOrNo.YES)
            .applicantBilingualLanguagePreference(YesOrNo.YES)
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.NO).build())
            .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("1234").build())
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        GeneralApplicationCaseData updatedData = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        assertThat(updatedData.getHearingNoticeDocument().size()).isEqualTo(0);
        verifyNoInteractions(sendFinalOrderPrintService);

    }
}

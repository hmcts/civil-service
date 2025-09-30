package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.GaForLipService;
import uk.gov.hmcts.reform.civil.service.SendFinalOrderPrintService;
import uk.gov.hmcts.reform.civil.service.docmosis.hearingorder.HearingFormGenerator;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.welshenhancements.PreTranslationGaDocumentType.HEARING_NOTICE_DOC;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    GenerateHearingNoticeDocumentCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    AssignCategoryId.class
})
class GenerateHearingNoticeDocumentCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private GenerateHearingNoticeDocumentCallbackHandler handler;
    @MockBean
    private HearingFormGenerator hearingFormGenerator;
    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private GaForLipService gaForLipService;
    @MockBean
    private CaseDetailsConverter caseDetailsConverter;
    @MockBean
    private CoreCaseDataService coreCaseDataService;
    @MockBean
    private SendFinalOrderPrintService sendFinalOrderPrintService;

    @Autowired
    private AssignCategoryId assignCategoryId;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Test
    void shouldReturnCorrectActivityId_whenRequested() {
        CaseData caseData = CaseDataBuilder.builder().generalOrderApplication().build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        assertThat(handler.camundaActivityId(CallbackParams.builder().build())).isEqualTo("GenerateHearingNoticeDocument");
    }

    @Test
    void shouldGenerateHearingNoticeDocument_whenAndWelseToggleEnabledAboutToSubmitEventIsCalled() {
        CaseDocument caseDocument = CaseDocument.builder()
            .documentLink(Document.builder().documentUrl("doc").build()).build();

        when(hearingFormGenerator.generate(any(), any())).thenReturn(caseDocument);
        when(gaForLipService.isGaForLip(any())).thenReturn(false);
        when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
        CaseData caseData = CaseDataBuilder.builder().generalOrderApplication()
            .isGaApplicantLip(YesOrNo.YES)
            .applicantBilingualLanguagePreferenceGA(YesOrNo.YES)
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getHearingNoticeDocumentGA().size()).isEqualTo(0);
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
        CaseData caseData = CaseDataBuilder.builder().generalOrderApplication()
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getHearingNoticeDocumentGA().size()).isEqualTo(1);
    }

    @Test
    void shouldGenerateHearingNoticeDocumentWithCoverLetterTwice() {
        CaseDocument caseDocument = CaseDocument.builder()
            .documentLink(Document.builder().documentUrl("doc").build()).build();

        when(hearingFormGenerator.generate(any(), any())).thenReturn(caseDocument);
        when(gaForLipService.isLipApp(any())).thenReturn(true);
        when(gaForLipService.isLipResp(any())).thenReturn(true);
        when(hearingFormGenerator.generate(any(), any(), any(), any())).thenReturn(caseDocument);

        when(coreCaseDataService.getCase(any())).thenReturn(CaseDetails.builder().build());
        when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(CaseData.builder().build());
        CaseData caseData = CaseDataBuilder.builder().generalOrderApplication()
            .applicationIsUncloakedOnce(YesOrNo.YES)
            .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("1234").build())
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getHearingNoticeDocumentGA().size()).isEqualTo(1);
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

        when(coreCaseDataService.getCase(any())).thenReturn(CaseDetails.builder().build());
        when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(CaseData.builder().build());
        CaseData caseData = CaseDataBuilder.builder().generalOrderApplication()
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.NO).build())
            .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("1234").build())
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getHearingNoticeDocumentGA().size()).isEqualTo(1);
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
        when(hearingFormGenerator.generate(any(), any(), any(), any())).thenReturn(caseDocument);

        when(coreCaseDataService.getCase(any())).thenReturn(CaseDetails.builder().build());
        when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(CaseData.builder().build());
        CaseData caseData = CaseDataBuilder.builder().generalOrderApplication()
            .isGaApplicantLip(YesOrNo.YES)
            .applicantBilingualLanguagePreferenceGA(YesOrNo.YES)
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YesOrNo.NO).build())
            .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("1234").build())
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getHearingNoticeDocumentGA().size()).isEqualTo(0);
        verifyNoInteractions(sendFinalOrderPrintService);

    }
}

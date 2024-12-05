package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.SettleDiscontinueYesOrNoList;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.settlediscontinue.NoticeOfDiscontinuanceLiPLetterGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_DISCONTINUANCE_LETTER_LIP_DEFENDANT1;

@ExtendWith(MockitoExtension.class)
public class PostNoticeOfDiscontinuanceLetterLiPDefendant1HandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NoticeOfDiscontinuanceLiPLetterGenerator letterGenerator;
    @InjectMocks
    private PostNoticeOfDiscontinuanceLetterLiPDefendant1Handler handler;
    public static final String TASK_ID_DEFENDANT = "PostNoticeOfDiscontinuanceDefendant1LIP";
    private static final CaseDocument caseDocument = CaseDocumentBuilder.builder()
            .documentName("document name")
            .documentType(DocumentType.NOTICE_OF_DISCONTINUANCE)
            .build();

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(SEND_DISCONTINUANCE_LETTER_LIP_DEFENDANT1);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
                        SEND_DISCONTINUANCE_LETTER_LIP_DEFENDANT1.name()).build())
                .build())).isEqualTo(TASK_ID_DEFENDANT);
    }

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfully() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .courtPermissionNeeded(SettleDiscontinueYesOrNoList.NO)
                .respondent1NoticeOfDiscontinueAllPartyViewDoc(caseDocument).build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(SEND_DISCONTINUANCE_LETTER_LIP_DEFENDANT1.name());

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isNull();
        verify(letterGenerator).printNoticeOfDiscontinuanceLetter(
                caseData,
                params.getParams().get(BEARER_TOKEN).toString());
    }
}

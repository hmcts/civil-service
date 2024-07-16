package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.DiscontinuanceTypeList;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.SettleDiscontinueYesOrNoList;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.settlediscontinue.NoticeOfDiscontinuanceFormGenerator;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GEN_NOTICE_OF_DISCONTINUANCE;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.NOTICE_OF_DISCONTINUANCE;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    GenerateDiscontinueClaimCallbackHandler.class,
    JacksonAutoConfiguration.class,
    AssignCategoryId.class
})
class GenerateDiscontinueClaimCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private GenerateDiscontinueClaimCallbackHandler handler;
    @MockBean
    private NoticeOfDiscontinuanceFormGenerator formGenerator;

    @Nested
    class AboutToSubmitCallback {
        @Test
        void shouldGenerateNoticeOfDiscontinueDocForCW_whenCourtPermissionRequired() {
            when(formGenerator.generateDocs(any(CaseData.class), anyString())).thenReturn(getCaseDocument());

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .respondent1(getPartyDetails())
                    .applicant1(getPartyDetails())
                    .courtPermissionNeeded(SettleDiscontinueYesOrNoList.YES)
                    .isPermissionGranted(SettleDiscontinueYesOrNoList.YES)
                    .typeOfDiscontinuance(DiscontinuanceTypeList.PART_DISCONTINUANCE)
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(formGenerator).generateDocs(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getNoticeOfDiscontinueCWDoc()).isNotNull();
        }

        @Test
        void shouldGenerateNoticeOfDiscontinueDocForAllParties_whenNoCourtPermissionRequired() {
            when(formGenerator.generateDocs(any(CaseData.class), anyString())).thenReturn(getCaseDocument());

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                    .respondent1(getPartyDetails())
                    .applicant1(getPartyDetails())
                    .courtPermissionNeeded(SettleDiscontinueYesOrNoList.NO)
                    .typeOfDiscontinuance(DiscontinuanceTypeList.PART_DISCONTINUANCE)
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(formGenerator).generateDocs(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getNoticeOfDiscontinueAllParitiesDoc()).isNotNull();
        }
    }

    @Test
    void shouldReturnCorrectActivityId_whenRequested() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        assertThat(handler.camundaActivityId(params)).isEqualTo("GenerateNoticeOfDiscontinueClaim");
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(GEN_NOTICE_OF_DISCONTINUANCE);
    }

    private Party getPartyDetails() {
        return PartyBuilder.builder().individual().build().toBuilder()
                .individualFirstName("John")
                .individualLastName("Doe")
                .build();
    }

    private CaseDocument getCaseDocument() {
        return CaseDocument.builder()
                .createdBy("John")
                .documentName("document name")
                .documentSize(0L)
                .documentType(NOTICE_OF_DISCONTINUANCE)
                .createdDatetime(LocalDateTime.now())
                .documentLink(Document.builder()
                        .documentUrl("fake-url")
                        .documentFileName("file-name")
                        .documentBinaryUrl("binary-url")
                        .build())
                .build();
    }
}

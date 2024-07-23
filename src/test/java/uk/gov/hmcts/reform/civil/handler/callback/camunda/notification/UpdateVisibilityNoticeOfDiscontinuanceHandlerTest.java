package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.RuntimeService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.ConfirmOrderGivesPermission;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@SpringBootTest(classes = {
    UpdateVisibilityNoticeOfDiscontinuanceHandler.class,
    JacksonAutoConfiguration.class,
    AssignCategoryId.class
})
class UpdateVisibilityNoticeOfDiscontinuanceHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private RuntimeService runTimeService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UpdateVisibilityNoticeOfDiscontinuanceHandler handler;

    @Autowired
    private AssignCategoryId assignCategoryId;

    private static final String processId = "process-id";
    private static final CaseDocument caseDocument = CaseDocumentBuilder.builder()
            .documentName("document name")
            .documentType(DocumentType.NOTICE_OF_DISCONTINUANCE)
            .build();

    @Nested
    class AboutToSubmitCallback {
        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shouldUpdateCamundaVariables_whenInvoked(Boolean toggleState) {
            //Given
            CaseData caseData = CaseDataBuilder.builder().businessProcess(BusinessProcess.builder().processInstanceId(
                processId).build()).build();
            caseData.setConfirmOrderGivesPermission(toggleState ? ConfirmOrderGivesPermission.YES : ConfirmOrderGivesPermission.NO);
            caseData.setNoticeOfDiscontinueCWDoc(caseDocument);
            CallbackParams params = CallbackParams.builder().caseData(caseData).type(ABOUT_TO_SUBMIT).request(
                CallbackRequest.builder().eventId(CaseEvent.UPDATE_VISIBILITY_NOTICE_OF_DISCONTINUANCE.name()).build()).build();
            //When
            handler.handle(params);
            //Then
            verify(runTimeService).setVariable(processId, "discontinuanceValidationSuccess", toggleState);
        }

        @Test
        void shouldCopyNoticeOfDiscontinuance() {
            CaseDocument noticeOfDiscontinuance = CaseDocumentBuilder.builder().documentName("NOTICE_OF_DISCONTINUANCE").build();
            CaseData caseData = CaseDataBuilder.builder().noticeOfDiscontinueCWDoc(noticeOfDiscontinuance).businessProcess(
                BusinessProcess.builder().processInstanceId(processId).build()).build();
            caseData.setConfirmOrderGivesPermission(ConfirmOrderGivesPermission.YES);
            CallbackParams params = CallbackParams.builder().caseData(caseData).type(ABOUT_TO_SUBMIT).request(
                CallbackRequest.builder().eventId(CaseEvent.UPDATE_VISIBILITY_NOTICE_OF_DISCONTINUANCE.name()).build()).build();
            // When
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            // Then
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getNoticeOfDiscontinueCWDoc()).isNull();
            assertThat(updatedData.getNoticeOfDiscontinueAllParitiesDoc()).isNotNull();
            assertThat(updatedData.getNoticeOfDiscontinueAllParitiesDoc().getDocumentLink().getCategoryID()).isEqualTo(
                DocCategory.NOTICE_OF_DISCONTINUE.getValue());
        }
    }
}

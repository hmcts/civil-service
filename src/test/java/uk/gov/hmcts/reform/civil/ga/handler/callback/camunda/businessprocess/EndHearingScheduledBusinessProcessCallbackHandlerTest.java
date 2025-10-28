package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.businessprocess;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.ga.service.ParentCaseUpdateHelper;
import uk.gov.hmcts.reform.civil.ga.utils.JudicialDecisionNotificationUtil;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.END_HEARING_SCHEDULED_PROCESS_GASPEC;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@SpringBootTest(classes = {
    EndHearingScheduledBusinessProcessCallbackHandler.class,
    CoreCaseDataService.class,
    ObjectMapper.class,
    JudicialDecisionNotificationUtil.class
})
public class EndHearingScheduledBusinessProcessCallbackHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    @Autowired
    private EndHearingScheduledBusinessProcessCallbackHandler handler;
    @MockBean
    private CaseDetailsConverter caseDetailsConverter;
    @MockBean
    private CoreCaseDataService coreCaseDataService;
    @MockBean
    private ParentCaseUpdateHelper parentCaseUpdateHelper;
    private CallbackParams params;

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(END_HEARING_SCHEDULED_PROCESS_GASPEC);
    }

    @Test
    void shouldTriggerEventAndChangeStateToHearingScheduled() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().hearingScheduledApplication(YES).build();
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(caseData);
        params = getCallbackParams(caseData);
        handler.handle(params);

        verify(parentCaseUpdateHelper, times(1))
            .updateParentWithGAState(any(), any());
    }

    private CallbackParams getCallbackParams(GeneralApplicationCaseData caseData) {
        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
        return CallbackParams.builder()
                              .type(ABOUT_TO_SUBMIT)
                              .pageId(null)
                              .request(CallbackRequest.builder()
                                           .caseDetails(CaseDetails.builder()
                                                            .data(objectMapper.convertValue(
                                                                caseData,
                                                                new TypeReference<Map<String, Object>>() {}))
                                                            .id(CASE_ID).build())
                                           .eventId("END_HEARING_SCHEDULED_PROCESS_GASPEC")
                                           .build())
                              .baseCaseData(caseData)
                              .version(null)
                              .params(null)
                              .build();
    }
}

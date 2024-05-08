package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.docmosis.judgmentonline.DefaultJudgmentCoverLetterGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT2;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis.DefaultJudgmentDefendantLrCoverLetterHandler.TASK_ID_DEFENDANT_1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis.DefaultJudgmentDefendantLrCoverLetterHandler.TASK_ID_DEFENDANT_2;

@SpringBootTest(classes = {
    DefaultJudgmentDefendantLrCoverLetterHandler.class,
    JacksonAutoConfiguration.class
})
public class DefaultJudgmentDefendantLrCoverLetterHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private DefaultJudgmentDefendantLrCoverLetterHandler handler;
    @MockBean
    private DefaultJudgmentCoverLetterGenerator coverLetterGenerator;
    @MockBean
    private OrganisationService organisationService;

    public static final String TASK_ID = "SendCoverLetterToDefendantLR";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT1);
        assertThat(handler.handledEvents()).contains(POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT2);
    }

    @ParameterizedTest
    @EnumSource(
        value = CaseEvent.class,
        names = {"POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT1", "POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT2"})
    void shouldReturnCorrectCamundaActivityId_whenInvoked(CaseEvent caseEvent) {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
                caseEvent.name()).build()).build())).isEqualTo(
                    caseEvent.name()
                        .equals("POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT1") ? TASK_ID_DEFENDANT_1 : TASK_ID_DEFENDANT_2);
    }

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfullyForDefendant1() {
        // given
        OrganisationPolicy organisation1Policy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID("1234").build()).build();

        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued1v2AndBothDefendantsDefaultJudgment()
            .respondent2(Party.builder().partyName("Respondent2 name").type(Party.Type.INDIVIDUAL).build())
            .addRespondent2(YesOrNo.YES)
            .respondent1Represented(YesOrNo.YES)
            .respondent1OrganisationPolicy(organisation1Policy)
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT1.name());

        when(coverLetterGenerator.generateAndPrintDjCoverLettersPlusDocument(eq(caseData), any(), eq(false)))
            .thenReturn(new byte[]{50});

        // when
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        verify(coverLetterGenerator, times(1)).generateAndPrintDjCoverLettersPlusDocument(
            caseData, params.getParams().get(BEARER_TOKEN).toString(), false);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldDownloadDocumentAndPrintLetterSuccessfullyForDefendant2(boolean sameLegalOrgs) {
        // given
        OrganisationPolicy organisation1Policy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID("1234").build()).build();

        OrganisationPolicy organisation2Policy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID("3456").build()).build();

        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued1v2AndBothDefendantsDefaultJudgment()
            .respondent2(Party.builder().partyName("Respondent2 name").type(Party.Type.INDIVIDUAL).build())
            .addRespondent2(YesOrNo.YES)
            .respondent1Represented(YesOrNo.YES)
            .respondent1OrganisationPolicy(organisation1Policy)
            .respondent2OrganisationPolicy(sameLegalOrgs ? organisation1Policy : organisation2Policy)
                .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT2.name());

        when(coverLetterGenerator.generateAndPrintDjCoverLettersPlusDocument(eq(caseData), any(), eq(true)))
            .thenReturn(new byte[]{20});

        // when
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // then
        assertThat(response.getErrors()).isNull();
        verify(coverLetterGenerator, times(sameLegalOrgs ? 0 : 1)).generateAndPrintDjCoverLettersPlusDocument(
            caseData, params.getParams().get(BEARER_TOKEN).toString(), true);
    }
}

package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import uk.gov.hmcts.reform.civil.service.docmosis.judgmentonline.DefaultJudgmentCoverLetterGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT2;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis.DefaultJudgmentDefendantLrCoverLetterHandler.TASK_ID_DEFENDANT_1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis.DefaultJudgmentDefendantLrCoverLetterHandler.TASK_ID_DEFENDANT_2;

@ExtendWith(MockitoExtension.class)
class DefaultJudgmentDefendantLrCoverLetterHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private DefaultJudgmentDefendantLrCoverLetterHandler handler;

    @Mock
    private DefaultJudgmentCoverLetterGenerator coverLetterGenerator;

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents())
            .containsExactlyInAnyOrder(POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT1, POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT2);
    }

    @ParameterizedTest
    @EnumSource(value = CaseEvent.class, names = {
        "POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT1",
        "POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT2"})
    void shouldReturnCorrectCamundaActivityId_whenInvoked(CaseEvent caseEvent) {
        String expectedTaskId = caseEvent == POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT1 ? TASK_ID_DEFENDANT_1 : TASK_ID_DEFENDANT_2;

        CallbackParams params = CallbackParamsBuilder.builder()
            .request(CallbackRequest.builder().eventId(caseEvent.name()).build())
            .build();

        assertThat(handler.camundaActivityId(params)).isEqualTo(expectedTaskId);
    }

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfullyForDefendant1() {
        testDownloadDocumentAndPrintLetter(POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT1, true);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldDownloadDocumentAndPrintLetterSuccessfullyForDefendant2(boolean sameLegalOrgs) {
        testDownloadDocumentAndPrintLetter(POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT2, sameLegalOrgs);
    }

    private void testDownloadDocumentAndPrintLetter(CaseEvent caseEvent, boolean sameLegalOrgs) {
        OrganisationPolicy organisation1Policy = createOrganisationPolicy("1234");
        OrganisationPolicy organisation2Policy = createOrganisationPolicy("3456");

        CaseData caseData = createCaseData(organisation1Policy, organisation2Policy, sameLegalOrgs);

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(caseEvent.name());

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isNull();
        verify(coverLetterGenerator, times(sameLegalOrgs && caseEvent == POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT2 ? 0 : 1))
            .generateAndPrintDjCoverLettersPlusDocument(caseData, params.getParams().get(BEARER_TOKEN).toString(), caseEvent == POST_DJ_NON_DIVERGENT_LETTER_DEFENDANT2);
    }

    private OrganisationPolicy createOrganisationPolicy(String organisationID) {
        return OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID(organisationID).build())
            .build();
    }

    private CaseData createCaseData(OrganisationPolicy organisation1Policy, OrganisationPolicy organisation2Policy, boolean sameLegalOrgs) {
        return CaseDataBuilder.builder()
            .atStateClaimIssued1v2AndBothDefendantsDefaultJudgment()
            .respondent2(Party.builder().partyName("Respondent2 name").type(Party.Type.INDIVIDUAL).build())
            .addRespondent2(YesOrNo.YES)
            .respondent1Represented(YesOrNo.YES)
            .respondent1OrganisationPolicy(organisation1Policy)
            .respondent2OrganisationPolicy(sameLegalOrgs ? organisation1Policy : organisation2Policy)
            .build();
    }
}

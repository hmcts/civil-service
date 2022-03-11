package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.HearingDates;
import uk.gov.hmcts.reform.civil.model.HearingSupportRequirementsDJ;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.Document;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.dj.DefaultJudgmentFormGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.documents.DocumentType.DEFAULT_JUDGMENT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    DefaultJudgementHandler.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    CaseDetailsConverter.class,
})
public class DefaultJudgementHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private DefaultJudgementHandler handler;
    @MockBean
    private DefaultJudgmentFormGenerator defaultJudgmentFormGenerator;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldNotReturnError_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNotEmpty();
        }

        @Test
        void shouldReturnError_WhenAboutToStartIsInvokedOneDefendants() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1ResponseDeadline(
                    LocalDateTime.now().minusDays(15)).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnError_WhenAboutToStartIsInvokedWithTwoDefendants() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

    }

    @Nested
    class MidEventShowCertifyConditionCallback {

        private static final String PAGE_ID = "showcertifystatement";

        @Test
        void shouldReturnBoth_whenHaveTwoDefendants() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetails(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("Both")
                                                 .build())
                                      .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData().get("bothDefendants")).isEqualTo("Both");

        }

        @Test
        void shouldReturnOne_whenHaveOneDefendants() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetails(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("Test User")
                                                 .build())
                                      .build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData().get("bothDefendants")).isEqualTo("One");

        }
    }

    @Nested
    class MidEventHearingTypeSelection {

        private static final String PAGE_ID = "hearingTypeSelection";

        @Test
        void shouldReturnDisposalText_whenHearingTypeSelectionDisposal() {
            String disposalText = "will be disposal hearing provided text";
            //text that will populate text area when the hearing type selected is disposal
            //dummy text for now until proper text provided.

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .detailsOfDirectionDisposal(disposalText)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData().get("detailsOfDirectionDisposal")).isEqualTo(disposalText);

        }

        @Test
        void shouldReturnTrialText_whenHearingTypeSelectionTrial() {

            String trialText = "will be trial hearing provided text";
            //text that will populate text area when the hearing type selected is trial
            //dummy text for now until proper text provided.

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .detailsOfDirectionDisposal(trialText)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData().get("detailsOfDirectionTrial")).isEqualTo(trialText);

        }
    }

    @Nested
    class MidEventHearingSupportCallback {

        private static final String PAGE_ID = "HearingSupportRequirementsDJ";

        @Test
        void shouldReturnError_whenDateFromDateGreaterThanDateToProvided() {
            HearingDates hearingDates = HearingDates.builder().hearingUnavailableFrom(
                LocalDate.now().plusMonths(2)).hearingUnavailableUntil(
                LocalDate.now().plusMonths(1)).build();
            HearingSupportRequirementsDJ hearingSupportRequirementsDJ = HearingSupportRequirementsDJ.builder()
                .hearingDates(
                    wrapElements(hearingDates)).build();

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .hearingSupportRequirementsDJ(hearingSupportRequirementsDJ)
                .respondent2SameLegalRepresentative(YES)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .hearingSupportRequirementsDJ(hearingSupportRequirementsDJ)
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors().get(0)).isEqualTo("Unavailable From Date should be less than To Date");

        }

        @Test
        void shouldReturnError_whenDateFromDateGreaterThreeMonthsProvided() {
            HearingDates hearingDates = HearingDates.builder().hearingUnavailableFrom(
                LocalDate.now().plusMonths(2)).hearingUnavailableUntil(
                LocalDate.now().plusMonths(4)).build();
            HearingSupportRequirementsDJ hearingSupportRequirementsDJ = HearingSupportRequirementsDJ
                .builder().hearingDates(
                    wrapElements(hearingDates)).build();

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .hearingSupportRequirementsDJ(hearingSupportRequirementsDJ)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors().get(0)).isEqualTo("Unavailable Dates must be within the next 3 months.");

        }

        @Test
        void shouldReturnError_whenDateFromPastDatedProvided() {

            HearingDates hearingDates = HearingDates.builder().hearingUnavailableFrom(
                LocalDate.now().plusMonths(1)).hearingUnavailableUntil(
                LocalDate.now().plusMonths(-4)).build();
            HearingSupportRequirementsDJ hearingSupportRequirementsDJ = HearingSupportRequirementsDJ
                .builder().hearingDates(
                    wrapElements(hearingDates)).build();

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .hearingSupportRequirementsDJ(hearingSupportRequirementsDJ)
                .respondent2SameLegalRepresentative(YES)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors().get(0)).isEqualTo("Unavailable Date cannot be past date");

        }

        @Test
        void shouldNotReturnError_whenValidDateRangeProvided() {
            HearingDates hearingDates = HearingDates.builder().hearingUnavailableFrom(
                LocalDate.now().plusMonths(1)).hearingUnavailableUntil(
                LocalDate.now().plusMonths(2)).build();
            HearingSupportRequirementsDJ hearingSupportRequirementsDJ = HearingSupportRequirementsDJ
                .builder().hearingDates(
                    wrapElements(hearingDates)).build();

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .hearingSupportRequirementsDJ(hearingSupportRequirementsDJ)
                .respondent2SameLegalRepresentative(YES)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();

        }

        @Test
        void shouldNotReturnError_whenNoDateRangeProvided() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .hearingSupportRequirementsDJ(HearingSupportRequirementsDJ.builder().hearingDates(
                    null).build())
                .respondent2SameLegalRepresentative(YES)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNull();

        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenInvoked() {

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .hearingSupportRequirementsDJ(HearingSupportRequirementsDJ.builder().hearingDates(
                    null).build())
                .respondent2SameLegalRepresentative(YES)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .build();

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
            assertThat(response.getConfirmationHeader()).isEqualTo(
                "# Judgment for damages to be decided Granted");

        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        public void shouldGenerateTwoForm_when1v2() {
            CaseDocument document = CaseDocument.builder()
                .createdBy("John")
                .documentName("document name")
                .documentSize(0L)
                .documentType(DEFAULT_JUDGMENT)
                .createdDatetime(LocalDateTime.now())
                .documentLink(Document.builder()
                                  .documentUrl("fake-url")
                                  .documentFileName("file-name")
                                  .documentBinaryUrl("binary-url")
                                  .build())
                .build();

            List<CaseDocument> documents = new ArrayList<>();
            documents.add(document);
            documents.add(document);
            when(defaultJudgmentFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(documents);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetails(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("Both")
                                                 .build()).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(defaultJudgmentFormGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getDefaultJudgmentDocuments().size()).isEqualTo(2);

        }

        @Test
        public void shouldNotGenerateTwoForm_when1v2And1DefSelected() {

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetails(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("One")
                                                 .build()).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getDefaultJudgmentDocuments()).isNull();

        }


        @Test
        public void shouldNotGenerateOneForm_when1v1() {
            CaseDocument document = CaseDocument.builder()
                .createdBy("John")
                .documentName("document name")
                .documentSize(0L)
                .documentType(DEFAULT_JUDGMENT)
                .createdDatetime(LocalDateTime.now())
                .documentLink(Document.builder()
                                  .documentUrl("fake-url")
                                  .documentFileName("file-name")
                                  .documentBinaryUrl("binary-url")
                                  .build())
                .build();

            List<CaseDocument> documents = new ArrayList<>();
            documents.add(document);
            when(defaultJudgmentFormGenerator.generate(any(CaseData.class), anyString())).thenReturn(documents);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .addRespondent2(NO)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .defendantDetails(DynamicList.builder()
                                      .value(DynamicListElement.builder()
                                                 .label("One")
                                                 .build()).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(defaultJudgmentFormGenerator).generate(any(CaseData.class), eq("BEARER_TOKEN"));

            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getDefaultJudgmentDocuments().size()).isEqualTo(1);

        }

    }

}

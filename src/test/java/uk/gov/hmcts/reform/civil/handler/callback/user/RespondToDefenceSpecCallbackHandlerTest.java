package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.ExitSurveyConfiguration;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.Document;
import uk.gov.hmcts.reform.civil.model.documents.DocumentType;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.HearingLRspec;
import uk.gov.hmcts.reform.civil.model.dq.SmallClaimHearing;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    RespondToDefenceSpecCallbackHandler.class,
    ExitSurveyConfiguration.class,
    ExitSurveyContentService.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    UnavailableDateValidator.class,
    CaseDetailsConverter.class
})
class RespondToDefenceSpecCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private RespondToDefenceSpecCallbackHandler handler;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UnavailableDateValidator unavailableDateValidator;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            Document document = Document.builder()
                .documentFileName("filename")
                .documentUrl("url 1")
                .documentBinaryUrl("url 2")
                .build();
            CaseDocument caseDocument = CaseDocument.builder()
                .documentLink(document)
                .documentType(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
                .build().toBuilder()
                .ccdState(AWAITING_APPLICANT_INTENTION)
                .systemGeneratedCaseDocuments(ElementUtils.wrapElements(caseDocument))
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
            Map documentMap = (Map) response.getData().get("respondent1SpecDefenceResponseDocument");
            documentMap = (Map) documentMap.get("file");

            assertThat(documentMap.get("document_filename")).isEqualTo(document.getDocumentFileName());
            assertThat(documentMap.get("document_url")).isEqualTo(document.getDocumentUrl());
            assertThat(documentMap.get("document_binary_url")).isEqualTo(document.getDocumentBinaryUrl());
        }
    }

    @Nested
    class ValidateUnavailableDates {

        @Test
        void shouldCheckDates_whenFastClaim() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
                .build().toBuilder()
                .ccdState(AWAITING_APPLICANT_INTENTION)
                .responseClaimTrack(AllocatedTrack.FAST_CLAIM.name())
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQHearingLRspec(HearingLRspec.builder()
                                                                 .build())
                                  .build())
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(MID, caseData)
                .pageId("validate-unavailable-dates")
                .build();

            Mockito.when(unavailableDateValidator.validateFastClaimHearing(
                    caseData.getApplicant1DQ().getApplicant1DQHearingLRspec()))
                .thenReturn(Collections.emptyList());

            handler.handle(params);

            Mockito.verify(unavailableDateValidator).validateFastClaimHearing(
                caseData.getApplicant1DQ().getApplicant1DQHearingLRspec());
        }

        @Test
        void shouldCheckDates_whenSmallClaim() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence()
                .build().toBuilder()
                .ccdState(AWAITING_APPLICANT_INTENTION)
                .responseClaimTrack(SpecJourneyConstantLRSpec.SMALL_CLAIM)
                .applicant1DQ(Applicant1DQ.builder()
                                  .applicant1DQSmallClaimHearing(SmallClaimHearing.builder()
                                                                 .build())
                                  .build())
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(MID, caseData)
                .pageId("validate-unavailable-dates")
                .build();

            Mockito.when(unavailableDateValidator.validateSmallClaimsHearing(
                    caseData.getApplicant1DQ().getApplicant1DQSmallClaimHearing()))
                .thenReturn(Collections.emptyList());

            handler.handle(params);

            Mockito.verify(unavailableDateValidator).validateSmallClaimsHearing(
                caseData.getApplicant1DQ().getApplicant1DQSmallClaimHearing());
        }
    }
}

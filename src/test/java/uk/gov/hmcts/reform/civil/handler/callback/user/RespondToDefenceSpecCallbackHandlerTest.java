package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.ExitSurveyConfiguration;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.Document;
import uk.gov.hmcts.reform.civil.model.documents.DocumentType;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;

import java.time.LocalDateTime;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE_SPEC;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;
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
    private Time time;

    @Nested
    class MidStatementOfTruth {

        @Test
        void shouldSetStatementOfTruthFieldsToNull_whenPopulated() {
            String name = "John Smith";
            String role = "Solicitor";

            CaseData caseData = CaseDataBuilder.builder()
                .uiStatementOfTruth(StatementOfTruth.builder().name(name).role(role).build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, "statement-of-truth");
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("uiStatementOfTruth")
                .extracting("name", "role")
                .containsExactly(null, null);
        }
    }
}

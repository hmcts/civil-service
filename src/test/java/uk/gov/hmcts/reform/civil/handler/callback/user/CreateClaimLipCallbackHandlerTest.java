package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.ClaimUrlsConfiguration;
import uk.gov.hmcts.reform.civil.config.MockDatabaseConfiguration;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.repositories.SpecReferenceNumberRepository;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.service.pininpost.DefendantPinToPostLRspecService;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_LIP_CLAIM;

@SpringBootTest(classes = {
    CreateClaimLipCallBackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    ClaimUrlsConfiguration.class,
    MockDatabaseConfiguration.class,
    ValidationAutoConfiguration.class,
    StateFlowEngine.class,
    InterestCalculator.class,
    StateFlowEngine.class,
    },
    properties = {"reference.database.enabled=false"})
class CreateClaimLipCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @MockBean
    private FeatureToggleService toggleService;

    @MockBean
    private SpecReferenceNumberRepository specReferenceNumberRepository;

    @MockBean
    private Time time;

    @MockBean
    private DefendantPinToPostLRspecService defendantPinToPostLRspecService;

    @Autowired
    private CreateClaimLipCallBackHandler handler;

    public static final String REFERENCE_NUMBER = "000MC001";

    @Nested
    class AboutToStatCallback {

        @Test
        void shouldNotHaveErrors_whenAboutToStart() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStatePendingClaimIssued()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class AboutToSubmitCallback {

        private CallbackParams params;
        private CaseData caseData;

        private final LocalDateTime submittedDate = LocalDateTime.now();

        @BeforeEach
        void setup() {
            caseData = CaseDataBuilder.builder().atStateClaimDraft().build();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            given(time.now()).willReturn(submittedDate);
            given(specReferenceNumberRepository.getSpecReferenceNumber()).willReturn(REFERENCE_NUMBER);
            given(deadlinesCalculator.plus28DaysAt4pmDeadline(any())).willReturn(submittedDate);
        }

        @Test
        void shouldAddCaseReferenceSubmittedDateAndAllocatedTrack_whenInvoked() {
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId(CREATE_LIP_CLAIM.name()).build())
                .build();
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            System.out.println(response.getData());
            assertThat(response.getData())
                .containsEntry("legacyCaseReference", REFERENCE_NUMBER)
                .containsEntry("submittedDate", submittedDate.format(DateTimeFormatter.ISO_DATE_TIME));

        }
    }
}

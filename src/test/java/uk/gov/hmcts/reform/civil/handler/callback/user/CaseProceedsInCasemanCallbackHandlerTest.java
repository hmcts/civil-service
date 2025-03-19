package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.cosc.CoscApplicationStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimProceedsInCaseman;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CASE_PROCEEDS_IN_CASEMAN;

@ExtendWith(MockitoExtension.class)
class CaseProceedsInCasemanCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private Time time;

    @Mock
    private FeatureToggleService featureToggleService;

    private CaseProceedsInCasemanCallbackHandler handler;

    @BeforeEach
    void setup() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        ValidatorFactory validatorFactory = Validation.byDefaultProvider()
            .configure()
            .messageInterpolator(new ParameterMessageInterpolator())
            .buildValidatorFactory();

        Validator validator = validatorFactory.getValidator();
        handler = new CaseProceedsInCasemanCallbackHandler(validator, time, objectMapper, featureToggleService);
    }

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseDetails caseDetails = CaseDetailsBuilder.builder().atStateAwaitingRespondentAcknowledgement().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseDetails).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class MidEventTransferDateCallback {

        private static final String PAGE_ID = "transfer-date";

        @Test
        void shouldReturnErrors_whenDateInFuture() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaff()
                .claimProceedsInCaseman(ClaimProceedsInCaseman.builder().date(LocalDate.now().plusDays(1)).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsOnly("The date entered cannot be in the future");
        }

        @Test
        void shouldReturnNoErrors_whenDateInPast() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .claimProceedsInCaseman(ClaimProceedsInCaseman.builder().date(LocalDate.now().minusDays(1)).build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class AboutToSubmit {
        private final LocalDateTime takenOfflineByStaffDate = LocalDateTime.now();

        @BeforeEach
        void setup() {
            when(time.now()).thenReturn(takenOfflineByStaffDate);
        }

        @Test
        void shouldAddTakenOfflineDate_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .containsEntry("takenOfflineByStaffDate", takenOfflineByStaffDate.format(ISO_DATE_TIME));

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent")
                .isEqualTo(CASE_PROCEEDS_IN_CASEMAN.name());

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("status")
                .isEqualTo("READY");
        }

        @Test
        void shouldAddPreviousCaseState_whenInvokedForLipVLipOrLrVLip() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                    .respondent1Represented(YesOrNo.NO)
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            params.getRequest().getCaseDetailsBefore().setState("AWAITING_RESPONDENT_ACKNOWLEDGEMENT");

            AboutToStartOrSubmitCallbackResponse response =
                    (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                    .extracting("previousCCDState").isEqualTo("AWAITING_RESPONDENT_ACKNOWLEDGEMENT");
        }

        @Test
        void shouldNotAddPreviousCaseState_whenInvokedForLipVLipOrLrVLip() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            params.getRequest().getCaseDetailsBefore().setState(null);

            AboutToStartOrSubmitCallbackResponse response =
                    (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                    .extracting("previousCCDState").isNull();
        }

        @Test
        void shouldUpdateCoScApplicationStatusValue_whenInvoked() {
            CaseData caseData = CaseData.builder()
                .respondent1Represented(YesOrNo.NO)
                .coSCApplicationStatus(CoscApplicationStatus.ACTIVE)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(featureToggleService.isCoSCEnabled()).thenReturn(true);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("coSCApplicationStatus")
                .isEqualTo(CoscApplicationStatus.INACTIVE.toString());
        }

        @Test
        void shouldNotUpdateCoScApplicationStatusValue_whenInvoked() {
            CaseData caseData = CaseData.builder()
                .respondent1Represented(YesOrNo.NO)
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(featureToggleService.isCoSCEnabled()).thenReturn(true);

            AboutToStartOrSubmitCallbackResponse response =
                (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("coSCApplicationStatus").isNull();
        }
    }
}

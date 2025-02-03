package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.LocalDate.now;
import static java.time.LocalTime.MIDNIGHT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@ExtendWith(MockitoExtension.class)
class ClaimIssueCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private ClaimIssueCallbackHandler handler;

    @Mock
    private DeadlinesCalculator deadlinesCalculator;

    @Mock
    private ObjectMapper mapper;

    private final LocalDateTime deadline = now().atTime(MIDNIGHT);

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper();
        handler = new ClaimIssueCallbackHandler(mapper, deadlinesCalculator);
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    @Test
    void shouldAddClaimNotificationDeadline_whenClaimIsIssued() {
        CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued()
            .build().toBuilder()
            .respondent1OrganisationIDCopy("")
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        when(deadlinesCalculator.addMonthsToDateAtMidnight(eq(4), any(LocalDate.class)))
            .thenReturn(deadline);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getClaimNotificationDeadline()).isEqualTo(deadline);
        assertThat(updatedData.getNextDeadline()).isEqualTo(deadline.toLocalDate());
    }

    @Test
    void shouldNotThrowException_whenIdCopyIsDefined() {
        CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued()
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        when(deadlinesCalculator.addMonthsToDateAtMidnight(eq(4), any(LocalDate.class)))
            .thenReturn(deadline);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getClaimNotificationDeadline()).isEqualTo(deadline);
        assertThat(updatedData.getNextDeadline()).isEqualTo(deadline.toLocalDate());
    }

    @Test
    void shouldNotThrowException_whenIdCopyIsNotDefined() {
        CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued()
            .build().toBuilder()
            .respondent1OrganisationIDCopy("")
            .build();
        caseData = caseData.toBuilder()
            .respondent1OrganisationPolicy(
                OrganisationPolicy.builder().build()
            )
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        when(deadlinesCalculator.addMonthsToDateAtMidnight(eq(4), any(LocalDate.class)))
            .thenReturn(deadline);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getClaimNotificationDeadline()).isEqualTo(deadline);
        assertThat(updatedData.getNextDeadline()).isEqualTo(deadline.toLocalDate());
    }

    @Test
    void shouldRemoveSubmitterIdOnly() {
        CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().applicantSolicitor1UserDetails(
            IdamUserDetails.builder().id("submitter-id").email("applicantsolicitor@example.com").build()).build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        when(deadlinesCalculator.addMonthsToDateAtMidnight(eq(4), any(LocalDate.class)))
            .thenReturn(deadline);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(updatedData.getApplicantSolicitor1UserDetails().getId()).isNull();
        assertThat(updatedData.getApplicantSolicitor1UserDetails().getEmail())
            .isEqualTo("applicantsolicitor@example.com");
    }

    @Test
    void shouldReturnCorrectActivityId_whenRequested() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        assertThat(handler.camundaActivityId(params)).isEqualTo("IssueClaim");
    }
}

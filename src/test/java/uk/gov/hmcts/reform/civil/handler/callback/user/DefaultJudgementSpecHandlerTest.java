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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.DefaultJudgmentOnlineMapper;
import uk.gov.hmcts.reform.civil.model.CaseData;

import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceLiftInfo;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;

import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;

import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.AddressLinesMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.handler.callback.user.DefaultJudgementSpecHandler.JUDGMENT_GRANTED_HEADER;
import static uk.gov.hmcts.reform.civil.handler.callback.user.DefaultJudgementSpecHandler.JUDGMENT_REQUESTED_HEADER;
import static uk.gov.hmcts.reform.civil.handler.callback.user.DefaultJudgementSpecHandler.JUDGMENT_REQUESTED_LIP_CASE;

import static uk.gov.hmcts.reform.civil.model.Party.Type.INDIVIDUAL;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    DefaultJudgementSpecHandler.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    CaseDetailsConverter.class,
    DefaultJudgmentOnlineMapper.class,
    RoboticsAddressMapper.class,
    AddressLinesMapper.class
})
public class DefaultJudgementSpecHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private DefaultJudgementSpecHandler handler;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private InterestCalculator interestCalculator;

    @MockBean
    private Time time;

    @MockBean
    private FeatureToggleService featureToggleService;
    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @MockBean
    private RoboticsAddressMapper addressMapper;

    @Autowired
    private CaseDetailsConverter caseDetailsConverter;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnError_WhenAboutToStartIsInvoked() {
            BreathingSpaceLiftInfo breathingSpaceLiftInfo = BreathingSpaceLiftInfo.builder()
                .expectedEnd(LocalDate.now().minusDays(5))
                .build();
            BreathingSpaceInfo breathingSpaceInfo = BreathingSpaceInfo.builder()
                .lift(breathingSpaceLiftInfo)
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .breathing(breathingSpaceInfo)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isNotEmpty();
        }

        @Test
        void shouldNotReturnError_WhenAboutToStartIsInvokedOneDefendant() {
            // Given
            BreathingSpaceLiftInfo breathingSpaceLiftInfo = BreathingSpaceLiftInfo.builder()
                .expectedEnd(LocalDate.now().minusDays(5))
                .build();
            BreathingSpaceInfo breathingSpaceInfo = BreathingSpaceInfo.builder()
                .lift(breathingSpaceLiftInfo)
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            caseData.setBreathing(breathingSpaceInfo);
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnDefendantDetails_WhenAboutToStartIsInvokedOneDefendant() {
            // Given
            BreathingSpaceLiftInfo breathingSpaceLiftInfo = BreathingSpaceLiftInfo.builder()
                .expectedEnd(LocalDate.now().minusDays(5))
                .build();
            BreathingSpaceInfo breathingSpaceInfo = BreathingSpaceInfo.builder()
                .lift(breathingSpaceLiftInfo)
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            caseData.setBreathing(breathingSpaceInfo);
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            // When
            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            // Then
            assertThat(response.getErrors()).isEmpty();
            CaseData responseCaseData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(responseCaseData.getDefendantDetailsSpec().getValue().getLabel())
                .isEqualTo(caseData.getRespondent1().getPartyName());
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenInvokedOneDefendant() {
            // Given
            final String body = "<br /><a href=\"/cases/case-details/1594901956117591#Claim documents\" "
                + "target=\"_blank\">Download  default judgment</a> "
                + "%n%n The defendant will be served with the Default Judgment.";
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            // Then
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader("# Default Judgment Granted ")
                    .confirmationBody(format(body))
                    .build());
        }

        @Test
        void shouldReturnJudgementRequestedResponse_whenInvokedOneDefendantWhen1v2() {
            // Given
            final String header = "# Default judgment requested";
            final String body = "A default judgment has been sent to John Smith. "
                + "The claim will now progress offline (on paper)";
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setApplicant1(PartyBuilder.builder().build());
            caseData.setRespondent1(PartyBuilder.builder().build());
            caseData.setRespondent2(PartyBuilder.builder().build());
            caseData.setAddRespondent2(YesOrNo.YES);
            caseData.setRespondent2SameLegalRepresentative(YesOrNo.YES);
            caseData.setDefendantDetailsSpec(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .label("John Smith")
                    .build())
                .build());

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            // Then
            assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                .confirmationHeader(header)
                .confirmationBody(String.format(body))
                .build());
        }

        @Test
        void shouldReturnJudgementRequestedResponse_whenInvokedBothDefendantWhen1v2() {
            // Given
            final String body = "<br /><a href=\"/cases/case-details/1594901956117591#Claim documents\" "
                + "target=\"_blank\">Download  default judgment</a> "
                + "%n%n The defendant will be served the Default Judgment.";
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setApplicant1(PartyBuilder.builder().build());
            caseData.setRespondent1(PartyBuilder.builder().build());
            caseData.setRespondent2(PartyBuilder.builder().build());
            caseData.setAddRespondent2(YesOrNo.YES);
            caseData.setRespondent2SameLegalRepresentative(YesOrNo.YES);
            caseData.setDefendantDetailsSpec(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .label("Both")
                    .build())
                .build());

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            // Then
            assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                .confirmationHeader(
                    "# Default Judgment Granted ")
                .confirmationBody(String.format(body))
                .build());
        }

        @Test
        void shouldReturnJudgementRequestedResponse_whenLrVLip() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setApplicant1(PartyBuilder.builder().build());
            caseData.setRespondent1(PartyBuilder.builder().build());
            caseData.setRespondent1Represented(NO);

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            // Then
            assertThat(caseData.isLRvLipOneVOne()).isTrue();
            assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                .confirmationHeader(
                    JUDGMENT_REQUESTED_HEADER)
                .confirmationBody(String.format(
                    JUDGMENT_REQUESTED_LIP_CASE))
                .build());
        }

        @Test
        void shouldReturnJudgementGrantedResponse_whenisJudgmentLiveTrueAndLrVLip() {
            // Given
            final String body = "<br /><a href=\"/cases/case-details/1594901956117591#Claim documents\" "
                + "target=\"_blank\">Download  default judgment</a> "
                + "%n%n The defendant will be served with the Default Judgment.";
            when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setApplicant1(PartyBuilder.builder().build());
            caseData.setRespondent1(PartyBuilder.builder().build());
            caseData.setRespondent1Represented(NO);

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            // When
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            // Then
            assertThat(caseData.isLRvLipOneVOne()).isTrue();
            assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                .confirmationHeader(
                    JUDGMENT_GRANTED_HEADER)
                .confirmationBody(format(body))
                .build());
        }
    }

    @Test
    void shouldExtendDeadline() {
        // Given
        when(deadlinesCalculator.addMonthsToDateToNextWorkingDayAtMidnight(36, LocalDate.now()))
            .thenReturn(LocalDateTime.now().plusMonths(36));

        Flags respondent1Flags = Flags.builder().partyName("respondent1name").roleOnCase("respondent1").build();
        Party respondent = Party.builder()
            .individualFirstName("Dis")
            .individualLastName("Guy")
            .type(INDIVIDUAL).flags(respondent1Flags).build();

        CaseData caseDataBefore = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .respondent1(respondent).build();
        caseDataBefore.setRespondent1DetailsForClaimDetailsTab(respondent.toBuilder().flags(respondent1Flags).build());
        caseDataBefore.setCaseNameHmctsInternal("Mr. John Rambo v Dis Guy");
        caseDataBefore.setCaseNamePublic("'John Rambo' v 'Dis Guy'");

        when(interestCalculator.calculateInterest(any()))
            .thenReturn(BigDecimal.valueOf(0));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
        caseData.setDefendantDetailsSpec(DynamicList.builder()
            .value(DynamicListElement.builder()
                .label("John Smith")
                .build())
            .build());

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT, caseDataBefore.toMap(mapper));

        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(false);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        Object deadlineValue = response.getData().get("claimDismissedDeadline");
        assertThat(deadlineValue).isNotNull();

        LocalDate expectedDate = LocalDate.now().plusMonths(36);
        LocalDate actualDate = LocalDateTime.parse(deadlineValue.toString()).toLocalDate();

        assertThat(actualDate).isEqualTo(expectedDate);
    }
}

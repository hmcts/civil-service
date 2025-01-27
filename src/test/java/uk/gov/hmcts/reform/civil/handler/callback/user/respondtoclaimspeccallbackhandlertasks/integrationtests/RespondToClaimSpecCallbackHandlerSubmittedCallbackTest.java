package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.integrationtests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.ExitSurveyConfiguration;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.RespondToClaimSpecCallbackHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.RespondToClaimSpecCallbackHandlerTestConfig;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationHeaderSpecGenerator;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationTextSpecGenerator;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.CounterClaimConfirmationText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.FullAdmitAlreadyPaidConfirmationText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.FullAdmitSetDateConfirmationText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.PartialAdmitPaidFullConfirmationText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.PartialAdmitPaidLessConfirmationText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.PartialAdmitPayImmediatelyConfirmationText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.PartialAdmitSetDateConfirmationText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.RepayPlanConfirmationText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.SpecResponse1v2DivergentText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.header.SpecResponse1v2DivergentHeaderText;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.response.confirmation.header.SpecResponse2v1DifferentHeaderText;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.stateflow.simplegrammar.SimpleStateFlowBuilder;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.DQResponseDocumentUtils;
import uk.gov.hmcts.reform.civil.utils.FrcDocumentsUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.PaymentDateValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        RespondToClaimSpecCallbackHandler.class,
        ExitSurveyConfiguration.class,
        ExitSurveyContentService.class,
        JacksonAutoConfiguration.class,
        ValidationAutoConfiguration.class,
        DateOfBirthValidator.class,
        UnavailableDateValidator.class,
        CaseDetailsConverter.class,
        LocationReferenceDataService.class,
        CourtLocationUtils.class,
        SimpleStateFlowEngine.class,
        SimpleStateFlowBuilder.class,
        AssignCategoryId.class,
        FrcDocumentsUtils.class,
        RespondToClaimSpecCallbackHandlerTestConfig.class
})
public class RespondToClaimSpecCallbackHandlerSubmittedCallbackTest extends BaseCallbackHandlerTest {

    @Autowired
    private RespondToClaimSpecCallbackHandler handler;

    @MockBean
    private PaymentDateValidator validator;

    @MockBean
    private PostcodeValidator postcodeValidator;

    @MockBean
    private FeatureToggleService toggleService;

    @MockBean
    private LocationReferenceDataService locationRefDataService;

    @MockBean
    private SimpleStateFlowBuilder simpleStateFlowBuilder;

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @MockBean
    private DQResponseDocumentUtils dqResponseDocumentUtils;

    @MockBean
    private CaseFlagsInitialiser caseFlagsInitialiser;

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @MockBean
    private DeadlineExtensionCalculatorService deadlineExtensionCalculatorService;

    @MockBean
    private Time time;

    @Spy
    private List<RespondToClaimConfirmationTextSpecGenerator> confirmationTextGenerators = List.of(
            new FullAdmitAlreadyPaidConfirmationText(),
            new FullAdmitSetDateConfirmationText(),
            new PartialAdmitPaidFullConfirmationText(),
            new PartialAdmitPaidLessConfirmationText(),
            new PartialAdmitPayImmediatelyConfirmationText(),
            new PartialAdmitSetDateConfirmationText(),
            new RepayPlanConfirmationText(),
            new SpecResponse1v2DivergentText(),
            new RepayPlanConfirmationText(),
            new CounterClaimConfirmationText()
    );

    private final List<RespondToClaimConfirmationHeaderSpecGenerator> confirmationHeaderSpecGenerators = List.of(
            new SpecResponse1v2DivergentHeaderText(),
            new SpecResponse2v1DifferentHeaderText()
    );

    @BeforeEach
    public void setup() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        ReflectionTestUtils.setField(handler, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(handler, "confirmationTextSpecGenerators", confirmationTextGenerators);
        ReflectionTestUtils.setField(handler, "confirmationHeaderGenerators", confirmationHeaderSpecGenerators);
        when(dqResponseDocumentUtils.buildClaimantResponseDocuments(any(CaseData.class))).thenReturn(new ArrayList<>());
    }

    private SubmittedCallbackResponse handleCallback(CaseData caseData) {
        CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
        return (SubmittedCallbackResponse) handler.handle(params);
    }

    private String buildConfirmationHeader(String claimNumber) {
        return format("# You have submitted your response%n## Claim number: %s", claimNumber);
    }

    private String buildConfirmationBody(Object... args) {
        return format("<h2 class=\"govuk-heading-m\">What happens next</h2>%n%nThe claimant has until 4pm on %s to respond to your claim. We will let you know when they respond.%n%n<a href=\"%s\" target=\"_blank\">Download questionnaire (opens in a new tab)</a>", args);
    }

    private String buildDivergentResponseBody() {
        String bodyTemplate = "<br>The defendants have chosen different responses and the claim cannot continue online." +
                "<br>Use form N9A to admit, or form N9B to counterclaim. Do not create a new claim to counterclaim." +
                "%n%n<a href=\"%s\" target=\"_blank\">Download form N9A (opens in a new tab)</a>" +
                "<br><a href=\"%s\" target=\"_blank\">Download form N9B (opens in a new tab)</a>" +
                "<br><br>Post the completed form to:" +
                "<br><br>County Court Business Centre<br>St. Katherine's House" +
                "<br>21-27 St.Katherine Street<br>Northampton<br>NN1 2LH";
        return format(bodyTemplate, "https://www.gov.uk/respond-money-claim", "https://www.gov.uk/respond-money-claim");
    }

    @Test
    void shouldReturnExpectedResponse_whenApplicantIsProceedingWithClaim() {
        CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build();
        SubmittedCallbackResponse response = handleCallback(caseData);
        LocalDateTime responseDeadline = caseData.getApplicant1ResponseDeadline();
        String claimNumber = caseData.getLegacyCaseReference();
        String body = buildConfirmationBody(
                formatLocalDateTime(responseDeadline, DATE),
                format("/cases/case-details/%s#Claim documents", caseData.getCcdCaseReference())
        );
        SubmittedCallbackResponse expected = SubmittedCallbackResponse.builder()
                .confirmationHeader(buildConfirmationHeader(claimNumber))
                .confirmationBody(body)
                .build();
        assertThat(response).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void specificSummary_whenPartialAdmitNotPay() {
        BigDecimal admitted = BigDecimal.valueOf(1000);
        LocalDate whenWillPay = LocalDate.now().plusMonths(1);
        CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
                .respondToAdmittedClaimOwingAmountPounds(admitted)
                .respondToClaimAdmitPartLRspec(
                        RespondToClaimAdmitPartLRspec.builder()
                                .whenWillThisAmountBePaid(whenWillPay)
                                .build()
                )
                .totalClaimAmount(admitted.multiply(BigDecimal.valueOf(2)))
                .build();
        SubmittedCallbackResponse response = handleCallback(caseData);
        assertThat(response.getConfirmationBody())
                .contains(caseData.getApplicant1().getPartyName())
                .contains(admitted.toString())
                .contains(caseData.getTotalClaimAmount().toString())
                .contains(DateFormatHelper.formatLocalDate(whenWillPay, DATE));
    }

    @Test
    void specificSummary_whenPartialAdmitPayImmediately() {
        BigDecimal admitted = BigDecimal.valueOf(1000);
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .defenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY)
                .respondToAdmittedClaimOwingAmountPounds(admitted)
                .respondToClaimAdmitPartLRspec(
                        RespondToClaimAdmitPartLRspec.builder()
                                .whenWillThisAmountBePaid(whenWillPay)
                                .build()
                )
                .build();
        SubmittedCallbackResponse response = handleCallback(caseData);
        assertThat(response.getConfirmationBody())
                .contains(caseData.getApplicant1().getPartyName())
                .contains(DateFormatHelper.formatLocalDate(whenWillPay, DATE));
    }

    @Test
    void specificSummary_whenRepayPlanFullAdmit() {
        CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(FULL_ADMISSION)
                .specDefenceFullAdmittedRequired(YesOrNo.NO)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
                .build();
        SubmittedCallbackResponse response = handleCallback(caseData);
        assertThat(response.getConfirmationBody())
                .contains(caseData.getApplicant1().getPartyName())
                .contains("repayment plan");
    }

    @Test
    void specificSummary_whenRepayPlanPartialAdmit() {
        CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .specDefenceFullAdmittedRequired(YesOrNo.NO)
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN)
                .build();
        SubmittedCallbackResponse response = handleCallback(caseData);
        assertThat(response.getConfirmationBody())
                .contains(caseData.getApplicant1().getPartyName())
                .contains("repayment plan");
    }

    @Test
    void specificSummary_whenFullAdmitAlreadyPaid() {
        CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(FULL_ADMISSION)
                .specDefenceFullAdmittedRequired(YesOrNo.YES)
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .build();
        SubmittedCallbackResponse response = handleCallback(caseData);
        assertThat(response.getConfirmationBody())
                .contains(caseData.getApplicant1().getPartyName())
                .contains(caseData.getTotalClaimAmount().toString())
                .contains("you've paid");
    }

    @Test
    void specificSummary_whenFullAdmitBySetDate() {
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(FULL_ADMISSION)
                .specDefenceFullAdmittedRequired(YesOrNo.NO)
                .totalClaimAmount(BigDecimal.valueOf(1000))
                .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
                .respondToClaimAdmitPartLRspec(
                        RespondToClaimAdmitPartLRspec.builder()
                                .whenWillThisAmountBePaid(whenWillPay)
                                .build()
                )
                .build();
        SubmittedCallbackResponse response = handleCallback(caseData);
        assertThat(response.getConfirmationBody())
                .contains(caseData.getApplicant1().getPartyName())
                .contains(" and your explanation of why you cannot pay before then.")
                .contains(DateFormatHelper.formatLocalDate(whenWillPay, DATE))
                .doesNotContain(caseData.getTotalClaimAmount().toString());
    }

    @Test
    void specificSummary_whenPartialAdmitPaidFull() {
        BigDecimal totalClaimAmount = BigDecimal.valueOf(1000);
        BigDecimal howMuchWasPaid = new BigDecimal(MonetaryConversions.poundsToPennies(totalClaimAmount));
        CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .specDefenceAdmittedRequired(YesOrNo.YES)
                .respondToAdmittedClaim(RespondToClaim.builder().howMuchWasPaid(howMuchWasPaid).build())
                .totalClaimAmount(totalClaimAmount)
                .build();
        SubmittedCallbackResponse response = handleCallback(caseData);
        assertThat(response.getConfirmationBody())
                .contains(caseData.getApplicant1().getPartyName())
                .contains(caseData.getTotalClaimAmount().toString());
    }

    @Test
    void shouldReturnExpectedResponse_when1v2SameSolicitorDivergentResponseFullDefenceFullAdmission() {
        CaseData caseData = CaseDataBuilder.builder()
                .atState1v2SameSolicitorDivergentResponseSpec(FULL_DEFENCE, FULL_ADMISSION)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2SameLegalRepresentative(YES)
                .build();
        SubmittedCallbackResponse response = handleCallback(caseData);
        String claimNumber = caseData.getLegacyCaseReference();
        String body = buildDivergentResponseBody();
        SubmittedCallbackResponse expected = SubmittedCallbackResponse.builder()
                .confirmationHeader(format("# The defendants have chosen their responses%n## Claim number <br>%s", claimNumber))
                .confirmationBody(body)
                .build();
        assertThat(response).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void shouldReturnExpectedResponse_when1v2SameSolicitorDivergentResponsePartAdmissionFullAdmission() {
        CaseData caseData = CaseDataBuilder.builder()
                .atState1v2SameSolicitorDivergentResponseSpec(RespondentResponseTypeSpec.PART_ADMISSION, FULL_ADMISSION)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2SameLegalRepresentative(YES)
                .build();
        SubmittedCallbackResponse response = handleCallback(caseData);
        String claimNumber = caseData.getLegacyCaseReference();
        String body = buildDivergentResponseBody();
        SubmittedCallbackResponse expected = SubmittedCallbackResponse.builder()
                .confirmationHeader(format("# The defendants have chosen their responses%n## Claim number <br>%s", claimNumber))
                .confirmationBody(body)
                .build();
        assertThat(response).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void shouldReturnExpectedResponse_when1v2SameSolicitorDivergentResponseCounterClaimFullAdmission() {
        CaseData caseData = CaseDataBuilder.builder()
                .atState1v2SameSolicitorDivergentResponseSpec(RespondentResponseTypeSpec.COUNTER_CLAIM, FULL_ADMISSION)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2SameLegalRepresentative(YES)
                .build();
        SubmittedCallbackResponse response = handleCallback(caseData);
        String claimNumber = caseData.getLegacyCaseReference();
        String body = buildDivergentResponseBody();
        SubmittedCallbackResponse expected = SubmittedCallbackResponse.builder()
                .confirmationHeader(format("# The defendants have chosen their responses%n## Claim number <br>%s", claimNumber))
                .confirmationBody(body)
                .build();
        assertThat(response).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void shouldReturnExpectedResponse_when1v2SameSolicitorDivergentResponseCounterClaimPartAdmission() {
        CaseData caseData = CaseDataBuilder.builder()
                .atState1v2SameSolicitorDivergentResponseSpec(RespondentResponseTypeSpec.COUNTER_CLAIM, RespondentResponseTypeSpec.PART_ADMISSION)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2SameLegalRepresentative(YES)
                .build();
        SubmittedCallbackResponse response = handleCallback(caseData);
        String claimNumber = caseData.getLegacyCaseReference();
        String body = buildDivergentResponseBody();
        SubmittedCallbackResponse expected = SubmittedCallbackResponse.builder()
                .confirmationHeader(format("# The defendants have chosen their responses%n## Claim number <br>%s", claimNumber))
                .confirmationBody(body)
                .build();
        assertThat(response).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void specificSummary_whenPartialAdmitPaidLess() {
        BigDecimal howMuchWasPaid = BigDecimal.valueOf(1000);
        BigDecimal totalClaimAmount = BigDecimal.valueOf(10000);
        CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .specDefenceAdmittedRequired(YesOrNo.YES)
                .respondToAdmittedClaim(RespondToClaim.builder().howMuchWasPaid(howMuchWasPaid).build())
                .totalClaimAmount(totalClaimAmount)
                .build();
        SubmittedCallbackResponse response = handleCallback(caseData);
        assertThat(response.getConfirmationBody())
                .contains(caseData.getApplicant1().getPartyName())
                .contains("The claim will be settled. We'll contact you when they respond.")
                .contains(MonetaryConversions.penniesToPounds(caseData.getRespondToAdmittedClaim().getHowMuchWasPaid()).toString());
    }

    @Test
    void specificSummary_whenCounterClaim() {
        CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .build().toBuilder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
                .build();
        SubmittedCallbackResponse response = handleCallback(caseData);
        assertThat(response.getConfirmationBody())
                .doesNotContain(caseData.getApplicant1().getPartyName())
                .contains("You've chosen to counterclaim - this means your defence cannot continue online.");
    }
}

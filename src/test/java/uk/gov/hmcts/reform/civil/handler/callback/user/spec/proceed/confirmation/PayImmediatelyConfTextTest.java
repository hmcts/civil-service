package uk.gov.hmcts.reform.civil.handler.callback.user.spec.proceed.confirmation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.config.ClaimUrlsConfiguration;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.PaymentDateService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.mockStatic;

import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;

@ExtendWith(MockitoExtension.class)
class PayImmediatelyConfTextTest {

    @Mock
    private PaymentDateService paymentDateService;

    @Mock
    private ClaimUrlsConfiguration claimUrlsConfiguration;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private PayImmediatelyConfText payImmediatelyConfText;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseDataBuilder.builder()
            .ccdCaseReference(1234567890123456L)
            .build();
    }

    @Test
    void shouldReturnEmpty_WhenDefenceAdmitPartPaymentTimeRouteRequiredIsNull() {
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(null);

        Optional<String> result = payImmediatelyConfText.generateTextFor(caseData, featureToggleService);

        assertThat(result).isEmpty();
        verifyNoInteractions(paymentDateService);
    }

    @Test
    void shouldReturnEmpty_WhenDefenceAdmitPartPaymentTimeRouteRequiredIsNotImmediately() {
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);

        Optional<String> result = payImmediatelyConfText.generateTextFor(caseData, featureToggleService);

        assertThat(result).isEmpty();
        verifyNoInteractions(paymentDateService);
    }

    @Test
    void shouldReturnEmpty_WhenNotFullAdmissionAndNotPartAdmitImmediatePayment() {
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setApplicant1ProceedWithClaim(YesOrNo.YES);

        Optional<String> result = payImmediatelyConfText.generateTextFor(caseData, featureToggleService);

        assertThat(result).isEmpty();
        verifyNoInteractions(paymentDateService);
    }

    @Test
    void shouldReturnEmpty_WhenFullAdmissionButApplicant1ProceedWithClaimIsNotNull() {
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setApplicant1ProceedWithClaim(YesOrNo.YES);

        Optional<String> result = payImmediatelyConfText.generateTextFor(caseData, featureToggleService);

        assertThat(result).isEmpty();
        verifyNoInteractions(paymentDateService);
    }

    @Test
    void shouldReturnEmpty_WhenPartAdmitButRespondForImmediateOptionIsNotYes() {
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY);
        caseData.setRespondForImmediateOption(YesOrNo.NO);

        Optional<String> result = payImmediatelyConfText.generateTextFor(caseData, featureToggleService);

        assertThat(result).isEmpty();
        verifyNoInteractions(paymentDateService);
    }

    @Test
    void shouldThrowException_WhenFormattedPaymentDateIsNull() {
        caseData.setDefenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setApplicant1ProceedWithClaim(null);

        when(paymentDateService.getFormattedPaymentDate(any(CaseData.class))).thenReturn(null);

        assertThatThrownBy(() -> payImmediatelyConfText.generateTextFor(caseData, featureToggleService))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Unable to format the payment date for caseId: 1234567890123456");

        verify(paymentDateService).getFormattedPaymentDate(caseData);
    }

    @Test
    void shouldGenerateTextForFullAdmission_WithLrPayImmediatelyPlan() {
        CaseData caseData1 = caseData;
        caseData1.setDefenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY);
        caseData1.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData1.setApplicant1ProceedWithClaim(null);
        CaseData spyData = spy(caseData1);

        when(paymentDateService.getFormattedPaymentDate(any(CaseData.class))).thenReturn("1 January 2025");

        try (MockedStatic<MultiPartyScenario> mockedStatic = mockStatic(MultiPartyScenario.class)) {
            mockedStatic.when(() -> MultiPartyScenario.isOneVOne(any(CaseData.class))).thenReturn(true);
            when(spyData.isPayImmediately()).thenReturn(true);

            Optional<String> result = payImmediatelyConfText.generateTextFor(spyData, featureToggleService);

            assertThat(result).isPresent();
            assertThat(result.get()).contains("They must make sure you have the money by 1 January 2025");
            assertThat(result.get()).contains("Request Judgment by Admission");
            assertThat(result.get()).contains("Any cheques or transfers should be clear in your account");
            assertThat(result.get()).contains("You need to tell us if");
            assertThat(result.get()).contains("You can settle for less than the full claim amount");
            assertThat(result.get()).contains("If the defendant has not paid you");
            assertThat(result.get()).doesNotContain("N225");
            assertThat(result.get()).doesNotContain("contactocmc@justice.gov.uk");
            verify(paymentDateService).getFormattedPaymentDate(spyData);
        }
    }

    @Test
    void shouldGenerateTextForFullAdmission_WithoutLrPayImmediatelyPlan() {
        CaseData caseData1 = caseData;
        caseData1.setDefenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY);
        caseData1.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData1.setApplicant1ProceedWithClaim(null);
        CaseData spyData = spy(caseData1);

        when(paymentDateService.getFormattedPaymentDate(any(CaseData.class))).thenReturn("1 January 2025");
        when(claimUrlsConfiguration.getN225Link()).thenReturn("http://example.com/n225");

        try (MockedStatic<MultiPartyScenario> mockedStatic = mockStatic(MultiPartyScenario.class)) {
            mockedStatic.when(() -> MultiPartyScenario.isOneVOne(any(CaseData.class))).thenReturn(false);
            when(spyData.isPayImmediately()).thenReturn(true);

            Optional<String> result = payImmediatelyConfText.generateTextFor(spyData, featureToggleService);

            assertThat(result).isPresent();
            assertThat(result.get()).contains("They must make sure you have the money by 1 January 2025");
            assertThat(result.get()).contains("N225");
            assertThat(result.get()).contains("http://example.com/n225");
            assertThat(result.get()).contains("contactocmc@justice.gov.uk");
            assertThat(result.get()).doesNotContain("Request Judgment by Admission");
            verify(paymentDateService).getFormattedPaymentDate(spyData);
            verify(claimUrlsConfiguration).getN225Link();
        }
    }

    @Test
    void shouldGenerateTextForPartAdmit_WithLrPayImmediatelyPlan_WithFiveDaysMessage() {
        CaseData caseData1 = caseData;
        caseData1.setDefenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY);
        caseData1.setRespondForImmediateOption(YesOrNo.YES);
        CaseData spyData = spy(caseData1);

        when(paymentDateService.getFormattedPaymentDate(any(CaseData.class))).thenReturn("1 January 2025");

        try (MockedStatic<MultiPartyScenario> mockedStatic = mockStatic(MultiPartyScenario.class)) {
            mockedStatic.when(() -> MultiPartyScenario.isOneVOne(any(CaseData.class))).thenReturn(true);
            when(spyData.isPartAdmitClaimSpec()).thenReturn(true);
            when(spyData.isPayImmediately()).thenReturn(true);
            when(spyData.isPartAdmitImmediatePaymentClaimSettled()).thenReturn(true);

            Optional<String> result = payImmediatelyConfText.generateTextFor(spyData, featureToggleService);

            assertThat(result).isPresent();
            assertThat(result.get()).contains(
                "They must make sure you have the money within 5 days of the claimant response");
            assertThat(result.get()).contains("Request Judgment by Admission");
            assertThat(result.get()).doesNotContain("by 1 January 2025");
            verify(paymentDateService).getFormattedPaymentDate(spyData);
        }
    }

    @Test
    void shouldGenerateTextForPartAdmit_WithoutLrPayImmediatelyPlan() {
        CaseData caseData1 = caseData;
        caseData1.setDefenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY);
        caseData1.setRespondForImmediateOption(YesOrNo.YES);
        CaseData spyData = spy(caseData1);

        when(paymentDateService.getFormattedPaymentDate(any(CaseData.class))).thenReturn("1 January 2025");
        when(claimUrlsConfiguration.getN225Link()).thenReturn("http://example.com/n225");

        try (MockedStatic<MultiPartyScenario> mockedStatic = mockStatic(MultiPartyScenario.class)) {
            mockedStatic.when(() -> MultiPartyScenario.isOneVOne(any(CaseData.class))).thenReturn(false);
            when(spyData.isPartAdmitClaimSpec()).thenReturn(true);
            when(spyData.isPayImmediately()).thenReturn(true);
            when(spyData.isPartAdmitImmediatePaymentClaimSettled()).thenReturn(true);

            Optional<String> result = payImmediatelyConfText.generateTextFor(spyData, featureToggleService);

            assertThat(result).isPresent();
            assertThat(result.get()).contains("They must make sure you have the money by 1 January 2025");
            assertThat(result.get()).contains("N225");
            assertThat(result.get()).contains("contactocmc@justice.gov.uk");
            verify(paymentDateService).getFormattedPaymentDate(spyData);
            verify(claimUrlsConfiguration).getN225Link();
        }
    }

    @Test
    void shouldGenerateTextForPartAdmit_NotPartAdmitSpec_WithoutLrPayImmediatelyPlan() {
        CaseData caseData1 = caseData;
        caseData1.setDefenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY);
        caseData1.setRespondForImmediateOption(YesOrNo.YES);
        CaseData spyData = spy(caseData1);

        when(paymentDateService.getFormattedPaymentDate(any(CaseData.class))).thenReturn("1 January 2025");
        when(claimUrlsConfiguration.getN225Link()).thenReturn("http://example.com/n225");

        try (MockedStatic<MultiPartyScenario> mockedStatic = mockStatic(MultiPartyScenario.class)) {
            mockedStatic.when(() -> MultiPartyScenario.isOneVOne(any(CaseData.class))).thenReturn(false);
            when(spyData.isPartAdmitClaimSpec()).thenReturn(false);
            when(spyData.isPayImmediately()).thenReturn(true);
            when(spyData.isPartAdmitImmediatePaymentClaimSettled()).thenReturn(true);

            Optional<String> result = payImmediatelyConfText.generateTextFor(spyData, featureToggleService);

            assertThat(result).isPresent();
            assertThat(result.get()).contains("They must make sure you have the money by 1 January 2025");
            assertThat(result.get()).doesNotContain("within 5 days");
            verify(paymentDateService).getFormattedPaymentDate(spyData);
        }
    }

    @Test
    void shouldGenerateText_WhenPayImmediatelyFalseButOneVOneTrue() {
        CaseData caseData1 = caseData;
        caseData1.setDefenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY);
        caseData1.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData1.setApplicant1ProceedWithClaim(null);
        CaseData spyData = spy(caseData1);

        when(paymentDateService.getFormattedPaymentDate(any(CaseData.class))).thenReturn("1 January 2025");
        when(claimUrlsConfiguration.getN225Link()).thenReturn("http://example.com/n225");

        try (MockedStatic<MultiPartyScenario> mockedStatic = mockStatic(MultiPartyScenario.class)) {
            mockedStatic.when(() -> MultiPartyScenario.isOneVOne(any(CaseData.class))).thenReturn(true);
            when(spyData.isPayImmediately()).thenReturn(false);

            Optional<String> result = payImmediatelyConfText.generateTextFor(spyData, featureToggleService);

            assertThat(result).isPresent();
            assertThat(result.get()).contains("N225");
            assertThat(result.get()).contains("contactocmc@justice.gov.uk");
            verify(claimUrlsConfiguration).getN225Link();
        }
    }

    @Test
    void shouldGenerateText_WhenPayImmediatelyTrueButOneVOneFalse() {
        CaseData caseData1 = caseData;
        caseData1.setDefenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY);
        caseData1.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData1.setApplicant1ProceedWithClaim(null);
        CaseData spyData = spy(caseData1);

        when(paymentDateService.getFormattedPaymentDate(any(CaseData.class))).thenReturn("1 January 2025");
        when(claimUrlsConfiguration.getN225Link()).thenReturn("http://example.com/n225");

        try (MockedStatic<MultiPartyScenario> mockedStatic = mockStatic(MultiPartyScenario.class)) {
            mockedStatic.when(() -> MultiPartyScenario.isOneVOne(any(CaseData.class))).thenReturn(false);
            when(spyData.isPayImmediately()).thenReturn(true);

            Optional<String> result = payImmediatelyConfText.generateTextFor(spyData, featureToggleService);

            assertThat(result).isPresent();
            assertThat(result.get()).contains("N225");
            assertThat(result.get()).contains("contactocmc@justice.gov.uk");
            verify(claimUrlsConfiguration).getN225Link();
        }
    }

    @Test
    void testIsLrPayImmediatelyPlan_WhenBothConditionsTrue() {
        CaseData spyData = spy(caseData);
        try (MockedStatic<MultiPartyScenario> mockedStatic = mockStatic(MultiPartyScenario.class)) {
            mockedStatic.when(() -> MultiPartyScenario.isOneVOne(any(CaseData.class))).thenReturn(true);
            when(spyData.isPayImmediately()).thenReturn(true);

            boolean result = payImmediatelyConfText.isLrPayImmediatelyPlan(spyData, featureToggleService);

            assertThat(result).isTrue();
        }
    }

    @Test
    void testIsLrPayImmediatelyPlan_WhenPayImmediatelyFalse() {
        CaseData spyData = spy(caseData);
        try (MockedStatic<MultiPartyScenario> mockedStatic = mockStatic(MultiPartyScenario.class)) {
            mockedStatic.when(() -> MultiPartyScenario.isOneVOne(any(CaseData.class))).thenReturn(true);
            when(spyData.isPayImmediately()).thenReturn(false);

            boolean result = payImmediatelyConfText.isLrPayImmediatelyPlan(spyData, featureToggleService);

            assertThat(result).isFalse();
        }
    }

    @Test
    void testIsLrPayImmediatelyPlan_WhenOneVOneFalse() {
        CaseData spyData = spy(caseData);
        try (MockedStatic<MultiPartyScenario> mockedStatic = mockStatic(MultiPartyScenario.class)) {
            mockedStatic.when(() -> MultiPartyScenario.isOneVOne(any(CaseData.class))).thenReturn(false);
            when(spyData.isPayImmediately()).thenReturn(true);

            boolean result = payImmediatelyConfText.isLrPayImmediatelyPlan(spyData, featureToggleService);

            assertThat(result).isFalse();
        }
    }

    @Test
    void testIsLrPayImmediatelyPlan_WhenBothConditionsFalse() {
        CaseData spyData = spy(caseData);
        try (MockedStatic<MultiPartyScenario> mockedStatic = mockStatic(MultiPartyScenario.class)) {
            mockedStatic.when(() -> MultiPartyScenario.isOneVOne(any(CaseData.class))).thenReturn(false);
            when(spyData.isPayImmediately()).thenReturn(false);

            boolean result = payImmediatelyConfText.isLrPayImmediatelyPlan(spyData, featureToggleService);

            assertThat(result).isFalse();
        }
    }
}

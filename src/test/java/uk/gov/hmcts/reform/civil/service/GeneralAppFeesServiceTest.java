package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.client.FeesApiClient;
import uk.gov.hmcts.reform.civil.config.GeneralAppFeesConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.FeeLookupResponseDto;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDateGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.OTHER;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.SET_ASIDE_JUDGEMENT;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.VARY_ORDER;

@ExtendWith(MockitoExtension.class)
class GeneralAppFeesServiceTest {

    private static final String APPLICATION_TO_VARY_OR_SUSPEND = "AppnToVaryOrSuspend";
    private static final String CERT_OF_SATISFACTION_OR_CANCEL = "CertificateOfSorC";
    private static final String GENERAL_APP_WITHOUT_NOTICE = "GeneralAppWithoutNotice";
    private static final String GENERAL_APPLICATION_WITH_NOTICE = "GAOnNotice";
    private static final String CERTIFICATE_OF_SATISFACTION = "CoS";
    public static final String DEFAULT_CHANNEL = "default";
    public static final String CIVIL_JURISDICTION = "civil";
    public static final String TEST_FEE_CODE = "test_fee_code";

    private static final BigDecimal TEST_FEE_AMOUNT_POUNDS = new BigDecimal("108.00");
    private static final BigDecimal TEST_FEE_AMOUNT_PENCE = new BigDecimal(TEST_FEE_AMOUNT_POUNDS.intValue() * 100);
    private static final FeeLookupResponseDto FEE_POUNDS = new FeeLookupResponseDto()
        .setFeeAmount(TEST_FEE_AMOUNT_POUNDS).setCode(TEST_FEE_CODE).setVersion(1);
    private static final Fee FEE_PENCE;
    public static final String FREE_REF = "FREE";
    private static final Fee FEE_PENCE_0;

    static {
        FEE_PENCE = new Fee();
        FEE_PENCE.setCalculatedAmountInPence(TEST_FEE_AMOUNT_PENCE);
        FEE_PENCE.setCode(TEST_FEE_CODE);
        FEE_PENCE.setVersion("1");

        FEE_PENCE_0 = new Fee();
        FEE_PENCE_0.setCalculatedAmountInPence(BigDecimal.ZERO);
        FEE_PENCE_0.setCode(FREE_REF);
        FEE_PENCE_0.setVersion("1");
    }

    public static final String GENERAL_APPLICATION = "general application";
    public static final String GENERAL_SERVICE = "general";

    @Captor
    private ArgumentCaptor<String> keywordCaptor;

    @Mock
    private FeesApiClient feesApiClient;

    @Mock
    private GeneralAppFeesConfiguration feesConfiguration;

    @InjectMocks
    private GeneralAppFeesService generalAppFeesService;

    @Nested
    class FeeForJOWithApplicationType {
        @ParameterizedTest
        @CsvSource({
            "STRIKE_OUT",
            "SUMMARY_JUDGEMENT",
            "STAY_THE_CLAIM",
            "EXTEND_TIME",
            "AMEND_A_STMT_OF_CASE",
            "RELIEF_FROM_SANCTIONS",
            "PROCEEDS_IN_HERITAGE",
            "UNLESS_ORDER",
            "VARY_PAYMENT_TERMS_OF_JUDGMENT",
            "CONFIRM_CCJ_DEBT_PAID",
            "ADJOURN_HEARING"
        })
        void shouldReturnNullForUnmatchedApplicationType(GeneralApplicationTypes applicationType) {
            Fee fee = generalAppFeesService.getFeeForJOWithApplicationType(applicationType);
            assertThat(fee).isNull();
            verify(feesConfiguration, times(0)).getChannel();
            verify(feesConfiguration, times(0)).getJurisdiction1();
            verify(feesConfiguration, times(0)).getJurisdiction2();
            verify(feesConfiguration, times(0)).getWithNoticeKeyword();
            verify(feesApiClient, times(0)).lookupFee(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
            );
        }

        @Test
        void shouldThrowsExceptionWhenFeeAmountReturnedIsNull() {
            when(feesConfiguration.getChannel()).thenReturn(DEFAULT_CHANNEL);
            when(feesConfiguration.getJurisdiction1()).thenReturn(CIVIL_JURISDICTION);
            when(feesConfiguration.getJurisdiction2()).thenReturn(CIVIL_JURISDICTION);
            when(feesConfiguration.getWithNoticeKeyword()).thenReturn(GENERAL_APPLICATION_WITH_NOTICE);
            FeeLookupResponseDto feeLookupResponse = new FeeLookupResponseDto()
                .setCode(TEST_FEE_CODE).setVersion(1);
            when(feesApiClient.lookupFee(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(feeLookupResponse);

            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                generalAppFeesService.getFeeForJOWithApplicationType(GeneralApplicationTypes.SET_ASIDE_JUDGEMENT)
            );
            assertThat(exception.getMessage()).isEqualTo(
                "No Fees returned by fee-service while creating General Application");
        }

        @Test
        void shouldThrowsExceptionWhenFeeServiceReturnsNull() {
            when(feesConfiguration.getChannel()).thenReturn(DEFAULT_CHANNEL);
            when(feesConfiguration.getJurisdiction1()).thenReturn(CIVIL_JURISDICTION);
            when(feesConfiguration.getJurisdiction2()).thenReturn(CIVIL_JURISDICTION);
            when(feesConfiguration.getWithNoticeKeyword()).thenReturn(GENERAL_APPLICATION_WITH_NOTICE);
            when(feesApiClient.lookupFee(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(null);

            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                generalAppFeesService.getFeeForJOWithApplicationType(GeneralApplicationTypes.SET_ASIDE_JUDGEMENT)
            );
            assertThat(exception.getMessage()).isEqualTo(
                "No Fees returned by fee-service while creating General Application");
        }

        static Stream<Arguments> joWithApplicationTypedData() {
            return Stream.of(
                Arguments.of(GeneralApplicationTypes.VARY_ORDER, APPLICATION_TO_VARY_OR_SUSPEND, FEE_PENCE),
                Arguments.of(GeneralApplicationTypes.SET_ASIDE_JUDGEMENT, GENERAL_APPLICATION_WITH_NOTICE, FEE_PENCE),
                Arguments.of(GeneralApplicationTypes.OTHER, CERTIFICATE_OF_SATISFACTION, FEE_PENCE)
            );
        }

        @ParameterizedTest
        @MethodSource(
            "joWithApplicationTypedData"
        )
        void shouldReturnFeeForCorrectApplicationType(GeneralApplicationTypes generalApplicationTypes, String keyword, Fee expectedFee) {
            //Given
            when(feesConfiguration.getChannel()).thenReturn(DEFAULT_CHANNEL);
            when(feesConfiguration.getJurisdiction1()).thenReturn(CIVIL_JURISDICTION);
            when(feesConfiguration.getJurisdiction2()).thenReturn(CIVIL_JURISDICTION);

            if (Objects.requireNonNull(generalApplicationTypes) == VARY_ORDER) {
                when(feesConfiguration.getAppnToVaryOrSuspend()).thenReturn(APPLICATION_TO_VARY_OR_SUSPEND);
            } else if (generalApplicationTypes == SET_ASIDE_JUDGEMENT) {
                when(feesConfiguration.getWithNoticeKeyword()).thenReturn(GENERAL_APPLICATION_WITH_NOTICE);
            } else if (generalApplicationTypes == OTHER) {
                when(feesConfiguration.getCertificateOfSatisfaction()).thenReturn(CERTIFICATE_OF_SATISFACTION);
            }

            when(feesApiClient.lookupFee(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                eq(keyword)
            )).thenReturn(FEE_POUNDS);
            //When
            Fee feeForVaryOrder = generalAppFeesService.getFeeForJOWithApplicationType(generalApplicationTypes);
            //Then
            assertThat(feeForVaryOrder).isEqualTo(expectedFee);
        }
    }

    @Nested
    class FeeForGALiP {

        @BeforeEach
        void setup() {
            when(feesApiClient.lookupFee(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                keywordCaptor.capture()
            )).thenReturn(FEE_POUNDS);
            when(feesConfiguration.getService()).thenReturn(GENERAL_SERVICE);
            when(feesConfiguration.getChannel()).thenReturn(DEFAULT_CHANNEL);
            when(feesConfiguration.getJurisdiction1()).thenReturn(CIVIL_JURISDICTION);
            when(feesConfiguration.getJurisdiction2()).thenReturn(CIVIL_JURISDICTION);
            when(feesConfiguration.getEvent()).thenReturn(GENERAL_APPLICATION);
        }

        @ParameterizedTest
        @CsvSource({
            "false, false",
            "false, true",
        })
        void default_types_should_pay_ForGALiP(Boolean isWithConsent, Boolean isWithNotice) {

            if (isWithNotice) {
                when(feesConfiguration.getWithNoticeKeyword()).thenReturn(GENERAL_APPLICATION_WITH_NOTICE);
            } else {
                when(feesConfiguration.getConsentedOrWithoutNoticeKeyword()).thenReturn(GENERAL_APP_WITHOUT_NOTICE);
            }

            List<GeneralApplicationTypes> allTypes = getGADefaultTypes();

            //single
            for (GeneralApplicationTypes generalApplicationType : allTypes) {
                Fee feeDto = generalAppFeesService.getFeeForGALiP(List.of(generalApplicationType), isWithConsent, isWithNotice, null);
                assertThat(feeDto).isEqualTo(FEE_PENCE);
            }
            //mix
            Fee feeDto = generalAppFeesService.getFeeForGALiP(allTypes, isWithConsent, isWithNotice, null);
            assertThat(feeDto).isEqualTo(FEE_PENCE);
        }
    }

    @Test
    void shouldReturnFeeData_whenCertificateOfSatisfactionOrCancelRequested() {
        when(feesApiClient.lookupFee(
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            keywordCaptor.capture()
        )).thenReturn(FEE_POUNDS);
        when(feesConfiguration.getChannel()).thenReturn(DEFAULT_CHANNEL);
        when(feesConfiguration.getJurisdiction1()).thenReturn(CIVIL_JURISDICTION);
        when(feesConfiguration.getJurisdiction2()).thenReturn(CIVIL_JURISDICTION);

        when(feesConfiguration.getCertificateOfSatisfaction()).thenReturn(CERT_OF_SATISFACTION_OR_CANCEL);

        Fee feeDto = generalAppFeesService.getFeeForGALiP(
            GeneralAppFeesService.CONFIRM_YOU_PAID_CCJ_DEBT,
            false,
            false,
            null
        );

        assertThat(feeDto).isEqualTo(FEE_PENCE);
        assertThat(keywordCaptor.getValue())
            .hasToString(CERT_OF_SATISFACTION_OR_CANCEL);
    }

    @Nested
    class FeeForGA {

        @ParameterizedTest
        @CsvSource({
            "GAOnNotice, NO, YES",
            "GeneralAppWithoutNotice, NO, NO"
        })
        void default_types_should_pay_ForGA(String noticeType, YesOrNo hasAgreed, YesOrNo isWithNotice) {
            when(feesApiClient.lookupFee(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                keywordCaptor.capture()
            )).thenReturn(FEE_POUNDS);
            when(feesConfiguration.getService()).thenReturn(GENERAL_SERVICE);
            when(feesConfiguration.getChannel()).thenReturn(DEFAULT_CHANNEL);
            when(feesConfiguration.getJurisdiction1()).thenReturn(CIVIL_JURISDICTION);
            when(feesConfiguration.getJurisdiction2()).thenReturn(CIVIL_JURISDICTION);
            when(feesConfiguration.getEvent()).thenReturn(GENERAL_APPLICATION);

            if (GENERAL_APPLICATION_WITH_NOTICE.equals(noticeType)) {
                when(feesConfiguration.getWithNoticeKeyword()).thenReturn(GENERAL_APPLICATION_WITH_NOTICE);
            } else {
                when(feesConfiguration.getConsentedOrWithoutNoticeKeyword()).thenReturn(GENERAL_APP_WITHOUT_NOTICE);
            }
            List<GeneralApplicationTypes> allTypes = getGADefaultTypes();
            //single
            for (GeneralApplicationTypes generalApplicationType : allTypes) {
                CaseData caseData = getFeeCase(
                    List.of(generalApplicationType), hasAgreed, isWithNotice, null);
                Fee feeDto = generalAppFeesService.getFeeForGA(caseData);
                assertThat(feeDto).isEqualTo(FEE_PENCE);
            }
            //mix
            CaseData caseData = getFeeCase(
                allTypes, hasAgreed, isWithNotice, null);
            Fee feeDto = generalAppFeesService.getFeeForGA(caseData);
            assertThat(feeDto).isEqualTo(FEE_PENCE);
            assertThat(keywordCaptor.getValue())
                .hasToString(noticeType);
        }

        static Stream<Arguments> adjourn_with_hearingScheduledDate_outside_14daysData() {
            return Stream.of(
                Arguments.of(GeneralApplicationTypes.ADJOURN_HEARING, YesOrNo.YES, YesOrNo.YES, 15, FEE_PENCE_0),
                Arguments.of(GeneralApplicationTypes.ADJOURN_HEARING, YesOrNo.YES, YesOrNo.NO, 15, FEE_PENCE_0)
            );
        }

        @ParameterizedTest
        @MethodSource(
            "adjourn_with_hearingScheduledDate_outside_14daysData"
        )
        void adjourn_with_hearingScheduledDate_outside_14days_should_pay_no_fee(GeneralApplicationTypes generalApplicationTypes,
                                                                                YesOrNo hasAgreed,
                                                                                YesOrNo isWithNotice,
                                                                                Integer daysToAdd,
                                                                                Fee expectedFee) {

            CaseData caseData = getFeeCase(
                List.of(generalApplicationTypes),
                hasAgreed, isWithNotice, LocalDate.now().plusDays(daysToAdd)
            );
            Fee feeForGA = generalAppFeesService.getFeeForGA(caseData);
            assertThat(feeForGA)
                .isEqualTo(expectedFee);
        }

        static Stream<Arguments> generateDefaultTypesData() {
            return Stream.of(
                Arguments.of(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT, YesOrNo.YES, YesOrNo.YES, -1, FEE_PENCE),
                Arguments.of(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT, YesOrNo.NO, YesOrNo.NO, -1, FEE_PENCE),
                Arguments.of(GeneralApplicationTypes.SETTLE_BY_CONSENT, YesOrNo.YES, YesOrNo.YES, -1, FEE_PENCE),
                Arguments.of(GeneralApplicationTypes.SET_ASIDE_JUDGEMENT, YesOrNo.NO, YesOrNo.YES, -1, FEE_PENCE),
                Arguments.of(GeneralApplicationTypes.SET_ASIDE_JUDGEMENT, YesOrNo.NO, YesOrNo.NO, -1, FEE_PENCE),
                Arguments.of(GeneralApplicationTypes.ADJOURN_HEARING, YesOrNo.NO, YesOrNo.YES, 1, FEE_PENCE),
                Arguments.of(GeneralApplicationTypes.ADJOURN_HEARING, YesOrNo.NO, YesOrNo.NO, 1, FEE_PENCE),
                Arguments.of(GeneralApplicationTypes.ADJOURN_HEARING, YesOrNo.NO, YesOrNo.NO, 15, FEE_PENCE)
            );
        }

        @ParameterizedTest
        @MethodSource(
            "generateDefaultTypesData"
        )
        void default_types_should_pay(GeneralApplicationTypes generalApplicationTypes, YesOrNo hasAgreed, YesOrNo isWithNotice, Integer daysToAdd, Fee expectedFee) {
            when(feesApiClient.lookupFee(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                keywordCaptor.capture()
            )).thenReturn(FEE_POUNDS);

            when(feesConfiguration.getChannel()).thenReturn(DEFAULT_CHANNEL);
            when(feesConfiguration.getJurisdiction1()).thenReturn(CIVIL_JURISDICTION);
            when(feesConfiguration.getJurisdiction2()).thenReturn(CIVIL_JURISDICTION);

            if (generalApplicationTypes == GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT) {
                when(feesConfiguration.getAppnToVaryOrSuspend()).thenReturn(APPLICATION_TO_VARY_OR_SUSPEND);
            } else {
                when(feesConfiguration.getService()).thenReturn(GENERAL_SERVICE);
                when(feesConfiguration.getEvent()).thenReturn(GENERAL_APPLICATION);
            }

            if (generalApplicationTypes == GeneralApplicationTypes.SETTLE_BY_CONSENT) {
                when(feesConfiguration.getConsentedOrWithoutNoticeKeyword()).thenReturn(GENERAL_APP_WITHOUT_NOTICE);
            } else if (generalApplicationTypes == GeneralApplicationTypes.SET_ASIDE_JUDGEMENT && hasAgreed == YesOrNo.NO) {
                if (isWithNotice == YesOrNo.YES) {
                    when(feesConfiguration.getWithNoticeKeyword()).thenReturn(GENERAL_APPLICATION_WITH_NOTICE);
                } else if (isWithNotice == YesOrNo.NO) {
                    when(feesConfiguration.getConsentedOrWithoutNoticeKeyword()).thenReturn(GENERAL_APP_WITHOUT_NOTICE);
                }
            } else if (generalApplicationTypes == GeneralApplicationTypes.ADJOURN_HEARING) {
                if (isWithNotice == YesOrNo.YES) {
                    when(feesConfiguration.getWithNoticeKeyword()).thenReturn(GENERAL_APPLICATION_WITH_NOTICE);
                } else if (isWithNotice == YesOrNo.NO) {
                    when(feesConfiguration.getConsentedOrWithoutNoticeKeyword()).thenReturn(GENERAL_APP_WITHOUT_NOTICE);
                }
            }

            LocalDate noOfDays = daysToAdd > 0 ? LocalDate.now().plusDays(daysToAdd) : null;
            CaseData caseDataWithNotice = getFeeCase(
                List.of(generalApplicationTypes),
                hasAgreed, isWithNotice, noOfDays
            );
            Fee feeDto = generalAppFeesService.getFeeForGA(caseDataWithNotice);
            assertThat(feeDto).isEqualTo(expectedFee);
        }

        static Stream<Arguments> mixDefaultTypesData() {
            return Stream.of(
                Arguments.of(GeneralApplicationTypes.SET_ASIDE_JUDGEMENT, YesOrNo.YES, YesOrNo.YES, 15, FEE_PENCE),
                Arguments.of(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT, YesOrNo.YES, YesOrNo.YES, 15, FEE_PENCE)
            );
        }

        @ParameterizedTest
        @MethodSource(
            "mixDefaultTypesData"
        )
        void mix_default_types_should_be_charged_a_fee(GeneralApplicationTypes generalApplicationTypes,
                                                       YesOrNo hasAgreed,
                                                       YesOrNo isWithNotice,
                                                       Integer daysToAdd,
                                                       Fee expectedFee) {

            when(feesApiClient.lookupFee(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                keywordCaptor.capture()
            ))
                .thenReturn(FEE_POUNDS);
            when(feesConfiguration.getService()).thenReturn(GENERAL_SERVICE);
            when(feesConfiguration.getChannel()).thenReturn(DEFAULT_CHANNEL);
            when(feesConfiguration.getJurisdiction1()).thenReturn(CIVIL_JURISDICTION);
            when(feesConfiguration.getJurisdiction2()).thenReturn(CIVIL_JURISDICTION);
            when(feesConfiguration.getEvent()).thenReturn(GENERAL_APPLICATION);
            when(feesConfiguration.getConsentedOrWithoutNoticeKeyword()).thenReturn(GENERAL_APP_WITHOUT_NOTICE);

            if (generalApplicationTypes == GeneralApplicationTypes.SET_ASIDE_JUDGEMENT && hasAgreed == YesOrNo.NO) {
                when(feesConfiguration.getWithNoticeKeyword()).thenReturn(GENERAL_APPLICATION_WITH_NOTICE);
            } else if (generalApplicationTypes == GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT) {
                when(feesConfiguration.getAppnToVaryOrSuspend()).thenReturn(APPLICATION_TO_VARY_OR_SUSPEND);
            }

            LocalDate noOfDays = daysToAdd > 0 ? LocalDate.now().plusDays(daysToAdd) : null;
            List<GeneralApplicationTypes> randomList = getGADefaultTypes();
            randomList.add(generalApplicationTypes);
            CaseData caseDataOutside14Days = getFeeCase(
                randomList,
                hasAgreed, isWithNotice, noOfDays
            );
            assertThat(generalAppFeesService.getFeeForGA(caseDataOutside14Days))
                .isEqualTo(expectedFee);
            List<String> keywords = keywordCaptor.getAllValues();
            assertThat(keywords).contains(GENERAL_APP_WITHOUT_NOTICE);
            if (generalApplicationTypes == GeneralApplicationTypes.SET_ASIDE_JUDGEMENT && hasAgreed == YesOrNo.NO) {
                assertThat(keywords).contains(GENERAL_APPLICATION_WITH_NOTICE);
            } else if (generalApplicationTypes == GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT) {
                assertThat(keywords).contains(APPLICATION_TO_VARY_OR_SUSPEND);
            }
        }

        static Stream<Arguments> adjourn_with_hearingScheduledDate_outside_14daysDataGeneralApplication() {
            return Stream.of(
                Arguments.of(GeneralApplicationTypes.ADJOURN_HEARING, YesOrNo.YES, YesOrNo.YES, 15, FEE_PENCE_0),
                Arguments.of(GeneralApplicationTypes.ADJOURN_HEARING, YesOrNo.YES, YesOrNo.NO, 15, FEE_PENCE_0)
            );
        }

        static Stream<Arguments> generateDefaultTypesDataGeneralApplication() {
            return Stream.of(
                Arguments.of(
                    GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT,
                    YesOrNo.YES,
                    YesOrNo.YES,
                    -1,
                    FEE_PENCE
                ),
                Arguments.of(
                    GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT,
                    YesOrNo.NO,
                    YesOrNo.NO,
                    -1,
                    FEE_PENCE
                ),
                Arguments.of(GeneralApplicationTypes.SETTLE_BY_CONSENT, YesOrNo.YES, YesOrNo.YES, -1, FEE_PENCE),
                Arguments.of(GeneralApplicationTypes.SET_ASIDE_JUDGEMENT, YesOrNo.YES, YesOrNo.YES, -1, FEE_PENCE),
                Arguments.of(GeneralApplicationTypes.SET_ASIDE_JUDGEMENT, YesOrNo.NO, YesOrNo.YES, -1, FEE_PENCE),
                Arguments.of(GeneralApplicationTypes.ADJOURN_HEARING, YesOrNo.NO, YesOrNo.YES, 1, FEE_PENCE),
                Arguments.of(GeneralApplicationTypes.ADJOURN_HEARING, YesOrNo.NO, YesOrNo.NO, 1, FEE_PENCE),
                Arguments.of(GeneralApplicationTypes.ADJOURN_HEARING, YesOrNo.NO, YesOrNo.NO, 15, FEE_PENCE)
            );
        }

        @ParameterizedTest
        @CsvSource({
            "GAOnNotice, NO, YES",
            "GeneralAppWithoutNotice, NO, NO"
        })
        void default_types_should_pay_ForGA_using_general_application_case_data(String noticeType, YesOrNo hasAgreed, YesOrNo isWithNotice) {
            when(feesApiClient.lookupFee(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                keywordCaptor.capture()
            )).thenReturn(FEE_POUNDS);
            when(feesConfiguration.getService()).thenReturn(GENERAL_SERVICE);
            when(feesConfiguration.getChannel()).thenReturn(DEFAULT_CHANNEL);
            when(feesConfiguration.getJurisdiction1()).thenReturn(CIVIL_JURISDICTION);
            when(feesConfiguration.getJurisdiction2()).thenReturn(CIVIL_JURISDICTION);
            when(feesConfiguration.getEvent()).thenReturn(GENERAL_APPLICATION);

            if (GENERAL_APPLICATION_WITH_NOTICE.equals(noticeType)) {
                when(feesConfiguration.getWithNoticeKeyword()).thenReturn(GENERAL_APPLICATION_WITH_NOTICE);
            } else {
                when(feesConfiguration.getConsentedOrWithoutNoticeKeyword()).thenReturn(GENERAL_APP_WITHOUT_NOTICE);
            }
            List<GeneralApplicationTypes> allTypes = getGADefaultTypes();
            //single
            for (GeneralApplicationTypes generalApplicationType : allTypes) {
                GeneralApplicationCaseData caseData = getFeeCaseGeneralApplication(
                    List.of(generalApplicationType), hasAgreed, isWithNotice, null);
                Fee feeDto = generalAppFeesService.getFeeForGA(caseData);
                assertThat(feeDto).isEqualTo(FEE_PENCE);
            }
            //mix
            GeneralApplicationCaseData caseData = getFeeCaseGeneralApplication(
                allTypes, hasAgreed, isWithNotice, null);
            Fee feeDto = generalAppFeesService.getFeeForGA(caseData);
            assertThat(feeDto).isEqualTo(FEE_PENCE);
            assertThat(keywordCaptor.getValue())
                .hasToString(noticeType);
        }

        @ParameterizedTest
        @MethodSource(
            "adjourn_with_hearingScheduledDate_outside_14daysDataGeneralApplication"
        )
        void adjourn_with_hearingScheduledDate_outside_14days_should_pay_no_fee_using_general_application(GeneralApplicationTypes generalApplicationTypes,
                                                                                                          YesOrNo hasAgreed,
                                                                                                          YesOrNo isWithNotice,
                                                                                                          Integer daysToAdd,
                                                                                                          Fee expectedFee) {

            GeneralApplicationCaseData caseData = getFeeCaseGeneralApplication(
                List.of(generalApplicationTypes),
                hasAgreed, isWithNotice, LocalDate.now().plusDays(daysToAdd)
            );
            Fee feeForGA = generalAppFeesService.getFeeForGA(caseData);
            assertThat(feeForGA)
                .isEqualTo(expectedFee);
        }

        @ParameterizedTest
        @MethodSource(
            "generateDefaultTypesDataGeneralApplication"
        )
        void default_types_should_pay_general_using_application_case_data(GeneralApplicationTypes generalApplicationTypes,
                                                                          YesOrNo hasAgreed,
                                                                          YesOrNo isWithNotice,
                                                                          Integer daysToAdd,
                                                                          Fee expectedFee) {
            when(feesApiClient.lookupFee(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                keywordCaptor.capture()
            )).thenReturn(FEE_POUNDS);

            when(feesConfiguration.getChannel()).thenReturn(DEFAULT_CHANNEL);
            when(feesConfiguration.getJurisdiction1()).thenReturn(CIVIL_JURISDICTION);
            when(feesConfiguration.getJurisdiction2()).thenReturn(CIVIL_JURISDICTION);

            if (generalApplicationTypes == GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT) {
                when(feesConfiguration.getAppnToVaryOrSuspend()).thenReturn(APPLICATION_TO_VARY_OR_SUSPEND);
            } else {
                when(feesConfiguration.getService()).thenReturn(GENERAL_SERVICE);
                when(feesConfiguration.getEvent()).thenReturn(GENERAL_APPLICATION);
            }

            if (generalApplicationTypes == GeneralApplicationTypes.SETTLE_BY_CONSENT) {
                when(feesConfiguration.getConsentedOrWithoutNoticeKeyword()).thenReturn(GENERAL_APP_WITHOUT_NOTICE);
            } else if (generalApplicationTypes == GeneralApplicationTypes.SET_ASIDE_JUDGEMENT && hasAgreed == YesOrNo.NO) {
                if (isWithNotice == YesOrNo.YES) {
                    when(feesConfiguration.getWithNoticeKeyword()).thenReturn(GENERAL_APPLICATION_WITH_NOTICE);
                } else if (isWithNotice == YesOrNo.NO) {
                    when(feesConfiguration.getConsentedOrWithoutNoticeKeyword()).thenReturn(GENERAL_APP_WITHOUT_NOTICE);
                }
            } else if (generalApplicationTypes == GeneralApplicationTypes.ADJOURN_HEARING) {
                if (isWithNotice == YesOrNo.YES) {
                    when(feesConfiguration.getWithNoticeKeyword()).thenReturn(GENERAL_APPLICATION_WITH_NOTICE);
                } else if (isWithNotice == YesOrNo.NO) {
                    when(feesConfiguration.getConsentedOrWithoutNoticeKeyword()).thenReturn(GENERAL_APP_WITHOUT_NOTICE);
                }
            }

            LocalDate noOfDays = daysToAdd > 0 ? LocalDate.now().plusDays(daysToAdd) : null;
            GeneralApplicationCaseData caseDataWithNotice = getFeeCaseGeneralApplication(
                List.of(generalApplicationTypes),
                hasAgreed, isWithNotice, noOfDays
            );
            Fee feeDto = generalAppFeesService.getFeeForGA(caseDataWithNotice);
            assertThat(feeDto).isEqualTo(expectedFee);
        }

        @Test
        void shouldReturnFeeData_whenCertificateOfSatisfactionOrCancelRequestedGeneralApplicationCaseData() {
            when(feesConfiguration.getChannel()).thenReturn("default");
            when(feesConfiguration.getJurisdiction1()).thenReturn("civil");
            when(feesConfiguration.getJurisdiction2()).thenReturn("civil");
            when(feesConfiguration.getCertificateOfSatisfaction()).thenReturn("CertificateOfSorC");

            when(feesApiClient.lookupFee(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                eq(CERT_OF_SATISFACTION_OR_CANCEL)
            )).thenReturn(FEE_POUNDS);

            GeneralApplicationCaseData caseData = getFeeCaseGeneralApplication(
                List.of(GeneralApplicationTypes.CONFIRM_CCJ_DEBT_PAID), YesOrNo.NO, YesOrNo.NO, null);
            Fee feeDto = generalAppFeesService.getFeeForGA(caseData);
            assertThat(feeDto).isEqualTo(FEE_PENCE);
        }
    }

    private CaseData getFeeCase(List<GeneralApplicationTypes> types, YesOrNo hasAgreed,
                                YesOrNo isWithNotice, LocalDate hearingScheduledDate) {
        CaseData caseData = CaseDataBuilder.builder().build();
        GAApplicationType gaApplicationType = new GAApplicationType();
        gaApplicationType.setTypes(types);
        caseData.setGeneralAppType(gaApplicationType);
        if (Objects.nonNull(hasAgreed)) {
            GARespondentOrderAgreement respondentAgreement = new GARespondentOrderAgreement();
            respondentAgreement.setHasAgreed(hasAgreed);
            caseData.setGeneralAppRespondentAgreement(respondentAgreement);
        }
        if (Objects.nonNull(isWithNotice)) {
            GAInformOtherParty informOtherParty = new GAInformOtherParty();
            informOtherParty.setIsWithNotice(isWithNotice);
            caseData.setGeneralAppInformOtherParty(informOtherParty);
        }
        if (Objects.nonNull(hearingScheduledDate)) {
            GAHearingDateGAspec hearingDate = new GAHearingDateGAspec();
            hearingDate.setHearingScheduledDate(hearingScheduledDate);
            caseData.setGeneralAppHearingDate(hearingDate);
        }
        return caseData;
    }

    private GeneralApplicationCaseData getFeeCaseGeneralApplication(List<GeneralApplicationTypes> types, YesOrNo hasAgreed,
                                                                    YesOrNo isWithNotice, LocalDate hearingScheduledDate) {
        GeneralApplicationCaseData builder = new GeneralApplicationCaseData();
        builder.generalAppType(GAApplicationType.builder().types(types).build());
        if (Objects.nonNull(hasAgreed)) {
            builder.generalAppRespondentAgreement(GARespondentOrderAgreement
                                                      .builder().hasAgreed(hasAgreed).build());
        }
        if (Objects.nonNull(isWithNotice)) {
            builder.generalAppInformOtherParty(
                GAInformOtherParty.builder().isWithNotice(isWithNotice).build());
        }
        if (Objects.nonNull(hearingScheduledDate)) {
            builder.generalAppHearingDate(GAHearingDateGAspec.builder()
                                              .hearingScheduledDate(hearingScheduledDate).build());
        }
        return builder.build();
    }

    private List<GeneralApplicationTypes> getGADefaultTypes() {
        List<GeneralApplicationTypes> allTypes =
            Stream.of(GeneralApplicationTypes.values()).collect(Collectors.toList());
        allTypes.removeAll(GeneralAppFeesService.VARY_TYPES);
        allTypes.removeAll(GeneralAppFeesService.SET_ASIDE);
        allTypes.removeAll(GeneralAppFeesService.ADJOURN_TYPES);
        allTypes.removeAll(GeneralAppFeesService.SD_CONSENT_TYPES);
        allTypes.removeAll(GeneralAppFeesService.CONFIRM_YOU_PAID_CCJ_DEBT);
        Collections.shuffle(allTypes);
        return allTypes;
    }
}

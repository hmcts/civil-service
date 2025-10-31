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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.FeeLookupResponseDto;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDateGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.ADJOURN_HEARING;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.OTHER;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.SETTLE_BY_CONSENT;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.SET_ASIDE_JUDGEMENT;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.VARY_ORDER;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.values;
import static uk.gov.hmcts.reform.civil.service.GeneralAppFeesService.FREE_GA_DAYS;

@ExtendWith(MockitoExtension.class)
class GeneralAppFeesServiceTest {

    public static final String DEFAULT_CHANNEL = "default";
    public static final String CIVIL_JURISDICTION = "civil";
    public static final String TEST_FEE_CODE = "test_fee_code";
    public static final String FREE_REF = "FREE";
    public static final String GENERAL_APPLICATION = "general application";
    public static final String GENERAL_SERVICE = "general";
    private static final String APPLICATION_TO_VARY_OR_SUSPEND = "AppnToVaryOrSuspend";
    private static final String CERT_OF_SATISFACTION_OR_CANCEL = "CertificateOfSorC";
    private static final String GENERAL_APP_WITHOUT_NOTICE = "GeneralAppWithoutNotice";
    private static final String GENERAL_APPLICATION_WITH_NOTICE = "GAOnNotice";
    private static final String CERTIFICATE_OF_SATISFACTION = "CoS";
    private static final BigDecimal TEST_FEE_AMOUNT_POUNDS = new BigDecimal("108.00");
    private static final BigDecimal TEST_FEE_AMOUNT_PENCE = new BigDecimal(TEST_FEE_AMOUNT_POUNDS.intValue() * 100);
    private static final Fee FEE_PENCE = Fee.builder()
        .calculatedAmountInPence(TEST_FEE_AMOUNT_PENCE).code(TEST_FEE_CODE).version("1").build();
    private static final FeeLookupResponseDto FEE_POUNDS = FeeLookupResponseDto.builder()
        .feeAmount(TEST_FEE_AMOUNT_POUNDS).code(TEST_FEE_CODE).version(1).build();
    private static final Fee FEE_PENCE_0 = Fee.builder()
        .calculatedAmountInPence(BigDecimal.ZERO).code(FREE_REF).version("1").build();
    @Captor
    private ArgumentCaptor<String> keywordCaptor;

    @Mock
    private FeesApiClient feesApiClient;

    @Mock
    private GeneralAppFeesConfiguration feesConfiguration;

    @InjectMocks
    private GeneralAppFeesService generalAppFeesService;

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

    @Test
    void shouldReturnTrue_whenAllCriteriaAreMetForFreeGA() {
        final GeneralApplication application = GeneralApplication.builder()
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(ADJOURN_HEARING))
                                .build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                               .hasAgreed(YES)
                                               .build())
            .generalAppHearingDate(GAHearingDateGAspec.builder()
                                       .hearingScheduledDate(LocalDate.now().plusDays(FREE_GA_DAYS + 1))
                                       .build())
            .build();

        assertThat(generalAppFeesService.isFreeGa(application)).isTrue();
    }

    @Test
    void shouldReturnFalse_whenNotAdjournHearingType() {
        final GeneralApplication application = GeneralApplication.builder()
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(SET_ASIDE_JUDGEMENT))
                                .build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                               .hasAgreed(YES)
                                               .build())
            .generalAppHearingDate(GAHearingDateGAspec.builder()
                                       .hearingScheduledDate(LocalDate.now().plusDays(FREE_GA_DAYS + 1))
                                       .build())
            .build();

        assertThat(generalAppFeesService.isFreeGa(application)).isFalse();
    }

    @Test
    void shouldReturnFalse_whenHasAgreedIsNullOrNo() {
        final GAApplicationType appType = GAApplicationType.builder()
            .types(Collections.singletonList(ADJOURN_HEARING))
            .build();
        final GeneralApplication application_no = GeneralApplication.builder()
            .generalAppType(appType)
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YesOrNo.NO).build())
            .generalAppHearingDate(GAHearingDateGAspec.builder().hearingScheduledDate(LocalDate.now().plusDays(100)).build())
            .build();
        final GeneralApplication application_null = GeneralApplication.builder()
            .generalAppType(appType)
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(null).build())
            .generalAppHearingDate(GAHearingDateGAspec.builder().hearingScheduledDate(LocalDate.now().plusDays(100)).build())
            .build();

        assertThat(generalAppFeesService.isFreeGa(application_no)).isFalse();
        assertThat(generalAppFeesService.isFreeGa(application_null)).isFalse();
    }

    @Test
    void shouldReturnFalse_whenHearingDateIsNull() {
        final GeneralApplication application = GeneralApplication.builder()
            .generalAppType(GAApplicationType.builder()
                                .types(Collections.singletonList(ADJOURN_HEARING))
                                .build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YES).build())
            .generalAppHearingDate(null)
            .build();

        assertThat(generalAppFeesService.isFreeGa(application)).isFalse();
    }

    @Test
    void shouldReturnFalse_whenHearingDateIsIneligible() {
        final GeneralApplication application = GeneralApplication.builder()
            .generalAppType(GAApplicationType.builder()
                                .types(Collections.singletonList(ADJOURN_HEARING))
                                .build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YES).build())
            .generalAppHearingDate(GAHearingDateGAspec.builder()
                                       .hearingScheduledDate(LocalDate.now().plusDays(FREE_GA_DAYS - 2))
                                       .build())
            .build();

        assertThat(generalAppFeesService.isFreeGa(application)).isFalse();
    }

    private CaseData getFeeCase(List<GeneralApplicationTypes> types, YesOrNo hasAgreed,
                                YesOrNo isWithNotice, LocalDate hearingScheduledDate) {
        CaseData.CaseDataBuilder builder = CaseData.builder();
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
            Stream.of(values()).collect(Collectors.toList());
        allTypes.removeAll(GeneralAppFeesService.VARY_TYPES);
        allTypes.removeAll(GeneralAppFeesService.SET_ASIDE);
        allTypes.removeAll(GeneralAppFeesService.ADJOURN_TYPES);
        allTypes.removeAll(GeneralAppFeesService.SD_CONSENT_TYPES);
        allTypes.removeAll(GeneralAppFeesService.CONFIRM_YOU_PAID_CCJ_DEBT);
        Collections.shuffle(allTypes);
        return allTypes;
    }

    @Nested
    class FeeForJOWithApplicationType {
        static Stream<Arguments> joWithApplicationTypedData() {
            return Stream.of(
                Arguments.of(VARY_ORDER, APPLICATION_TO_VARY_OR_SUSPEND, FEE_PENCE),
                Arguments.of(SET_ASIDE_JUDGEMENT, GENERAL_APPLICATION_WITH_NOTICE, FEE_PENCE),
                Arguments.of(OTHER, CERTIFICATE_OF_SATISFACTION, FEE_PENCE)
            );
        }

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
            when(feesApiClient.lookupFee(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(FeeLookupResponseDto.builder().code(TEST_FEE_CODE).version(1).build());

            RuntimeException exception = assertThrows(
                RuntimeException.class, () ->
                    generalAppFeesService.getFeeForJOWithApplicationType(SET_ASIDE_JUDGEMENT)
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

            RuntimeException exception = assertThrows(
                RuntimeException.class, () ->
                    generalAppFeesService.getFeeForJOWithApplicationType(SET_ASIDE_JUDGEMENT)
            );
            assertThat(exception.getMessage()).isEqualTo(
                "No Fees returned by fee-service while creating General Application");
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
                Fee feeDto = generalAppFeesService.getFeeForGALiP(
                    List.of(generalApplicationType),
                    isWithConsent,
                    isWithNotice,
                    null
                );
                assertThat(feeDto).isEqualTo(FEE_PENCE);
            }
            //mix
            Fee feeDto = generalAppFeesService.getFeeForGALiP(allTypes, isWithConsent, isWithNotice, null);
            assertThat(feeDto).isEqualTo(FEE_PENCE);
        }
    }

    @Nested
    class FeeForGA {

        static Stream<Arguments> adjourn_with_hearingScheduledDate_outside_14daysData() {
            return Stream.of(
                Arguments.of(ADJOURN_HEARING, YES, YES, 15, FEE_PENCE_0),
                Arguments.of(ADJOURN_HEARING, YES, YesOrNo.NO, 15, FEE_PENCE_0)
            );
        }

        static Stream<Arguments> generateDefaultTypesData() {
            return Stream.of(
                Arguments.of(VARY_PAYMENT_TERMS_OF_JUDGMENT, YES, YES, -1, FEE_PENCE),
                Arguments.of(VARY_PAYMENT_TERMS_OF_JUDGMENT, YesOrNo.NO, YesOrNo.NO, -1, FEE_PENCE),
                Arguments.of(SETTLE_BY_CONSENT, YES, YES, -1, FEE_PENCE),
                Arguments.of(SET_ASIDE_JUDGEMENT, YesOrNo.NO, YES, -1, FEE_PENCE),
                Arguments.of(SET_ASIDE_JUDGEMENT, YesOrNo.NO, YesOrNo.NO, -1, FEE_PENCE),
                Arguments.of(ADJOURN_HEARING, YesOrNo.NO, YES, 1, FEE_PENCE),
                Arguments.of(ADJOURN_HEARING, YesOrNo.NO, YesOrNo.NO, 1, FEE_PENCE),
                Arguments.of(ADJOURN_HEARING, YesOrNo.NO, YesOrNo.NO, 15, FEE_PENCE)
            );
        }

        static Stream<Arguments> mixDefaultTypesData() {
            return Stream.of(
                Arguments.of(SET_ASIDE_JUDGEMENT, YES, YES, 15, FEE_PENCE),
                Arguments.of(VARY_PAYMENT_TERMS_OF_JUDGMENT, YES, YES, 15, FEE_PENCE)
            );
        }

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

            if (generalApplicationTypes == VARY_PAYMENT_TERMS_OF_JUDGMENT) {
                when(feesConfiguration.getAppnToVaryOrSuspend()).thenReturn(APPLICATION_TO_VARY_OR_SUSPEND);
            } else {
                when(feesConfiguration.getService()).thenReturn(GENERAL_SERVICE);
                when(feesConfiguration.getEvent()).thenReturn(GENERAL_APPLICATION);
            }

            if (generalApplicationTypes == SETTLE_BY_CONSENT) {
                when(feesConfiguration.getConsentedOrWithoutNoticeKeyword()).thenReturn(GENERAL_APP_WITHOUT_NOTICE);
            } else if (generalApplicationTypes == SET_ASIDE_JUDGEMENT && hasAgreed == YesOrNo.NO) {
                if (isWithNotice == YES) {
                    when(feesConfiguration.getWithNoticeKeyword()).thenReturn(GENERAL_APPLICATION_WITH_NOTICE);
                } else if (isWithNotice == YesOrNo.NO) {
                    when(feesConfiguration.getConsentedOrWithoutNoticeKeyword()).thenReturn(GENERAL_APP_WITHOUT_NOTICE);
                }
            } else if (generalApplicationTypes == ADJOURN_HEARING) {
                if (isWithNotice == YES) {
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

            if (generalApplicationTypes == SET_ASIDE_JUDGEMENT && hasAgreed == YesOrNo.NO) {
                when(feesConfiguration.getWithNoticeKeyword()).thenReturn(GENERAL_APPLICATION_WITH_NOTICE);
            } else if (generalApplicationTypes == VARY_PAYMENT_TERMS_OF_JUDGMENT) {
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
            if (generalApplicationTypes == SET_ASIDE_JUDGEMENT && hasAgreed == YesOrNo.NO) {
                assertThat(keywords).contains(GENERAL_APPLICATION_WITH_NOTICE);
            } else if (generalApplicationTypes == VARY_PAYMENT_TERMS_OF_JUDGMENT) {
                assertThat(keywords).contains(APPLICATION_TO_VARY_OR_SUSPEND);
            }
        }
    }
}

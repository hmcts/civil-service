package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.civil.config.GeneralAppFeesConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDateGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.fees.client.model.FeeLookupResponseDto;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.ADJOURN_HEARING;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.EXTEND_TIME;

@SpringBootTest(classes = {GeneralAppFeesService.class, RestTemplate.class, GeneralAppFeesConfiguration.class})
class GeneralAppFeesServiceTest {

    private static final BigDecimal TEST_FEE_AMOUNT_POUNDS_108 = new BigDecimal("108.00");
    private static final BigDecimal TEST_FEE_AMOUNT_PENCE_108 = new BigDecimal("10800");
    private static final BigDecimal TEST_FEE_AMOUNT_POUNDS_275 = new BigDecimal("275.00");
    private static final BigDecimal TEST_FEE_AMOUNT_PENCE_275 = new BigDecimal("27500");

    private static final BigDecimal TEST_FEE_AMOUNT_POUNDS_14 = new BigDecimal("14.00");
    private static final BigDecimal TEST_FEE_AMOUNT_PENCE_14 = new BigDecimal("1400");
    private static final String AppnToVaryOrSuspend = "dummy_urlgeneral%20application?channel=default&event=miscellaneous&jurisdiction1=civil"
            + "&jurisdiction2=civil&service=other&keyword=AppnToVaryOrSuspend";
    private static final String WithoutNotice = "dummy_urlgeneral%20application?channel=default&event&jurisdiction1=civil&"
            + "jurisdiction2=civil&service=general&keyword=GeneralAppWithoutNotice";
    private static final String GAOnNotice = "dummy_urlgeneral%20application?channel=default&event&jurisdiction1=civil&"
            + "jurisdiction2=civil&service=general&keyword=GAOnNotice";
    private static final FeeLookupResponseDto FEE_POUNDS_108 = FeeLookupResponseDto.builder()
            .feeAmount(TEST_FEE_AMOUNT_POUNDS_108).code("test_fee_code").version(1).build();
    private static final Fee FEE_PENCE_108 = Fee.builder()
            .calculatedAmountInPence(TEST_FEE_AMOUNT_PENCE_108).code("test_fee_code").version("1").build();
    private static final FeeLookupResponseDto FEE_POUNDS_275 = FeeLookupResponseDto.builder()
            .feeAmount(TEST_FEE_AMOUNT_POUNDS_275).code("test_fee_code").version(1).build();
    private static final Fee FEE_PENCE_275 = Fee.builder()
            .calculatedAmountInPence(TEST_FEE_AMOUNT_PENCE_275).code("test_fee_code").version("1").build();
    private static final FeeLookupResponseDto FEE_POUNDS_14 = FeeLookupResponseDto.builder()
            .feeAmount(TEST_FEE_AMOUNT_POUNDS_14).code("test_fee_code").version(2).build();
    private static final Fee FEE_PENCE_14 = Fee.builder()
            .calculatedAmountInPence(TEST_FEE_AMOUNT_PENCE_14).code("test_fee_code").version("2").build();
    private static final FeeLookupResponseDto FEE_POUNDS_0 = FeeLookupResponseDto.builder()
            .feeAmount(BigDecimal.ZERO).code("test_fee_code").version(2).build();
    public static final String FREE_REF = "FREE";
    private static final Fee FEE_PENCE_0 = Fee.builder()
            .calculatedAmountInPence(BigDecimal.ZERO).code(FREE_REF).version("1").build();

    @Captor
    private ArgumentCaptor<URI> queryCaptor;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private GeneralAppFeesConfiguration feesConfiguration;

    @InjectMocks
    private GeneralAppFeesService feesService;

    @BeforeEach
    void setUp() {
        when(feesConfiguration.getUrl()).thenReturn("dummy_url");
        when(feesConfiguration.getEndpoint()).thenReturn("/fees-register/fees/lookup");
        when(feesConfiguration.getService()).thenReturn("general");
        when(feesConfiguration.getChannel()).thenReturn("default");
        when(feesConfiguration.getEndpoint()).thenReturn("general application");
        when(feesConfiguration.getJurisdiction1()).thenReturn("civil");
        when(feesConfiguration.getJurisdiction2()).thenReturn("civil");
        when(feesConfiguration.getWithNoticeKeyword()).thenReturn("GAOnNotice");
        when(feesConfiguration.getConsentedOrWithoutNoticeKeyword()).thenReturn("GeneralAppWithoutNotice");
        when(feesConfiguration.getAppnToVaryOrSuspend()).thenReturn("AppnToVaryOrSuspend");
    }

    @Test
    void shouldReturnFeeData_whenConsentedApplicationIsBeingMadeForVaryAppln() {
        when(restTemplate.getForObject(queryCaptor.capture(), eq(FeeLookupResponseDto.class)))
                .thenReturn(FEE_POUNDS_108);

        Fee feeDto = feesService.getFeeForGA(feesConfiguration.getAppnToVaryOrSuspend(), "miscellaneous", "other");

        assertThat(feeDto).isEqualTo(FEE_PENCE_108);
        assertThat(queryCaptor.getValue().toString())
                .hasToString(AppnToVaryOrSuspend);
    }

    @Test
    void shouldReturnFeeData_whenConsentedApplicationIsBeingMade() {
        when(restTemplate.getForObject(queryCaptor.capture(), eq(FeeLookupResponseDto.class)))
                .thenReturn(FEE_POUNDS_108);

        Fee feeDto = feesService.getFeeForGA(feesConfiguration.getConsentedOrWithoutNoticeKeyword(), null, null);

        assertThat(feeDto).isEqualTo(FEE_PENCE_108);
        assertThat(queryCaptor.getValue().toString())
                .hasToString(WithoutNotice);
    }

    @Test
    void shouldReturnFeeData_whenUnonsentedWithoutNoticeApplicationIsBeingMade() {
        when(restTemplate.getForObject(queryCaptor.capture(), eq(FeeLookupResponseDto.class)))
                .thenReturn(FEE_POUNDS_108);

        Fee feeDto = feesService.getFeeForGA(feesConfiguration.getConsentedOrWithoutNoticeKeyword(), null, null);

        assertThat(feeDto).isEqualTo(FEE_PENCE_108);
        assertThat(queryCaptor.getValue().toString())
                .hasToString(WithoutNotice);
    }

    @Test
    void shouldReturnFeeData_whenUnconsentedNotifiedApplicationIsBeingMade() {
        when(restTemplate.getForObject(queryCaptor.capture(), eq(FeeLookupResponseDto.class)))
                .thenReturn(FEE_POUNDS_275);

        Fee feeDto = feesService.getFeeForGA(feesConfiguration.getWithNoticeKeyword(), null, null);

        assertThat(feeDto).isEqualTo(FEE_PENCE_275);
        verify(feesConfiguration, times(1)).getWithNoticeKeyword();
        verify(feesConfiguration, never()).getConsentedOrWithoutNoticeKeyword();
        assertThat(queryCaptor.getValue().toString())
                .hasToString(GAOnNotice);
    }

    @Test
    void shouldPay_whenIsNotAdjournVacateApplication() {
        CaseData caseData = getFeeCase(
                List.of(EXTEND_TIME),
                YesOrNo.YES, YesOrNo.YES, LocalDate.now().plusDays(15));

        assertThat(feesService.isFreeApplication(caseData)).isFalse();
    }

    @Test
    void shouldPay_whenConsentedWithin14DaysAdjournVacateApplicationIsBeingMade() {
        CaseData caseData = getFeeCase(
                List.of(ADJOURN_HEARING),
                YesOrNo.YES, YesOrNo.YES, LocalDate.now().plusDays(14));

        assertThat(feesService.isFreeApplication(caseData)).isFalse();
    }

    @Test
    void shouldBeFree_whenConsentedLateThan14DaysAdjournVacateApplicationIsBeingMade() {
        CaseData caseData = getFeeCase(
                List.of(ADJOURN_HEARING),
                YesOrNo.YES, YesOrNo.YES, LocalDate.now().plusDays(15));

        assertThat(feesService.isFreeApplication(caseData)).isTrue();
    }

    @Test
    void throwRuntimeException_whenFeeServiceThrowsException() {
        when(restTemplate.getForObject(queryCaptor.capture(), eq(FeeLookupResponseDto.class)))
                .thenThrow(new RuntimeException("Some Exception"));
        String keyword = feesConfiguration.getWithNoticeKeyword();
        Exception exception = assertThrows(RuntimeException.class, () -> feesService
                .getFeeForGA(keyword, null, null));

        assertThat(exception.getMessage()).isEqualTo("java.lang.RuntimeException: Some Exception");
    }

    @Test
    void throwRuntimeException_whenNoFeeIsReturnedByFeeService() {
        when(restTemplate.getForObject(queryCaptor.capture(), eq(FeeLookupResponseDto.class)))
                .thenReturn(null);
        String keyword = feesConfiguration.getWithNoticeKeyword();
        Exception exception = assertThrows(RuntimeException.class, () -> feesService
                .getFeeForGA(keyword, null, null));

        assertThat(exception.getMessage())
                .isEqualTo("No Fees returned by fee-service while creating General Application");
    }

    @Test
    void throwRuntimeException_whenNoFeeAmountIsReturnedByFeeService() {
        when(restTemplate.getForObject(queryCaptor.capture(), eq(FeeLookupResponseDto.class)))
                .thenReturn(FeeLookupResponseDto.builder()
                        .code("test_fee_code")
                        .version(1)
                        .build());
        String keyword = feesConfiguration.getWithNoticeKeyword();
        Exception exception = assertThrows(RuntimeException.class, () -> feesService
                .getFeeForGA(keyword, null, null));

        assertThat(exception.getMessage())
                .isEqualTo("No Fees returned by fee-service while creating General Application");
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

    @Nested
    class LowestFee {
        @BeforeEach
        void setUp() {
            try {
                when(restTemplate.getForObject(new URI(WithoutNotice), FeeLookupResponseDto.class))
                        .thenReturn(FEE_POUNDS_108);
                when(restTemplate.getForObject(new URI(GAOnNotice), FeeLookupResponseDto.class))
                        .thenReturn(FEE_POUNDS_275);
                when(restTemplate.getForObject(new URI(AppnToVaryOrSuspend), FeeLookupResponseDto.class))
                        .thenReturn(FEE_POUNDS_14);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        void default_types_with_notice_should_pay_275() {
            List<GeneralApplicationTypes> allTypes =
                    Stream.of(GeneralApplicationTypes.values()).collect(Collectors.toList());
            allTypes.removeAll(GeneralAppFeesService.VARY_TYPES);
            allTypes.removeAll(GeneralAppFeesService.SET_ASIDE);
            allTypes.removeAll(GeneralAppFeesService.ADJOURN_TYPES);
            allTypes.removeAll(GeneralAppFeesService.SD_CONSENT_TYPES);
            //single
            for (GeneralApplicationTypes generalApplicationType : allTypes) {
                CaseData caseData = getFeeCase(
                        List.of(generalApplicationType), YesOrNo.NO, YesOrNo.YES, null);
                Fee feeDto = feesService.getFeeForGA(caseData);
                assertThat(feeDto).isEqualTo(FEE_PENCE_275);
            }
            //mix
            CaseData caseData = getFeeCase(
                    allTypes, YesOrNo.NO, YesOrNo.YES, null);
            Fee feeDto = feesService.getFeeForGA(caseData);
            assertThat(feeDto).isEqualTo(FEE_PENCE_275);
        }

        @Test
        void default_types_without_notice_should_pay_108() {
            List<GeneralApplicationTypes> allTypes =
                    Stream.of(GeneralApplicationTypes.values()).collect(Collectors.toList());
            allTypes.removeAll(GeneralAppFeesService.VARY_TYPES);
            allTypes.removeAll(GeneralAppFeesService.SET_ASIDE);
            allTypes.removeAll(GeneralAppFeesService.ADJOURN_TYPES);
            allTypes.removeAll(GeneralAppFeesService.SD_CONSENT_TYPES);
            //single
            for (GeneralApplicationTypes generalApplicationType : allTypes) {
                CaseData caseData = getFeeCase(
                        List.of(generalApplicationType), YesOrNo.NO, YesOrNo.NO, null);
                Fee feeDto = feesService.getFeeForGA(caseData);
                assertThat(feeDto).isEqualTo(FEE_PENCE_108);
            }
            //mix
            CaseData caseData = getFeeCase(
                    allTypes, YesOrNo.NO, YesOrNo.NO, null);
            Fee feeDto = feesService.getFeeForGA(caseData);
            assertThat(feeDto).isEqualTo(FEE_PENCE_108);
        }

        @Test
        void adjourn_should_pay_default_or_free_fee() {
            CaseData caseDataWithin14DaysWithNotice = getFeeCase(
                    List.of(GeneralApplicationTypes.ADJOURN_HEARING),
                    YesOrNo.NO, YesOrNo.YES, LocalDate.now().plusDays(1));
            assertThat(feesService.getFeeForGA(caseDataWithin14DaysWithNotice))
                    .isEqualTo(FEE_PENCE_275);
            CaseData caseDataWithin14DaysWithoutNotice = getFeeCase(
                    List.of(GeneralApplicationTypes.ADJOURN_HEARING),
                    YesOrNo.NO, YesOrNo.NO, LocalDate.now().plusDays(1));
            assertThat(feesService.getFeeForGA(caseDataWithin14DaysWithoutNotice))
                    .isEqualTo(FEE_PENCE_108);
            CaseData caseDataOutside14Days = getFeeCase(
                    List.of(GeneralApplicationTypes.ADJOURN_HEARING),
                    YesOrNo.YES, YesOrNo.NO, LocalDate.now().plusDays(15));
            assertThat(feesService.getFeeForGA(caseDataOutside14Days))
                    .isEqualTo(FEE_PENCE_0);
        }

        @Test
        void vary_types_should_be_14() {
            for (GeneralApplicationTypes type : GeneralAppFeesService.VARY_TYPES) {
                CaseData caseDataWithNotice = getFeeCase(
                        List.of(type),
                        YesOrNo.YES, YesOrNo.YES, null);
                Fee feeDto = feesService.getFeeForGA(caseDataWithNotice);
                assertThat(feeDto).isEqualTo(FEE_PENCE_14);
                CaseData caseDataWithoutNotice = getFeeCase(
                        List.of(type),
                        YesOrNo.NO, YesOrNo.NO, null);
                feeDto = feesService.getFeeForGA(caseDataWithoutNotice);
                assertThat(feeDto).isEqualTo(FEE_PENCE_14);
            }
        }

        @Test
        void settle_should_be_108() {
            CaseData caseDataWithNotice = getFeeCase(
                    List.of(GeneralApplicationTypes.SETTLE_BY_CONSENT),
                    YesOrNo.YES, YesOrNo.YES, null);
            Fee feeDto = feesService.getFeeForGA(caseDataWithNotice);
            assertThat(feeDto).isEqualTo(FEE_PENCE_108);
        }

        @Test
        void setAside_should_be_275() {
            CaseData caseDataWithNotice = getFeeCase(
                    List.of(GeneralApplicationTypes.SET_ASIDE_JUDGEMENT),
                    YesOrNo.YES, YesOrNo.YES, null);
            Fee feeDto = feesService.getFeeForGA(caseDataWithNotice);
            assertThat(feeDto).isEqualTo(FEE_PENCE_275);
            CaseData caseDataWithoutNotice = getFeeCase(
                    List.of(GeneralApplicationTypes.SET_ASIDE_JUDGEMENT),
                    YesOrNo.NO, YesOrNo.NO, null);
            feeDto = feesService.getFeeForGA(caseDataWithoutNotice);
            assertThat(feeDto).isEqualTo(FEE_PENCE_275);
        }

        @Test
        void mix_default_adjourn_should_not_free() {
            List<GeneralApplicationTypes> randomList = getRandomDefaultTypes();
            randomList.add(GeneralApplicationTypes.ADJOURN_HEARING);
            CaseData caseDataOutside14Days = getFeeCase(
                    randomList,
                    YesOrNo.YES, YesOrNo.YES, LocalDate.now().plusDays(15));
            assertThat(feesService.getFeeForGA(caseDataOutside14Days))
                    .isEqualTo(FEE_PENCE_108);
        }

        @Test
        void mix_default_set_aside_should_be_108() {
            List<GeneralApplicationTypes> randomList = getRandomDefaultTypes();
            randomList.add(GeneralApplicationTypes.SET_ASIDE_JUDGEMENT);
            CaseData caseDataOutside14Days = getFeeCase(
                    randomList,
                    YesOrNo.YES, YesOrNo.YES, LocalDate.now().plusDays(15));
            assertThat(feesService.getFeeForGA(caseDataOutside14Days))
                    .isEqualTo(FEE_PENCE_108);
        }

        @Test
        void mix_default_vary_should_be_14() {
            List<GeneralApplicationTypes> randomList = getRandomDefaultTypes();
            randomList.add(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT);
            CaseData caseDataOutside14Days = getFeeCase(
                    randomList,
                    YesOrNo.YES, YesOrNo.YES, LocalDate.now().plusDays(15));
            assertThat(feesService.getFeeForGA(caseDataOutside14Days))
                    .isEqualTo(FEE_PENCE_14);
            randomList.remove(randomList.size() - 1);
            randomList.add(GeneralApplicationTypes.VARY_ORDER);
            caseDataOutside14Days = getFeeCase(
                    randomList,
                    YesOrNo.YES, YesOrNo.YES, LocalDate.now().plusDays(15));
            assertThat(feesService.getFeeForGA(caseDataOutside14Days))
                    .isEqualTo(FEE_PENCE_14);
            randomList.remove(randomList.size() - 1);
        }

        @Test
        void mix_default_vary_set_aside_should_be_14() {
            List<GeneralApplicationTypes> randomList = getRandomDefaultTypes();
            randomList.add(GeneralApplicationTypes.VARY_ORDER);
            randomList.add(GeneralApplicationTypes.SET_ASIDE_JUDGEMENT);
            CaseData caseDataOutside14Days = getFeeCase(
                    randomList,
                    YesOrNo.YES, YesOrNo.YES, LocalDate.now().plusDays(15));
            assertThat(feesService.getFeeForGA(caseDataOutside14Days))
                    .isEqualTo(FEE_PENCE_14);
        }

        private List<GeneralApplicationTypes> getRandomDefaultTypes() {
            List<GeneralApplicationTypes> allTypes =
                    Stream.of(GeneralApplicationTypes.values()).collect(Collectors.toList());
            allTypes.removeAll(GeneralAppFeesService.VARY_TYPES);
            allTypes.removeAll(GeneralAppFeesService.SET_ASIDE);
            allTypes.removeAll(GeneralAppFeesService.ADJOURN_TYPES);
            allTypes.removeAll(GeneralAppFeesService.SD_CONSENT_TYPES);
            Collections.shuffle(allTypes);
            Random rand = new Random();
            int min = 1;
            int max = allTypes.size();
            return allTypes.subList(0, rand.nextInt(min, max));
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
    }
}

package uk.gov.hmcts.reform.civil.service;

import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.client.PaymentGroupsApiClient;
import uk.gov.hmcts.reform.civil.model.servicerequest.CasePaymentGroupsResponse;
import uk.gov.hmcts.reform.civil.model.servicerequest.CasePaymentGroupsResponse.Fee;
import uk.gov.hmcts.reform.civil.model.servicerequest.CasePaymentGroupsResponse.PaymentGroup;
import uk.gov.hmcts.reform.payments.client.models.FeeDto;
import uk.gov.hmcts.reform.payments.request.CreateServiceRequestDTO;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ServiceRequestLookupServiceTest {

    private static final String AUTH = "Bearer token";
    private static final String S2S = "s2s token";
    private static final String CASE = "1795511306447577";
    private static final FeeDto CLAIM_FEE = FeeDto.builder().code("FEE0209").calculatedAmount(new BigDecimal("455.00")).version("3").volume(1).build();
    private static final FeeDto OTHER_REMEDY_FEE = FeeDto.builder().code("FEE0210").calculatedAmount(new BigDecimal("50")).version("1").volume(1).build();

    @Mock
    private PaymentGroupsApiClient paymentGroupsApiClient;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @InjectMocks
    private ServiceRequestLookupService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "enabled", true);
        lenient().when(authTokenGenerator.generate()).thenReturn(S2S);
    }

    @Test
    void reusesUnpaidServiceRequestWithTheSameFee() {
        givenGroups(group("2026-1111", "Not paid", fee("FEE0209", "455")));

        assertThat(service.findUnpaidServiceRequest(request(CLAIM_FEE), AUTH)).contains("2026-1111");
    }

    @Test
    void matchesSeveralFeesInAnyOrder() {
        givenGroups(group("2026-2222", "Not paid", fee("FEE0210", "50.00"), fee("FEE0209", "455")));

        assertThat(service.findUnpaidServiceRequest(request(CLAIM_FEE, OTHER_REMEDY_FEE), AUTH)).contains("2026-2222");
    }

    @Test
    void ignoresPaidAndPartiallyPaidServiceRequests() {
        givenGroups(
            group("2026-3333", "Paid", fee("FEE0209", "455")),
            group("2026-4444", "Partially paid", fee("FEE0209", "455"))
        );

        assertThat(service.findUnpaidServiceRequest(request(CLAIM_FEE), AUTH)).isEmpty();
    }

    @Test
    void ignoresServiceRequestsForADifferentFeeOrAmount() {
        givenGroups(
            group("2026-5555", "Not paid", fee("FEE0443", "455")),
            group("2026-6666", "Not paid", fee("FEE0209", "410")),
            group("2026-7777", "Not paid", fee("FEE0209", "455"), fee("FEE0210", "50"))
        );

        assertThat(service.findUnpaidServiceRequest(request(CLAIM_FEE), AUTH)).isEmpty();
    }

    @Test
    void returnsEmptyWhenTheCaseHasNoServiceRequests() {
        given(paymentGroupsApiClient.getPaymentGroups(AUTH, S2S, CASE)).willReturn(null);

        assertThat(service.findUnpaidServiceRequest(request(CLAIM_FEE), AUTH)).isEmpty();
    }

    @Test
    void returnsEmptyWhenTheLookupFails() {
        Request feignRequest = Request.create(Request.HttpMethod.GET, "/cases/" + CASE + "/paymentgroups",
                                              Map.of(), null, StandardCharsets.UTF_8, null);
        given(paymentGroupsApiClient.getPaymentGroups(any(), any(), any()))
            .willThrow(new FeignException.Forbidden("forbidden", feignRequest, null, Map.of()));

        assertThat(service.findUnpaidServiceRequest(request(CLAIM_FEE), AUTH)).isEmpty();
    }

    @Test
    void doesNotLookUpWhenSwitchedOff() {
        ReflectionTestUtils.setField(service, "enabled", false);

        assertThat(service.findUnpaidServiceRequest(request(CLAIM_FEE), AUTH)).isEmpty();
        verifyNoInteractions(paymentGroupsApiClient);
    }

    private void givenGroups(PaymentGroup... groups) {
        given(paymentGroupsApiClient.getPaymentGroups(AUTH, S2S, CASE))
            .willReturn(CasePaymentGroupsResponse.builder().paymentGroups(List.of(groups)).build());
    }

    private static CreateServiceRequestDTO request(FeeDto... fees) {
        return CreateServiceRequestDTO.builder().ccdCaseNumber(CASE).fees(fees).build();
    }

    private static PaymentGroup group(String reference, String status, Fee... fees) {
        return PaymentGroup.builder().paymentGroupReference(reference).serviceRequestStatus(status).fees(List.of(fees)).build();
    }

    private static Fee fee(String code, String amount) {
        return Fee.builder().code(code).calculatedAmount(new BigDecimal(amount)).build();
    }
}

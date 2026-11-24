package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.client.PaymentGroupsApiClient;
import uk.gov.hmcts.reform.civil.model.servicerequest.CasePaymentGroupsResponse;
import uk.gov.hmcts.reform.civil.model.servicerequest.CasePaymentGroupsResponse.PaymentGroup;
import uk.gov.hmcts.reform.payments.request.CreateServiceRequestDTO;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Finds an unpaid service request on the case that already asks for exactly the fees about to be requested.
 *
 * <p>Creating a service request is not idempotent: the payment service creates a new one on every
 * {@code POST /service-request}. civil-service stores the reference only when the CCD event that asked for it
 * is saved, so when the payments call succeeds but the event does not (a gateway 502 or 504, or a read timeout,
 * after payments has committed), a retry of the Camunda task would raise a second request for the same fee
 * and the payer would see both. Reusing the unpaid one makes the retry safe (DTSCCI-7095).
 *
 * <p>The lookup is best effort. If it fails for any reason the caller creates a new service request, as before.
 *
 * <p>Off by default ({@code payments.api.reuse-unpaid-service-request}). The payment service allows
 * {@code GET /cases/{ccdCaseNumber}/paymentgroups} only for the {@code payments} and {@code pui-*} roles, and the
 * civil system update user has neither (403 in AAT, 24 Nov 2026). Until it does, the payment handlers label a
 * failure that may have reached payments as NOT RETRYABLE instead
 * ({@code CallbackFailureClassifier.labelledErrorForNonIdempotentCall}).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceRequestLookupService {

    static final String NOT_PAID = "Not paid";

    private final PaymentGroupsApiClient paymentGroupsApiClient;
    private final AuthTokenGenerator authTokenGenerator;

    @Value("${payments.api.reuse-unpaid-service-request:false}")
    private boolean enabled;

    public Optional<String> findUnpaidServiceRequest(CreateServiceRequestDTO request, String authToken) {
        if (!enabled || request == null || request.getCcdCaseNumber() == null || request.getFees() == null) {
            return Optional.empty();
        }
        try {
            CasePaymentGroupsResponse response = paymentGroupsApiClient.getPaymentGroups(
                authToken,
                authTokenGenerator.generate(),
                request.getCcdCaseNumber()
            );
            if (response == null || response.getPaymentGroups() == null) {
                return Optional.empty();
            }
            List<String> requested = Arrays.stream(request.getFees())
                .filter(Objects::nonNull)
                .map(fee -> key(fee.getCode(), fee.getCalculatedAmount()))
                .sorted()
                .toList();
            return response.getPaymentGroups().stream()
                .filter(Objects::nonNull)
                .filter(group -> NOT_PAID.equalsIgnoreCase(group.getServiceRequestStatus()))
                .filter(group -> group.getPaymentGroupReference() != null)
                .filter(group -> requested.equals(feeKeys(group)))
                .map(PaymentGroup::getPaymentGroupReference)
                .findFirst();
        } catch (RuntimeException e) {
            log.warn("Could not look up existing service requests for case {}, creating a new one: {}",
                     request.getCcdCaseNumber(), e.getMessage());
            return Optional.empty();
        }
    }

    private static List<String> feeKeys(PaymentGroup group) {
        if (group.getFees() == null) {
            return List.of();
        }
        return group.getFees().stream()
            .filter(Objects::nonNull)
            .map(fee -> key(fee.getCode(), fee.getCalculatedAmount()))
            .sorted()
            .toList();
    }

    private static String key(String code, BigDecimal amount) {
        String amountKey = amount == null ? "" : amount.stripTrailingZeros().toPlainString();
        return code + "|" + amountKey;
    }

}

package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PaymentServiceHelperTest {

    @Mock
    private CoreCaseDataService coreCaseDataService;

    private PaymentServiceHelper paymentServiceHelper;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        paymentServiceHelper = new PaymentServiceHelper(coreCaseDataService, objectMapper);
    }

    @Test
    void shouldUpdateCaseDataByFeeType() {
        CaseData caseData = CaseData.builder().build();
        PaymentDetails paymentDetails = PaymentDetails.builder().status(PaymentStatus.SUCCESS).build();

        CaseData updatedCaseData = paymentServiceHelper.updateCaseDataByFeeType(caseData, FeeType.HEARING.name(), paymentDetails);

        assertEquals(paymentDetails, updatedCaseData.getHearingFeePaymentDetails());
    }

    @Test
    void shouldBuildPaymentDetails() {
        CardPaymentStatusResponse response = CardPaymentStatusResponse.builder().status("SUCCESS").paymentReference("ref").build();

        PaymentDetails paymentDetails = paymentServiceHelper.buildPaymentDetails(response);

        assertEquals(PaymentStatus.SUCCESS, paymentDetails.getStatus());
        assertEquals("ref", paymentDetails.getReference());
    }
}

package uk.gov.hmcts.reform.civil.contracts;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.civil.controllers.fees.FeesPaymentController;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.model.CardPaymentStatusResponse;
import uk.gov.hmcts.reform.civil.service.FeesPaymentService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@Provider("civil-service")
@PactFolder("src/contractTest/resources/pacts")
@WebMvcTest(controllers = FeesPaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class CivilCitizenUiProviderContractTest {

    private static final String AUTH_HEADER = "Bearer some-auth-token";
    private static final String CASE_REFERENCE = "1234567890123456";
    private static final String PAYMENT_REFERENCE = "RC-1701-0909-0602-0418";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FeesPaymentService feesPaymentService;

    @BeforeEach
    void beforeEach(PactVerificationContext context) {
        MockMvcTestTarget target = new MockMvcTestTarget();
        target.setMockMvc(mockMvc);
        context.setTarget(target);
        reset(feesPaymentService);
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPactInteractions(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @State("Claim issue payment can be initiated for case 1234567890123456")
    void claimIssuePaymentExists() {
        when(feesPaymentService.createGovPaymentRequest(
            FeeType.CLAIMISSUED,
            CASE_REFERENCE,
            AUTH_HEADER
        )).thenReturn(
            CardPaymentStatusResponse.builder()
                .externalReference("2023-1701090705688")
                .paymentReference(PAYMENT_REFERENCE)
                .status("Initiated")
                .nextUrl("https://card.payments.service.gov.uk/secure/7b0716b2-40c4-413e-b62e-72c599c91960")
                .dateCreated(OffsetDateTime.parse("2023-11-27T13:15:06.313+00:00"))
                .build()
        );
    }

    @State("Payment status SUCCESS is available for payment RC-1701-0909-0602-0418")
    void paymentStatusSuccess() {
        when(feesPaymentService.getGovPaymentRequestStatus(
            FeeType.CLAIMISSUED,
            CASE_REFERENCE,
            PAYMENT_REFERENCE,
            AUTH_HEADER
        )).thenReturn(
            CardPaymentStatusResponse.builder()
                .externalReference("2023-1701090705688")
                .paymentReference(PAYMENT_REFERENCE)
                .status("Success")
                .paymentFor("claimissued")
                .paymentAmount(new BigDecimal("200"))
                .build()
        );
    }
}

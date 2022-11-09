package uk.gov.hmcts.reform.civil.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.hearing.HFPbaDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.payments.client.InvalidPaymentRequestException;
import uk.gov.hmcts.reform.payments.client.PaymentsClient;
import uk.gov.hmcts.reform.payments.client.models.FeeDto;
import uk.gov.hmcts.reform.payments.request.CreditAccountPaymentRequest;
import uk.gov.hmcts.reform.payments.response.PBAServiceRequestResponse;
import uk.gov.hmcts.reform.payments.response.PaymentServiceResponse;
import uk.gov.hmcts.reform.prd.model.ContactInformation;
import uk.gov.hmcts.reform.prd.model.Organisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.enums.SuperClaimType.SPEC_CLAIM;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    PaymentsService.class,
    JacksonAutoConfiguration.class
})
class PaymentsServiceTest {

    private static final String SERVICE = "service";
    private static final String SITE_ID = "site_id";
    private static final String SPEC_SERVICE = "spec_service";
    private static final String SPEC_SITE_ID = "spec_site_id";
    private static final String AUTH_TOKEN = "Bearer token";
    private static final PBAServiceRequestResponse PAYMENT_DTO = PBAServiceRequestResponse.builder()
        .paymentReference("RC-1234-1234-1234-1234").build();
    private static final PaymentServiceResponse PAYMENT_SERVICE_RESPONSE = PaymentServiceResponse.builder()
        .serviceRequestReference("RC-1234-1234-1234-1234").build();
    private static final Organisation ORGANISATION = Organisation.builder()
        .name("test org")
        .contactInformation(List.of(ContactInformation.builder().build()))
        .build();
    private static final String CUSTOMER_REFERENCE = "12345";
    private static final String FEE_NOT_SET_CORRECTLY_ERROR = "Fees are not set correctly.";

    @MockBean
    private PaymentsClient paymentsClient;

    @MockBean
    private PaymentsConfiguration paymentsConfiguration;

    @MockBean
    private OrganisationService organisationService;

    @Autowired
    private PaymentsService paymentsService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Nested
    class Unspecified {
        @BeforeEach
        void setUp() {
            given(paymentsClient.createServiceRequest(any(), any())).willReturn(PAYMENT_SERVICE_RESPONSE);
            given(paymentsConfiguration.getService()).willReturn(SERVICE);
            given(paymentsConfiguration.getSiteId()).willReturn(SITE_ID);
            given(paymentsClient.createPbaPayment(any(), any(), any())).willReturn(PAYMENT_DTO);
            given(organisationService.findOrganisationById(any())).willReturn(Optional.of(ORGANISATION));
        }

        @Test
        void validateRequestShouldNotThrowAnError_whenValidCaseDataIsProvided() {
            CaseData caseData = CaseDataBuilder.builder().buildMakePaymentsCaseData();
            paymentsService.validateRequest(caseData);
            assertThat(caseData).isNotNull();
        }

        @Test
        void shouldCreateCreditAccountPayment_whenValidCaseDetails() {
            uk.gov.hmcts.reform.ccd.model.Organisation orgId = uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                .organisationID("OrgId").build();

            HFPbaDetails hfPbaDetails = HFPbaDetails.builder()
                .serviceReqReference("request-reference")
                .applicantsPbaAccounts(DynamicList.builder()
                                           .value(DynamicListElement
                                                      .builder().label("account-no")
                                                      .build()
                                           ).build()
                )
                .build();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted()
                .applicant1OrganisationPolicy(OrganisationPolicy.builder().organisation(orgId).build())
                .build();
            caseData = caseData.toBuilder().hearingFeePBADetails(hfPbaDetails).build();

            PBAServiceRequestResponse paymentResponse = paymentsService
                .createCreditAccountPayment(caseData, AUTH_TOKEN);

            verify(organisationService).findOrganisationById("OrgId");
            verify(paymentsClient).createPbaPayment(eq("request-reference"), eq(AUTH_TOKEN), any());
            assertThat(paymentResponse).isEqualTo(PAYMENT_DTO);
        }

        private CreditAccountPaymentRequest getExpectedCreditAccountPaymentRequest(CaseData caseData) {
            return CreditAccountPaymentRequest.builder()
                .accountNumber("PBA0077597")
                .amount(caseData.getClaimFee().toFeeDto().getCalculatedAmount())
                .caseReference("000DC001")
                .ccdCaseNumber(caseData.getCcdCaseReference().toString())
                .customerReference(CUSTOMER_REFERENCE)
                .description("Claim issue payment")
                .organisationName(ORGANISATION.getName())
                .service(SERVICE)
                .siteId(SITE_ID)
                .fees(new FeeDto[]{caseData.getClaimFee().toFeeDto()})
                .build();
        }
    }

    @Nested
    class Specified {
        @BeforeEach
        void setUp() {
            given(paymentsClient.createServiceRequest(any(), any())).willReturn(PAYMENT_SERVICE_RESPONSE);
            given(paymentsConfiguration.getSpecService()).willReturn(SPEC_SERVICE);
            given(paymentsConfiguration.getSpecSiteId()).willReturn(SPEC_SITE_ID);
            given(paymentsClient.createPbaPayment(any(), any(), any())).willReturn(PAYMENT_DTO);
            given(organisationService.findOrganisationById(any())).willReturn(Optional.of(ORGANISATION));
        }

        @Test
        void shouldCreateCreditAccountPayment_whenValidCaseDetails() {
            // Given
            uk.gov.hmcts.reform.ccd.model.Organisation orgId = uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                .organisationID("OrgId").build();

            HFPbaDetails hfPbaDetails = HFPbaDetails.builder()
                .serviceReqReference("request-reference")
                .applicantsPbaAccounts(DynamicList.builder()
                                           .value(DynamicListElement
                                                      .builder().label("account-no")
                                                      .build()
                                           ).build()
                )
                .build();

            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted()
                .applicant1OrganisationPolicy(OrganisationPolicy.builder().organisation(orgId).build())
                .build();
            caseData = caseData.toBuilder().superClaimType(SPEC_CLAIM).hearingFeePBADetails(hfPbaDetails).build();

            var expectedCreditAccountPaymentRequest =
                getExpectedCreditAccountPaymentRequest(caseData);

            // When
            PBAServiceRequestResponse paymentResponse = paymentsService
                .createCreditAccountPayment(caseData, AUTH_TOKEN);

            // Then
            verify(organisationService).findOrganisationById("OrgId");
            verify(paymentsClient).createPbaPayment(eq("request-reference"), eq(AUTH_TOKEN), any());
            assertThat(paymentResponse).isEqualTo(PAYMENT_DTO);
        }

        private CreditAccountPaymentRequest getExpectedCreditAccountPaymentRequest(CaseData caseData) {
            return CreditAccountPaymentRequest.builder()
                .accountNumber("PBA0077597")
                .amount(caseData.getClaimFee().toFeeDto().getCalculatedAmount())
                .caseReference("000DC001")
                .ccdCaseNumber(caseData.getCcdCaseReference().toString())
                .customerReference(CUSTOMER_REFERENCE)
                .description("Claim issue payment")
                .organisationName(ORGANISATION.getName())
                .service(SPEC_SERVICE)
                .siteId(SPEC_SITE_ID)
                .fees(new FeeDto[]{caseData.getClaimFee().toFeeDto()})
                .build();
        }
    }

    @Nested
    class ServiceRequest {
        @BeforeEach
        void setUp() {
            given(paymentsClient.createServiceRequest(any(), any())).willReturn(PAYMENT_SERVICE_RESPONSE);
            given(paymentsClient.createPbaPayment(any(), any(), any())).willReturn(PAYMENT_DTO);
            given(paymentsConfiguration.getSpecService()).willReturn(SPEC_SERVICE);
            given(paymentsConfiguration.getSpecSiteId()).willReturn(SPEC_SITE_ID);
            given(organisationService.findOrganisationById(any())).willReturn(Optional.of(ORGANISATION));
        }

        @Test
        void validateRequestShouldNotThrowAnError_whenValidCaseDataIsProvided() {
            CaseData caseData = CaseDataBuilder.builder().buildMakePaymentsCaseData();
            paymentsService.validateRequest(caseData);
            assertThat(caseData).isNotNull();
        }

        @Test
        void validateRequestShouldThrowAnError_whenFeeDetailsNotProvided() {
            CaseData caseData = CaseData.builder()
                .hearingFeePBADetails(HFPbaDetails.builder().build())
                .build();

            Exception exception = assertThrows(
                InvalidPaymentRequestException.class,
                () -> paymentsService.validateRequest(caseData)
            );
            assertThat(exception.getMessage()).isEqualTo(FEE_NOT_SET_CORRECTLY_ERROR);
        }

        @Test
        void validateRequestShouldThrowAnError_whenFeeDetailsDoNotHaveFeeCode() {
            CaseData caseData = CaseData.builder()
                .hearingFeePBADetails(HFPbaDetails.builder()
                                                     .fee(Fee.builder()
                                                              .calculatedAmountInPence(BigDecimal.valueOf(10800))
                                                              .version("1")
                                                              .build())
                                                     .build())
                .build();

            Exception exception = assertThrows(
                InvalidPaymentRequestException.class,
                () -> paymentsService.validateRequest(caseData)
            );
            assertThat(exception.getMessage()).isEqualTo(FEE_NOT_SET_CORRECTLY_ERROR);
        }

        @Test
        void validateRequestShouldThrowAnError_whenFeeDetailsDoNotHaveFeeVersion() {
            CaseData caseData = CaseData.builder()
                .hearingFeePBADetails(HFPbaDetails.builder()
                                                     .fee(Fee.builder()
                                                              .calculatedAmountInPence(BigDecimal.valueOf(10800))
                                                              .code("FEE0442")
                                                              .build())
                                                     .build())
                .build();

            Exception exception = assertThrows(
                InvalidPaymentRequestException.class,
                () -> paymentsService.validateRequest(caseData)
            );
            assertThat(exception.getMessage()).isEqualTo(FEE_NOT_SET_CORRECTLY_ERROR);
        }

        @Test
        void validateRequestShouldThrowAnError_whenFeeDetailsDoNotHaveFeeAmount() {
            CaseData caseData = CaseData.builder()
                .hearingFeePBADetails(HFPbaDetails.builder()
                                                     .fee(Fee.builder()
                                                              .code("FEE0442")
                                                              .version("1")
                                                              .build())
                                                     .build())
                .build();

            Exception exception = assertThrows(
                InvalidPaymentRequestException.class,
                () -> paymentsService.validateRequest(caseData)
            );
            assertThat(exception.getMessage()).isEqualTo(FEE_NOT_SET_CORRECTLY_ERROR);
        }

        @Test
        void shouldCreatePaymentServiceRequest_whenValidCaseDetails() {

            CaseData caseData = CaseDataBuilder.builder().buildMakePaymentsCaseData();
            PaymentServiceResponse serviceRequestResponse = paymentsService.createServiceRequest(caseData, AUTH_TOKEN);
            assertThat(serviceRequestResponse).isEqualTo(PAYMENT_SERVICE_RESPONSE);

        }
    }
}

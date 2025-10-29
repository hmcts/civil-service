package uk.gov.hmcts.reform.civil.ga.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.prd.model.ContactInformation;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.payments.client.InvalidPaymentRequestException;
import uk.gov.hmcts.reform.payments.client.PaymentsClient;
import uk.gov.hmcts.reform.payments.request.PBAServiceRequestDTO;
import uk.gov.hmcts.reform.payments.response.PBAServiceRequestResponse;
import uk.gov.hmcts.reform.payments.response.PaymentServiceResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    GaPaymentsService.class,
    JacksonAutoConfiguration.class
})
class GaPaymentsServiceTest {

    private static final String SERVICE = "service";

    private static final String SITE_ID = "site_id";
    private static final String SPEC_SITE_ID = "spec_site_id";
    private static final String AUTH_TOKEN = "Bearer token";
    private static final PBAServiceRequestResponse PAYMENT_DTO = PBAServiceRequestResponse.builder()
        .paymentReference("RC-1234-1234-1234-1234").build();

    private static final PaymentServiceResponse PAYMENT_SERVICE_RESPONSE = PaymentServiceResponse.builder()
        .serviceRequestReference("RC-1234-1234-1234-1234").build();
    private static final Organisation ORGANISATION_RESPONSE = Organisation.builder()
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
    private GaPaymentsService paymentsService;

    @BeforeEach
    void setUp() {
        given(paymentsClient.createServiceRequest(any(), any())).willReturn(PAYMENT_SERVICE_RESPONSE);
        given(paymentsConfiguration.getService()).willReturn(SERVICE);
        given(paymentsConfiguration.getSiteId()).willReturn(SITE_ID);
        given(paymentsConfiguration.getSpecSiteId()).willReturn(SPEC_SITE_ID);
        given(paymentsClient.createPbaPayment(any(), any(), any())).willReturn(PAYMENT_DTO);
        given(organisationService.findOrganisationById(any())).willReturn(Optional.of(ORGANISATION_RESPONSE));
    }

    @Test
    void validateRequestShouldNotThrowAnError_whenValidCaseDataIsProvided() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildMakePaymentsCaseData();
        paymentsService.validateRequest(caseData);
        assertThat(caseData).isNotNull();
    }

    @Test
    void validateRequestShouldThrowAnError_whenPBADetailsNotProvided() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().organisationIdentifier("OrgId").build())
            .build();

        Exception exception = assertThrows(
            InvalidPaymentRequestException.class,
            () -> paymentsService.validateRequest(caseData)
        );
        assertThat(exception.getMessage()).isEqualTo("PBA details not received.");
    }

    @Test
    void validateRequestShouldThrowAnError_whenFeeDetailsNotProvided() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .generalAppPBADetails(GAPbaDetails.builder().build())
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().organisationIdentifier("OrgId").build())
            .build();

        Exception exception = assertThrows(
            InvalidPaymentRequestException.class,
            () -> paymentsService.validateRequest(caseData)
        );
        assertThat(exception.getMessage()).isEqualTo(FEE_NOT_SET_CORRECTLY_ERROR);
    }

    @Test
    void validateRequestShouldThrowAnError_whenFeeDetailsDoNotHaveFeeCode() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .fee(Fee.builder()
                                               .calculatedAmountInPence(BigDecimal.valueOf(10800))
                                               .version("1")
                                               .build())
                                      .build())
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().organisationIdentifier("OrgId").build())
            .build();

        Exception exception = assertThrows(
            InvalidPaymentRequestException.class,
            () -> paymentsService.validateRequest(caseData)
        );
        assertThat(exception.getMessage()).isEqualTo(FEE_NOT_SET_CORRECTLY_ERROR);
    }

    @Test
    void validateRequestShouldThrowAnError_whenFeeDetailsDoNotHaveFeeVersion() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .fee(Fee.builder()
                                               .calculatedAmountInPence(BigDecimal.valueOf(10800))
                                               .code("FEE0442")
                                               .build())
                                      .build())
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().organisationIdentifier("OrgId").build())
            .build();

        Exception exception = assertThrows(
            InvalidPaymentRequestException.class,
            () -> paymentsService.validateRequest(caseData)
        );
        assertThat(exception.getMessage()).isEqualTo(FEE_NOT_SET_CORRECTLY_ERROR);
    }

    @Test
    void validateRequestShouldThrowAnError_whenFeeDetailsDoNotHaveFeeAmount() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .fee(Fee.builder()
                                               .code("FEE0442")
                                               .version("1")
                                               .build())
                                      .build())
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().organisationIdentifier("OrgId").build())
            .build();

        Exception exception = assertThrows(
            InvalidPaymentRequestException.class,
            () -> paymentsService.validateRequest(caseData)
        );
        assertThat(exception.getMessage()).isEqualTo(FEE_NOT_SET_CORRECTLY_ERROR);
    }

    @Test
    void validateRequestShouldThrowAnError_whenApplicantSolicitorDetailsAreNotSet() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .generalAppPBADetails(GAPbaDetails.builder().fee(Fee.builder().build()).build())
            .build();

        Exception exception = assertThrows(
            InvalidPaymentRequestException.class,
            () -> paymentsService.validateRequest(caseData)
        );
        assertThat(exception.getMessage()).isEqualTo("Applicant's organization details not received.");
    }

    @Test
    void validateRequestShouldThrowAnError_whenApplicantSolicitorOrgDetailsAreNotSet() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .generalAppPBADetails(GAPbaDetails.builder().fee(Fee.builder().build()).build())
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().build())
            .build();

        Exception exception = assertThrows(
            InvalidPaymentRequestException.class,
            () -> paymentsService.validateRequest(caseData)
        );
        assertThat(exception.getMessage()).isEqualTo("Applicant's organization details not received.");
    }

    @Test
    void validateRequestShouldNotThrowAnError_whenApplicantSolicitorOrgDetailsAreNotSetForLiPApplicant() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .isGaApplicantLip(YesOrNo.YES)
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .fee(Fee.builder()
                                               .calculatedAmountInPence(BigDecimal.TEN)
                                               .version("version")
                                               .code("code").build()).build())
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().build())
            .build();

        paymentsService.validateRequest(caseData);

        assertThat(caseData).isNotNull();
    }

    @Test
    void shouldCreateCreditAccountPayment_whenValidCaseDetails() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildMakePaymentsCaseData();

        var expectedCreditAccountPaymentRequest = getExpectedCreditAccountPaymentRequest(caseData);

        PBAServiceRequestResponse paymentResponse = paymentsService.createCreditAccountPayment(caseData, AUTH_TOKEN);

        verify(organisationService).findOrganisationById("OrgId");
        assertThat(paymentResponse).isEqualTo(PAYMENT_DTO);
    }

    private PBAServiceRequestDTO getExpectedCreditAccountPaymentRequest(GeneralApplicationCaseData caseData) {
        return PBAServiceRequestDTO.builder()
            .accountNumber("PBA0078095")
            .amount(caseData.getGeneralAppPBADetails().getFee().toFeeDto().getCalculatedAmount())
            .customerReference(CUSTOMER_REFERENCE)
            .organisationName(ORGANISATION_RESPONSE.getName())
            .idempotencyKey("2634946490")
            .build();
    }

    @Test
    void shouldCreatePaymentServiceRequest_whenValidCaseDetails() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildMakePaymentsCaseData();
        PaymentServiceResponse serviceRequestResponse = paymentsService.createServiceRequest(caseData, AUTH_TOKEN);
        assertThat(caseData.getGeneralAppSuperClaimType()).isEqualTo("UNSPEC_CLAIM");
        assertThat(serviceRequestResponse).isEqualTo(PAYMENT_SERVICE_RESPONSE);

    }

    @Test
    void shouldCreatePaymentServiceRequest_whenGaTypeIsSpecClaim() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildMakePaymentsCaseData();
        caseData = caseData.toBuilder().generalAppSuperClaimType("SPEC_CLAIM").build();
        PaymentServiceResponse serviceRequestResponse = paymentsService.createServiceRequest(caseData, AUTH_TOKEN);
        assertThat(caseData.getGeneralAppSuperClaimType()).isEqualTo("SPEC_CLAIM");
        assertThat(serviceRequestResponse).isEqualTo(PAYMENT_SERVICE_RESPONSE);

    }

}

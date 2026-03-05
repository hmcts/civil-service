package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.SRPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;
import uk.gov.hmcts.reform.payments.client.InvalidPaymentRequestException;
import uk.gov.hmcts.reform.payments.client.PaymentsClient;
import uk.gov.hmcts.reform.payments.response.PBAServiceRequestResponse;
import uk.gov.hmcts.reform.payments.response.PaymentServiceResponse;
import uk.gov.hmcts.reform.civil.prd.model.ContactInformation;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    PaymentsService.class,
    JacksonAutoConfiguration.class
})
class PaymentsServiceTest {

    private static final String SERVICE = "service";
    private static final String SITE_ID = "site_id";
    private static final String SPEC_SITE_ID = "spec_site_id";
    private static final String AUTH_TOKEN = "Bearer token";
    private static final PBAServiceRequestResponse PAYMENT_DTO = PBAServiceRequestResponse.builder()
        .paymentReference("RC-1234-1234-1234-1234").build();
    private static final PaymentServiceResponse PAYMENT_SERVICE_RESPONSE = PaymentServiceResponse.builder()
        .serviceRequestReference("RC-1234-1234-1234-1234").build();
    private static final Organisation ORGANISATION = new Organisation()
        .setName("test org")
        .setContactInformation(List.of(new ContactInformation()));
    private static final String CUSTOMER_REFERENCE = "12345";
    private static final String FEE_NOT_SET_CORRECTLY_ERROR = "Fees are not set correctly.";

    @MockBean
    private PaymentsClient paymentsClient;

    @MockBean
    private PaymentsConfiguration paymentsConfiguration;

    @MockBean
    private OrganisationService organisationService;

    @MockBean
    private HearingFeesService hearingFeesService;

    @Autowired
    private PaymentsService paymentsService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        given(paymentsClient.createServiceRequest(any(), any())).willReturn(PAYMENT_SERVICE_RESPONSE);
        given(paymentsConfiguration.getService()).willReturn(SERVICE);
        given(paymentsConfiguration.getSiteId()).willReturn(SITE_ID);
        given(paymentsConfiguration.getSpecSiteId()).willReturn(SPEC_SITE_ID);
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
    void shouldCreatePaymentServiceRequest_whenValidCaseDetails() {

        CaseData caseData = CaseDataBuilder.builder().buildClaimIssuedPaymentCaseData();
        PaymentServiceResponse serviceRequestResponse = paymentsService.createServiceRequest(caseData, AUTH_TOKEN);
        assertThat(serviceRequestResponse).isEqualTo(PAYMENT_SERVICE_RESPONSE);

    }

    @Test
    void validateRequestShouldThrowAnError_whenFeeDetailsNotProvided() {
        uk.gov.hmcts.reform.ccd.model.Organisation orgId = new uk.gov.hmcts.reform.ccd.model.Organisation().setOrganisationID("OrgId");
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setClaimIssuedPBADetails(new SRPbaDetails());
        OrganisationPolicy policy = new OrganisationPolicy();
        policy.setOrganisation(orgId);
        caseData.setApplicant1OrganisationPolicy(policy);

        Exception exception = assertThrows(
            InvalidPaymentRequestException.class,
            () -> paymentsService.validateRequest(caseData)
        );
        assertThat(exception.getMessage()).isEqualTo(FEE_NOT_SET_CORRECTLY_ERROR);
    }

    @Test
    void validateRequestShouldThrowAnError_whenFeeDetailsNotProvided_withSpecAllocatedTrack() {
        uk.gov.hmcts.reform.ccd.model.Organisation orgId = new uk.gov.hmcts.reform.ccd.model.Organisation().setOrganisationID("OrgId");
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setResponseClaimTrack(FAST_CLAIM.name());
        caseData.setClaimIssuedPBADetails(new SRPbaDetails());
        OrganisationPolicy policy = new OrganisationPolicy();
        policy.setOrganisation(orgId);
        caseData.setApplicant1OrganisationPolicy(policy);

        Exception exception = assertThrows(
            InvalidPaymentRequestException.class,
            () -> paymentsService.validateRequest(caseData)
        );
        assertThat(exception.getMessage()).isEqualTo(FEE_NOT_SET_CORRECTLY_ERROR);
    }

    @Test
    void validateRequestShouldThrowAnError_whenFeeDetailsDoNotHaveFeeCode() {
        Fee fee = new Fee();
        fee.setCalculatedAmountInPence(BigDecimal.valueOf(10800));
        fee.setVersion("1");
        SRPbaDetails claimIssuedPBADetails = new SRPbaDetails();
        claimIssuedPBADetails.setFee(fee);
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setClaimIssuedPBADetails(claimIssuedPBADetails);

        uk.gov.hmcts.reform.ccd.model.Organisation orgId = new uk.gov.hmcts.reform.ccd.model.Organisation().setOrganisationID("OrgId");

        OrganisationPolicy policy = new OrganisationPolicy();
        policy.setOrganisation(orgId);
        caseData.setApplicant1OrganisationPolicy(policy);

        Exception exception = assertThrows(
            InvalidPaymentRequestException.class,
            () -> paymentsService.validateRequest(caseData)
        );
        assertThat(exception.getMessage()).isEqualTo(FEE_NOT_SET_CORRECTLY_ERROR);
    }

    @Test
    void validateRequestShouldThrowAnError_whenFeeDetailsDoNotHaveFeeAmount() {
        Fee fee = new Fee();
        fee.setCode("FEE0442");
        fee.setVersion("1");
        SRPbaDetails claimIssuedPBADetails = new SRPbaDetails();
        claimIssuedPBADetails.setFee(fee);
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setClaimIssuedPBADetails(claimIssuedPBADetails);

        uk.gov.hmcts.reform.ccd.model.Organisation orgId = new uk.gov.hmcts.reform.ccd.model.Organisation().setOrganisationID("OrgId");

        OrganisationPolicy policy = new OrganisationPolicy();
        policy.setOrganisation(orgId);
        caseData.setApplicant1OrganisationPolicy(policy);

        Exception exception = assertThrows(
            InvalidPaymentRequestException.class,
            () -> paymentsService.validateRequest(caseData)
        );
        assertThat(exception.getMessage()).isEqualTo(FEE_NOT_SET_CORRECTLY_ERROR);
    }

    @Test
    void shouldCreateCreditAccountPayment_whenValidCaseDetails() {
        DynamicListElement dynamicListElement = new DynamicListElement();
        dynamicListElement.setLabel("account-no");
        DynamicList applicantsPbaAccounts = new DynamicList();
        applicantsPbaAccounts.setValue(dynamicListElement);
        SRPbaDetails hfPbaDetails = new SRPbaDetails();
        hfPbaDetails.setServiceReqReference("request-reference");
        hfPbaDetails.setApplicantsPbaAccounts(applicantsPbaAccounts);

        uk.gov.hmcts.reform.ccd.model.Organisation orgId = new uk.gov.hmcts.reform.ccd.model.Organisation().setOrganisationID("OrgId");

        OrganisationPolicy policy = new OrganisationPolicy();
        policy.setOrganisation(orgId);
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted()
                .applicant1OrganisationPolicy(policy)
                .build();
        caseData.setCaseAccessCategory(SPEC_CLAIM);
        caseData.setClaimIssuedPBADetails(hfPbaDetails);

        PBAServiceRequestResponse paymentResponse = paymentsService
                .createPbaPayment(caseData, AUTH_TOKEN);

        verify(organisationService).findOrganisationById("OrgId");
        verify(paymentsClient).createPbaPayment(eq("request-reference"), eq(AUTH_TOKEN), any());
        assertThat(paymentResponse).isEqualTo(PAYMENT_DTO);
    }

    //General Application Tests
    @Test
    void validateRequestGaShouldNotThrowAnError_whenValidCaseDataIsProvided() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildMakePaymentsCaseData();
        paymentsService.validateRequestGa(caseData);
        assertThat(caseData).isNotNull();
    }

    @Test
    void validateRequestGaShouldThrowAnError_whenPBADetailsNotProvided() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().organisationIdentifier("OrgId").build())
            .build();

        Exception exception = assertThrows(
            InvalidPaymentRequestException.class,
            () -> paymentsService.validateRequestGa(caseData)
        );
        assertThat(exception.getMessage()).isEqualTo("PBA details not received.");
    }

    @Test
    void validateRequestGaShouldThrowAnError_whenFeeDetailsNotProvided() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .generalAppPBADetails(new GeneralApplicationPbaDetails())
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().organisationIdentifier("OrgId").build())
            .build();

        Exception exception = assertThrows(
            InvalidPaymentRequestException.class,
            () -> paymentsService.validateRequestGa(caseData)
        );
        assertThat(exception.getMessage()).isEqualTo(FEE_NOT_SET_CORRECTLY_ERROR);
    }

    @Test
    void validateRequestGaShouldThrowAnError_whenFeeDetailsDoNotHaveFeeCode() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .generalAppPBADetails(new GeneralApplicationPbaDetails()
                                      .setFee(new Fee()
                                               .setCalculatedAmountInPence(BigDecimal.valueOf(10800))
                                               .setVersion("1")
                                               )
                                      )
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().organisationIdentifier("OrgId").build())
            .build();

        Exception exception = assertThrows(
            InvalidPaymentRequestException.class,
            () -> paymentsService.validateRequestGa(caseData)
        );
        assertThat(exception.getMessage()).isEqualTo(FEE_NOT_SET_CORRECTLY_ERROR);
    }

    @Test
    void validateRequestGaShouldThrowAnError_whenFeeDetailsDoNotHaveFeeVersion() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .generalAppPBADetails(new GeneralApplicationPbaDetails()
                                      .setFee(new Fee()
                                               .setCalculatedAmountInPence(BigDecimal.valueOf(10800))
                                               .setCode("FEE0442")
                                               )
                                      )
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().organisationIdentifier("OrgId").build())
            .build();

        Exception exception = assertThrows(
            InvalidPaymentRequestException.class,
            () -> paymentsService.validateRequestGa(caseData)
        );
        assertThat(exception.getMessage()).isEqualTo(FEE_NOT_SET_CORRECTLY_ERROR);
    }

    @Test
    void validateRequestGaShouldThrowAnError_whenFeeDetailsDoNotHaveFeeAmount() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .generalAppPBADetails(new GeneralApplicationPbaDetails()
                                      .setFee(new Fee()
                                               .setCode("FEE0442")
                                               .setVersion("1")
                                               )
                                      )
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().organisationIdentifier("OrgId").build())
            .build();

        Exception exception = assertThrows(
            InvalidPaymentRequestException.class,
            () -> paymentsService.validateRequestGa(caseData)
        );
        assertThat(exception.getMessage()).isEqualTo(FEE_NOT_SET_CORRECTLY_ERROR);
    }

    @Test
    void validateRequestGaShouldThrowAnError_whenApplicantSolicitorDetailsAreNotSet() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .generalAppPBADetails(new GeneralApplicationPbaDetails().setFee(new Fee()))
            .build();

        Exception exception = assertThrows(
            InvalidPaymentRequestException.class,
            () -> paymentsService.validateRequestGa(caseData)
        );
        assertThat(exception.getMessage()).isEqualTo("Applicant's organization details not received.");
    }

    @Test
    void validateRequestGaShouldThrowAnError_whenApplicantSolicitorOrgDetailsAreNotSet() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .generalAppPBADetails(new GeneralApplicationPbaDetails().setFee(new Fee()))
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().build())
            .build();

        Exception exception = assertThrows(
            InvalidPaymentRequestException.class,
            () -> paymentsService.validateRequestGa(caseData)
        );
        assertThat(exception.getMessage()).isEqualTo("Applicant's organization details not received.");
    }

    @Test
    void validateRequestGaShouldNotThrowAnError_whenApplicantSolicitorOrgDetailsAreNotSetForLiPApplicant() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .isGaApplicantLip(YesOrNo.YES)
            .generalAppPBADetails(new GeneralApplicationPbaDetails()
                                      .setFee(new Fee()
                                               .setCalculatedAmountInPence(BigDecimal.TEN)
                                               .setVersion("version")
                                               .setCode("code")))
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().build())
            .build();

        paymentsService.validateRequestGa(caseData);

        assertThat(caseData).isNotNull();
    }

    @Test
    void shouldCreatePaymentServiceRequestGa_whenValidCaseDetails() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildMakePaymentsCaseData();
        PaymentServiceResponse serviceRequestResponse = paymentsService.createServiceRequestGa(caseData, AUTH_TOKEN);
        assertThat(caseData.getGeneralAppSuperClaimType()).isEqualTo("UNSPEC_CLAIM");
        assertThat(serviceRequestResponse).isEqualTo(PAYMENT_SERVICE_RESPONSE);

    }

    @Test
    void shouldCreatePaymentServiceRequestGa_whenGaTypeIsSpecClaim() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildMakePaymentsCaseData();
        caseData = caseData.copy().generalAppSuperClaimType("SPEC_CLAIM").build();
        PaymentServiceResponse serviceRequestResponse = paymentsService.createServiceRequestGa(caseData, AUTH_TOKEN);
        assertThat(caseData.getGeneralAppSuperClaimType()).isEqualTo("SPEC_CLAIM");
        assertThat(serviceRequestResponse).isEqualTo(PAYMENT_SERVICE_RESPONSE);

    }
}

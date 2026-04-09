package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimValue;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CalculateFeeTaskTest extends BaseCallbackHandlerTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private FeesService feesService;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private CalculateFeeTask calculateFeeTask;

    @BeforeEach
    public void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        calculateFeeTask = new CalculateFeeTask(featureToggleService, feesService, objectMapper, organisationService);
    }

    @Test
    void shouldCalculateFeesSuccessfully() {
        CaseData caseData = CaseDataBuilder.builder().build();
        ClaimValue claimValue = new ClaimValue();
        claimValue.setStatementOfValueInPennies(BigDecimal.valueOf(10000L));
        caseData.setClaimValue(claimValue);
        SolicitorReferences solicitorReferences = new SolicitorReferences();
        solicitorReferences.setApplicantSolicitor1Reference("REF123");
        caseData.setSolicitorReferences(solicitorReferences);
        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setCustomerReference("CUST123");
        caseData.setClaimIssuedPaymentDetails(paymentDetails);

        List<String> pbaAccounts = List.of("PBA1234567");
        Fee fee = new Fee();
        fee.setCalculatedAmountInPence(BigDecimal.valueOf(1000L));
        given(feesService.getFeeDataByClaimValue(any())).willReturn(fee);
        Organisation organisation = new Organisation();
        organisation.setPaymentAccount(pbaAccounts);
        given(organisationService.findOrganisation(any())).willReturn(Optional.of(organisation));

        CallbackResponse response = calculateFeeTask.calculateFees(caseData, "authToken");

        assertThat(response).isNotNull();
        Map<String, Object> responseData = ((AboutToStartOrSubmitCallbackResponse) response).getData();
        assertThat(responseData.get("claimFee")).isNotNull();
        assertThat(responseData.get("applicantSolicitor1PbaAccounts")).isNotNull();
    }

    @Test
    void shouldCalculateFeesSuccessfullyWithoutPbaAccounts() {
        Fee fee = new Fee();
        fee.setCalculatedAmountInPence(BigDecimal.valueOf(1000L));
        given(feesService.getFeeDataByClaimValue(any())).willReturn(fee);
        given(organisationService.findOrganisation(any())).willReturn(Optional.empty());

        ClaimValue claimValue = new ClaimValue();
        claimValue.setStatementOfValueInPennies(BigDecimal.valueOf(10000L));
        CaseData caseData = CaseDataBuilder.builder()
            .claimValue(claimValue)
            .build();
        CallbackResponse response = calculateFeeTask.calculateFees(caseData, "authToken");

        Map<String, Object> responseData = ((AboutToStartOrSubmitCallbackResponse) response).getData();
        assertThat(responseData.get("claimFee")).isNotNull();
        assertThat(responseData.get("applicantSolicitor1PbaAccounts")).isNotNull();
    }

    @Test
    void shouldHandleMissingSolicitorReferences() {
        List<String> pbaAccounts = List.of("PBA1234567");
        Fee fee = new Fee();
        fee.setCalculatedAmountInPence(BigDecimal.valueOf(1000L));
        given(feesService.getFeeDataByClaimValue(any())).willReturn(fee);
        Organisation organisation = new Organisation();
        organisation.setPaymentAccount(pbaAccounts);
        given(organisationService.findOrganisation(any())).willReturn(Optional.of(organisation));

        ClaimValue claimValue = new ClaimValue();
        claimValue.setStatementOfValueInPennies(BigDecimal.valueOf(10000L));
        CaseData caseData = CaseDataBuilder.builder()
            .claimValue(claimValue)
            .build();
        CallbackResponse response = calculateFeeTask.calculateFees(caseData, "authToken");

        assertThat(response).isNotNull();
        Map<String, Object> responseData = ((AboutToStartOrSubmitCallbackResponse) response).getData();
        assertThat(responseData.get("claimFee")).isNotNull();
        assertThat(responseData.get("applicantSolicitor1PbaAccounts")).isNotNull();
    }

    @Test
    void shouldHandleNullClaimValue() {
        Fee fee = new Fee();
        fee.setCalculatedAmountInPence(BigDecimal.ZERO);
        given(feesService.getFeeDataByClaimValue(any())).willReturn(fee);
        given(organisationService.findOrganisation(any())).willReturn(Optional.empty());

        CaseData caseData = CaseDataBuilder.builder().build();
        CallbackResponse response = calculateFeeTask.calculateFees(caseData, "authToken");

        Map<String, Object> responseData = ((AboutToStartOrSubmitCallbackResponse) response).getData();
        assertThat(responseData.get("claimFee")).isNotNull();
        assertThat(responseData.get("claimFee").toString()).contains("calculatedAmountInPence=0");
    }

    @Test
    void shouldCalculateFeesSuccessfullyForOtherRemedy() {
        CaseData caseData = CaseDataBuilder.builder().build();
        ClaimValue claimValue = new ClaimValue();
        claimValue.setStatementOfValueInPennies(BigDecimal.valueOf(10000L));
        caseData.setClaimValue(claimValue);
        SolicitorReferences solicitorReferences = new SolicitorReferences();
        solicitorReferences.setApplicantSolicitor1Reference("REF123");
        caseData.setSolicitorReferences(solicitorReferences);
        caseData.setIsClaimDeclarationAdded(YesOrNo.YES);
        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setCustomerReference("CUST123");
        caseData.setClaimIssuedPaymentDetails(paymentDetails);

        final List<String> pbaAccounts = List.of("PBA1234567");
        Fee fee = new Fee();
        fee.setCalculatedAmountInPence(BigDecimal.valueOf(1000L));
        given(feesService.getFeeDataByClaimValue(any())).willReturn(fee);
        given(feesService.getFeeDataForOtherRemedy(any())).willReturn(fee);
        Organisation organisation = new Organisation();
        organisation.setPaymentAccount(pbaAccounts);
        given(organisationService.findOrganisation(any())).willReturn(Optional.of(organisation));

        CallbackResponse response = calculateFeeTask.calculateFees(caseData, "authToken");

        assertThat(response).isNotNull();
        Map<String, Object> responseData = ((AboutToStartOrSubmitCallbackResponse) response).getData();
        assertThat(responseData.get("claimFee")).isNotNull();
        assertThat(responseData.get("applicantSolicitor1PbaAccounts")).isNotNull();
    }
}

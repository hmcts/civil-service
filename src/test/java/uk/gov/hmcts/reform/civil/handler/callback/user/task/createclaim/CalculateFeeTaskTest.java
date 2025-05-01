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
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.CalculateFeeTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimValue;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
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
        CaseData caseData = CaseData.builder()
            .claimValue(ClaimValue.builder().statementOfValueInPennies(BigDecimal.valueOf(10000L)).build())
            .solicitorReferences(SolicitorReferences.builder()
                                     .applicantSolicitor1Reference("REF123").build())
            .claimIssuedPaymentDetails(PaymentDetails.builder().customerReference("CUST123").build())
            .build();

        List<String> pbaAccounts = List.of("PBA1234567");
        given(feesService.getFeeDataByClaimValue(any())).willReturn(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(1000L)).build());
        given(organisationService.findOrganisation(any())).willReturn(Optional.of(Organisation.builder().paymentAccount(pbaAccounts).build()));

        CallbackResponse response = calculateFeeTask.calculateFees(caseData, "authToken");

        assertThat(response).isNotNull();
        Map<String, Object> responseData = ((AboutToStartOrSubmitCallbackResponse) response).getData();
        assertThat(responseData.get("claimFee")).isNotNull();
        assertThat(responseData.get("applicantSolicitor1PbaAccounts")).isNotNull();
    }

    @Test
    void shouldCalculateFeesSuccessfullyWithoutPbaAccounts() {
        CaseData caseData = CaseData.builder()
            .claimValue(ClaimValue.builder().statementOfValueInPennies(BigDecimal.valueOf(10000L)).build())
            .build();

        given(feesService.getFeeDataByClaimValue(any())).willReturn(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(1000L)).build());
        given(organisationService.findOrganisation(any())).willReturn(Optional.empty());

        CallbackResponse response = calculateFeeTask.calculateFees(caseData, "authToken");

        Map<String, Object> responseData = ((AboutToStartOrSubmitCallbackResponse) response).getData();
        assertThat(responseData.get("claimFee")).isNotNull();
        assertThat(responseData.get("applicantSolicitor1PbaAccounts")).isNotNull();
    }

    @Test
    void shouldHandleMissingSolicitorReferences() {
        CaseData caseData = CaseData.builder()
            .claimValue(ClaimValue.builder().statementOfValueInPennies(BigDecimal.valueOf(10000L)).build())
            .build();

        List<String> pbaAccounts = List.of("PBA1234567");
        given(feesService.getFeeDataByClaimValue(any())).willReturn(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(1000L)).build());
        given(organisationService.findOrganisation(any())).willReturn(Optional.of(Organisation.builder().paymentAccount(pbaAccounts).build()));

        CallbackResponse response = calculateFeeTask.calculateFees(caseData, "authToken");

        assertThat(response).isNotNull();
        Map<String, Object> responseData = ((AboutToStartOrSubmitCallbackResponse) response).getData();
        assertThat(responseData.get("claimFee")).isNotNull();
        assertThat(responseData.get("applicantSolicitor1PbaAccounts")).isNotNull();
    }

    @Test
    void shouldHandleNullClaimValue() {
        CaseData caseData = CaseData.builder().build();

        given(feesService.getFeeDataByClaimValue(any())).willReturn(Fee.builder().calculatedAmountInPence(BigDecimal.ZERO).build());
        given(organisationService.findOrganisation(any())).willReturn(Optional.empty());

        CallbackResponse response = calculateFeeTask.calculateFees(caseData, "authToken");

        Map<String, Object> responseData = ((AboutToStartOrSubmitCallbackResponse) response).getData();
        assertThat(responseData.get("claimFee")).isNotNull();
        assertThat(responseData.get("claimFee").toString()).contains("calculatedAmountInPence=0");
    }
}

package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Component
public class CalculateSpecFeeTask {

    private final InterestCalculator interestCalculator;
    private final FeesService feesService;
    private final ObjectMapper objectMapper;
    private final OrganisationService organisationService;

    @Autowired
    public CalculateSpecFeeTask(InterestCalculator interestCalculator, FeatureToggleService featureToggleService,
                                FeesService feesService, ObjectMapper objectMapper, OrganisationService organisationService) {
        this.interestCalculator = interestCalculator;
        this.feesService = feesService;
        this.objectMapper = objectMapper;
        this.organisationService = organisationService;
    }

    public CallbackResponse calculateSpecFee(CaseData caseData, String authorizationToken) {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        updatePaymentDetails(caseData, caseDataBuilder);

        calculateAndUpdateClaimFee(caseData, caseDataBuilder);

        handlePbaAndPaymentType(authorizationToken, caseDataBuilder);

        return buildCallbackResponse(caseDataBuilder);
    }

    private void updatePaymentDetails(CaseData caseData, CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        String solicitorReference = getSolicitorReference(caseData);
        String customerReference = getCustomerReference(caseData, solicitorReference);

        PaymentDetails updatedDetails = PaymentDetails.builder().customerReference(customerReference).build();
        caseDataBuilder.claimIssuedPaymentDetails(updatedDetails);
    }

    private String getSolicitorReference(CaseData caseData) {
        return Optional.ofNullable(caseData.getSolicitorReferences())
            .map(SolicitorReferences::getApplicantSolicitor1Reference)
            .orElse("");
    }

    private String getCustomerReference(CaseData caseData, String solicitorReference) {
        return Optional.ofNullable(caseData.getClaimIssuedPaymentDetails())
            .map(PaymentDetails::getCustomerReference)
            .orElse(solicitorReference);
    }

    private void calculateAndUpdateClaimFee(CaseData caseData, CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        BigDecimal interest = interestCalculator.calculateInterest(caseData);
        BigDecimal totalClaimAmountWithInterest = caseData.getTotalClaimAmount().add(interest);

        caseDataBuilder.claimFee(feesService.getFeeDataByTotalClaimAmount(totalClaimAmountWithInterest))
            .totalInterest(interest);
    }

    private void handlePbaAndPaymentType(String authorizationToken, CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        caseDataBuilder.paymentTypePBASpec("PBAv3");

        List<String> pbaNumbers = getPbaAccounts(authorizationToken);
        caseDataBuilder.applicantSolicitor1PbaAccounts(DynamicList.fromList(pbaNumbers))
            .applicantSolicitor1PbaAccountsIsEmpty(pbaNumbers.isEmpty() ? YES : NO);
    }

    private CallbackResponse buildCallbackResponse(CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private List<String> getPbaAccounts(String authToken) {
        return organisationService.findOrganisation(authToken)
            .map(Organisation::getPaymentAccount)
            .orElse(emptyList());
    }
}

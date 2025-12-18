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

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Component
public class CalculateFeeTask {

    private final FeesService feesService;
    private final ObjectMapper objectMapper;
    private final OrganisationService organisationService;

    @Autowired
    public CalculateFeeTask(FeatureToggleService featureToggleService, FeesService feesService, ObjectMapper objectMapper, OrganisationService organisationService) {
        this.feesService = feesService;
        this.objectMapper = objectMapper;
        this.organisationService = organisationService;
    }

    public CallbackResponse calculateFees(CaseData caseData, String authorizationToken) {
        updatePaymentDetails(caseData);

        updatePaymentType(caseData);

        updatePbaAccounts(authorizationToken, caseData);

        setClaimFee(caseData);

        return buildCallbackResponse(caseData);
    }

    private void updatePaymentDetails(CaseData caseData) {
        String solicitorReference = getSolicitorReference(caseData);
        String customerReference = getCustomerReference(caseData, solicitorReference);

        PaymentDetails updatedDetails = new PaymentDetails();
        updatedDetails.setCustomerReference(customerReference);
        caseData.setClaimIssuedPaymentDetails(updatedDetails);
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

    private void updatePaymentType(CaseData caseData) {
        caseData.setPaymentTypePBASpec("PBAv3");
    }

    private void updatePbaAccounts(String authToken, CaseData caseData) {
        List<String> pbaNumbers = getPbaAccounts(authToken);
        caseData.setApplicantSolicitor1PbaAccounts(DynamicList.fromList(pbaNumbers));
        caseData.setApplicantSolicitor1PbaAccountsIsEmpty(pbaNumbers.isEmpty() ? YES : NO);
    }

    private void setClaimFee(CaseData caseData) {
        caseData.setClaimFee(feesService.getFeeDataByClaimValue(caseData.getClaimValue()));
    }

    private CallbackResponse buildCallbackResponse(CaseData caseData) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private List<String> getPbaAccounts(String authToken) {
        return organisationService.findOrganisation(authToken)
            .map(Organisation::getPaymentAccount)
            .orElse(emptyList());
    }
}

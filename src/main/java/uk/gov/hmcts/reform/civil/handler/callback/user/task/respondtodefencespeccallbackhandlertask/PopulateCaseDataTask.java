package uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtodefencespeccallbackhandlertask;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.PaymentDateService;
import uk.gov.hmcts.reform.civil.service.citizenui.ResponseOneVOneShowTagService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_2;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.PaymentDateService.DATE_FORMATTER;

@Component
@RequiredArgsConstructor
@Slf4j
public class PopulateCaseDataTask implements CaseTask {

    private final LocationReferenceDataService locationRefDataService;
    private final ObjectMapper objectMapper;
    private final CourtLocationUtils courtLocationUtils;
    private final FeatureToggleService featureToggleService;

    private final PaymentDateService paymentDateService;
    private final ResponseOneVOneShowTagService responseOneVOneShowTagService;

    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        log.info("Populating Case Data for Case : {}", caseData.getCcdCaseReference());

        if (isDefendantFullAdmitPayImmediately(caseData)) {
            String whenBePaid = paymentDateService.getFormattedPaymentDate(caseData);
            caseData.setShowResponseOneVOneFlag(responseOneVOneShowTagService.setUpOneVOneFlow(caseData));
            caseData.setWhenToBePaidText(whenBePaid);
        }

        if (isDefendantPartAdmitPayImmediatelyAccepted(caseData)) {
            LocalDate whenBePaid = paymentDateService.calculatePaymentDeadline();
            caseData.setWhenToBePaidText(whenBePaid.format(DATE_FORMATTER));
            RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec = new RespondToClaimAdmitPartLRspec();
            respondToClaimAdmitPartLRspec.setWhenWillThisAmountBePaid(whenBePaid);
            caseData.setRespondToClaimAdmitPartLRspec(respondToClaimAdmitPartLRspec);
        }

        caseData.setRespondent1Copy(caseData.getRespondent1());
        caseData.setClaimantResponseScenarioFlag(getMultiPartyScenario(caseData));
        caseData.setCaseAccessCategory(CaseCategory.SPEC_CLAIM);

        if (featureToggleService.isCarmEnabledForCase(caseData)) {
            caseData.setShowCarmFields(YES);
        } else {
            caseData.setShowCarmFields(NO);
        }

        List<LocationRefData> locations = fetchLocationData(callbackParams);
        RequestedCourt requestedCourt = new RequestedCourt();
        requestedCourt.setResponseCourtLocations(
            courtLocationUtils.getLocationsFromList(locations));
        Applicant1DQ applicant1DQ = new Applicant1DQ();
        applicant1DQ.setApplicant1DQRequestedCourt(requestedCourt);
        caseData.setApplicant1DQ(applicant1DQ);

        if (V_2.equals(callbackParams.getVersion())) {
            caseData.setShowResponseOneVOneFlag(responseOneVOneShowTagService.setUpOneVOneFlow(caseData));
            caseData.setRespondent1PaymentDateToStringSpec(paymentDateService.getFormattedPaymentDate(caseData));

            Optional<BigDecimal> howMuchWasPaid = Optional.ofNullable(caseData.getRespondToAdmittedClaim())
                .map(RespondToClaim::getHowMuchWasPaid);

            howMuchWasPaid.ifPresent(howMuchWasPaidValue -> caseData.setPartAdmitPaidValuePounds(
                MonetaryConversions.penniesToPounds(howMuchWasPaidValue)));

            caseData.setResponseClaimTrack(AllocatedTrack.getAllocatedTrack(caseData.getTotalClaimAmount(),
                                                                                null, null, featureToggleService, caseData
            ).name());
        }
        populatePreviewDocuments(caseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private boolean isDefendantPartAdmitPayImmediatelyAccepted(CaseData caseData) {
        return caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != null
            && IMMEDIATELY.equals(caseData.getDefenceAdmitPartPaymentTimeRouteRequired())
            && (PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec()))
            && YES.equals(caseData.getApplicant1AcceptAdmitAmountPaidSpec());
    }

    private boolean isDefendantFullAdmitPayImmediately(CaseData caseData) {
        return caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != null
            && caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == IMMEDIATELY
            && (FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec()));
    }

    private List<LocationRefData> fetchLocationData(CallbackParams callbackParams) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        return locationRefDataService.getCourtLocationsForDefaultJudgments(authToken);
    }

    private void populatePreviewDocuments(CaseData caseData) {
        if (caseData.getSystemGeneratedCaseDocuments() == null) {
            caseData.setSystemGeneratedCaseDocuments(new ArrayList<>());
        }
        
        if (caseData.getRespondent2DocumentURL() == null) {
            caseData.getSystemGeneratedCaseDocuments().forEach(document -> {
                if (document.getValue().getDocumentName().contains("defendant_directions_questionnaire_form")) {
                    caseData.setRespondent1GeneratedResponseDocument(document.getValue());
                }
            });
        } else {
            caseData.getSystemGeneratedCaseDocuments().forEach(document -> {
                if (document.getValue().getDocumentLink().getDocumentUrl().equals(caseData.getRespondent1DocumentURL())) {
                    caseData.setRespondent1GeneratedResponseDocument(document.getValue());
                }
                if (document.getValue().getDocumentLink().getDocumentUrl().equals(caseData.getRespondent2DocumentURL())) {
                    caseData.setRespondent2GeneratedResponseDocument(document.getValue());
                }
            });
        }
        caseData.getSystemGeneratedCaseDocuments().forEach(document -> {
            if (document.getValue().getDocumentName().contains("response_sealed_form.pdf")) {
                caseData.setRespondent1ClaimResponseDocumentSpec(document.getValue());
            }
        });
    }
}

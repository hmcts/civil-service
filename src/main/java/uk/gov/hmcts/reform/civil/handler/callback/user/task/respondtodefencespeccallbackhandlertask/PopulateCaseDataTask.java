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
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.PaymentDateService;
import uk.gov.hmcts.reform.civil.service.citizenui.ResponseOneVOneShowTagService;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_2;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class PopulateCaseDataTask implements CaseTask {

    private final LocationReferenceDataService locationRefDataService;
    private final ObjectMapper objectMapper;
    private final CourtLocationUtils courtLocationUtils;
    private final FeatureToggleService featureToggleService;
    private static final String DATE_PATTERN = "dd MMMM yyyy";
    private final PaymentDateService paymentDateService;
    private final ResponseOneVOneShowTagService responseOneVOneShowTagService;
    private final DeadlineExtensionCalculatorService deadlineCalculatorService;

    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        CaseData.CaseDataBuilder<?, ?> updatedCaseData = caseData.toBuilder();

        log.info("Populating Case Data for Case : {}", caseData.getCcdCaseReference());

        if (isDefendantFullAdmitPayImmediately(caseData)) {
            LocalDate whenBePaid = paymentDateService.getPaymentDateAdmittedClaim(caseData);
            updatedCaseData.showResponseOneVOneFlag(responseOneVOneShowTagService.setUpOneVOneFlow(caseData));
            updatedCaseData.whenToBePaidText(formatLocalDate(whenBePaid, DATE));
        }

        updatedCaseData.respondent1Copy(caseData.getRespondent1())
            .claimantResponseScenarioFlag(getMultiPartyScenario(caseData))
            .caseAccessCategory(CaseCategory.SPEC_CLAIM);

        if (featureToggleService.isCarmEnabledForCase(caseData)) {
            updatedCaseData.showCarmFields(YES);
        } else {
            updatedCaseData.showCarmFields(NO);
        }

        List<LocationRefData> locations = fetchLocationData(callbackParams);
        updatedCaseData.applicant1DQ(
            Applicant1DQ.builder().applicant1DQRequestedCourt(
                RequestedCourt.builder().responseCourtLocations(
                    courtLocationUtils.getLocationsFromList(locations)).build()
            ).build());

        if (V_2.equals(callbackParams.getVersion()) && featureToggleService.isPinInPostEnabled()) {
            updatedCaseData.showResponseOneVOneFlag(responseOneVOneShowTagService.setUpOneVOneFlow(caseData));
            updatedCaseData.respondent1PaymentDateToStringSpec(setUpPayDateToString(caseData));

            Optional<BigDecimal> howMuchWasPaid = Optional.ofNullable(caseData.getRespondToAdmittedClaim())
                .map(RespondToClaim::getHowMuchWasPaid);

            howMuchWasPaid.ifPresent(howMuchWasPaidValue -> updatedCaseData.partAdmitPaidValuePounds(
                MonetaryConversions.penniesToPounds(howMuchWasPaidValue)));

            updatedCaseData.responseClaimTrack(AllocatedTrack.getAllocatedTrack(caseData.getTotalClaimAmount(),
                                                                                null, null, featureToggleService, caseData
            ).name());
        }
        populatePreviewDocuments(caseData, updatedCaseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
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

    private String setUpPayDateToString(CaseData caseData) {
        if (caseData.getRespondToClaimAdmitPartLRspec() != null
            && caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid() != null) {
            return caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid()
                .format(DateTimeFormatter.ofPattern(DATE_PATTERN, Locale.ENGLISH));
        }
        if (caseData.getRespondToAdmittedClaim() != null
            && caseData.getRespondToAdmittedClaim().getWhenWasThisAmountPaid() != null) {
            return caseData.getRespondToAdmittedClaim().getWhenWasThisAmountPaid()
                .format(DateTimeFormatter.ofPattern(DATE_PATTERN, Locale.ENGLISH));
        }
        if (caseData.getRespondent1ResponseDate() != null) {
            return deadlineCalculatorService.calculateExtendedDeadline(
                    caseData.getRespondent1ResponseDate().toLocalDate(),
                    RespondentResponsePartAdmissionPaymentTimeLRspec.DAYS_TO_PAY_IMMEDIATELY)
                .format(DateTimeFormatter.ofPattern(DATE_PATTERN, Locale.ENGLISH));
        }
        return null;
    }

    private void populatePreviewDocuments(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        if (caseData.getRespondent2DocumentURL() == null) {
            caseData.getSystemGeneratedCaseDocuments().forEach(document -> {
                if (document.getValue().getDocumentName().contains("defendant_directions_questionnaire_form")) {
                    updatedCaseData.respondent1GeneratedResponseDocument(document.getValue());
                }
            });
        } else {
            caseData.getSystemGeneratedCaseDocuments().forEach(document -> {
                if (document.getValue().getDocumentLink().getDocumentUrl().equals(caseData.getRespondent1DocumentURL())) {
                    updatedCaseData.respondent1GeneratedResponseDocument(document.getValue());
                }
                if (document.getValue().getDocumentLink().getDocumentUrl().equals(caseData.getRespondent2DocumentURL())) {
                    updatedCaseData.respondent2GeneratedResponseDocument(document.getValue());
                }
            });
        }
        if (featureToggleService.isPinInPostEnabled()) {
            caseData.getSystemGeneratedCaseDocuments().forEach(document -> {
                if (document.getValue().getDocumentName().contains("response_sealed_form.pdf")) {
                    updatedCaseData.respondent1ClaimResponseDocumentSpec(document.getValue());
                }
            });
        }
    }
}

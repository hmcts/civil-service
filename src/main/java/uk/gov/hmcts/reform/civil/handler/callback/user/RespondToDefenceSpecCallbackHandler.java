package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.CaseDataToTextGenerator;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGenerator;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationTextGenerator;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocation;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.HearingLRspec;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.SmallClaimHearing;
import uk.gov.hmcts.reform.civil.model.referencedata.response.LocationRefData;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;
import uk.gov.hmcts.reform.civil.validation.interfaces.ExpertsValidator;
import uk.gov.hmcts.reform.civil.validation.interfaces.WitnessesValidator;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE_SPEC;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.SuperClaimType.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Service
@RequiredArgsConstructor
@Slf4j
public class RespondToDefenceSpecCallbackHandler extends CallbackHandler
    implements ExpertsValidator, WitnessesValidator {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CLAIMANT_RESPONSE_SPEC);
    private final ObjectMapper objectMapper;
    private final Time time;
    private final UnavailableDateValidator unavailableDateValidator;
    private final List<RespondToResponseConfirmationHeaderGenerator> confirmationHeaderGenerators;
    private final List<RespondToResponseConfirmationTextGenerator> confirmationTextGenerators;
    private final LocationRefDataService locationRefDataService;
    private final CourtLocationUtils courtLocationUtils;
    private final FeatureToggleService featureToggleService;
    private final LocationHelper locationHelper = new LocationHelper();

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(MID, "experts"), this::validateApplicantExperts,
            callbackKey(MID, "witnesses"), this::validateApplicantWitnesses,
            callbackKey(MID, "statement-of-truth"), this::resetStatementOfTruth,
            callbackKey(MID, "validate-unavailable-dates"), this::validateUnavailableDates,
            callbackKey(MID, "set-applicant1-proceed-flag"), this::setApplicant1ProceedFlag,
            callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit,
            callbackKey(V_1, ABOUT_TO_SUBMIT), this::aboutToSubmit,
            callbackKey(ABOUT_TO_START), this::populateCaseData,
            callbackKey(V_1, ABOUT_TO_START), this::populateCaseData,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    private CallbackResponse validateUnavailableDates(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        List<String> errors;
        if (SpecJourneyConstantLRSpec.SMALL_CLAIM.equals(caseData.getResponseClaimTrack())) {
            SmallClaimHearing smallClaimHearing = caseData.getApplicant1DQ().getApplicant1DQSmallClaimHearing();
            errors = unavailableDateValidator.validateSmallClaimsHearing(smallClaimHearing);
        } else {
            HearingLRspec hearingLRspec = caseData.getApplicant1DQ().getApplicant1DQHearingLRspec();
            errors = unavailableDateValidator.validateFastClaimHearing(hearingLRspec);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse validateApplicantWitnesses(CallbackParams callbackParams) {
        return validateWitnesses(callbackParams.getCaseData().getApplicant1DQ());
    }

    private CallbackResponse validateApplicantExperts(CallbackParams callbackParams) {
        return validateExperts(callbackParams.getCaseData().getApplicant1DQ());
    }

    private CallbackResponse setApplicant1ProceedFlag(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        var updatedCaseData = caseData.toBuilder();
        if (TWO_V_ONE.equals(getMultiPartyScenario(caseData))
            && YES.equals(caseData.getApplicant1ProceedWithClaimSpec2v1())) {
            updatedCaseData.applicant1ProceedWithClaim(YES);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse resetStatementOfTruth(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        // resetting statement of truth field, this resets in the page, but the data is still sent to the db.
        // setting null here does not clear, need to overwrite with value.
        // must be to do with the way XUI cache data entered through the lifecycle of an event.
        CaseData updatedCaseData = caseData.toBuilder()
            .uiStatementOfTruth(StatementOfTruth.builder().role("").build())
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
            .businessProcess(BusinessProcess.ready(CLAIMANT_RESPONSE_SPEC))
            .applicant1ResponseDate(time.now());

        if (callbackParams.getVersion() == V_1) {
            locationHelper.getCaseManagementLocation(caseData)
                .ifPresent(requestedCourt -> locationHelper.updateCaseManagementLocation(
                    builder,
                    requestedCourt,
                    () -> locationRefDataService.getCourtLocationsForDefaultJudgments(callbackParams.getParams().get(
                        CallbackParams.Params.BEARER_TOKEN).toString())
                ));
            if (log.isDebugEnabled()) {
                log.debug("Case management location for " + caseData.getLegacyCaseReference()
                              + " is " + builder.build().getCaseManagementLocation());
            }
        }

        if (caseData.getApplicant1ProceedWithClaim() == YES
            || caseData.getApplicant1ProceedWithClaimSpec2v1() == YES) {
            // moving statement of truth value to correct field, this was not possible in mid event.
            StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
            Applicant1DQ.Applicant1DQBuilder dq = caseData.getApplicant1DQ().toBuilder()
                .applicant1DQStatementOfTruth(statementOfTruth);
            if (V_1.equals(callbackParams.getVersion())
                && featureToggleService.isCourtLocationDynamicListEnabledSpec()) {
                handleCourtLocationData(caseData, builder, dq, callbackParams);
            }

            builder.applicant1DQ(dq.build());
            // resetting statement of truth to make sure it's empty the next time it appears in the UI.
            builder.uiStatementOfTruth(StatementOfTruth.builder().build());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(builder.build().toMap(objectMapper))
            .build();
    }

    private void handleCourtLocationData(CaseData caseData, CaseData.CaseDataBuilder dataBuilder,
                                         Applicant1DQ.Applicant1DQBuilder dq,
                                         CallbackParams callbackParams) {
        RequestedCourt requestedCourt = caseData.getApplicant1DQ().getApplicant1DQRequestedCourt();
        if (requestedCourt != null) {
            LocationRefData courtLocation = courtLocationUtils.findPreferredLocationData(
                fetchLocationData(callbackParams), requestedCourt.getResponseCourtLocations());
            if (Objects.nonNull(courtLocation)) {
                dataBuilder
                    .applicant1DQ(dq.applicant1DQRequestedCourt(
                                          caseData.getApplicant1DQ().getApplicant1DQRequestedCourt().toBuilder()
                                              .responseCourtLocations(null)
                                              .caseLocation(CaseLocation.builder()
                                                                .region(courtLocation.getRegionId())
                                                                .baseLocation(courtLocation.getEpimmsId())
                                                                .build())
                                              .responseCourtCode(courtLocation.getCourtLocationCode()).build()
                                      ).build());
            }
        }
    }

    private CallbackResponse populateCaseData(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();

        CaseData.CaseDataBuilder<?, ?> updatedCaseData = caseData.toBuilder();

        if (V_1.equals(callbackParams.getVersion())
            && featureToggleService.isAccessProfilesEnabled()) {
            updatedCaseData.respondent1Copy(caseData.getRespondent1())
                .claimantResponseScenarioFlag(getMultiPartyScenario(caseData));
        } else {
            updatedCaseData.respondent1Copy(caseData.getRespondent1())
                .claimantResponseScenarioFlag(getMultiPartyScenario(caseData))
                .superClaimType(SPEC_CLAIM);
        }

        if (V_1.equals(callbackParams.getVersion()) && featureToggleService.isCourtLocationDynamicListEnabledSpec()) {
            List<LocationRefData> locations = fetchLocationData(callbackParams);
            updatedCaseData.applicant1DQ(Applicant1DQ.builder()
                                             .applicant1DQRequestedCourt(
                                                 RequestedCourt.builder()
                                                     .responseCourtLocations(
                                                         courtLocationUtils.getLocationsFromList(locations))
                                                     .build()
                                             )
                                             .build());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }

    private List<LocationRefData> fetchLocationData(CallbackParams callbackParams) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        return locationRefDataService.getCourtLocationsForDefaultJudgments(authToken);
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (featureToggleService.isSdoEnabled()) {
            caseData.toBuilder().ccdState(CaseState.JUDICIAL_REFERRAL).build();
        }

        SubmittedCallbackResponse.SubmittedCallbackResponseBuilder responseBuilder =
            SubmittedCallbackResponse.builder();

        responseBuilder.confirmationBody(
                CaseDataToTextGenerator.getTextFor(
                    confirmationTextGenerators.stream(),
                    () -> getDefaultConfirmationText(caseData),
                    caseData
                ))
            .confirmationHeader(
                CaseDataToTextGenerator.getTextFor(
                    confirmationHeaderGenerators.stream(),
                    () -> getDefaultConfirmationHeader(caseData),
                    caseData
                ));

        return responseBuilder.build();
    }

    private String getDefaultConfirmationText(CaseData caseData) {
        if (YesOrNo.YES.equals(caseData.getApplicant1ProceedWithClaim())
            || YES.equals(caseData.getApplicant1ProceedWithClaimSpec2v1())) {
            return "<h2 class=\"govuk-heading-m\">What happens next</h2>"
                + "We'll review the case and contact you about what to do next.<br>"
                + format(
                "%n%n<a href=\"%s\" target=\"_blank\">View Directions questionnaire</a>",
                format("/cases/case-details/%s#Claim documents", caseData.getCcdCaseReference())
            );
        } else {
            return "<h2 class=\"govuk-heading-m\">What happens next</h2>"
                + "You've decided not to proceed and the case will end.<br>"
                + format(
                "%n%n<a href=\"%s\" target=\"_blank\">View Directions questionnaire</a>",
                format("/cases/case-details/%s#Claim documents", caseData.getCcdCaseReference())
            );
        }
    }

    private String getDefaultConfirmationHeader(CaseData caseData) {
        String claimNumber = caseData.getLegacyCaseReference();
        if (YesOrNo.YES.equals(caseData.getApplicant1ProceedWithClaim())
            || YES.equals(caseData.getApplicant1ProceedWithClaimSpec2v1())) {
            return format(
                "# You have decided to proceed with the claim%n## Claim number: %s",
                claimNumber
            );
        } else {
            return format(
                "# You have decided not to proceed with the claim%n## Claim number: %s",
                claimNumber
            );
        }
    }
}

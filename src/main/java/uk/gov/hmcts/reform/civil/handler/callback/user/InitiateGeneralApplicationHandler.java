package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService.INVALID_SETTLE_BY_CONSENT;

@Service
@RequiredArgsConstructor
public class InitiateGeneralApplicationHandler extends CallbackHandler {

    private static final String VALIDATE_URGENCY_DATE_PAGE = "ga-validate-urgency-date";
    private static final String VALIDATE_GA_CONSENT = "ga-validate-consent";
    private static final String VALIDATE_GA_TYPE = "ga-validate-type";
    private static final String VALIDATE_HEARING_DATE = "ga-validate-hearing-date";
    private static final String VALIDATE_HEARING_PAGE = "ga-hearing-screen-validation";
    private static final String INVALID_HEARING_DATE = "The hearing date must be in the future";
    private static final String SET_FEES_AND_PBA = "ga-fees-and-pba";
    private static final String POUND_SYMBOL = "£";
    private static final List<CaseEvent> EVENTS = Collections.singletonList(INITIATE_GENERAL_APPLICATION);
    private static final String RESP_NOT_ASSIGNED_ERROR = "Application cannot be created until all the required "
            + "respondent solicitor are assigned to the case.";
    private final InitiateGeneralApplicationService initiateGeneralApplicationService;
    private final ObjectMapper objectMapper;
    private final OrganisationService organisationService;
    private final IdamClient idamClient;
    private final GeneralAppFeesService feesService;
    private final LocationRefDataService locationRefDataService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::aboutToStartValidattionAndSetup,
            callbackKey(MID, VALIDATE_GA_TYPE), this::gaValidateType,
            callbackKey(MID, VALIDATE_HEARING_DATE), this::gaValidateHearingDate,
            callbackKey(MID, VALIDATE_GA_CONSENT), this::gaValidateConsent,
            callbackKey(MID, VALIDATE_URGENCY_DATE_PAGE), this::gaValidateUrgencyDate,
            callbackKey(MID, VALIDATE_HEARING_PAGE), this::gaValidateHearingScreen,
            callbackKey(MID, SET_FEES_AND_PBA), this::setFeesAndPBA,
            callbackKey(ABOUT_TO_SUBMIT), this::submitApplication,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse aboutToStartValidattionAndSetup(CallbackParams callbackParams) {

        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();
        if (!initiateGeneralApplicationService.respondentAssigned(caseData)) {
            errors.add(RESP_NOT_ASSIGNED_ERROR);
        }

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        caseDataBuilder
                .generalAppHearingDetails(
                    GAHearingDetails
                        .builder()
                        .hearingPreferredLocation(getLocationsFromList(locationRefDataService
                                                               .getCourtLocationsForGeneralApplication(authToken)))
                        .build());
        return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .data(caseDataBuilder.build().toMap(objectMapper))
                .build();
    }

    private DynamicList getLocationsFromList(final List<LocationRefData> locations) {
        return fromList(locations.stream().map(location -> new StringBuilder().append(location.getSiteName())
                .append(" - ").append(location.getCourtAddress())
                .append(" - ").append(location.getPostcode()).toString())
                            .collect(Collectors.toList()));
    }

    private CallbackResponse gaValidateConsent(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        var generalAppTypes = caseData.getGeneralAppType().getTypes();
        var consent = Objects.nonNull(caseData.getGeneralAppRespondentAgreement())
                                && YES.equals(caseData.getGeneralAppRespondentAgreement().getHasAgreed());
        List<String> errors = new ArrayList<>();
        if (generalAppTypes.size() == 1
                && generalAppTypes.contains(GeneralApplicationTypes.SETTLE_BY_CONSENT)
                && !consent) {
            errors.add(INVALID_SETTLE_BY_CONSENT);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataBuilder.build().toMap(objectMapper))
                .errors(errors)
                .build();
    }

    private CallbackResponse gaValidateType(CallbackParams callbackParams) {

        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        List<String> errors = new ArrayList<>();
        var generalAppTypes = caseData.getGeneralAppType().getTypes();
        if (generalAppTypes.size() > 1
            && generalAppTypes.contains(GeneralApplicationTypes.VARY_JUDGEMENT)) {
            errors.add("It is not possible to select an additional application type when applying to vary judgment");
        }
        if (generalAppTypes.size() > 1
                && generalAppTypes.contains(GeneralApplicationTypes.SETTLE_BY_CONSENT)) {
            errors.add("It is not possible to select an additional application type " +
                    "when applying to Settle by consent");
        }

        if (generalAppTypes.size() == 1
            && generalAppTypes.contains(GeneralApplicationTypes.VARY_JUDGEMENT)) {
            caseDataBuilder.generalAppVaryJudgementType(YesOrNo.YES)
                    .generalAppInformOtherParty(
                            GAInformOtherParty.builder().isWithNotice(YesOrNo.YES).build());
        } else {
            caseDataBuilder.generalAppVaryJudgementType(YesOrNo.NO);
        }

        UserDetails userDetails = idamClient.getUserDetails(callbackParams.getParams().get(BEARER_TOKEN).toString());
        boolean isGAApplicantSameAsParentCaseClaimant = initiateGeneralApplicationService
                .isGAApplicantSameAsParentCaseClaimant(caseData, userDetails);
        caseDataBuilder
            .generalAppParentClaimantIsApplicant(isGAApplicantSameAsParentCaseClaimant ? YES : YesOrNo.NO).build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private CallbackResponse gaValidateHearingDate(CallbackParams callbackParams) {
        List<String> errors = new ArrayList<>();

        CaseData caseData = callbackParams.getCaseData();
        if (caseData.getGeneralAppHearingDate() != null
            && caseData.getGeneralAppHearingDate().getHearingScheduledPreferenceYesNo().equals(YesOrNo.YES)
            && caseData.getGeneralAppHearingDate().getHearingScheduledDate().isBefore(LocalDate.now())) {
            errors.add(INVALID_HEARING_DATE);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse gaValidateUrgencyDate(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        GAUrgencyRequirement generalAppUrgencyRequirement = caseData.getGeneralAppUrgencyRequirement();
        List<String> errors = generalAppUrgencyRequirement != null
            ? initiateGeneralApplicationService.validateUrgencyDates(generalAppUrgencyRequirement)
            : Collections.emptyList();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse gaValidateHearingScreen(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        GAHearingDetails hearingDetails = caseData.getGeneralAppHearingDetails();
        List<String> errors = hearingDetails != null
            ? initiateGeneralApplicationService.validateHearingScreen(hearingDetails)
            : Collections.emptyList();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse setFeesAndPBA(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        caseData = setWithNoticeByType(caseData);
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        Fee feeForGA = feesService.getFeeForGA(caseData);
        caseDataBuilder.generalAppPBADetails(GAPbaDetails.builder()
                .generalAppFeeToPayInText(POUND_SYMBOL + feeForGA.toPounds().toString())
                .fee(feeForGA)
                .build());

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataBuilder.build().toMap(objectMapper))
                .build();
    }

    private CaseData.CaseDataBuilder<?, ?> getSharedData(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        // second idam call is workaround for null pointer when hiding field in getIdamEmail callback
        return caseData.toBuilder();
    }

    private CallbackResponse submitApplication(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        caseData = setWithNoticeByType(caseData);
        UserDetails userDetails = idamClient.getUserDetails(callbackParams.getParams().get(BEARER_TOKEN).toString());

        // second idam call is workaround for null pointer when hiding field in getIdamEmail callback
        CaseData.CaseDataBuilder<?, ?> dataBuilder = getSharedData(callbackParams);

        if (caseData.getGeneralAppPBADetails().getFee() == null) {
            Fee feeForGA = feesService.getFeeForGA(caseData);
            GAPbaDetails generalAppPBADetails = caseData.getGeneralAppPBADetails().toBuilder().fee(feeForGA).build();
            CaseData newCaseData = caseData.toBuilder().generalAppPBADetails(generalAppPBADetails).build();
            caseData = newCaseData;
        }

        if ((caseData.getGeneralAppHearingDetails().getHearingPreferredLocation() != null)) {
            List<String> applicationLocationList = List.of(caseData.getGeneralAppHearingDetails()
                                                               .getHearingPreferredLocation()
                                                               .getValue().getLabel());
            DynamicList dynamicLocationList = fromList(applicationLocationList);
            Optional<DynamicListElement> first = dynamicLocationList.getListItems().stream()
                .filter(l -> l.getLabel().equals(applicationLocationList.get(0))).findFirst();
            first.ifPresent(dynamicLocationList::setValue);
            GAHearingDetails generalAppHearingDetails = caseData.getGeneralAppHearingDetails().toBuilder()
                .hearingPreferredLocation(dynamicLocationList).build();
            CaseData updatedCaseData = caseData.toBuilder()
                .generalAppHearingDetails(generalAppHearingDetails)
                .generalAppParentClaimantIsApplicant(null)
                .build();
            caseData = updatedCaseData;
        } else {
            GAHearingDetails generalAppHearingDetails = caseData.getGeneralAppHearingDetails().toBuilder()
                .hearingPreferredLocation(DynamicList.builder().build()).build();
            CaseData updatedCaseData = caseData.toBuilder()
                .generalAppHearingDetails(generalAppHearingDetails)
                .generalAppParentClaimantIsApplicant(null)
                .build();
            caseData = updatedCaseData;
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(initiateGeneralApplicationService
                      .buildCaseData(dataBuilder, caseData, userDetails, callbackParams.getParams().get(BEARER_TOKEN)
                          .toString()).toMap(objectMapper)).build();
    }

    /**
     * Returns empty submitted callback response. Used by events that set business process to ready, but doesn't have
     * any submitted callback logic (making callback is still required to trigger EventEmitterAspect)
     *
     * @param callbackParams This parameter is required as this is passed as reference for execute method in CallBack
     * @return empty submitted callback response
     */
    protected CallbackResponse emptySubmittedCallbackResponse(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder().build();
    }

    private CaseData setWithNoticeByType(CaseData caseData) {
        if (Objects.nonNull(caseData.getGeneralAppType())
                && caseData.getGeneralAppType().getTypes().size() == 1
                && caseData.getGeneralAppType().getTypes().contains(GeneralApplicationTypes.VARY_JUDGEMENT)) {
            caseData = caseData.toBuilder()
                    .generalAppInformOtherParty(
                            GAInformOtherParty.builder().isWithNotice(YesOrNo.YES).build()).build();
        }
        return caseData;
    }
}

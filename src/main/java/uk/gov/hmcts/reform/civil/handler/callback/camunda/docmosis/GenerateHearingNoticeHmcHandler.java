package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.hearing.HearingNoticeHmcGenerator;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeVariables;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.HearingFeeUtils;
import uk.gov.hmcts.reform.civil.utils.HmcDataUtils;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.hmc.service.HearingsService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.jsonwebtoken.lang.Collections.isEmpty;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_NOTICE_HMC;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_NOTICE_HMC_WELSH;
import static uk.gov.hmcts.reform.civil.utils.DateUtils.convertFromUTC;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.HearingFeeUtils.calculateAndApplyFee;
import static uk.gov.hmcts.reform.civil.utils.HmcDataUtils.getHearingDays;
import static uk.gov.hmcts.reform.civil.utils.HmcDataUtils.getLocationRefData;
import static uk.gov.hmcts.reform.civil.utils.HmcDataUtils.isWelshHearingTemplate;
import static uk.gov.hmcts.reform.civil.utils.HmcDataUtils.getTotalHearingDurationInMinutes;

@Service
@RequiredArgsConstructor
public class GenerateHearingNoticeHmcHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        CaseEvent.GENERATE_HEARING_NOTICE_HMC
    );
    private static final String TASK_ID = "GenerateHearingNotice";

    private final HearingNoticeCamundaService camundaService;

    private final HearingsService hearingsService;
    private final HearingNoticeHmcGenerator hearingNoticeHmcGenerator;
    private final ObjectMapper objectMapper;
    private final LocationReferenceDataService locationRefDataService;
    private final HearingFeesService hearingFeesService;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::generateHearingNotice);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private CallbackResponse generateHearingNotice(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String processInstanceId = caseData.getBusinessProcess().getProcessInstanceId();
        String bearerToken = callbackParams.getParams().get(BEARER_TOKEN).toString();

        var camundaVars = camundaService.getProcessVariables(processInstanceId);
        var hearing = hearingsService.getHearingResponse(
            bearerToken,
            camundaVars.getHearingId()
        );

        var hearingStartDay = HmcDataUtils.getHearingStartDay(hearing);
        String hearingLocation = getHearingLocation(camundaVars.getHearingId(), hearing,
                                                    bearerToken, locationRefDataService, false);

        buildDocument(callbackParams, hearing, hearingLocation, camundaVars.getHearingId(), HEARING_NOTICE_HMC);

        // Check DQ document language if Welsh not enabled, only check main language flag if enabled
        if ((!featureToggleService.isWelshEnabledForMainCase() && isWelshHearingTemplate(caseData))
                || (featureToggleService.isWelshEnabledForMainCase() && (caseData.isClaimantBilingual() || caseData.isRespondentResponseBilingual()))) {
            String hearingLocationWelsh = getHearingLocation(camundaVars.getHearingId(), hearing,
                                                        bearerToken, locationRefDataService, true);
            buildDocument(callbackParams, hearing, hearingLocationWelsh, camundaVars.getHearingId(), HEARING_NOTICE_HMC_WELSH);
        }

        var hearingStartDate = convertFromUTC(hearingStartDay.getHearingStartDateTime());
        HearingNoticeVariables updatedVars = new HearingNoticeVariables();
        updatedVars.setHearingId(camundaVars.getHearingId());
        updatedVars.setCaseId(camundaVars.getCaseId());
        updatedVars.setHearingStartDateTime(hearingStartDate);
        updatedVars.setHearingLocationEpims(hearingStartDay.getHearingVenueId());
        updatedVars.setDays(getHearingDays(hearing));
        updatedVars.setRequestVersion(hearing.getRequestDetails().getVersionNumber());
        updatedVars.setCaseState(caseData.getCcdState().name());
        updatedVars.setResponseDateTime(hearing.getHearingResponse().getReceivedDateTime());
        updatedVars.setHearingType(hearing.getHearingDetails().getHearingType());

        camundaService.setProcessVariables(processInstanceId, updatedVars);

        Integer totalDurationInMinutes = getTotalHearingDurationInMinutes(hearing);
        caseData.setHearingDurationInMinutesAHN(totalDurationInMinutes.toString());
        caseData.setTrialReadyNotified(null);
        caseData.setHearingDate(hearingStartDate.toLocalDate());
        caseData.setHearingDueDate(HearingFeeUtils.calculateHearingDueDate(LocalDate.now(), hearingStartDate.toLocalDate()));
        DynamicListElement dynamicListElement = new DynamicListElement();
        dynamicListElement.setLabel(hearingLocation);
        DynamicList dynamicList = new DynamicList();
        dynamicList.setValue(dynamicListElement);
        caseData.setHearingLocation(dynamicList);
        String claimTrack = determineClaimTrack(caseData);
        caseData.setHearingFee(calculateAndApplyFee(hearingFeesService, caseData, claimTrack));
        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toMap(objectMapper))
                .build();
    }

    private void buildDocument(CallbackParams callbackParams, HearingGetResponse hearing,
                               String hearingLocation, String hearingId, DocmosisTemplates template) {
        CaseData caseData = callbackParams.getCaseData();
        List<CaseDocument> caseDocuments = hearingNoticeHmcGenerator.generate(
            caseData,
            hearing,
            callbackParams.getParams().get(BEARER_TOKEN).toString(),
            hearingLocation,
            hearingId,
            template
        );
        List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();
        systemGeneratedCaseDocuments.add(element(caseDocuments.get(0)));
        if (HEARING_NOTICE_HMC_WELSH.equals(template)) {
            if (!isEmpty(caseData.getHearingDocumentsWelsh())) {
                systemGeneratedCaseDocuments.addAll(caseData.getHearingDocumentsWelsh());
            }
            caseData.setHearingDocumentsWelsh(systemGeneratedCaseDocuments);
        } else {
            if (!isEmpty(caseData.getHearingDocuments())) {
                systemGeneratedCaseDocuments.addAll(caseData.getHearingDocuments());
            }
            caseData.setHearingDocuments(systemGeneratedCaseDocuments);
        }
    }

    private String getHearingLocation(String hearingId, HearingGetResponse hearing,
                                      String bearerToken, LocationReferenceDataService locationRefDataService,
                                      boolean isWelsh) {
        LocationRefData hearingLocation = getLocationRefData(
            hearingId,
            HmcDataUtils.getHearingStartDay(hearing).getHearingVenueId(),
            bearerToken,
            locationRefDataService);
        if (hearingLocation != null) {
            return isWelsh
                ? LocationReferenceDataService.getDisplayEntryWelsh(hearingLocation)
                : LocationReferenceDataService.getDisplayEntry(hearingLocation);
        }
        return null;
    }

    private String determineClaimTrack(CaseData caseData) {
        if (caseData.getCaseAccessCategory().equals(UNSPEC_CLAIM)) {
            return caseData.getAllocatedTrack().name();
        } else if (caseData.getCaseAccessCategory().equals(SPEC_CLAIM)) {
            return caseData.getResponseClaimTrack();
        }
        return null;
    }
}

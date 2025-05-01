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
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        String processInstanceId = caseData.getBusinessProcess().getProcessInstanceId();
        String bearerToken = callbackParams.getParams().get(BEARER_TOKEN).toString();

        var camundaVars = camundaService.getProcessVariables(processInstanceId);
        var hearing = hearingsService.getHearingResponse(
            bearerToken,
            camundaVars.getHearingId()
        );

        var hearingStartDay = HmcDataUtils.getHearingStartDay(hearing);
        var hearingStartDate = convertFromUTC(hearingStartDay.getHearingStartDateTime());
        String hearingLocation = getHearingLocation(camundaVars.getHearingId(), hearing,
                                                    bearerToken, locationRefDataService, false);

        buildDocument(callbackParams, caseDataBuilder, hearing, hearingLocation, camundaVars.getHearingId(), HEARING_NOTICE_HMC);

        if (featureToggleService.isHmcForLipEnabled() && isWelshHearingTemplate(caseData)) {
            String hearingLocationWelsh = getHearingLocation(camundaVars.getHearingId(), hearing,
                                                        bearerToken, locationRefDataService, true);
            buildDocument(callbackParams, caseDataBuilder, hearing, hearingLocationWelsh, camundaVars.getHearingId(), HEARING_NOTICE_HMC_WELSH);
        }

        camundaService.setProcessVariables(
            processInstanceId,
            camundaVars.toBuilder()
                .hearingStartDateTime(hearingStartDate)
                .hearingLocationEpims(hearingStartDay.getHearingVenueId())
                .days(getHearingDays(hearing))
                .requestVersion(hearing.getRequestDetails().getVersionNumber())
                .caseState(caseData.getCcdState().name())
                .responseDateTime(hearing.getHearingResponse().getReceivedDateTime())
                .hearingType(hearing.getHearingDetails().getHearingType())
                .build()
        );

        String claimTrack = determineClaimTrack(caseData);
        Integer totalDurationInMinutes = getTotalHearingDurationInMinutes(hearing);
        if (featureToggleService.isHmcForLipEnabled()) {
            caseDataBuilder.hearingDurationInMinutesAHN(totalDurationInMinutes.toString())
                .trialReadyNotified(null);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder
                      .hearingDate(hearingStartDate.toLocalDate())
                      .hearingDueDate(HearingFeeUtils.calculateHearingDueDate(LocalDate.now(), hearingStartDate.toLocalDate()))
                      .hearingLocation(DynamicList.builder().value(DynamicListElement.builder()
                                                                       .label(hearingLocation)
                                                                       .build()).build())
                      .hearingFee(featureToggleService.isCaseProgressionEnabled()
                                      ? calculateAndApplyFee(hearingFeesService, caseData, claimTrack)
                                      : null)
                      .build().toMap(objectMapper))
            .build();
    }

    private void buildDocument(CallbackParams callbackParams, CaseData.CaseDataBuilder<?, ?> caseDataBuilder, HearingGetResponse hearing,
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
            caseDataBuilder.hearingDocumentsWelsh(systemGeneratedCaseDocuments);
        } else {
            if (!isEmpty(caseData.getHearingDocuments())) {
                systemGeneratedCaseDocuments.addAll(caseData.getHearingDocuments());
            }
            caseDataBuilder.hearingDocuments(systemGeneratedCaseDocuments);
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

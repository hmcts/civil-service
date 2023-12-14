package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REFER_TO_JUDGE;

@Service
@RequiredArgsConstructor
public class CreateReferToJudgeCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(REFER_TO_JUDGE);
    public static final String CONFIRMATION_HEADER = "# Your order has been referred to Judge%n## Claim number: %s";
    private static final Set<Party.Type> PEOPLE = EnumSet.of(Party.Type.INDIVIDUAL, Party.Type.SOLE_TRADER);
    private final LocationRefDataService locationRefDataService;
    private final LocationHelper locationHelper;

    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::emptyCallbackResponse)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::submitReferToJudge)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse submitReferToJudge(CallbackParams callbackParams) {
        CaseData.CaseDataBuilder dataBuilder = getSharedData(callbackParams);
        CaseData caseData = callbackParams.getCaseData();
        boolean leadDefendantIs1 = locationHelper.leadDefendantIs1(caseData);
        Supplier<Party.Type> getDefendantType;

        if (leadDefendantIs1) {
            getDefendantType = caseData.getRespondent1()::getType;
        } else {
            getDefendantType = caseData.getRespondent2()::getType;
        }

        if (CaseCategory.UNSPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {

            locationHelper.getClaimantRequestedCourt(caseData)
                    .filter(this::hasInfo)
                    .ifPresent(requestedCourt -> {
                        locationHelper.getMatching(locationRefDataService.getCourtLocationsForDefaultJudgments(
                        callbackParams.getParams().get(BEARER_TOKEN).toString()), requestedCourt)
                            .ifPresent(matchingLocation -> LocationHelper.updateWithLocation(dataBuilder, matchingLocation));
                    });
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(dataBuilder.build().toMap(objectMapper))
            .build();
    }

    private boolean hasInfo(RequestedCourt requestedCourt) {
        return StringUtils.isNotBlank(requestedCourt.getResponseCourtCode())
            || Optional.ofNullable(requestedCourt.getResponseCourtLocations())
            .map(DynamicList::getValue).isPresent();
    }

    private CaseData.CaseDataBuilder getSharedData(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder dataBuilder = caseData.toBuilder();

        return dataBuilder;
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(getHeader(caseData))
            .confirmationBody("<p>&nbsp;</p>")
            .build();
    }

    private String getHeader(CaseData caseData) {
        return format(
            CONFIRMATION_HEADER,
            caseData.getLegacyCaseReference()
        );
    }

}

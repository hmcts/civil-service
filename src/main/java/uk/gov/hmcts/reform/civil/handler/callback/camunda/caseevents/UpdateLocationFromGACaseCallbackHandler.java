package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.lang.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.service.GenAppStateHelperService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_GA_LOC_TO_MAIN_CASE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.model.Party.Type.INDIVIDUAL;
import static uk.gov.hmcts.reform.civil.model.Party.Type.SOLE_TRADER;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateLocationFromGACaseCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(UPDATE_GA_LOC_TO_MAIN_CASE);

    private final GenAppStateHelperService helper;
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::triggerUpdateGaLocation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse triggerUpdateGaLocation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();
        if (!Collections.isEmpty(caseData.getGeneralApplications())) {
            try {
                Pair<CaseLocationCivil, Boolean> caseLocation = getWorkAllocationLocation(caseData);
                caseData.getGeneralApplications().forEach(generalApplicationElement -> {
                   generalApplicationElement.getValue().toBuilder().caseManagementLocation(
                               caseLocation.getLeft())
                       .isCcmccLocation(NO);
                });
            } catch (Exception e) {
                String errorMessage = "Error occurred while updating claim with GA location: " + e.getMessage();
                log.error(errorMessage);
                errors.add(errorMessage);
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .errors(errors)
                    .build();
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private Pair<CaseLocationCivil, Boolean> getWorkAllocationLocation(CaseData caseData) {
            if (!(MultiPartyScenario.isMultiPartyScenario(caseData))) {
                if (INDIVIDUAL.equals(caseData.getRespondent1().getType())
                    || SOLE_TRADER.equals(caseData.getRespondent1().getType())) {
                    return Pair.of(getDefendant1PreferredLocation(caseData), false);
                } else {
                    return Pair.of(getClaimant1PreferredLocation(caseData), false);
                }
            } else {
                if (INDIVIDUAL.equals(caseData.getRespondent1().getType())
                    || SOLE_TRADER.equals(caseData.getRespondent1().getType())
                    || INDIVIDUAL.equals(caseData.getRespondent2().getType())
                    || SOLE_TRADER.equals(caseData.getRespondent2().getType())) {

                    return Pair.of(getDefendantPreferredLocation(caseData), false);
                } else {
                    return Pair.of(getClaimant1PreferredLocation(caseData), false);
                }
            }
    }
    private CaseLocationCivil getClaimant1PreferredLocation(CaseData caseData) {
        if (caseData.getApplicant1DQ() == null
            || caseData.getApplicant1DQ().getApplicant1DQRequestedCourt() == null
            || caseData.getApplicant1DQ().getApplicant1DQRequestedCourt().getResponseCourtCode() == null) {
            return CaseLocationCivil.builder()
                .region(caseData.getCourtLocation().getCaseLocation().getRegion())
                .baseLocation(caseData.getCourtLocation().getCaseLocation().getBaseLocation())
                .build();
        }
        return CaseLocationCivil.builder()
            .region(caseData.getApplicant1DQ().getApplicant1DQRequestedCourt()
                        .getCaseLocation().getRegion())
            .baseLocation(caseData.getApplicant1DQ().getApplicant1DQRequestedCourt()
                              .getCaseLocation().getBaseLocation())
            .build();
    }

    private boolean isDefendant1RespondedFirst(CaseData caseData) {
        return caseData.getRespondent2ResponseDate() == null
            || (caseData.getRespondent1ResponseDate() != null
            && !caseData.getRespondent1ResponseDate().isAfter(caseData.getRespondent2ResponseDate()));
    }

    private CaseLocationCivil getDefendant1PreferredLocation(CaseData caseData) {
        if (caseData.getRespondent1DQ() == null
            || caseData.getRespondent1DQ().getRespondent1DQRequestedCourt() == null
            || caseData.getRespondent1DQ().getRespondent1DQRequestedCourt().getResponseCourtCode() == null) {
            return CaseLocationCivil.builder().build();
        }
        return CaseLocationCivil.builder()
            .region(caseData.getRespondent1DQ().getRespondent1DQRequestedCourt()
                        .getCaseLocation().getRegion())
            .baseLocation(caseData.getRespondent1DQ().getRespondent1DQRequestedCourt()
                              .getCaseLocation().getBaseLocation())
            .build();
    }

    private CaseLocationCivil getDefendantPreferredLocation(CaseData caseData) {
        if (isDefendant1RespondedFirst(caseData) & !(caseData.getRespondent1DQ() == null
            || caseData.getRespondent1DQ().getRespondent1DQRequestedCourt() == null)) {

            return CaseLocationCivil.builder()
                .region(caseData.getRespondent1DQ().getRespondent1DQRequestedCourt()
                            .getCaseLocation().getRegion())
                .baseLocation(caseData.getRespondent1DQ().getRespondent1DQRequestedCourt()
                                  .getCaseLocation().getBaseLocation())
                .build();
        } else if (!(isDefendant1RespondedFirst(caseData)) || !(caseData.getRespondent2DQ() == null
            || caseData.getRespondent2DQ().getRespondent2DQRequestedCourt() == null)) {
            return CaseLocationCivil.builder()
                .region(caseData.getRespondent2DQ().getRespondent2DQRequestedCourt()
                            .getCaseLocation().getRegion())
                .baseLocation(caseData.getRespondent2DQ().getRespondent2DQRequestedCourt()
                                  .getCaseLocation().getBaseLocation())
                .build();
        } else {
            return CaseLocationCivil.builder().build();
        }
    }
}

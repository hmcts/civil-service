package uk.gov.hmcts.reform.civil.service.generalapplications;

import io.jsonwebtoken.lang.Collections;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.GenAppStateHelperService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;

import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPLICATION_PROCEEDS_IN_HERITAGE;

@Component
@Slf4j
@AllArgsConstructor
public class GeneralAppsTakeOffLineService {


    private final GenAppStateHelperService helperService;
    private static final String APPLICATION_PROCEEDS_OFFLINE_DESCRIPTION = "Proceeds In Heritage";


    public void takeOffLine(CaseData caseData, CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {

        log.info("Taking application offline for caseId: {}", caseData.getCcdCaseReference());
        //PROCEEDS_IN_HERITAGE_SYSTEM
        caseDataBuilder
            .takenOfflineDate(LocalDateTime.now());
        try {
            //APPLICATION_OFFLINE_UPDATE_CLAIM
            if (caseData.getGeneralApplications() != null && !caseData.getGeneralApplications().isEmpty()) {
                helperService.triggerEvent(caseData, APPLICATION_PROCEEDS_IN_HERITAGE);

                caseData = helperService.updateApplicationDetailsInClaim(caseData,
                            APPLICATION_PROCEEDS_OFFLINE_DESCRIPTION,
                            GenAppStateHelperService.RequiredState.APPLICATION_PROCEEDS_OFFLINE);

            }
        } catch (Exception e) {
            String errorMessage = "Could not take application offline for case: "
                + caseData.getCcdCaseReference();
            log.error(errorMessage, e);
        }

    }
}

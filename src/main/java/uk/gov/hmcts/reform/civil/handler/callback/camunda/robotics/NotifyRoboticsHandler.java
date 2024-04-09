package uk.gov.hmcts.reform.civil.handler.callback.camunda.robotics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.networknt.schema.ValidationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsCaseData;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsCaseDataSpec;
import uk.gov.hmcts.reform.civil.service.robotics.JsonSchemaValidationService;
import uk.gov.hmcts.reform.civil.service.robotics.RoboticsNotificationService;
import uk.gov.hmcts.reform.civil.service.robotics.exception.JsonSchemaValidationException;
import uk.gov.hmcts.reform.civil.service.robotics.exception.RoboticsDataException;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapperForSpec;

import java.util.Set;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isMultiPartyScenario;

@Slf4j
@RequiredArgsConstructor
public abstract class NotifyRoboticsHandler extends CallbackHandler {

    protected final RoboticsNotificationService roboticsNotificationService;
    private final JsonSchemaValidationService jsonSchemaValidationService;
    private final RoboticsDataMapper roboticsDataMapper;
    private final RoboticsDataMapperForSpec roboticsDataMapperForSpec;
    protected final FeatureToggleService toggleService;

    protected CallbackResponse notifyRobotics(CallbackParams callbackParams) {
        if (toggleService.isRPAEmailEnabled()) {
            RoboticsCaseData roboticsCaseData = null;
            RoboticsCaseDataSpec roboticsCaseDataSpec = null;
            Set<ValidationMessage> errors = null;

            CaseData caseData = callbackParams.getCaseData();
            String legacyCaseReference = caseData.getLegacyCaseReference();
            boolean multiPartyScenario = isMultiPartyScenario(caseData);
            try {
                log.info(String.format("Start notify robotics for %s", legacyCaseReference));
                if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
                    roboticsCaseDataSpec = roboticsDataMapperForSpec.toRoboticsCaseData(caseData);
                    errors = jsonSchemaValidationService.validate(roboticsCaseDataSpec.toJsonString());
                } else {
                    log.info(String.format("Unspec robotics Data Mapping for %s", legacyCaseReference));
                    roboticsCaseData = roboticsDataMapper.toRoboticsCaseData(
                        caseData,
                        callbackParams.getParams().get(BEARER_TOKEN).toString()
                    );
                    errors = jsonSchemaValidationService.validate(roboticsCaseData.toJsonString());
                }

                if (errors == null || errors.isEmpty()) {
                    log.info(String.format("Valid RPA Json payload for %s", legacyCaseReference));
                    sendNotifications(callbackParams, caseData, multiPartyScenario);
                } else {
                    throw new JsonSchemaValidationException(
                        format("Invalid RPA Json payload for %s", legacyCaseReference), errors);
                }
            } catch (JsonProcessingException e) {
                throw new RoboticsDataException(e.getMessage(), e);
            }
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }

    protected void sendNotifications(CallbackParams callbackParams, CaseData caseData, boolean multiPartyScenario) {
        roboticsNotificationService.notifyRobotics(caseData, multiPartyScenario,
                                                   callbackParams.getParams().get(BEARER_TOKEN).toString()
        );
    }
}

package uk.gov.hmcts.reform.civil.handler.callback.camunda.robotics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.networknt.schema.ValidationMessage;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
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
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.SuperClaimType.SPEC_CLAIM;

@RequiredArgsConstructor
public abstract class NotifyRoboticsHandler extends CallbackHandler {

    private final RoboticsNotificationService roboticsNotificationService;
    private final JsonSchemaValidationService jsonSchemaValidationService;
    private final RoboticsDataMapper roboticsDataMapper;
    private final RoboticsDataMapperForSpec roboticsDataMapperForSpec;
    private final FeatureToggleService toggleService;

    protected CallbackResponse notifyRobotics(CallbackParams callbackParams) {
        RoboticsCaseData roboticsCaseData = null;
        RoboticsCaseDataSpec roboticsCaseDataSpec = null;
        Set<ValidationMessage> errors = null;

        CaseData caseData = callbackParams.getCaseData();
        boolean multiPartyScenario = isMultiPartyScenario(caseData);
        try {

            if (SPEC_CLAIM.equals(caseData.getSuperClaimType())) {
                if (toggleService.isLrSpecEnabled()) {
                    roboticsCaseDataSpec = roboticsDataMapperForSpec.toRoboticsCaseData(caseData);
                    errors = jsonSchemaValidationService.validate(roboticsCaseDataSpec.toJsonString());
                } else {
                    throw new UnsupportedOperationException("Specified claims are not enabled");
                }
            } else {
                roboticsCaseData = roboticsDataMapper.toRoboticsCaseData(caseData);
                errors = jsonSchemaValidationService.validate(roboticsCaseData.toJsonString());
            }

            if (errors == null || errors.isEmpty()) {
                roboticsNotificationService.notifyRobotics(caseData, multiPartyScenario);
            } else {
                throw new JsonSchemaValidationException(
                    format("Invalid RPA Json payload for %s", caseData.getLegacyCaseReference()),
                    errors
                );
            }
        } catch (JsonProcessingException e) {
            throw new RoboticsDataException(e.getMessage(), e);
        }
        return AboutToStartOrSubmitCallbackResponse.builder().build();
    }
}

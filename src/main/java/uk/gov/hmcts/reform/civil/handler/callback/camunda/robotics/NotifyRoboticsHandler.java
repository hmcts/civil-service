package uk.gov.hmcts.reform.civil.handler.callback.camunda.robotics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.networknt.schema.ValidationMessage;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsCaseData;
import uk.gov.hmcts.reform.civil.service.robotics.JsonSchemaValidationService;
import uk.gov.hmcts.reform.civil.service.robotics.RoboticsNotificationService;
import uk.gov.hmcts.reform.civil.service.robotics.exception.JsonSchemaValidationException;
import uk.gov.hmcts.reform.civil.service.robotics.exception.RoboticsDataException;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapper;

import java.util.Set;

import static java.lang.String.format;

@RequiredArgsConstructor
public abstract class NotifyRoboticsHandler extends CallbackHandler {

    private final RoboticsNotificationService roboticsNotificationService;
    private final JsonSchemaValidationService jsonSchemaValidationService;
    private final RoboticsDataMapper roboticsDataMapper;

    protected CallbackResponse notifyRoboticsForCaseHandedOffline(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        try {
            RoboticsCaseData roboticsCaseData = roboticsDataMapper.toRoboticsCaseData(caseData);
            Set<ValidationMessage> errors = jsonSchemaValidationService.validate(roboticsCaseData.toJsonString());
            if (errors.isEmpty()) {
                roboticsNotificationService.notifyRobotics(caseData);
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

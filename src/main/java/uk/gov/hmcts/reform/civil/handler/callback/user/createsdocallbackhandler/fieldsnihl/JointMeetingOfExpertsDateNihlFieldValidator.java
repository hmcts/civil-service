package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fieldsnihl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JointMeetingOfExpertsDateNihlFieldValidator implements NihlFieldValidator {

    private final FieldsNihlUtils fieldsNihlUtils;

    @Override
    public void validate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2PermissionToRelyOnExpert() != null
                && caseData.getSdoR2PermissionToRelyOnExpert().getSdoJointMeetingOfExpertsDate() != null) {
            log.debug("Validating Joint Meeting Of Experts Date");
            fieldsNihlUtils.validateFutureDate(caseData.getSdoR2PermissionToRelyOnExpert().getSdoJointMeetingOfExpertsDate()).ifPresent(error -> {
                log.warn("Joint Meeting Of Experts Date validation failed: {}", error);
                errors.add(error);
            });
        }
    }
}

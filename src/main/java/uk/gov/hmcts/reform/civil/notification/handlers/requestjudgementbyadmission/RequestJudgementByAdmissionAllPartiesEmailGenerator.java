package uk.gov.hmcts.reform.civil.notification.handlers.requestjudgementbyadmission;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class RequestJudgementByAdmissionAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public RequestJudgementByAdmissionAllPartiesEmailGenerator(
        RequestJudgementByAdmissionApplicantEmailDTOGenerator applicantEmailDTOGenerator,
        RequestJudgementByAdmissionAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator,
        RequestJudgementByAdmissionLipRespondentEmailDTOGenerator lipRespondentEmailDTOGenerator,
        RequestJudgementByAdmissionRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator,
        RequestJudgementByAdmissionDefendantEmailDTOGenerator defendantEmailDTOGenerator) {
        super(List.of(applicantEmailDTOGenerator,
                      appSolOneEmailDTOGenerator,
                      lipRespondentEmailDTOGenerator,
                      respSolOneEmailDTOGenerator,
                      defendantEmailDTOGenerator));
    }
}

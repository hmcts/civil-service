package uk.gov.hmcts.reform.civil.notification.handlers.judgmentbyadmission;

import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class JudgmentByAdmissionAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public JudgmentByAdmissionAllPartiesEmailGenerator(JudgmentByAdmissionAppSolOneEmailDTOGenerator appSolOneEmailGenerator,
                                                       JudgmentByAdmissionDefendantEmailDTOGenerator defendantEmailDTOGenerator) {

        super(List.of(appSolOneEmailGenerator,
                      defendantEmailDTOGenerator));
    }
}

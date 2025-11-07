package uk.gov.hmcts.reform.civil.helpers.bundle.util;

import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;

import org.springframework.stereotype.Component;

@Component
public class FilenameGenerator {

    public String generateBundleFilenamePrefix(CaseData caseData) {
        String applicantName = caseData.getApplicant1().isIndividual()
            ? caseData.getApplicant1().getIndividualLastName() : caseData.getApplicant1().getPartyName();
        String respondentName = caseData.getRespondent1().isIndividual()
            ? caseData.getRespondent1().getIndividualLastName() : caseData.getRespondent1().getPartyName();
        return applicantName + " v " + respondentName + "-"
            + DateFormatHelper.formatLocalDate(caseData.getHearingDate(), "ddMMyyyy");
    }
}



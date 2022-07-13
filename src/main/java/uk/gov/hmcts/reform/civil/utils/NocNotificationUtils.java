package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RepresentationUpdate;
import uk.gov.hmcts.reform.civil.model.RepresentationUpdateHistory;

import java.util.Collections;
import java.util.Comparator;

public class NocNotificationUtils {

    private NocNotificationUtils() {
        //NO-OP
    }

    public static RepresentationUpdate getLatestUpdate(RepresentationUpdateHistory representationUpdateHistory) {
        return Collections.max(representationUpdateHistory.getRepresentationUpdateHistory(), Comparator.comparing(c -> c.getDate()));
    }

    public static String getNewSolicitorEmail(CaseData caseData) {
      return getLatestUpdate(caseData.getRepresentationUpdateHistory()).getAdded().getEmail();
    }

    public static String getNewSolicitorName(CaseData caseData) {
        return getLatestUpdate(caseData.getRepresentationUpdateHistory()).getAdded().getName();
    }

    public static String getPreviousSolicitorEmail(CaseData caseData) {
        return getLatestUpdate(caseData.getRepresentationUpdateHistory()).getRemoved().getEmail();
    }

    public static String getOtherSolicitor1Email(CaseData caseData) {
        String newSolicitorEmail = getNewSolicitorEmail(caseData);
        if(newSolicitorForRespondent2(caseData)) {
            return caseData.getApplicantSolicitor1UserDetails().getEmail();
        } else if(newSolicitorEmail.equals(caseData.getApplicantSolicitor1UserDetails().getEmail())) {
            return caseData.getRespondentSolicitor2EmailAddress();
        } else {
            return caseData.getRespondentSolicitor1EmailAddress();
        }
    }

    public static String getOtherSolicitor2Email(CaseData caseData) {
        return newSolicitorForRespondent2(caseData) ?
            caseData.getRespondentSolicitor1EmailAddress() : caseData.getRespondentSolicitor2EmailAddress();
    }

    public static boolean newSolicitorForRespondent2(CaseData caseData) {
        return getNewSolicitorEmail(caseData).equals(caseData.getRespondentSolicitor2EmailAddress());
    }
}

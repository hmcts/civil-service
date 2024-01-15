package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;

import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.model.common.DynamicListElement.dynamicElementFromCode;

public class UploadMediationDocumentsUtils {

    private UploadMediationDocumentsUtils() {
        // NO-OP
    }

    public static final String CLAIMANT_ONE_ID = "CLAIMANT_1";
    public static final String CLAIMANT_TWO_ID = "CLAIMANT_2";
    public static final String CLAIMANTS_ID = "CLAIMANTS";

    public static final String DEFENDANT_ONE_ID = "DEFENDANT_1";
    public static final String DEFENDANT_TWO_ID = "DEFENDANT_2";
    public static final String DEFENDANTS_ID = "DEFENDANTS";

    private static final String CLAIMANT_ONE = "Claimant 1:";
    private static final String CLAIMANT_TWO = "Claimant 2:";
    private static final String CLAIMANTS = "Claimants 1 and 2";

    private static final String DEFENDANT_ONE = "Defendant 1:";
    private static final String DEFENDANT_TWO = "Defendant 2:";
    private static final String DEFENDANTS = "Defendants 1 and 2";

    public static void addApplicantOptions(List<DynamicListElement> list, CaseData caseData) {
        addPartyOption(list, CLAIMANT_ONE_ID, CLAIMANT_ONE, caseData.getApplicant1().getPartyName());
        if (TWO_V_ONE.equals(getMultiPartyScenario(caseData))) {
            addPartyOption(list, CLAIMANT_TWO_ID, CLAIMANT_TWO, caseData.getApplicant2().getPartyName());
            addBothPartiesOption(list, CLAIMANTS_ID, CLAIMANTS);
        }
    }

    public static void addDefendant1Option(List<DynamicListElement> list, CaseData caseData) {
        addPartyOption(list, DEFENDANT_ONE_ID, DEFENDANT_ONE, caseData.getRespondent1().getPartyName());
    }

    public static void addDefendant2Option(List<DynamicListElement> list, CaseData caseData) {
        addPartyOption(list, DEFENDANT_TWO_ID, DEFENDANT_TWO, caseData.getRespondent2().getPartyName());
    }

    public static void addSameSolicitorDefendantOptions(List<DynamicListElement> list, CaseData caseData) {
        addDefendant1Option(list, caseData);
        addDefendant2Option(list, caseData);
        addBothPartiesOption(list, DEFENDANTS_ID, DEFENDANTS);
    }

    private static void addPartyOption(List<DynamicListElement> list, String id, String prefix, String partyName) {
        list.add(dynamicElementFromCode(id,
                                        String.format("%s %s", prefix, partyName)));
    }

    private static void addBothPartiesOption(List<DynamicListElement> list, String id, String prefix) {
        list.add(dynamicElementFromCode(id, prefix));
    }
}

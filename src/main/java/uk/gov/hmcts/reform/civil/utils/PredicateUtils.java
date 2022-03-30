package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.function.Predicate;

public class PredicateUtils {

    private PredicateUtils() {
        //NO-OP
    }

    public static final Predicate<CaseData> defendant1ExtensionExists = caseData ->
        caseData.getRespondent1TimeExtensionDate() != null
            && caseData.getRespondentSolicitor1AgreedDeadlineExtension() != null;

    public static final Predicate<CaseData> defendant2ExtensionExists = caseData ->
        caseData.getRespondent2TimeExtensionDate() != null
            && caseData.getRespondentSolicitor2AgreedDeadlineExtension() != null;


    public static final Predicate<CaseData> defendant1AckExists = caseData ->
        caseData.getRespondent1AcknowledgeNotificationDate()  != null;

    public static final Predicate<CaseData> defendant2AckExists = caseData ->
        caseData.getRespondent2AcknowledgeNotificationDate() != null;
}

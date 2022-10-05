package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.function.Predicate;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

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
        caseData.getRespondent1AcknowledgeNotificationDate() != null;

    public static final Predicate<CaseData> defendant2AckExists = caseData ->
        caseData.getRespondent2AcknowledgeNotificationDate() != null;

    public static final Predicate<CaseData> defendant1ResponseExists = caseData ->
        caseData.getRespondent1ResponseDate() != null;

    public static final Predicate<CaseData> defendant2ResponseExists = caseData ->
        caseData.getRespondent2() != null && caseData.getRespondent2ResponseDate() != null
        && caseData.getRespondent2SameLegalRepresentative() != YesOrNo.YES;

    public static final Predicate<CaseData> defendant2DivergentResponseExists = caseData ->
        caseData.getRespondent2() != null && (caseData.getRespondent2ResponseDate() != null
            || (caseData.getRespondent2SameLegalRepresentative() == YesOrNo.YES
            && caseData.getRespondent1ResponseDate() != null && caseData.getRespondentResponseIsSame()  == NO));

    public static final Predicate<CaseData> defendant1v2SameSolicitorSameResponse =  caseData ->
        caseData.getRespondent1ResponseDate() != null && caseData.getRespondent2() != null
            && caseData.getRespondentResponseIsSame()  == YES;
}

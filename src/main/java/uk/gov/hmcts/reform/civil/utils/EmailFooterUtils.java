package uk.gov.hmcts.reform.civil.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_DISMISSED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CLOSED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PENDING_CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PROCEEDS_IN_HERITAGE_SYSTEM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationDataGA.SPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationDataGA.WELSH_CONTACT;

@Slf4j
public class EmailFooterUtils {

    private EmailFooterUtils() {
        //NO-OP
    }

    public static String RAISE_QUERY_LR = "Contact us about your claim by selecting "
        + "Raise a query from the next steps menu.";

    public static String RAISE_QUERY_LIP = "To contact the court, select contact or "
        + "apply to the court on your dashboard.";

    public static String RAISE_QUERY_LIP_WELSH = "I gysylltu â’r llys, dewiswch "
        + "‘contact or apply to the court’ ar eich dangosfwrdd.";

    public static final Set<CaseState> qmNotAllowedStates = EnumSet.of(PENDING_CASE_ISSUED, CLOSED,
                                                                       PROCEEDS_IN_HERITAGE_SYSTEM, CASE_DISMISSED);

    private static boolean queryNotAllowedCaseStates(CaseData mainCaseData) {
        return qmNotAllowedStates.contains(mainCaseData.getCcdState());
    }

    public static Map<String, String> addAllFooterItems(CaseData caseData, CaseData mainCaseData, Map<String, String> properties,
                                 NotificationsSignatureConfiguration configuration,
                                 boolean isPublicQMEnabled) {
        addCommonFooterSignature(properties, configuration);
        addCommonFooterSignatureWelsh(properties, configuration);
        addSpecAndUnspecContact(caseData, mainCaseData, properties, configuration,
                                isPublicQMEnabled);
        addLipContact(caseData, mainCaseData, properties, configuration,
                      isPublicQMEnabled);
        addWelshLipContact(caseData, mainCaseData, properties, configuration,
                           isPublicQMEnabled);
        return properties;
    }

    public static void addCommonFooterSignature(Map<String, String> properties,
                                                NotificationsSignatureConfiguration configuration) {
        properties.putAll(Map.of(HMCTS_SIGNATURE, configuration.getHmctsSignature(),
                                 PHONE_CONTACT, configuration.getPhoneContact(),
                                 OPENING_HOURS, configuration.getOpeningHours()));
    }

    public static void addCommonFooterSignatureWelsh(Map<String, String> properties,
                                                     NotificationsSignatureConfiguration configuration) {
        properties.putAll(Map.of(WELSH_HMCTS_SIGNATURE, configuration.getWelshHmctsSignature(),
                                 WELSH_PHONE_CONTACT, configuration.getWelshPhoneContact(),
                                 WELSH_OPENING_HOURS, configuration.getWelshOpeningHours()));
    }

    public static void addSpecAndUnspecContact(CaseData caseData, CaseData mainCaseData, Map<String, String> properties,
                                               NotificationsSignatureConfiguration configuration,
                                               boolean isPublicQMEnabled) {
        log.info("add LR contact");
        log.info("isPublicQMEnabled " + isPublicQMEnabled);
        log.info("!isLipCase(caseData) " + !isLipCase(caseData));
        log.info("!queryNotAllowedCaseStates(caseData) " + !queryNotAllowedCaseStates(mainCaseData));
        log.info("app rep " + caseData.getApplicant1Represented());
        log.info("res rep " + caseData.getRespondent1Represented());
        if (!queryNotAllowedCaseStates(mainCaseData)
            && (!isLipCase(caseData) || (isPublicQMEnabled && isLipCase(caseData)))) {
            properties.put(SPEC_UNSPEC_CONTACT, RAISE_QUERY_LR);
        } else {
            properties.put(SPEC_UNSPEC_CONTACT, configuration.getSpecUnspecContact());
        }
    }

    public static void addLipContact(CaseData caseData, CaseData mainCaseData, Map<String, String> properties,
                                     NotificationsSignatureConfiguration configuration,
                                     boolean isPublicQMEnabled) {
        log.info("add lip contact");
        log.info("isPublicQMEnabled " + isPublicQMEnabled);
        log.info("!queryNotAllowedCaseStates(caseData) " + !queryNotAllowedCaseStates(mainCaseData));
        log.info("!isLipCase(caseData) " + !isLipCase(caseData));
        log.info("res rep " + caseData.getRespondent1Represented());
        log.info("app rep " + caseData.getApplicant1Represented());
        if (!queryNotAllowedCaseStates(mainCaseData)
            && isLipCase(caseData) && isPublicQMEnabled) {
            properties.put(SPEC_CONTACT, RAISE_QUERY_LIP);
        } else {
            properties.put(SPEC_CONTACT, configuration.getSpecContact());
        }
    }

    public static void addWelshLipContact(CaseData caseData, CaseData mainCaseData, Map<String, String> properties,
                                          NotificationsSignatureConfiguration configuration,
                                          boolean isPublicQMEnabled) {
        log.info("add welsh lip contact");
        log.info("!queryNotAllowedCaseStates(caseData) " + !queryNotAllowedCaseStates(mainCaseData));
        log.info("isPublicQMEnabled " + isPublicQMEnabled);
        log.info("res rep " + caseData.getRespondent1Represented());
        log.info("isLipCase(caseData) " + isLipCase(caseData));
        log.info("app rep " + caseData.getApplicant1Represented());
        if (!queryNotAllowedCaseStates(mainCaseData)
            && isLipCase(caseData) && isPublicQMEnabled) {
            properties.put(WELSH_CONTACT, RAISE_QUERY_LIP_WELSH);
        } else {
            properties.put(WELSH_CONTACT, configuration.getWelshContact());
        }
    }

    private static boolean isLipCase(CaseData caseData) {
        log.info("getIsGaApplicantLip " + caseData.getIsGaApplicantLip());
        log.info("getIsGaRespondentOneLip " + caseData.getIsGaRespondentOneLip());
        return YesOrNo.YES.equals(caseData.getIsGaApplicantLip())
            || YesOrNo.YES.equals(caseData.getIsGaRespondentOneLip());
    }
}

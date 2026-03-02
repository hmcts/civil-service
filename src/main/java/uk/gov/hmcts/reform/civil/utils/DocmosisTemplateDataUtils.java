package uk.gov.hmcts.reform.civil.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;

@Slf4j
public class DocmosisTemplateDataUtils {

    public static final int CASE_NAME_LENGTH_TO_FIT_IN_DOCS = 37;
    public static final String REFERENCE_NOT_PROVIDED = "Not Provided";
    //TODO Need to confirm the case name logic
    public static final Function<CaseData, String> toCaseName = caseData -> {
        String caseName = fetchApplicantName(caseData) + " vs " + fetchRespondentName(caseData);


        return caseName.length() > CASE_NAME_LENGTH_TO_FIT_IN_DOCS
            ? caseName.replace(" vs ", " \nvs ")
            : caseName;
    };
    public static final String DELIMITER = "-";

    private DocmosisTemplateDataUtils() {
        //NO-OP
    }

    public static String fetchRespondentName(CaseData caseData) {
        StringBuilder respondentNameBuilder = new StringBuilder();
        if (caseData.getRespondent2() != null) {
            respondentNameBuilder.append("1 ");
            respondentNameBuilder.append(caseData.getRespondent1().getPartyName());
            soleTraderCompany(caseData.getRespondent1(), respondentNameBuilder);
            litigationFriend(caseData.getRespondent1LitigationFriend(), respondentNameBuilder);
            respondentNameBuilder.append(" & 2 ");
            respondentNameBuilder.append(caseData.getRespondent2().getPartyName());
            soleTraderCompany(caseData.getRespondent2(), respondentNameBuilder);
            litigationFriend(caseData.getRespondent2LitigationFriend(), respondentNameBuilder);
        } else {
            respondentNameBuilder.append(caseData.getRespondent1().getPartyName());
            soleTraderCompany(caseData.getRespondent1(), respondentNameBuilder);
            litigationFriend(caseData.getRespondent1LitigationFriend(), respondentNameBuilder);
        }

        return respondentNameBuilder.toString();
    }

    public static String fetchApplicantName(CaseData caseData) {
        StringBuilder applicantNameBuilder = new StringBuilder();

        if (caseData.getApplicant1() != null && caseData.getApplicant2() != null) {
            applicantNameBuilder.append("1 ");
            applicantNameBuilder.append(caseData.getApplicant1().getPartyName());
            soleTraderCompany(caseData.getApplicant1(), applicantNameBuilder);
            litigationFriend(caseData.getApplicant1LitigationFriend(), applicantNameBuilder);
            applicantNameBuilder.append(" & 2 ");
            applicantNameBuilder.append(caseData.getApplicant2().getPartyName());
            soleTraderCompany(caseData.getApplicant2(), applicantNameBuilder);
            litigationFriend(caseData.getApplicant2LitigationFriend(), applicantNameBuilder);
        } else if (caseData.getApplicant1() != null) {
            applicantNameBuilder.append(caseData.getApplicant1().getPartyName());
            soleTraderCompany(caseData.getApplicant1(), applicantNameBuilder);
            litigationFriend(caseData.getApplicant1LitigationFriend(), applicantNameBuilder);
        } else {
            String errorMsg = String.format("Applicant1 not found for claim number: %s",
                                            caseData.getCcdCaseReference());
            log.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        return applicantNameBuilder.toString();
    }

    public static SolicitorReferences fetchSolicitorReferences(CaseData caseData) {
        return new SolicitorReferences()
            .setApplicantSolicitor1Reference(
                ofNullable(caseData.getSolicitorReferences())
                    .map(SolicitorReferences::getApplicantSolicitor1Reference)
                    .orElse(REFERENCE_NOT_PROVIDED))
            .setRespondentSolicitor1Reference(
                ofNullable(caseData.getSolicitorReferences())
                    .map(SolicitorReferences::getRespondentSolicitor1Reference)
                    .orElse(REFERENCE_NOT_PROVIDED))
            .setRespondentSolicitor2Reference(
                ofNullable(caseData.getRespondentSolicitor2Reference())
                    .orElse(REFERENCE_NOT_PROVIDED));
    }

    public static String fetchSoleTraderCompany(Party party) {
        StringBuilder soleTraderCompanyBuilder = new StringBuilder();
        if (party.getType() == uk.gov.hmcts.reform.civil.model.Party.Type.SOLE_TRADER
            && StringUtils.isNotBlank(party.getSoleTraderTradingAs())) {
            soleTraderCompanyBuilder.append(" T/A ").append(party.getSoleTraderTradingAs());
        }
        return soleTraderCompanyBuilder.toString();
    }

    private static void soleTraderCompany(Party party, StringBuilder stringBuilder) {
        if (party.getType() == Party.Type.SOLE_TRADER && StringUtils.isNotBlank(party.getSoleTraderTradingAs())) {
            stringBuilder.append(" T/A ").append(party.getSoleTraderTradingAs());
        }
    }

    private static void litigationFriend(LitigationFriend litigationFriend, StringBuilder stringBuilder) {
        if (litigationFriend != null) {
            String fullName = litigationFriend.getFirstName() + " " + litigationFriend.getLastName();
            stringBuilder.append(format(" (proceeding by L/F %s)", fullName));
        }
    }

    public static SolicitorReferences fetchSolicitorReferencesMultiparty(CaseData caseData) {

        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        if (multiPartyScenario == ONE_V_TWO_TWO_LEGAL_REP) {
            //case where respondent 2 acknowledges first
            if ((caseData.getRespondent1AcknowledgeNotificationDate() == null)
                && (caseData.getRespondent2AcknowledgeNotificationDate() != null)) {
                if (null == caseData.getRespondentSolicitor2Reference()) {
                    return new SolicitorReferences()
                        .setRespondentSolicitor2Reference(REFERENCE_NOT_PROVIDED)
                        .setApplicantSolicitor1Reference(
                            ofNullable(caseData.getSolicitorReferences())
                                .map(SolicitorReferences::getApplicantSolicitor1Reference)
                                .orElse(REFERENCE_NOT_PROVIDED));
                }
                return new SolicitorReferences()
                    .setRespondentSolicitor2Reference(caseData.getRespondentSolicitor2Reference())
                    .setApplicantSolicitor1Reference(
                        ofNullable(caseData.getSolicitorReferences())
                            .map(SolicitorReferences::getApplicantSolicitor1Reference)
                            .orElse(REFERENCE_NOT_PROVIDED));

            } else if ((caseData.getRespondent1AcknowledgeNotificationDate() != null)//case where both respondents acklg
                && (caseData.getRespondent2AcknowledgeNotificationDate() != null)) {
                //case where resp 1 acknowledges first
                if (caseData.getRespondent2AcknowledgeNotificationDate()
                    .isAfter(caseData.getRespondent1AcknowledgeNotificationDate())) {
                    if (null == caseData.getRespondentSolicitor2Reference()) {
                        return new SolicitorReferences()
                            .setRespondentSolicitor2Reference(REFERENCE_NOT_PROVIDED)
                            .setApplicantSolicitor1Reference(
                                ofNullable(caseData.getSolicitorReferences())
                                    .map(SolicitorReferences::getApplicantSolicitor1Reference)
                                    .orElse(REFERENCE_NOT_PROVIDED));
                    }
                    return new SolicitorReferences()
                        .setRespondentSolicitor2Reference(caseData.getRespondentSolicitor2Reference())
                        .setApplicantSolicitor1Reference(
                            ofNullable(caseData.getSolicitorReferences())
                                .map(SolicitorReferences::getApplicantSolicitor1Reference)
                                .orElse(REFERENCE_NOT_PROVIDED));

                } else { //case where resp 2 acknowledges first
                    return new SolicitorReferences()
                        .setRespondentSolicitor1Reference(
                            ofNullable(caseData.getSolicitorReferences())
                                .map(SolicitorReferences::getRespondentSolicitor1Reference)
                                .orElse(REFERENCE_NOT_PROVIDED))
                        .setApplicantSolicitor1Reference(
                            ofNullable(caseData.getSolicitorReferences())
                                .map(SolicitorReferences::getApplicantSolicitor1Reference)
                                .orElse(REFERENCE_NOT_PROVIDED));
                }
            } else { //case where respondent 1 acknowledges first
                return new SolicitorReferences()
                    .setRespondentSolicitor1Reference(
                        ofNullable(caseData.getSolicitorReferences())
                            .map(SolicitorReferences::getRespondentSolicitor1Reference)
                            .orElse(REFERENCE_NOT_PROVIDED))
                    .setApplicantSolicitor1Reference(
                        ofNullable(caseData.getSolicitorReferences())
                            .map(SolicitorReferences::getApplicantSolicitor1Reference)
                            .orElse(REFERENCE_NOT_PROVIDED));
            }
        } else { //cases other than ONE_V_TWO_TWO_LEGAL_REP
            return new SolicitorReferences()
                .setApplicantSolicitor1Reference(
                    ofNullable(caseData.getSolicitorReferences())
                        .map(SolicitorReferences::getApplicantSolicitor1Reference)
                        .orElse(REFERENCE_NOT_PROVIDED))
                .setRespondentSolicitor1Reference(
                    ofNullable(caseData.getSolicitorReferences())
                        .map(SolicitorReferences::getRespondentSolicitor1Reference)
                        .orElse(REFERENCE_NOT_PROVIDED));
        }

    }

    public static List<String> fetchResponseIntentionsDocmosisTemplate(CaseData caseData) {
        List<String> responseIntentions = new ArrayList<>();

        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_TWO_TWO_LEGAL_REP:
                if ((caseData.getRespondent1AcknowledgeNotificationDate() == null)
                    && (caseData.getRespondent2AcknowledgeNotificationDate() != null)) {
                    //case where respondent 2 acknowledges first
                    responseIntentions.add(caseData.getRespondent2ClaimResponseIntentionType().getLabel());
                } else if ((caseData.getRespondent1AcknowledgeNotificationDate() != null)
                    && (caseData.getRespondent2AcknowledgeNotificationDate() != null)) {
                    if (caseData.getRespondent2AcknowledgeNotificationDate()
                        .isAfter(caseData.getRespondent1AcknowledgeNotificationDate())) {
                        //case where respondent 2 acknowledges 2nd
                        responseIntentions.add(caseData.getRespondent2ClaimResponseIntentionType().getLabel());
                    } else {
                        //case where respondent 1 acknowledges 2nd
                        responseIntentions.add(caseData.getRespondent1ClaimResponseIntentionType().getLabel());
                    }
                } else { //case where respondent 1 acknowledges first
                    responseIntentions.add(caseData.getRespondent1ClaimResponseIntentionType().getLabel());
                }
                break;
            case ONE_V_TWO_ONE_LEGAL_REP:
                responseIntentions.add("Defendant 1 :"
                                           + caseData.getRespondent1ClaimResponseIntentionType().getLabel());
                responseIntentions.add("Defendant 2 :"
                                           + caseData.getRespondent2ClaimResponseIntentionType().getLabel());
                break;
            case TWO_V_ONE:
                responseIntentions.add("Against Claimant 1: "
                                           + caseData.getRespondent1ClaimResponseIntentionType().getLabel());
                responseIntentions.add("Against Claimant 2: "
                                           + caseData.getRespondent1ClaimResponseIntentionTypeApplicant2().getLabel());
                break;
            default:
                responseIntentions.add(caseData.getRespondent1ClaimResponseIntentionType().getLabel());
                return responseIntentions;
        }
        return responseIntentions;
    }

    public static String formatCcdCaseReference(CaseData caseData) {
        return Optional.ofNullable(caseData.getCcdCaseReference())
            .map(val -> String.join(
                DELIMITER,
                Arrays.stream(
                val.toString().split("(?<=\\G.{4})")
            ).toList()))
            .orElse("");
    }
}

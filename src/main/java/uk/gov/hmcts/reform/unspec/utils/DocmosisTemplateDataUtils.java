package uk.gov.hmcts.reform.unspec.utils;

import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.LitigationFriend;
import uk.gov.hmcts.reform.unspec.model.Party;
import uk.gov.hmcts.reform.unspec.model.SolicitorReferences;

import java.util.Optional;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

public class DocmosisTemplateDataUtils {

    public static final int CASE_NAME_LENGTH_TO_FIT_IN_DOCS = 37;

    //TODO Need to confirm the case name logic
    public static final Function<CaseData, String> toCaseName = caseData -> {
        String caseName = fetchApplicantName(caseData) + " vs " + fetchRespondentName(caseData);

        return caseName.length() > CASE_NAME_LENGTH_TO_FIT_IN_DOCS
            ? caseName.replace(" vs ", " \nvs ")
            : caseName;
    };
    
    private DocmosisTemplateDataUtils() {
        //NO-OP
    }

    public static String fetchRespondentName(CaseData caseData) {
        StringBuilder respondentNameBuilder = new StringBuilder();
        if (caseData.getRespondent2() != null) {
            respondentNameBuilder.append("1 ");
            respondentNameBuilder.append(caseData.getRespondent1().getPartyName());
            soleTraderCompany(caseData.getRespondent1(), respondentNameBuilder);
            respondentNameBuilder.append(" & 2 ");
            respondentNameBuilder.append(caseData.getRespondent2().getPartyName());
            soleTraderCompany(caseData.getRespondent2(), respondentNameBuilder);
        } else {
            respondentNameBuilder.append(caseData.getRespondent1().getPartyName());
            soleTraderCompany(caseData.getRespondent1(), respondentNameBuilder);
            litigationFriend(caseData.getRespondent1LitigationFriend(), respondentNameBuilder);
        }

        return respondentNameBuilder.toString();
    }

    public static String fetchApplicantName(CaseData caseData) {
        StringBuilder applicantNameBuilder = new StringBuilder();

        if (caseData.getApplicant2() != null) {
            applicantNameBuilder.append("1 ");
            applicantNameBuilder.append(caseData.getApplicant1().getPartyName());
            soleTraderCompany(caseData.getApplicant1(), applicantNameBuilder);
            applicantNameBuilder.append(" & 2 ");
            applicantNameBuilder.append(caseData.getApplicant2().getPartyName());
            soleTraderCompany(caseData.getApplicant2(), applicantNameBuilder);
        } else {
            applicantNameBuilder.append(caseData.getApplicant1().getPartyName());
            soleTraderCompany(caseData.getApplicant1(), applicantNameBuilder);
            litigationFriend(caseData.getApplicant1LitigationFriend(), applicantNameBuilder);
        }

        return applicantNameBuilder.toString();
    }

    public static SolicitorReferences fetchSolicitorReferences(SolicitorReferences solicitorReferences) {
        return SolicitorReferences
            .builder()
            .applicantSolicitor1Reference(
                ofNullable(solicitorReferences)
                    .map(SolicitorReferences::getApplicantSolicitor1Reference)
                    .orElse("Not Provided"))
            .respondentSolicitor1Reference(
                ofNullable(solicitorReferences)
                    .map(SolicitorReferences::getRespondentSolicitor1Reference)
                    .orElse("Not Provided"))
            .build();
    }

    private static void soleTraderCompany(Party party, StringBuilder stringBuilder) {
        if (party.getType() == Party.Type.SOLE_TRADER && StringUtils.isNotBlank(party.getSoleTraderTradingAs())) {
            stringBuilder.append(" T/A ").append(party.getSoleTraderTradingAs());
        }
    }

    private static void litigationFriend(LitigationFriend litigationFriend, StringBuilder stringBuilder) {
        Optional.ofNullable(litigationFriend)
            .map(LitigationFriend::getFullName)
            .ifPresent(fullName -> stringBuilder.append(format(" (proceeding by L/F %s)", fullName)));
    }
}

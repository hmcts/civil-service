package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;
import static java.util.Optional.ofNullable;

public class PersistDataUtils {

    private PersistDataUtils() {
        //NO-OP
    }

    public static void persistFlagsForParties(CaseData oldCaseData, CaseData caseData, CaseData.CaseDataBuilder<?, ?> builder) {
        // persist respondent flags (ccd issue)
        var updatedRespondent1 = caseData.getRespondent1().toBuilder()
            .flags(oldCaseData.getRespondent1().getFlags())
            .build();

        builder.respondent1(updatedRespondent1);

        // persist applicant flags (ccd issue)
        var updatedApplicant1 = caseData.getApplicant1().toBuilder()
            .flags(oldCaseData.getApplicant1().getFlags())
            .build();

        builder.applicant1(updatedApplicant1);

        // if present, persist the 2nd respondent flags in the same fashion as above, i.e ignore for 1v1
        if (ofNullable(caseData.getRespondent2()).isPresent()
            && ofNullable(oldCaseData.getRespondent2()).isPresent()) {
            var updatedRespondent2 = caseData.getRespondent2().toBuilder()
                .flags(oldCaseData.getRespondent2().getFlags())
                .build();

            builder.respondent2(updatedRespondent2);
        }

        // if present, persist the 2nd applicant flags in the same fashion as above, i.e ignore for 1v1
        if (ofNullable(caseData.getApplicant2()).isPresent()
            && ofNullable(oldCaseData.getApplicant2()).isPresent()) {
            var updatedApplicant2 = caseData.getApplicant2().toBuilder()
                .flags(oldCaseData.getApplicant2().getFlags())
                .build();

            builder.applicant2(updatedApplicant2);
        }
    }

    public static void persistFlagsForLitigationFriendParties(CaseData oldCaseData, CaseData caseData, CaseData.CaseDataBuilder<?, ?> builder) {
        // Litigation Friend
        if (ofNullable(oldCaseData.getApplicant1LitigationFriend()).isPresent()) {
            var party = caseData.getApplicant1LitigationFriend().toBuilder()
                .flags(oldCaseData.getApplicant1LitigationFriend().getFlags())
                .build();

            builder.applicant1LitigationFriend(party);
        }

        // Litigation Friend
        if (ofNullable(oldCaseData.getApplicant2LitigationFriend()).isPresent()) {
            var party = caseData.getApplicant2LitigationFriend().toBuilder()
                .flags(oldCaseData.getApplicant2LitigationFriend().getFlags())
                .build();

            builder.applicant2LitigationFriend(party);
        }

        // Litigation Friend
        if (ofNullable(oldCaseData.getRespondent1LitigationFriend()).isPresent()) {
            var party = caseData.getRespondent1LitigationFriend().toBuilder()
                .flags(oldCaseData.getRespondent1LitigationFriend().getFlags())
                .build();

            builder.respondent1LitigationFriend(party);
        }

        // Litigation Friend
        if (ofNullable(oldCaseData.getRespondent2LitigationFriend()).isPresent()) {
            var party = caseData.getRespondent2LitigationFriend().toBuilder()
                .flags(oldCaseData.getRespondent2LitigationFriend().getFlags())
                .build();

            builder.respondent2LitigationFriend(party);
        }
    }

    public static CaseData persistPartyAddress(CaseData oldCaseData, CaseData caseData) {
        if (null != caseData.getApplicant1()
            && null == caseData.getApplicant1().getPrimaryAddress()
            && null != oldCaseData && null != oldCaseData.getApplicant1()
            && null != oldCaseData.getApplicant1().getPrimaryAddress()) {
            caseData.getApplicant1().setPrimaryAddress(oldCaseData.getApplicant1().getPrimaryAddress());
        }
        if (null != caseData.getRespondent1()
            && null == caseData.getRespondent1().getPrimaryAddress()
            && null != oldCaseData && null != oldCaseData.getRespondent1()
            && null != oldCaseData.getRespondent1().getPrimaryAddress()) {
            caseData.getRespondent1().setPrimaryAddress(oldCaseData.getRespondent1().getPrimaryAddress());
        }
        if (null != caseData.getApplicant2()
            && null == caseData.getApplicant2().getPrimaryAddress()
            && null != oldCaseData && null != oldCaseData.getApplicant2()
            && null != oldCaseData.getApplicant2().getPrimaryAddress()) {
            caseData.getApplicant2().setPrimaryAddress(oldCaseData.getApplicant2().getPrimaryAddress());
        }
        if (null != caseData.getRespondent2()
            && null == caseData.getRespondent2().getPrimaryAddress()
            && null != oldCaseData && null != oldCaseData.getRespondent2()
            && null != oldCaseData.getRespondent2().getPrimaryAddress()) {
            caseData.getRespondent2().setPrimaryAddress(oldCaseData.getRespondent2().getPrimaryAddress());
        }
        return caseData;
    }
}

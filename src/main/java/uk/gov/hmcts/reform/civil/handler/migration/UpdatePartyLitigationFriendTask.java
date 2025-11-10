package uk.gov.hmcts.reform.civil.handler.migration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.LitigationFriendCaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;

import java.util.Optional;
import java.util.function.BiFunction;

@Component
public class UpdatePartyLitigationFriendTask extends MigrationTask<LitigationFriendCaseReference> {

    // Centralized BiFunction to update LitigationFriend non-null fields
    private static final BiFunction<LitigationFriend, LitigationFriend, LitigationFriend> UPDATE_NON_NULL_FIELDS =
        (source, target) -> {
            LitigationFriend.LitigationFriendBuilder builder = target.toBuilder();

            Optional.ofNullable(source.getPartyID()).ifPresent(builder::partyID);
            Optional.ofNullable(source.getFirstName()).ifPresent(builder::firstName);
            Optional.ofNullable(source.getLastName()).ifPresent(builder::lastName);
            Optional.ofNullable(source.getFullName()).ifPresent(builder::fullName);
            Optional.ofNullable(source.getEmailAddress()).ifPresent(builder::emailAddress);
            Optional.ofNullable(source.getPhoneNumber()).ifPresent(builder::phoneNumber);
            Optional.ofNullable(source.getHasSameAddressAsLitigant()).ifPresent(builder::hasSameAddressAsLitigant);
            Optional.ofNullable(source.getPrimaryAddress()).ifPresent(builder::primaryAddress);
            Optional.ofNullable(source.getCertificateOfSuitability()).ifPresent(builder::certificateOfSuitability);
            Optional.ofNullable(source.getFlags()).ifPresent(builder::flags);

            return builder.build();
        };

    protected UpdatePartyLitigationFriendTask() {
        super(LitigationFriendCaseReference.class);
    }

    @Override
    protected String getEventSummary() {
        return "Update case party litigation friend via migration task";
    }

    @Override
    protected String getTaskName() {
        return "UpdatePartyLitigationFriendTask";
    }

    @Override
    protected String getEventDescription() {
        return "This task UpdatePartyLitigationFriendTask updates litigation friend on the case";
    }

    @Override
    protected CaseData migrateCaseData(CaseData caseData, LitigationFriendCaseReference caseRef) {
        validateCaseReference(caseRef);

        LitigationFriend litgFriendToBeUpdated = caseRef.getLitigationFriend();
        LitigationFriend currentLitFriend = getLitigationFriend(caseData, caseRef);
        LitigationFriend updatedLitFriend = UPDATE_NON_NULL_FIELDS.apply(litgFriendToBeUpdated, currentLitFriend);

        return setUpdatedLitigationFriend(caseData, caseRef, updatedLitFriend);
    }

    private void validateCaseReference(LitigationFriendCaseReference caseRef) {
        if (caseRef == null || caseRef.getCaseReference() == null) {
            throw new IllegalArgumentException("CaseReference fields must not be null");
        }
    }

    private LitigationFriend getLitigationFriend(CaseData caseData, LitigationFriendCaseReference ref) {
        return Optional.ofNullable(getLitigationFriendByParty(caseData, ref))
            .orElseThrow(() -> new RuntimeException("Failed to determine Party to update"));
    }

    private LitigationFriend getLitigationFriendByParty(CaseData caseData, LitigationFriendCaseReference ref) {
        if (ref.isApplicant1()) {
            return caseData.getApplicant1LitigationFriend();
        } else if (ref.isApplicant2()) {
            return caseData.getApplicant2LitigationFriend();
        } else if (ref.isRespondent1()) {
            return caseData.getRespondent1LitigationFriend();
        } else if (ref.isRespondent2()) {
            return caseData.getRespondent2LitigationFriend();
        }
        return null;
    }

    private CaseData setUpdatedLitigationFriend(CaseData caseData, LitigationFriendCaseReference ref,
                                                LitigationFriend updatedLitFriend) {

        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

        if (ref.isApplicant1()) {
            builder.applicant1LitigationFriend(updatedLitFriend);
        } else if (ref.isApplicant2()) {
            builder.applicant2LitigationFriend(updatedLitFriend);
        } else if (ref.isRespondent1()) {
            builder.respondent1LitigationFriend(updatedLitFriend);
        } else if (ref.isRespondent2()) {
            builder.respondent2LitigationFriend(updatedLitFriend);
        } else {
            throw new RuntimeException("Failed to set updated litigation friend in CaseData");
        }
        return builder.build();
    }
}

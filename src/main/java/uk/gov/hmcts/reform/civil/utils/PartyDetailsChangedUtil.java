package uk.gov.hmcts.reform.civil.utils;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ContactDetailsUpdatedEvent;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PartyDetailsChange;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.utils.AddressUtils.formatAddress;

/**
 * Utility class for comparing changes in party and litigation friend details and generating
 * contact detail update events.
 */
@Component
@AllArgsConstructor
public class PartyDetailsChangedUtil {

    private static String nameLabel = "Name";
    private static String addressLabel = "Address";

    private LitigationFriend updateLitigationFriendAddress(LitigationFriend litigationFriend, Party party) {
        if (litigationFriend != null && litigationFriend.getHasSameAddressAsLitigant() != null
            && litigationFriend.getHasSameAddressAsLitigant().equals(YesOrNo.YES)) {
            return litigationFriend.copy().setPrimaryAddress(party.getPrimaryAddress());
        }
        return litigationFriend;
    }

    /**
     * Builds a ContactDetailsUpdatedEvent based on changes detected between two CaseData instances.
     *
     * @param current The original CaseData instance.
     * @param updated The updated CaseData instance.
     * @return A ContactDetailsUpdatedEvent if changes are detected, or null if no changes are found.
     */
    public ContactDetailsUpdatedEvent buildChangesEvent(CaseData current, CaseData updated) {
        if (hasChanged(current.getApplicant1(), updated.getApplicant1())) {
            return buildChangesEvent(
                "Applicant 1 Details Changed", getChanges(current.getApplicant1(), updated.getApplicant1()));
        } else if (hasChanged(current.getApplicant2(), updated.getApplicant2())) {
            return buildChangesEvent(
                "Applicant 2 Details Changed",  getChanges(current.getApplicant2(), updated.getApplicant2()));
        } else if (hasChanged(current.getRespondent1(), updated.getRespondent1())) {
            return buildChangesEvent(
                "Respondent 1 Details Changed",  getChanges(current.getRespondent1(), updated.getRespondent1()));
        } else if (hasChanged(current.getRespondent2(), updated.getRespondent2())) {
            return buildChangesEvent(
                "Respondent 2 Details Changed",  getChanges(current.getRespondent2(), updated.getRespondent2()));
        } else if (hasChanged(current.getApplicant1LitigationFriend(), updated.getApplicant1LitigationFriend())) {
            return buildChangesEvent(
                "Applicant 1 Litigation Friend Details Changed",
                getChanges(
                    updateLitigationFriendAddress(current.getApplicant1LitigationFriend(), current.getApplicant1()),
                    updateLitigationFriendAddress(updated.getApplicant1LitigationFriend(), updated.getApplicant1()))
            );
        } else if (hasChanged(current.getApplicant2LitigationFriend(), updated.getApplicant2LitigationFriend())) {
            return buildChangesEvent(
                "Applicant 2 Litigation Friend Details Changed",
                getChanges(
                    updateLitigationFriendAddress(current.getApplicant2LitigationFriend(), current.getApplicant2()),
                    updateLitigationFriendAddress(updated.getApplicant2LitigationFriend(), updated.getApplicant2())
                )
            );
        } else if (hasChanged(current.getRespondent1LitigationFriend(), updated.getRespondent1LitigationFriend())) {
            return buildChangesEvent(
                "Respondent 1 Litigation Friend Details Changed",
                getChanges(
                    updateLitigationFriendAddress(current.getRespondent1LitigationFriend(), current.getRespondent1()),
                    updateLitigationFriendAddress(updated.getRespondent1LitigationFriend(), updated.getRespondent1())));
        } else if (hasChanged(current.getRespondent2LitigationFriend(), updated.getRespondent2LitigationFriend())) {
            return buildChangesEvent(
                "Respondent 2 Litigation Friend Details Changed",
                getChanges(
                    updateLitigationFriendAddress(current.getRespondent2LitigationFriend(), current.getRespondent2()),
                    updateLitigationFriendAddress(updated.getRespondent2LitigationFriend(), updated.getRespondent2())));
        }
        return null;
    }

    /**
     * Constructs a ContactDetailsUpdatedEvent based on the provided summary and list of changes.
     *
     * @param summary The summary of the changes.
     * @param changes The list of PartyDetailsChange representing the changes.
     * @return A ContactDetailsUpdatedEvent summarizing the changes.
     */
    public ContactDetailsUpdatedEvent buildChangesEvent(String summary, List<PartyDetailsChange> changes) {

        String description = changes.stream()
            .map(change -> String.format("%s: From '%s' to '%s'.", change.getFieldName(), change.getPreviousValue(),
                                         change.getUpdatedValue()))
            .collect(Collectors.joining(" "));

        return new ContactDetailsUpdatedEvent().setSummary(summary).setDescription(description);
    }

    private boolean hasChanged(Object current, Object updated) {
        return current == null && updated != null
            || current != null && updated == null
            || current != null && updated != null && !current.equals(updated);
    }

    /**
     * Compares two Party instances and generates a list of PartyDetailsChange based on differences.
     *
     * @param current The original Party instance.
     * @param updated The updated Party instance.
     * @return A list of PartyDetailsChange objects representing detected differences.
     */
    public List<PartyDetailsChange> getChanges(Party current, Party updated) {
        List<PartyDetailsChange> changes = new ArrayList<>();

        if (current == null || updated == null) {
            return changes;
        }

        if (!current.getPartyName(true).equals(updated.getPartyName(true))) {
            changes.add(new PartyDetailsChange()
                            .setFieldName(nameLabel)
                            .setPreviousValue(current.getPartyName(true))
                            .setUpdatedValue(updated.getPartyName(true)));
        }

        if (!formatAddress(current.getPrimaryAddress()).equals(formatAddress(updated.getPrimaryAddress()))) {
            changes.add(new PartyDetailsChange()
                            .setFieldName(addressLabel)
                            .setPreviousValue(formatAddress(current.getPrimaryAddress()))
                            .setUpdatedValue(formatAddress(updated.getPrimaryAddress())));
        }

        return changes;
    }

    /**
     * Compares two LitigationFriend instances and generates a list of PartyDetailsChange based on differences.
     *
     * @param current The original LitigationFriend instance.
     * @param updated The updated LitigationFriend instance.
     * @return A list of PartyDetailsChange objects representing detected differences.
     */
    public List<PartyDetailsChange> getChanges(LitigationFriend current, LitigationFriend updated) {
        List<PartyDetailsChange> changes = new ArrayList<>();

        if (current == null || updated == null) {
            return changes;
        }

        if (!current.getFirstName().equals(updated.getFirstName()) || !current.getLastName().equals(updated.getLastName())) {
            changes.add(new PartyDetailsChange()
                            .setFieldName(nameLabel)
                            .setPreviousValue(String.format("%s %s", current.getFirstName(), current.getLastName()))
                            .setUpdatedValue(String.format("%s %s", updated.getFirstName(), updated.getLastName())));
        }

        if (!formatAddress(current.getPrimaryAddress()).equals(formatAddress(updated.getPrimaryAddress()))) {
            changes.add(new PartyDetailsChange()
                            .setFieldName(addressLabel)
                            .setPreviousValue(formatAddress(current.getPrimaryAddress()))
                            .setUpdatedValue(formatAddress(updated.getPrimaryAddress())));
        }

        return changes;
    }

}

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

    private static String NAME_LABEL = "Name";
    private static String ADDRESS_LABEL = "Address";

    private LitigationFriend updateLitigationFriendAddress(LitigationFriend litigationFriend, Party party) {
        if (litigationFriend != null && litigationFriend.getHasSameAddressAsLitigant() != null
            && litigationFriend.getHasSameAddressAsLitigant().equals(YesOrNo.YES)) {
            return litigationFriend.toBuilder().primaryAddress(party.getPrimaryAddress()).build();
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
                "Applicant Litigation Friend Details Changed",
                getChanges(
                    updateLitigationFriendAddress(current.getApplicant1LitigationFriend(), current.getApplicant1()),
                    updateLitigationFriendAddress(updated.getApplicant1LitigationFriend(), updated.getApplicant1()))
            );
        } else if (hasChanged(current.getRespondent1LitigationFriend(), updated.getRespondent1LitigationFriend())) {
            return buildChangesEvent(
                "Respondent 1 Litigation Friend Details Changed",
                getChanges(
                    updateLitigationFriendAddress(current.getRespondent1LitigationFriend(), current.getRespondent1()),
                    updateLitigationFriendAddress(updated.getRespondent1LitigationFriend(), updated.getRespondent1())));
        } else if (hasChanged(current.getRespondent2LitigationFriend(), updated.getRespondent2LitigationFriend())) {
            return buildChangesEvent(
                "Respondent 2 Litigation Friend Details changed",
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

        return ContactDetailsUpdatedEvent.builder().summary(summary).description(description).build();
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
            changes.add(PartyDetailsChange.builder()
                            .fieldName(NAME_LABEL)
                            .previousValue(current.getPartyName(true))
                            .updatedValue(updated.getPartyName(true))
                            .build());
        }

        if (!formatAddress(current.getPrimaryAddress()).equals(formatAddress(updated.getPrimaryAddress()))) {
            changes.add(PartyDetailsChange.builder()
                            .fieldName(ADDRESS_LABEL)
                            .previousValue(formatAddress(current.getPrimaryAddress()))
                            .updatedValue(formatAddress(updated.getPrimaryAddress()))
                            .build());
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
            changes.add(PartyDetailsChange.builder()
                            .fieldName(NAME_LABEL)
                            .previousValue(String.format("%s %s", current.getFirstName(), current.getLastName()))
                            .updatedValue(String.format("%s %s", updated.getFirstName(), updated.getLastName()))
                            .build());
        }

        if (!formatAddress(current.getPrimaryAddress()).equals(formatAddress(updated.getPrimaryAddress()))) {
            changes.add(PartyDetailsChange.builder()
                            .fieldName(ADDRESS_LABEL)
                            .previousValue(formatAddress(current.getPrimaryAddress()))
                            .updatedValue(formatAddress(updated.getPrimaryAddress()))
                            .build());
        }

        return changes;
    }

}

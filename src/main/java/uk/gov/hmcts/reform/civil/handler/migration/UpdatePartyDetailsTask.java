package uk.gov.hmcts.reform.civil.handler.migration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.PartyDetailsCaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

@Component
public class UpdatePartyDetailsTask extends MigrationTask<PartyDetailsCaseReference> {

    public UpdatePartyDetailsTask() {
        super(CaseReference.class);
    }

    @Override
    protected String getEventSummary() {
        return "Update case party details via migration task";
    }

    @Override
    protected String getTaskName() {
        return "UpdatePartyDetailsTask";
    }

    @Override
    protected String getEventDescription() {
        return "This task updates party information on the case";
    }

    @Override
    protected CaseData migrateCaseData(CaseData caseData, PartyDetailsCaseReference partyCaseRef) {
        if (partyCaseRef == null || partyCaseRef.getCaseReference() == null) {
            throw new IllegalArgumentException("CaseReference fields must not be null");
        }

        Party partyFromTask = partyCaseRef.getParty();

        // Get the current party to be updated
        Party currentParty = getPartyToUpdate(caseData, partyCaseRef);

        // Merge fields from partyFromTask into currentParty
        Party updatedParty = updatePartyFields(currentParty, partyFromTask);

        // Set the updated party back into the correct field in CaseData
        return setUpdatedParty(caseData, partyCaseRef, updatedParty);
    }

    /**
     * Selects the correct Party from CaseData based on PartyDetailsCaseReference flags
     */
    private Party getPartyToUpdate(CaseData caseData, PartyDetailsCaseReference ref) {
        if (ref.isApplicant1()) return caseData.getApplicant1();
        if (ref.isApplicant2()) return caseData.getApplicant2();
        if (ref.isRespondent1()) return caseData.getRespondent1();
        if (ref.isRespondent2()) return caseData.getRespondent2();

        throw new RuntimeException("Failed to determine Party to update");
    }

    /**
     * Sets the updated Party back into the correct field in CaseData
     */
    private CaseData setUpdatedParty(CaseData caseData, PartyDetailsCaseReference ref, Party updatedParty) {
        CaseData.CaseDataBuilder builder = caseData.toBuilder();

        if (ref.isApplicant1()) builder.applicant1(updatedParty);
        else if (ref.isApplicant2()) builder.applicant2(updatedParty);
        else if (ref.isRespondent1()) builder.respondent1(updatedParty);
        else if (ref.isRespondent2()) builder.respondent2(updatedParty);
        else throw new RuntimeException("Failed to set updated Party in CaseData");

        return builder.build();
    }

    /**
     * Updates all fields of a Party using type-safe logic:
     * - For Strings: only update if non-blank
     * - For other objects: update if non-null
     * - Handles type-specific fields for INDIVIDUAL, SOLE_TRADER, COMPANY, ORGANISATION
     */
    private Party updatePartyFields(Party original, Party updates) {
        if (updates == null) return original;

        Party.PartyBuilder builder = original.toBuilder();

        // Common fields
        builder.partyID(updateIfExists(updates.getPartyID(), original.getPartyID()));
        builder.primaryAddress(updateIfExists(updates.getPrimaryAddress(), original.getPrimaryAddress()));
        builder.partyEmail(updateIfExists(updates.getPartyEmail(), original.getPartyEmail()));
        builder.partyPhone(updateIfExists(updates.getPartyPhone(), original.getPartyPhone()));
        builder.legalRepHeading(updateIfExists(updates.getLegalRepHeading(), original.getLegalRepHeading()));
        builder.unavailableDates(updateIfExists(updates.getUnavailableDates(), original.getUnavailableDates()));
        builder.flags(updateIfExists(updates.getFlags(), original.getFlags()));

        // Type-specific fields
        if (original.isIndividual()) {
            builder.individualTitle(updateIfExists(updates.getIndividualTitle(), original.getIndividualTitle()));
            builder.individualFirstName(updateIfExists(updates.getIndividualFirstName(), original.getIndividualFirstName()));
            builder.individualLastName(updateIfExists(updates.getIndividualLastName(), original.getIndividualLastName()));
            builder.individualDateOfBirth(updateIfExists(updates.getIndividualDateOfBirth(), original.getIndividualDateOfBirth()));
        }

        if (original.isSoleTrader()) {
            builder.soleTraderTitle(updateIfExists(updates.getSoleTraderTitle(), original.getSoleTraderTitle()));
            builder.soleTraderFirstName(updateIfExists(updates.getSoleTraderFirstName(), original.getSoleTraderFirstName()));
            builder.soleTraderLastName(updateIfExists(updates.getSoleTraderLastName(), original.getSoleTraderLastName()));
            builder.soleTraderTradingAs(updateIfExists(updates.getSoleTraderTradingAs(), original.getSoleTraderTradingAs()));
            builder.soleTraderDateOfBirth(updateIfExists(updates.getSoleTraderDateOfBirth(), original.getSoleTraderDateOfBirth()));
        }

        if (original.isCompany()) {
            builder.companyName(updateIfExists(updates.getCompanyName(), original.getCompanyName()));
        }

        if (original.isOrganisation()) {
            builder.organisationName(updateIfExists(updates.getOrganisationName(), original.getOrganisationName()));
        }

        return builder.build();
    }

    /**
     * Updates value if newValue is meaningful
     * - For Strings: only update if non-blank
     * - For other objects: update if non-null
     */
    private <T> T updateIfExists(T newValue, T oldValue) {
        if (newValue == null) return oldValue;
        if (newValue instanceof String str && str.isBlank()) return oldValue;
        return newValue;
    }
}

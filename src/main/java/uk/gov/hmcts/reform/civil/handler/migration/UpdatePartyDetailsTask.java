package uk.gov.hmcts.reform.civil.handler.migration;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.PartyDetailsCaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import java.util.Optional;
import java.util.function.BiFunction;

@Component
public class UpdatePartyDetailsTask extends MigrationTask<PartyDetailsCaseReference> {

    public UpdatePartyDetailsTask() {
        super(PartyDetailsCaseReference.class);
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
        validateCaseReference(partyCaseRef);

        Party updates = partyCaseRef.getParty();
        Party current = getParty(caseData, partyCaseRef);

        Party updated = UPDATE_PARTY.apply(current, updates);

        return setParty(caseData, partyCaseRef, updated);
    }

    private void validateCaseReference(PartyDetailsCaseReference ref) {
        if (ref == null || ref.getCaseReference() == null) {
            throw new IllegalArgumentException("CaseReference fields must not be null");
        }
    }

    private Party getParty(CaseData caseData, PartyDetailsCaseReference ref) {
        return Optional.ofNullable(getPartyByRole(caseData, ref))
            .orElseThrow(() -> new RuntimeException("Failed to determine Party to update"));
    }

    private Party getPartyByRole(CaseData caseData, PartyDetailsCaseReference ref) {
        if (ref.isApplicant1()) {
            return caseData.getApplicant1();
        } else if (ref.isApplicant2()) {
            return caseData.getApplicant2();
        } else if (ref.isRespondent1()) {
            return caseData.getRespondent1();
        } else if (ref.isRespondent2()) {
            return caseData.getRespondent2();
        }
        return null;
    }

    private CaseData setParty(CaseData caseData, PartyDetailsCaseReference ref, Party updatedParty) {
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();

        if (ref.isApplicant1()) {
            builder.applicant1(updatedParty);
        } else if (ref.isApplicant2()) {
            builder.applicant2(updatedParty);
        } else if (ref.isRespondent1()) {
            builder.respondent1(updatedParty);
        } else if (ref.isRespondent2()) {
            builder.respondent2(updatedParty);
        } else {
            throw new RuntimeException("Failed to set updated Party in CaseData");
        }

        return builder.build();
    }

    private static final BiFunction<Party, Party, Party> UPDATE_PARTY = (original, updates) -> {
        if (updates == null) {
            return original;
        }

        Party.PartyBuilder builder = original.toBuilder();

        // Common fields
        builder.partyID(updateIfExists(updates.getPartyID(), original.getPartyID()));
        builder.primaryAddress(updateIfExists(updates.getPrimaryAddress(), original.getPrimaryAddress()));
        builder.partyEmail(updateIfExists(updates.getPartyEmail(), original.getPartyEmail()));
        builder.partyPhone(updateIfExists(updates.getPartyPhone(), original.getPartyPhone()));
        builder.legalRepHeading(updateIfExists(updates.getLegalRepHeading(), original.getLegalRepHeading()));
        builder.unavailableDates(updateIfExists(updates.getUnavailableDates(), original.getUnavailableDates()));
        builder.flags(updateIfExists(updates.getFlags(), original.getFlags()));

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
    };

    private static <T> T updateIfExists(T newValue, T oldValue) {
        if (newValue == null) {
            return oldValue;
        }
        if (newValue instanceof String str && str.isBlank()) {
            return oldValue;
        }
        return newValue;
    }
}

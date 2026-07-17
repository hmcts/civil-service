package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.validation.groups.DateOfBirthGroup;

import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.model.Party.Type.INDIVIDUAL;
import static uk.gov.hmcts.reform.civil.model.Party.Type.SOLE_TRADER;
import static uk.gov.hmcts.reform.civil.model.Party.Type.COMPANY;
import static uk.gov.hmcts.reform.civil.model.Party.Type.ORGANISATION;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Party {

    @CCD(label = " ", showCondition = "type = \"DO NOT SHOW\"", searchable = false, retainHiddenValue = true)
    private String partyID;
    @CCD(label = "Party type", searchable = false, retainHiddenValue = true)
    private Type type;
    @CCD(label = "Title", showCondition = "type = \"INDIVIDUAL\"", searchable = false, retainHiddenValue = true)
    private String individualTitle;
    @CCD(label = "First name", showCondition = "type = \"INDIVIDUAL\"", searchable = false, retainHiddenValue = true)
    private String individualFirstName;
    @CCD(label = "Last name", showCondition = "type = \"INDIVIDUAL\"", searchable = false, retainHiddenValue = true)
    private String individualLastName;

    @CCD(label = "Date of birth", showCondition = "type = \"INDIVIDUAL\"", searchable = false, retainHiddenValue = true)
    @PastOrPresent(message = "The date entered cannot be in the future", groups = DateOfBirthGroup.class)
    private LocalDate individualDateOfBirth;
    @CCD(label = "Name", showCondition = "type = \"COMPANY\"", searchable = false, retainHiddenValue = true)
    private String companyName;
    @CCD(label = "Name", showCondition = "type = \"ORGANISATION\"", searchable = false, retainHiddenValue = true)
    private String organisationName;
    @CCD(label = "Title", showCondition = "type = \"SOLE_TRADER\"", searchable = false, retainHiddenValue = true)
    private String soleTraderTitle;
    @CCD(label = "First name", showCondition = "type = \"SOLE_TRADER\"", searchable = false, retainHiddenValue = true)
    private String soleTraderFirstName;
    @CCD(label = "Last name", showCondition = "type = \"SOLE_TRADER\"", searchable = false, retainHiddenValue = true)
    private String soleTraderLastName;
    @CCD(label = "Trading as", showCondition = "type = \"SOLE_TRADER\"", searchable = false, retainHiddenValue = true)
    private String soleTraderTradingAs;

    @CCD(
            label = "Date of birth",
            showCondition = "type = \"SOLE_TRADER\"",
            searchable = false,
            retainHiddenValue = true
    )
    @PastOrPresent(message = "The date entered cannot be in the future", groups = DateOfBirthGroup.class)
    private LocalDate soleTraderDateOfBirth;
    @CCD(label = "Address", searchable = false, retainHiddenValue = true)
    private Address primaryAddress;

    @CCD(label = "Party name", searchable = false, retainHiddenValue = true)
    private String partyName;
    @CCD(label = "Party name", showCondition = "type = \"DO_NOT_SHOW\"", searchable = false, retainHiddenValue = true)
    private String bulkClaimPartyName;
    @CCD(label = "Party type displayed value", searchable = false, retainHiddenValue = true)
    private String partyTypeDisplayValue;

    @CCD(label = "Email", searchable = false, retainHiddenValue = true, max = 320, typeOverride = FieldType.Email)
    private String partyEmail;
    @CCD(label = "Phone", searchable = false, retainHiddenValue = true, typeOverride = FieldType.PhoneUK)
    private String partyPhone;
    @CCD(ignore = true)
    private String legalRepHeading;

    @CCD(label = "Unavailable date", searchable = false, retainHiddenValue = true)
    private List<Element<UnavailableDate>> unavailableDates;

    @CCD(label = " ", searchable = false, retainHiddenValue = true)
    private Flags flags;

    public enum Type {
        INDIVIDUAL,
        COMPANY,
        ORGANISATION,
        SOLE_TRADER;

        public String getDisplayValue() {
            return StringUtils.capitalize(this.name().toLowerCase().replace('_', ' '));
        }
    }

    public String getPartyName(boolean omitTitle) {
        return getPartyNameBasedOnType(this, omitTitle);
    }

    public String getPartyName() {
        return getPartyName(false);
    }

    public String getPartyTypeDisplayValue() {
        return this.getType().getDisplayValue();
    }

    @JsonIgnore
    public boolean isIndividual() {
        return INDIVIDUAL.equals(getType());
    }

    @JsonIgnore
    public boolean isSoleTrader() {
        return SOLE_TRADER.equals(getType());
    }

    @JsonIgnore
    public boolean isCompany() {
        return COMPANY.equals(getType());
    }

    @JsonIgnore
    public boolean isOrganisation() {
        return ORGANISATION.equals(getType());
    }

    @JsonIgnore
    public boolean isCompanyOROrganisation() {
        return this.isCompany() || this.isOrganisation();
    }

    @JsonIgnore
    public boolean isIndividualORSoleTrader() {
        return this.isIndividual() || this.isSoleTrader();
    }

    @JsonIgnore
    public LocalDate getDateOfBirth() {
        return Optional.ofNullable(individualDateOfBirth).orElse(soleTraderDateOfBirth);
    }

    public Party copy() {
        return new Party()
            .setPartyID(partyID)
            .setType(type)
            .setIndividualTitle(individualTitle)
            .setIndividualFirstName(individualFirstName)
            .setIndividualLastName(individualLastName)
            .setIndividualDateOfBirth(individualDateOfBirth)
            .setCompanyName(companyName)
            .setOrganisationName(organisationName)
            .setSoleTraderTitle(soleTraderTitle)
            .setSoleTraderFirstName(soleTraderFirstName)
            .setSoleTraderLastName(soleTraderLastName)
            .setSoleTraderTradingAs(soleTraderTradingAs)
            .setSoleTraderDateOfBirth(soleTraderDateOfBirth)
            .setPrimaryAddress(primaryAddress)
            .setPartyName(partyName)
            .setBulkClaimPartyName(bulkClaimPartyName)
            .setPartyTypeDisplayValue(partyTypeDisplayValue)
            .setPartyEmail(partyEmail)
            .setPartyPhone(partyPhone)
            .setLegalRepHeading(legalRepHeading)
            .setUnavailableDates(unavailableDates)
            .setFlags(flags);
    }
}

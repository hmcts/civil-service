package uk.gov.hmcts.reform.civil.ga.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.validation.groups.DateOfBirthGroup;

import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.model.Party.Type.COMPANY;
import static uk.gov.hmcts.reform.civil.model.Party.Type.INDIVIDUAL;
import static uk.gov.hmcts.reform.civil.model.Party.Type.ORGANISATION;
import static uk.gov.hmcts.reform.civil.model.Party.Type.SOLE_TRADER;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class GeneralApplicationParty {

    private String partyID;
    private Type type;
    private String individualTitle;
    private String individualFirstName;
    private String individualLastName;

    @PastOrPresent(message = "The date entered cannot be in the future", groups = DateOfBirthGroup.class)
    private LocalDate individualDateOfBirth;
    private String companyName;
    private String organisationName;
    private String soleTraderTitle;
    private String soleTraderFirstName;
    private String soleTraderLastName;
    private String soleTraderTradingAs;

    @PastOrPresent(message = "The date entered cannot be in the future", groups = DateOfBirthGroup.class)
    private LocalDate soleTraderDateOfBirth;
    private Address primaryAddress;

    private String partyName;
    private String bulkClaimPartyName;
    private String partyTypeDisplayValue;

    private String partyEmail;
    private String partyPhone;
    private String legalRepHeading;

    private Flags flags;

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

    public enum Type {
        INDIVIDUAL,
        COMPANY,
        ORGANISATION,
        SOLE_TRADER;

        public String getDisplayValue() {
            return StringUtils.capitalize(this.name().toLowerCase().replace('_', ' '));
        }
    }
}

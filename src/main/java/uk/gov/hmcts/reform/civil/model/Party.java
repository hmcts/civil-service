package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.civil.validation.groups.DateOfBirthGroup;

import java.time.LocalDate;
import javax.validation.constraints.PastOrPresent;

import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Party {

    private final Type type;
    private final String individualTitle;
    private final String individualFirstName;
    private final String individualLastName;

    @PastOrPresent(message = "The date entered cannot be in the future", groups = DateOfBirthGroup.class)
    private final LocalDate individualDateOfBirth;
    private final String companyName;
    private final String organisationName;
    private final String soleTraderTitle;
    private final String soleTraderFirstName;
    private final String soleTraderLastName;
    private final String soleTraderTradingAs;

    @PastOrPresent(message = "The date entered cannot be in the future", groups = DateOfBirthGroup.class)
    private final LocalDate soleTraderDateOfBirth;
    private final Address primaryAddress;

    private final String partyName;
    private final String partyTypeDisplayValue;

    public enum Type {
        INDIVIDUAL,
        COMPANY,
        ORGANISATION,
        SOLE_TRADER;

        public String getDisplayValue() {
            return StringUtils.capitalize(this.name().toLowerCase().replace('_', ' '));
        }
    }

    public String getPartyName() {
        if (partyName == null) {
            return getPartyNameBasedOnType(this);
        }
        return partyName;
    }

    public String getPartyTypeDisplayValue() {
        return this.getType().getDisplayValue();
    }
}

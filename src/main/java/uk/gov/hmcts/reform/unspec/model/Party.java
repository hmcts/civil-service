package uk.gov.hmcts.reform.unspec.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Party {

    private final Type type;
    private final String individualTitle;
    private final String individualFirstName;
    private final String individualLastName;
    private final LocalDate individualDateOfBirth;
    private final String companyName;
    private final String organisationName;
    private final String soleTraderTitle;
    private final String soleTraderFirstName;
    private final String soleTraderLastName;
    private final String soleTraderTradingAs;
    private final LocalDate soleTraderDateOfBirth;
    private final Address primaryAddress;

    public enum Type {
        INDIVIDUAL,
        COMPANY,
        ORGANISATION,
        SOLE_TRADER
    }
}

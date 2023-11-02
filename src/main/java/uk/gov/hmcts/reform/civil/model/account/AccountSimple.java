package uk.gov.hmcts.reform.civil.model.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.math.BigDecimal;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AccountSimple {

    /**
     * balance in pounds.
     */
    @NotNull
    private BigDecimal balance;
    /**
     * true if joint account, false if not, null if unknown.
     */
    @NotNull
    private YesOrNo jointAccount;
    @NotNull
    private AccountType accountType;
}

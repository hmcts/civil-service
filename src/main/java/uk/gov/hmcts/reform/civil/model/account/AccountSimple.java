package uk.gov.hmcts.reform.civil.model.account;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountSimple {

    /**
     * balance in pounds.
     */
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal balance;
    /**
     * true if joint account, false if not, null if unknown.
     */
    @NotNull
    private YesOrNo jointAccount;
    @NotNull
    private AccountType accountType;
}

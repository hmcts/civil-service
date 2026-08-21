package uk.gov.hmcts.reform.civil.model.account;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "BankAccount", generate = true)
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class AccountSimple {

    /**
     * balance in pounds.
     */
    @CCD(label = "Balance (£)", searchable = false)
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal balance;
    /**
     * true if joint account, false if not, null if unknown.
     */
    @CCD(label = "Joint account", searchable = false, typeOverride = FieldType.YesOrNo)
    @NotNull
    private YesOrNo jointAccount;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.FixedList, typeParameterOverride = "AccountType")
    @NotNull
    private AccountType accountType;
}

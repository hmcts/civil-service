package uk.gov.hmcts.reform.civil.model.docmosis.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.civil.model.account.AccountSimple;

@SuperBuilder
@AllArgsConstructor
@Getter
public class AccountSimpleTemplateData extends AccountSimple {

    private int index;

    public AccountSimpleTemplateData(AccountSimple data, int index) {
        setAccountType(data.getAccountType());
        setJointAccount(data.getJointAccount());
        setBalance(data.getBalance().setScale(2));
        this.index = index;
    }

    public String getTypeDisplay() {
        if (getAccountType() != null) {
            switch (getAccountType()) {
                case ISA:
                    return "ISA";
                case CURRENT:
                    return "Current";
                case SAVINGS:
                    return "Savings";
                default:
                    return "Other";
            }
        }
        return null;
    }
}

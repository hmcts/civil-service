package uk.gov.hmcts.reform.civil.model.docmosis.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.account.AccountSimple;

@Builder
@AllArgsConstructor
@Getter
public class AccountSimpleTemplateData extends AccountSimple {

    private int index;

    public AccountSimpleTemplateData(AccountSimple data, int index) {
        setAccountType(data.getAccountType());
        setJointAccount(data.getJointAccount());
        setBalance(data.getBalance());
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

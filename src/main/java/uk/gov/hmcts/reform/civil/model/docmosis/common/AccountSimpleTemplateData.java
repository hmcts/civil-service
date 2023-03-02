package uk.gov.hmcts.reform.civil.model.docmosis.common;

import uk.gov.hmcts.reform.civil.model.account.AccountSimple;

public class AccountSimpleTemplateData extends AccountSimple {

    public AccountSimpleTemplateData(AccountSimple data) {
        setAccountType(data.getAccountType());
        setJointAccount(data.getJointAccount());
        setBalance(data.getBalance());
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

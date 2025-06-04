package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.HashMap;

@Component
public class PayBySetDateDefendantAmountIncludesTextParamsBuilder extends DashboardNotificationsParamsBuilder {

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        if (caseData.isPayBySetDate() || caseData.isPayByInstallment()) {
            if (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())) {
                params.put("defendantAmountIncludesTextEn",
                    "(this amount includes interest if it has been claimed which will continue to accrue to the date of Judgment, settlement agreement or earlier payment)");
                params.put("defendantAmountIncludesTextCy",
                    "(mae'r swm hwn yn cynnwys llog os hawlir a fydd yn parhau i gronni hyd at ddyddiad y dyfarniad, cytundeb setlo neu daliad cynharach)");
            } else if (RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())) {
                {
                    params.put("defendantAmountIncludesTextEn",
                        "plus the claim fee and any fixed costs claimed");
                    params.put("defendantAmountIncludesTextCy",
                        "ynghyd â ffi’r hawliad ac unrhyw gostau sefydlog a hawlir");
                }
            }
        }
    }
}


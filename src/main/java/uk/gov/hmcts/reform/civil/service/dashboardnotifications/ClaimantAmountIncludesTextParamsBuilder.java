package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.HashMap;

@Component
public class ClaimantAmountIncludesTextParamsBuilder extends DashboardNotificationsParamsBuilder {

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {

        if (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())) {
            params.put("claimantAmountIncludesTextEn", "(this amount includes interest if it has been claimed which may continue to accrue to " +
                "the date of Judgment, settlement agreement or earlier payment)");
            params.put("claimantAmountIncludesTextCy",
                "(mae’r swm hwn yn cynnwys llog os yw wedi’i hawlio a gall barhau i gronni hyd dyddiad y Dyfarniad, " +
                    "y cytundeb setlo neu daliad cynharach)");
        } else if (RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())) {
            params.put("claimantAmountIncludesTextEn", "plus the claim fee");
            params.put("claimantAmountIncludesTextCy", "ynghyd â ffi’r hawliad");
        }
    }
}

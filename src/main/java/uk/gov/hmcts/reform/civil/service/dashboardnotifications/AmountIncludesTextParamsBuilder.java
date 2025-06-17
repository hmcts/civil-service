package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.HashMap;

@Component
public class AmountIncludesTextParamsBuilder extends DashboardNotificationsParamsBuilder {

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        if (caseData.isFullAdmitClaimSpec()) {
            params.put("amountIncludesTextEn",
                ". This amount includes interest if it has been claimed which may continue to accrue to the date of Judgment,"
                    + " settlement agreement or earlier payment");
            params.put("amountIncludesTextCy",
                ". Mae’r swm hwn yn cynnwys llog os yw wedi’i hawlio a gall barhau i gronni hyd dyddiad y Dyfarniad, y cytundeb setlo neu daliad cynharach");
        } else if (caseData.isPartAdmitClaimSpec()) {
            params.put("amountIncludesTextEn", " plus the claim fee and any fixed costs claimed");
            params.put("amountIncludesTextCy", " ynghyd â ffi’r hawliad ac unrhyw gostau sefydlog a hawlir");

        }
    }
}

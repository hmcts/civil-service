package uk.gov.hmcts.reform.civil.service.sdo;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;

@Service
public class SdoCaseClassificationService {

    public boolean isSmallClaimsTrack(CaseData caseData) {
        return SdoHelper.isSmallClaimsTrack(caseData);
    }

    public boolean isFastTrack(CaseData caseData) {
        return SdoHelper.isFastTrack(caseData);
    }

    public boolean isNihlFastTrack(CaseData caseData) {
        return SdoHelper.isNihlFastTrack(caseData);
    }

    public boolean isDrhSmallClaim(CaseData caseData) {
        return SdoHelper.isSDOR2ScreenForDRHSmallClaim(caseData);
    }
}

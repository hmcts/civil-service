package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateddor2andnihlfields;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2AddendumReport;

import java.time.LocalDate;

@Component
public class SdoR2AndNihlAddendumReportFieldBuilder implements SdoR2AndNihlCaseFieldBuilder {

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoR2AddendumReport(SdoR2AddendumReport.builder()
                .sdoAddendumReportTxt(SdoR2UiConstantFastTrack.ADDENDUM_REPORT)
                .sdoAddendumReportDate(LocalDate.now().plusDays(56))
                .build());
    }
}

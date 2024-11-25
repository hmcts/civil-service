package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateddor2andnihlfields;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2PermissionToRelyOnExpert;

import java.time.LocalDate;

@Component
public class SdoR2AndNihlPermissionToRelyOnExpertFieldBuilder implements SdoR2AndNihlCaseFieldBuilder {

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.sdoR2PermissionToRelyOnExpert(SdoR2PermissionToRelyOnExpert.builder()
                .sdoPermissionToRelyOnExpertTxt(SdoR2UiConstantFastTrack.PERMISSION_TO_RELY_ON_EXPERT)
                .sdoPermissionToRelyOnExpertDate(LocalDate.now().plusDays(119))
                .sdoJointMeetingOfExpertsTxt(SdoR2UiConstantFastTrack.JOINT_MEETING_OF_EXPERTS)
                .sdoJointMeetingOfExpertsDate(LocalDate.now().plusDays(147))
                .sdoUploadedToDigitalPortalTxt(SdoR2UiConstantFastTrack.UPLOADED_TO_DIGITAL_PORTAL_7_DAYS)
                .build());
    }
}

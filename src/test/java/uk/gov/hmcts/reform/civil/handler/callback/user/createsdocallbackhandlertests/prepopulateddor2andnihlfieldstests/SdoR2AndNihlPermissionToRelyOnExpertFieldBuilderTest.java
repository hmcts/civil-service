package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.prepopulateddor2andnihlfieldstests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateddor2andnihlfields.SdoR2AndNihlPermissionToRelyOnExpertFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2PermissionToRelyOnExpert;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class SdoR2AndNihlPermissionToRelyOnExpertFieldBuilderTest {

    @InjectMocks
    private SdoR2AndNihlPermissionToRelyOnExpertFieldBuilder sdoR2AndNihlPermissionToRelyOnExpertFieldBuilder;

    @Test
    void shouldBuildSdoR2PermissionToRelyOnExpert() {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        sdoR2AndNihlPermissionToRelyOnExpertFieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        SdoR2PermissionToRelyOnExpert permissionToRelyOnExpert = caseData.getSdoR2PermissionToRelyOnExpert();

        assertEquals(SdoR2UiConstantFastTrack.PERMISSION_TO_RELY_ON_EXPERT, permissionToRelyOnExpert.getSdoPermissionToRelyOnExpertTxt());
        assertEquals(LocalDate.now().plusDays(119), permissionToRelyOnExpert.getSdoPermissionToRelyOnExpertDate());
        assertEquals(SdoR2UiConstantFastTrack.JOINT_MEETING_OF_EXPERTS, permissionToRelyOnExpert.getSdoJointMeetingOfExpertsTxt());
        assertEquals(LocalDate.now().plusDays(147), permissionToRelyOnExpert.getSdoJointMeetingOfExpertsDate());
        assertEquals(SdoR2UiConstantFastTrack.UPLOADED_TO_DIGITAL_PORTAL_7_DAYS, permissionToRelyOnExpert.getSdoUploadedToDigitalPortalTxt());
    }
}
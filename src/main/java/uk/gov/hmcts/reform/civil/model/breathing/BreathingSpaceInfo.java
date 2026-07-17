package uk.gov.hmcts.reform.civil.model.breathing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.civil.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECruRESSOLONESPECPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruCaseworkerCivilStaffRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLTWOSPECPROFILERAccess;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class BreathingSpaceInfo {

    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruRESSOLONESPECPROFILERAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilStaffRAccess.class, RESSOLTWOSPECPROFILERAccess.class}
    )
    @JsonProperty("enterBreathing")
    private BreathingSpaceEnterInfo enter;

    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruRESSOLONESPECPROFILERAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilStaffRAccess.class, RESSOLTWOSPECPROFILERAccess.class}
    )
    @JsonProperty("liftBreathing")
    private BreathingSpaceLiftInfo lift;
}

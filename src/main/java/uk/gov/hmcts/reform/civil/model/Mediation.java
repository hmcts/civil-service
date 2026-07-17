package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILERESSOLONESPECPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLTWOSPECPROFILERCaseworkerCivilCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENDEFENDANTPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLTWOSPECPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilCuAccess;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mediation {

    @JsonUnwrapped
    private MediationSuccessful mediationSuccessful;
    @CCD(
            label = "Mediation failed reason",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "MediationUnsuccessfulReasons",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILERAccess.class, APPSOLSPECPROFILERESSOLONESPECPROFILERAccess.class, RESSOLTWOSPECPROFILERCaseworkerCivilCruAccess.class}
    )
    private String unsuccessfulMediationReason;
    @CCD(
            label = "Mediation failed reason",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "MediationUnsuccessfulReasonsMultiSelect",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, APPSOLSPECPROFILERESSOLONESPECPROFILERAccess.class, CITIZENDEFENDANTPROFILERAccess.class, RESSOLTWOSPECPROFILERAccess.class, CaseworkerCivilCuAccess.class}
    )
    private List<MediationUnsuccessfulReason> mediationUnsuccessfulReasonsMultiSelect;
}

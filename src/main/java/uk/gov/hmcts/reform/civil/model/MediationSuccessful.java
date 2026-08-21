package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILERESSOLONESPECPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLTWOSPECPROFILERCaseworkerCivilCruAccess;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MediationSuccessful {

    @CCD(
            label = "Mediation settlement agreed on",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILERAccess.class, APPSOLSPECPROFILERESSOLONESPECPROFILERAccess.class, RESSOLTWOSPECPROFILERCaseworkerCivilCruAccess.class}
    )
    private LocalDate mediationSettlementAgreedAt;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILERAccess.class, APPSOLSPECPROFILERESSOLONESPECPROFILERAccess.class, RESSOLTWOSPECPROFILERCaseworkerCivilCruAccess.class}
    )
    private MediationAgreementDocument mediationAgreement;
}

package uk.gov.hmcts.reform.civil.model.mediation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECruCaseworkerCivilSystemFieldReaderRAccess;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadMediationDocumentsForm {

    @CCD(
            label = "Select one of the options",
            searchable = false,
            typeOverride = FieldType.DynamicRadioList,
            access = {RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, APPSOLSPECPROFILECruCaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private DynamicList uploadMediationDocumentsPartyChosen;
    @CCD(
            label = "Select the type of document you want to upload",
            searchable = false,
            access = {RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, APPSOLSPECPROFILECruCaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private List<MediationDocumentsType> mediationDocumentsType;
    @CCD(
            label = "Non-attendance statement",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "NonAttendanceMediationStatement",
            access = {RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, APPSOLSPECPROFILECruCaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private List<Element<MediationNonAttendanceStatement>> nonAttendanceStatementForm;
    @CCD(
            label = "Documents referred to in statement",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentsReferred",
            access = {RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, APPSOLSPECPROFILECruCaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private List<Element<MediationDocumentsReferredInStatement>> documentsReferredForm;
}

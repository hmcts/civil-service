package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCrudCaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONESPECPROFILERESSOLTWOSPECPROFILECrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLUNSPECPROFILECrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONEUNSPECPROFILECrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLTWOUNSPECPROFILECrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRAccess;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class UpdateDetailsForm {

    @CCD(
            label = "Select party",
            searchable = false,
            typeOverride = FieldType.DynamicRadioList,
            access = {CaseworkerCivilAdminCrudCaseworkerCivilSystemFieldReaderRAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECrudAccess.class, APPSOLSPECPROFILECrudAccess.class, APPSOLUNSPECPROFILECrudAccess.class, RESSOLONEUNSPECPROFILECrudAccess.class, RESSOLTWOUNSPECPROFILECrudAccess.class}
    )
    private DynamicList partyChosen;
    @CCD(
            label = "Party chosen id",
            searchable = false,
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilAdminCrudAccess.class}
    )
    private String partyChosenId;
    @CCD(
            label = "Party chosen type",
            searchable = false,
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilAdminCrudAccess.class}
    )
    private String partyChosenType;
    @CCD(
            label = "Hide party choice",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilAdminCrudAccess.class}
    )
    private YesOrNo hidePartyChoice;
    @CCD(
            label = "Unavailable Dates",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilSolicitorCruAccess.class}
    )
    private List<Element<UnavailableDate>> additionalUnavailableDates;
    @CCD(
            label = "Experts",
            searchable = false,
            access = {CaseworkerCivilAdminCrudCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilSolicitorCruAccess.class}
    )
    private List<Element<UpdatePartyDetailsForm>> updateExpertsDetailsForm;
    @CCD(
            label = "Witnesses",
            searchable = false,
            access = {CaseworkerCivilAdminCrudCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilSolicitorCruAccess.class}
    )
    private List<Element<UpdatePartyDetailsForm>> updateWitnessesDetailsForm;
    @CCD(
            label = "Individual attending for the legal representative details",
            searchable = false,
            access = {CaseworkerCivilAdminCrudCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilSolicitorCruAccess.class}
    )
    private List<Element<UpdatePartyDetailsForm>> updateLRIndividualsForm;
    @CCD(
            label = "Individuals attending for the organisation details",
            searchable = false,
            access = {CaseworkerCivilAdminCrudCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilSolicitorCruAccess.class}
    )
    private List<Element<UpdatePartyDetailsForm>> updateOrgIndividualsForm;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private YesOrNo manageContactDetailsEventUsed;
}

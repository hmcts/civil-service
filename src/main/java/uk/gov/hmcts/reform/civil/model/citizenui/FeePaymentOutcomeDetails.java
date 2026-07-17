package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRCuiAdminProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CuiAdminProfileCruAccess;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class FeePaymentOutcomeDetails {

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSystemFieldReaderRCuiAdminProfileCruAccess.class}
    )
    private YesOrNo hwfNumberAvailable;
    @CCD(
            label = "Enter the reference number",
            hint = "For example, HWF-A1B-23C, PA12-123456",
            regex = "HWF-[0-9A-Za-z]{3}-[0-9A-Za-z]{3}|PA[0-9]{2}-[0-9]{6}",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCuiAdminProfileCruAccess.class}
    )
    private String  hwfNumberForFeePaymentOutcome;
    @CCD(
            label = " ",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSystemFieldReaderRCuiAdminProfileCruAccess.class}
    )
    private YesOrNo hwfFullRemissionGrantedForClaimIssue;
    @CCD(
            label = " ",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class, CITIZENCLAIMANTPROFILERAccess.class, CuiAdminProfileCruAccess.class}
    )
    private YesOrNo hwfFullRemissionGrantedForHearingFee;
    @CCD(
            label = "Confirm payment of £${claimIssuedHwfDetails.outstandingFeeInPounds} has been taken.",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "ConfirmPaymentMade",
            access = {CaseworkerCivilSystemFieldReaderRCuiAdminProfileCruAccess.class}
    )
    private List<String> hwfOutstandingFeePaymentDoneForClaimIssue;
    @CCD(
            label = "Confirm payment of £${hearingHwfDetails.outstandingFeeInPounds} has been taken.",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "ConfirmPaymentMade",
            access = {CaseworkerCivilSystemFieldReaderRCuiAdminProfileCruAccess.class}
    )
    private List<String> hwfOutstandingFeePaymentDoneForHearingFee;
    @CCD(ignore = true)
    private YesOrNo hwfFullRemissionGrantedForGa;
    @CCD(ignore = true)
    private YesOrNo hwfFullRemissionGrantedForAdditionalFee;
    @CCD(ignore = true)
    private List<String> hwfOutstandingFeePaymentDoneForGa;
    @CCD(ignore = true)
    private List<String> hwfOutstandingFeePaymentDoneForAdditional;

    public FeePaymentOutcomeDetails copy() {
        return new FeePaymentOutcomeDetails(
            hwfNumberAvailable,
            hwfNumberForFeePaymentOutcome,
            hwfFullRemissionGrantedForClaimIssue,
            hwfFullRemissionGrantedForHearingFee,
            hwfOutstandingFeePaymentDoneForClaimIssue,
            hwfOutstandingFeePaymentDoneForHearingFee,
            hwfFullRemissionGrantedForGa,
            hwfFullRemissionGrantedForAdditionalFee,
            hwfOutstandingFeePaymentDoneForGa,
            hwfOutstandingFeePaymentDoneForAdditional
        );
    }
}

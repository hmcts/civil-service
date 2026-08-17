package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import java.math.BigDecimal;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.civil.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruCaseworkerCivilStaffRAccess;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CCJPaymentDetails {

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilSystemupdateCruAccess.class}
    )
    private YesOrNo ccjPaymentPaidSomeOption;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Text,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class, CITIZENCLAIMANTPROFILECruAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal ccjJudgmentAmountClaimAmount;
    @CCD(
            label = "Amount already paid",
            searchable = false,
            typeOverride = FieldType.MoneyGBP,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilSystemupdateCruAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal ccjPaymentPaidSomeAmount;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Text,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class, CITIZENCLAIMANTPROFILECruAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal ccjJudgmentAmountClaimFee;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Text,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal ccjPaymentPaidSomeAmountInPounds;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Text,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class, CITIZENCLAIMANTPROFILECruAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal ccjJudgmentSummarySubtotalAmount;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Text,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class, CITIZENCLAIMANTPROFILECruAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal ccjJudgmentTotalStillOwed;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Text,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class, CITIZENCLAIMANTPROFILECruAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal ccjJudgmentAmountInterestToDate;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Text,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal ccjJudgmentFixedCostAmount;
    @CCD(
            label = " ",
            hint = "This will include fixed costs for judgment",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class, CITIZENCLAIMANTPROFILECruAccess.class}
    )
    private YesOrNo ccjJudgmentFixedCostOption;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class}
    )
    private String ccjJudgmentStatement;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Text,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilStaffRAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal ccjJudgmentLipInterest;

}
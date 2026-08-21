package uk.gov.hmcts.reform.civil.model.judgmentonline;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "JoJudgment", generate = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class JudgmentDetails {

    @CCD(label = " ", searchable = false)
    private Integer judgmentId;
    @CCD(label = " ", searchable = false)
    private String defendant1Name;
    @CCD(label = " ", searchable = false)
    private String defendant2Name;
    @CCD(label = " ", searchable = false)
    private JudgmentAddress defendant1Address;
    @CCD(label = " ", searchable = false)
    private JudgmentAddress defendant2Address;
    @CCD(label = " ", searchable = false)
    private LocalDate defendant1Dob;
    @CCD(label = " ", searchable = false)
    private LocalDate defendant2Dob;
    @CCD(label = " ", searchable = false)
    private LocalDateTime lastUpdateTimeStamp;
    @CCD(label = " ", searchable = false)
    private LocalDateTime createdTimestamp;
    @CCD(label = " ", searchable = false)
    private LocalDateTime cancelledTimeStamp;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.FixedList, typeParameterOverride = "JudgmentState")
    private JudgmentState state;
    @CCD(label = " ", searchable = false)
    private String rtlState;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo isRegisterWithRTL;
    @CCD(label = " ", searchable = false)
    private LocalDate requestDate;
    @CCD(label = " ", searchable = false)
    private LocalDate issueDate;
    @CCD(label = " ", searchable = false)
    private LocalDate setAsideDate;
    @CCD(label = " ", searchable = false)
    private LocalDate setAsideApplicationDate;
    @CCD(label = " ", searchable = false)
    private LocalDate cancelDate;
    @CCD(label = " ", searchable = false)
    private LocalDate fullyPaymentMadeDate;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo isJointJudgment;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.MoneyGBP)
    private String orderedAmount;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.MoneyGBP)
    private String costs;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.MoneyGBP)
    private String claimFeeAmount;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.MoneyGBP)
    private String amountAlreadyPaid;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.MoneyGBP)
    private String totalAmount;
    @CCD(label = " ", searchable = false)
    private String courtLocation;
    @CCD(label = " ", searchable = false)
    private JudgmentInstalmentDetails instalmentDetails;
    @CCD(label = " ", searchable = false)
    private JudgmentPaymentPlan paymentPlan;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.FixedList, typeParameterOverride = "JudgmentType")
    private JudgmentType type;
}

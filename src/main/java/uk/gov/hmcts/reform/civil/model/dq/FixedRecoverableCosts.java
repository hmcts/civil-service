package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.ComplexityBand;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class FixedRecoverableCosts {

    @CCD(
            label = "Is this claim subject to the Fixed Recoverable Cost Regime requiring the court to determine a complexity band?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    public YesOrNo isSubjectToFixedRecoverableCostRegime;
    @CCD(
            label = "Which complexity band do you believe this claim falls into?",
            showCondition = "isSubjectToFixedRecoverableCostRegime = \"Yes\"",
            searchable = false
    )
    public ComplexityBand band;
    @CCD(
            label = "Has this complexity band been agreed with the other party?",
            showCondition = "isSubjectToFixedRecoverableCostRegime = \"Yes\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    public YesOrNo complexityBandingAgreed;
    @CCD(
            label = "Please give your reasons",
            showCondition = "isSubjectToFixedRecoverableCostRegime = \"Yes\" OR isSubjectToFixedRecoverableCostRegime = \"No\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    public String reasons;
    // below used in intermediate claim FRC
    @CCD(
            label = "Upload supporting evidence",
            showCondition = "isSubjectToFixedRecoverableCostRegime = \"DONOTSHOW\"",
            regex = ".pdf,.txt,.doc,.dot,.docx,.rtf,.xls,.xlt,.xla,.xlsx,.xltx,.xlsb,.ppt,.pot,.pps,.ppa,.pptx,.potx,.ppsx,.jpg,.jpeg,.bmp,.tif,.tiff,.png",
            searchable = false
    )
    public Document frcSupportingDocument;
}

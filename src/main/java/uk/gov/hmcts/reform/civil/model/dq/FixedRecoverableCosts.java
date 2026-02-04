package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.ComplexityBand;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class FixedRecoverableCosts {

    public YesOrNo isSubjectToFixedRecoverableCostRegime;
    public ComplexityBand band;
    public YesOrNo complexityBandingAgreed;
    public String reasons;
    // below used in intermediate claim FRC
    public Document frcSupportingDocument;
}

package uk.gov.hmcts.reform.civil.model.defaultjudgment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingBundleType;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DisposalHearingBundleDJ {

    @CCD(label = " ", searchable = false)
    private String input;
    @CCD(label = " ", searchable = false)
    private List<DisposalHearingBundleType> type;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "## Disposal hearing bundle ##", searchable = false, typeOverride = FieldType.Label)
  private String label1;
  @CCD(label = "#### Select bundle type", searchable = false, typeOverride = FieldType.Label)
  private String label2;
  // ==== end synthesised definition-only fields ====
}

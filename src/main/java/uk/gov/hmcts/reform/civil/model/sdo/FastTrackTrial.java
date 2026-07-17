package uk.gov.hmcts.reform.civil.model.sdo;

import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackTrialBundleType;

import java.time.LocalDate;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class FastTrackTrial {

    @CCD(label = " ", searchable = false)
    private String input1;
    @CCD(label = " ", searchable = false)
    @Future(message = "The date entered must be in the future")
    private LocalDate date1;
    @CCD(label = " ", searchable = false)
    @Future(message = "The date entered must be in the future")
    private LocalDate date2;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String input2;
    @CCD(label = " ", searchable = false)
    private String input3;
    @CCD(label = " ", searchable = false)
    private List<FastTrackTrialBundleType> type;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String label;
  @CCD(label = "Trial date from", searchable = false, typeOverride = FieldType.Label)
  private String text1;
  @CCD(label = "Trial date to", searchable = false, typeOverride = FieldType.Label)
  private String text2;
  @CCD(label = "#### Bundle Type", searchable = false, typeOverride = FieldType.Label)
  private String label2;
  // ==== end synthesised definition-only fields ====
}

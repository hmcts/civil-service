package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimelineOfEvents {

    @CCD(ignore = true)
    private TimelineOfEventDetails value;
    @JsonIgnore
    private String id;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Date", hint = "For example, 12 11 2007", searchable = false)
  private java.time.LocalDate timelineDate;
  @CCD(label = "What happened", searchable = false, typeOverride = FieldType.TextArea)
  private String timelineDescription;
  // ==== end synthesised definition-only fields ====
}

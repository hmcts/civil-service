package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Setter
@Data
@NoArgsConstructor
public class GAHearingDateGAspec {

    @CCD(label = "Hearing scheduled related to this application", searchable = false)
    private YesOrNo hearingScheduledPreferenceYesNo;
    @CCD(
            label = " ",
            hint = "If there is more than one hearing, please provide the date of the earliest hearing",
            showCondition = "hearingScheduledPreferenceYesNo = \"Yes\"",
            searchable = false
    )
    private LocalDate hearingScheduledDate;

    @JsonCreator
    GAHearingDateGAspec(@JsonProperty("hearingScheduledPreferenceYesNo") YesOrNo hearingScheduledPreferenceYesNo,
                         @JsonProperty("hearingScheduledDate") LocalDate hearingScheduledDate) {

        this.hearingScheduledPreferenceYesNo = hearingScheduledPreferenceYesNo;
        this.hearingScheduledDate = hearingScheduledDate;
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "<p class=\"govuk-body govuk-!-margin-bottom-1 govuk-!-font-weight-bold\">Is the application for an adjournment of a hearing which is at least 14 days away?</p>",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String hearingScheduledPreferenceYesNoLabel;
  @CCD(
          label = "<p class=\"govuk-body govuk-!-margin-bottom-1 govuk-!-font-weight-bold\">Please provide the hearing dates.</p>",
          showCondition = "hearingScheduledPreferenceYesNo = \"Yes\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String hearingScheduledDateLabel;
  // ==== end synthesised definition-only fields ====
}

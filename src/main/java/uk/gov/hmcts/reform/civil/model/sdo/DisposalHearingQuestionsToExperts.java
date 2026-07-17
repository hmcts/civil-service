package uk.gov.hmcts.reform.civil.model.sdo;

import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DisposalHearingQuestionsToExperts {

    @CCD(label = " ", searchable = false)
    @Future(message = "The date entered must be in the future")
    private LocalDate date;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String label;
  @CCD(
          label = "Any questions which are to be addressed to an expert must be sent to the expert directly and uploaded to the Digital Portal by 4pm on",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String text1;
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String dateLabel;
  @CCD(
          label = "The answers to the questions shall be answered by the Expert within 14 days and uploaded to the Digital Portal within 21 days.",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String text2;
  // ==== end synthesised definition-only fields ====
}

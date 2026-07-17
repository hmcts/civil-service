package uk.gov.hmcts.reform.civil.model.finalorders;

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
public class OrderMadeOnDetailsOrderWithoutNotice {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String withOutNoticeText;
    @CCD(label = " ", hint = "For example, 16 4 2021", searchable = false)
    private LocalDate withOutNoticeDate;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "### Order without notice", hint = " ", searchable = false, typeOverride = FieldType.Label)
  private String withOutNoticeLabel;
  @CCD(label = "### Please enter date", hint = " ", searchable = false, typeOverride = FieldType.Label)
  private String withOutNoticeDateLabel;
  // ==== end synthesised definition-only fields ====
}

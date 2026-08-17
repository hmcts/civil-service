package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "GAWithOrWithoutNotice", generate = true)
@Setter
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class GAInformOtherParty {

    @CCD(label = " ", searchable = false)
    private YesOrNo isWithNotice;
    @CCD(
            label = "Why do you not want the court to inform the other party?\n",
            showCondition = "isWithNotice = \"No\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String reasonsForWithoutNotice;

    @JsonCreator
    GAInformOtherParty(
            @JsonProperty("isWithNotice") YesOrNo isWithNotice,
            @JsonProperty("reasonsForWithoutNotice") String reasonsForWithoutNotice) {
        this.isWithNotice = isWithNotice;
        this.reasonsForWithoutNotice = reasonsForWithoutNotice;
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "<p class=\"govuk-body\">If you select 'No', this is known as making an application without notice – you can only do this if it's permitted by a rule practice direction or court order \n\n If you confirm that the application should be dealt with without notice, the court will then decide if the application should be dealt with without notice</p>\n",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String isWithNoticeLabel;
  // ==== end synthesised definition-only fields ====
}

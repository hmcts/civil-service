package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.civil.model.GAUrgencyConsentGAspec;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "GAUrgencyRecordGAspec", generate = true)
@Setter
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class GAUrgencyRequirement {

    @CCD(
            label = "If you confirm that the application is urgent, the court will then decide if the application is urgent",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo generalAppUrgency;
    @CCD(
            label = "Reasons for urgency. Disclose all relevant information to the court, including any information that may undermine the need for urgency\n",
            showCondition = "generalAppUrgency = \"Yes\"",
            searchable = false,
            max = 400,
            typeOverride = FieldType.TextArea
    )
    private String reasonsForUrgency;
    @CCD(
            label = "Enter the date by which this application must be considered by a judge\n",
            showCondition = "generalAppUrgency = \"Yes\"",
            searchable = false
    )
    private LocalDate urgentAppConsiderationDate;

    @JsonCreator
    GAUrgencyRequirement(@JsonProperty("generalAppUrgency") YesOrNo generalAppUrgency,
                         @JsonProperty("reasonsForUrgency") String reasonsForUrgency,
                         @JsonProperty("urgentAppConsiderationDate") LocalDate urgentAppConsiderationDate) {
        this.generalAppUrgency = generalAppUrgency;
        this.reasonsForUrgency = reasonsForUrgency;
        this.urgentAppConsiderationDate = urgentAppConsiderationDate;
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @JsonProperty("ConsentAgreementCheckBox")
  @CCD(label = " ", showCondition = "generalAppUrgency = \"Yes\"", searchable = false)
  private java.util.Set<GAUrgencyConsentGAspec> consentAgreementCheckBox;
  // ==== end synthesised definition-only fields ====
}

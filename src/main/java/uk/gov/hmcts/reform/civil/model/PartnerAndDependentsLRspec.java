package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class PartnerAndDependentsLRspec {

    @CCD(label = "Does your client live with a partner?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo liveWithPartnerRequired;
    @CCD(
            label = "Is the partner aged 18 or over?",
            showCondition = "liveWithPartnerRequired = \"Yes\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo partnerAgedOver;
    @CCD(label = "Does your client have any children?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo haveAnyChildrenRequired;
    @CCD(
            label = "Do any of the children that live your client receive severe disability premium payments",
            showCondition = "haveAnyChildrenRequired = \"Yes\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo receiveDisabilityPayments;
    @CCD(label = " ", showCondition = "haveAnyChildrenRequired = \"Yes\"", searchable = false)
    private ChildrenByAgeGroupLRspec howManyChildrenByAgeGroup;
    @CCD(
            label = "Does your client support anyone else financially",
            hint = "This can include adults and people your client does not live with",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo supportedAnyoneFinancialRequired;
    @CCD(
            label = "Number of people",
            showCondition = "supportedAnyoneFinancialRequired = \"Yes\"",
            regex = "\\d+",
            searchable = false
    )
    private String supportPeopleNumber;
    @CCD(
            label = "Provide details",
            showCondition = "supportedAnyoneFinancialRequired = \"Yes\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String supportPeopleDetails;

    @JsonIgnore
    public boolean hasPartner() {
        return YesOrNo.YES == liveWithPartnerRequired;
    }
}

package uk.gov.hmcts.reform.civil.model.mediation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
public class MediationAvailability {

    @CCD(
            label = "Are there any dates in the next 3 months when you or your client cannot attend a mediation appointment? ",
            hint = "These should only be the dates of important events like medical appointments, other court hearings, or holidays that are already booked. If the mediation appointment is not attended, your client may face a penalty. \n\nThe Small Claims Mediation Service operates Monday to Friday from 8am to 5pm, except bank holidays",
            searchable = false,
            retainHiddenValue = true,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo isMediationUnavailablityExists;
    @CCD(
            label = "Unavailable dates",
            showCondition = "isMediationUnavailablityExists = \"Yes\"",
            searchable = false,
            retainHiddenValue = true,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "MediationUnavailableDate"
    )
    private List<Element<UnavailableDate>> unavailableDatesForMediation;

    @JsonCreator
    MediationAvailability(@JsonProperty("isMediationUnavailablityExists") YesOrNo isMediationUnavailablityExists,
                          @JsonProperty("unavailableDatesForMediation") List<Element<UnavailableDate>> unavailableDatesForMediation) {
        this.isMediationUnavailablityExists = isMediationUnavailablityExists;
        this.unavailableDatesForMediation = unavailableDatesForMediation;

    }

}

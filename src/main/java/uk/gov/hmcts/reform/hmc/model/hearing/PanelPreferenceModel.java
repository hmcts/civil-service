package uk.gov.hmcts.reform.hmc.model.hearing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PanelPreferenceModel {

    private String memberID;
    private MemberType memberType;
    private RequirementType requirementType;
}

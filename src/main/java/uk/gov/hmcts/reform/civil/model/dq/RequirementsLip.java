package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.dq.SupportRequirements;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RequirementsLip {

    @CCD(label = " ", searchable = false)
    private String name;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "HearingSupportRequirements"
    )
    private List<SupportRequirements> requirements;
    @CCD(label = " ", searchable = false)
    private String signLanguageRequired;
    @CCD(label = " ", searchable = false)
    private String languageToBeInterpreted;
    @CCD(label = " ", searchable = false)
    private String otherSupport;
}

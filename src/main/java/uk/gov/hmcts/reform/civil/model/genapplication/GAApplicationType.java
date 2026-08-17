package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "GATypeGAspec", generate = true)
@Setter
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class GAApplicationType {

    @CCD(
            label = "  ",
            hint = "  ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "GeneralApplicationTypesGAspec"
    )
    private List<GeneralApplicationTypes> types;

    @JsonCreator
    public GAApplicationType(@JsonProperty("types") List<GeneralApplicationTypes> types) {
        this.types = types;
    }
}

package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Objects;
import java.util.stream.Stream;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "ChildrenByAgeGroupDetails", generate = true)
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ChildrenByAgeGroupLRspec {

    @CCD(label = "Under 11", regex = "\\d+", searchable = false)
    private String numberOfUnderEleven;
    @CCD(label = "11 to 15", regex = "\\d+", searchable = false)
    private String numberOfElevenToFifteen;
    @CCD(label = "16 to 19", regex = "\\d+", searchable = false)
    private String numberOfSixteenToNineteen;

    @JsonIgnore
    public int getTotalChildren() {
        return Stream.of(
                numberOfElevenToFifteen,
                numberOfUnderEleven,
                numberOfSixteenToNineteen
            ).filter(Objects::nonNull)
            .mapToInt(Integer::parseInt).sum();
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "<p>Enter how many for each age group:</p>", searchable = false, typeOverride = FieldType.Label)
  private String ageGroupLabel;
  // ==== end synthesised definition-only fields ====
}
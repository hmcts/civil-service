package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Objects;
import java.util.stream.Stream;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class ChildrenByAgeGroupLRspec {

    private String numberOfUnderEleven;
    private String numberOfElevenToFifteen;
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
}
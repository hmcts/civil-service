package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

import java.util.Objects;
import java.util.stream.Stream;

@Data
@Builder
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

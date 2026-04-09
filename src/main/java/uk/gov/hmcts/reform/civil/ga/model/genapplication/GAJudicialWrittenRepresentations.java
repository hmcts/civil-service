package uk.gov.hmcts.reform.civil.ga.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeWrittenRepresentationsOptions;

import java.time.LocalDate;

@Setter
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class GAJudicialWrittenRepresentations {

    private GAJudgeWrittenRepresentationsOptions writtenOption;
    private LocalDate writtenSequentailRepresentationsBy;
    private LocalDate sequentialApplicantMustRespondWithin;
    private LocalDate writtenConcurrentRepresentationsBy;

    @JsonCreator
    GAJudicialWrittenRepresentations(@JsonProperty("makeAnOrderForWrittenRepresentations")
                                         GAJudgeWrittenRepresentationsOptions writtenOption,
                                     @JsonProperty("writtenSequentailRepresentationsBy")
                                         LocalDate writtenSequentailRepresentationsBy,
                                     @JsonProperty("sequentialApplicantMustRespondWithin")
                                         LocalDate sequentialApplicantMustRespondWithin,
                                     @JsonProperty("writtenConcurrentRepresentationsBy")
                                         LocalDate writtenConcurrentRepresentationsBy) {
        this.writtenOption = writtenOption;
        this.writtenSequentailRepresentationsBy = writtenSequentailRepresentationsBy;
        this.sequentialApplicantMustRespondWithin = sequentialApplicantMustRespondWithin;
        this.writtenConcurrentRepresentationsBy = writtenConcurrentRepresentationsBy;
    }
}

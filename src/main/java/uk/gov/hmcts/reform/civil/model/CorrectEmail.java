package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
public class CorrectEmail {

    private final String email;
    private final YesOrNo correct;

    public boolean isCorrect() {
        return correct == YesOrNo.YES;
    }
}

package uk.gov.hmcts.reform.civil.ga.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.civil.ga.enums.MakeAppAvailableCheckGAspec;

import java.util.List;

@Setter
@Data
@Builder(toBuilder = true)
public class GAMakeApplicationAvailableCheck {

    private List<MakeAppAvailableCheckGAspec> makeAppAvailableCheck;

    @JsonCreator
    GAMakeApplicationAvailableCheck(@JsonProperty("makeAppAvailableCheck") List<MakeAppAvailableCheckGAspec>
                                        makeAppAvailableCheck) {
        this.makeAppAvailableCheck = makeAppAvailableCheck;
    }
}

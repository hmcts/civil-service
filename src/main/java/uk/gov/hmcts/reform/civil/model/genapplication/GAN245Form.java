package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.civil.model.documents.Document;

@Setter
@Data
@Builder(toBuilder = true)
public class GAN245Form {

    private final Document generalAppN245FormUpload;

    @JsonCreator
    GAN245Form(@JsonProperty("generalAppN245FormUpload") Document generalAppN245FormUpload) {
        this.generalAppN245FormUpload = generalAppN245FormUpload;
    }
}

package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Optional;

@Accessors(chain = true)
@Setter
@Data
@NoArgsConstructor
@Builder(toBuilder = true)
public class GASolicitorDetailsGAspec {

    private String email;
    private String id;
    private String forename;
    private Optional<String> surname;
    private String organisationIdentifier;

    @JsonCreator
    GASolicitorDetailsGAspec(@JsonProperty("email") String email,
                 @JsonProperty("id") String id,
                 @JsonProperty("forename") String forename,
                 @JsonProperty("surname") Optional<String> surname,
                 @JsonProperty("organisationIdentifier") String organisationIdentifier) {

        this.email = email;
        this.id = id;
        this.forename = forename;
        this.surname = surname;
        this.organisationIdentifier = organisationIdentifier;
    }
}

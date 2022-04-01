package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.util.Optional;

@Setter
@Data
@Builder(toBuilder = true)
public class GAApplnSolGAspec {

    private final String email;
    private final String id;
    private final String forename;
    private final Optional<String> surname;
    private final String organisationIdentifier;

    @JsonCreator
    GAApplnSolGAspec(@JsonProperty("email") String email,
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

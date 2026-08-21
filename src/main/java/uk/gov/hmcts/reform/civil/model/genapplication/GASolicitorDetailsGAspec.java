package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Optional;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Setter
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class GASolicitorDetailsGAspec {

    @CCD(label = "Email ID", searchable = false, typeOverride = FieldType.Email)
    private String email;
    @CCD(label = "User ID", searchable = false)
    private String id;
    @CCD(label = "forename", searchable = false)
    private String forename;
    @CCD(label = "surname", searchable = false, typeOverride = FieldType.Text)
    private Optional<String> surname;
    @CCD(label = "organisationIdentifier", searchable = false)
    private String organisationIdentifier;

    @JsonCreator
    GASolicitorDetailsGAspec(
            @JsonProperty("email") String email,
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

    public GASolicitorDetailsGAspec copy() {
        return new GASolicitorDetailsGAspec()
                .setEmail(email)
                .setId(id)
                .setForename(forename)
                .setSurname(surname)
                .setOrganisationIdentifier(organisationIdentifier);
    }
}

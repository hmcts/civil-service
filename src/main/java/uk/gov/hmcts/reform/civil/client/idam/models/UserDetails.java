package uk.gov.hmcts.reform.civil.client.idam.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;
import java.util.Optional;

@Getter
@Builder
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDetails {
    private String id;
    private String email;
    private String forename;
    private String surname;
    private List<String> roles;

    public UserDetails() {
        super();
    }

    public UserDetails(
        String id,
        String email,
        String forename,
        String surname,
        List<String> roles
    ) {
        this.id = id;
        this.email = email;
        this.forename = forename;
        this.surname = surname;
        this.roles = roles;
    }
    
    public Optional<String> getSurname() {
        return Optional.ofNullable(surname);
    }

    public String getFullName() {
        return getSurname().map(s -> String.join(" ", forename, s))
            .orElse(forename);
    }
}

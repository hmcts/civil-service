package uk.gov.hmcts.reform.prd.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalUsersResponse {

    private String userIdentifier;
    private String firstName;
    private String lastName;
    private String email;
    private String idamStatus;
    private List<String> roles;
    private String idamStatusCode;
    private String idamMessage;
}

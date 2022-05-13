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
    private String email;
    private String firstName;
    private String idamMessage;
    private String idamStatus;
    private String idamStatusCode;
    private String lastName;
    private List<String> roles;
}

package uk.gov.hmcts.reform.civil.controllers.testingsupport;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prd.model.Organisation;

@Api
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(
    path = "/testing-support",
    produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE
)
@ConditionalOnExpression("${testing.support.enabled:false}")
public class AssignCaseSupportController {

    private final CoreCaseUserService coreCaseUserService;
    private final IdamClient idamClient;
    private final OrganisationService organisationService;
    private final AuthTokenGenerator authTokenGenerator;

    @PostMapping("/assign-case/{caseId}")
    @ApiOperation("Assign case to defendant")
    public void assignCase(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                           @PathVariable("caseId") String caseId) {
        String userId = idamClient.getUserInfo(authorisation).getUid();

        String organisationId = organisationService.findOrganisation(authorisation)
            .map(Organisation::getOrganisationIdentifier).orElse(null);

        coreCaseUserService.assignCase(caseId, userId, organisationId, CaseRole.RESPONDENTSOLICITORONE);
    }
}

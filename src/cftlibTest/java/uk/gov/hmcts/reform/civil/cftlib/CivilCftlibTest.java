package uk.gov.hmcts.reform.civil.cftlib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.civil.documentmanagement.SecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.rse.ccd.lib.test.CftlibTest;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The local stack bootWithCCD runs civil on: CCD with civil's definitions, Camunda with civil's business processes,
 * and cftlib's stand-ins for the platform services outside CCD.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CivilCftlibTest extends CftlibTest {

    private static final String SOLICITOR = "hmcts.civil+organisation.1.solicitor.1@gmail.com";
    private static final String SYSTEM_UPDATE = "civil-system-update@mailnesia.com";
    private static final String PASSWORD = "password";
    // cftlib's runtime, which serves IDAM's /loginUser for tests over the IDAM simulator.
    private static final String RUNTIME = "http://localhost:8489";

    @Autowired
    private UserService userService;

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private SecuredDocumentManagementService documentManagementService;

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void solicitorStartsASpecClaimAgainstTheImportedDefinition() {
        String authorisation = userService.getAccessToken(SOLICITOR, PASSWORD);
        String userId = userService.getUserInfo(authorisation).getUid();

        // Runs CREATE_CLAIM_SPEC's about-to-start callback in this service.
        var start = coreCaseDataApi.startForCaseworker(
            authorisation, serviceToken(), userId, "CIVIL", "CIVIL", "CREATE_CLAIM_SPEC");

        assertThat(start.getToken()).isNotBlank();
        assertThat(start.getEventId()).isEqualTo("CREATE_CLAIM_SPEC");
    }

    @Test
    @SneakyThrows
    void importsTheGeneralApplicationCaseType() {
        String authorisation = userService.getAccessToken(SYSTEM_UPDATE, PASSWORD);

        HttpResponse<String> jurisdictions = client.send(
            HttpRequest.newBuilder(URI.create(
                    "http://localhost:4452/aggregated/caseworkers/:uid/jurisdictions?access=read"))
                .header("Content-Type", "application/json")
                .header("Authorization", authorisation)
                .header("ServiceAuthorization", serviceToken())
                .build(),
            HttpResponse.BodyHandlers.ofString());

        assertThat(jurisdictions.statusCode()).isEqualTo(200);
        assertThat(mapper.readTree(jurisdictions.body()).findValuesAsText("id"))
            .contains("CIVIL", "GENERALAPPLICATION");
    }

    @Test
    @SneakyThrows
    void deploysCivilsBusinessProcessesToCamunda() {
        for (String process : new String[] {"CREATE_CLAIM_PROCESS_ID_SPEC_AFTER_PAYMENT", "GA_INITIATE_PROCESS_ID"}) {
            HttpResponse<String> definition = client.send(
                HttpRequest.newBuilder(URI.create(
                    CivilCftlibConfig.CAMUNDA_URL + "/engine-rest/process-definition/key/" + process + "/tenant-id/civil"))
                    .build(),
                HttpResponse.BodyHandlers.ofString());

            assertThat(definition.statusCode()).as(process).isEqualTo(200);
        }
    }

    @Test
    void storesAndReturnsDocumentsThroughCaseDocumentAm() {
        String authorisation = userService.getAccessToken(SOLICITOR, PASSWORD);
        byte[] content = "%PDF-1.4 civil cftlib".getBytes(StandardCharsets.UTF_8);

        CaseDocument uploaded = documentManagementService.uploadDocument(
            authorisation, new PDF("cftlib-test.pdf", content, DocumentType.SEALED_CLAIM));
        byte[] downloaded = documentManagementService.downloadDocument(
            authorisation, uploaded.getDocumentLink().getDocumentUrl());

        assertThat(uploaded.getDocumentLink().getDocumentHash()).isNotBlank();
        assertThat(downloaded).isEqualTo(content);
    }

    @Test
    @SneakyThrows
    void signsTestsInThroughLoginUser() {
        HttpResponse<String> login = client.send(
            HttpRequest.newBuilder(URI.create(RUNTIME + "/loginUser"
                                                  + "?username=" + URLEncoder.encode(SOLICITOR, StandardCharsets.UTF_8)
                                                  + "&password=" + PASSWORD))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build(),
            HttpResponse.BodyHandlers.ofString());
        assertThat(login.statusCode()).isEqualTo(200);
        String accessToken = mapper.readTree(login.body()).path("access_token").asText();

        HttpResponse<String> userInfo = client.send(
            HttpRequest.newBuilder(URI.create(RUNTIME + "/o/userinfo"))
                .header("Authorization", "Bearer " + accessToken)
                .build(),
            HttpResponse.BodyHandlers.ofString());
        JsonNode user = mapper.readTree(userInfo.body());
        assertThat(user.path("email").asText()).isEqualTo(SOLICITOR);
        assertThat(user.path("roles").toString()).contains("caseworker-civil-solicitor");
    }

    private String serviceToken() {
        return cftlib().generateDummyS2SToken("civil_service");
    }
}

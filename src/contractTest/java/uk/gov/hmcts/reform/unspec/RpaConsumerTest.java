package uk.gov.hmcts.reform.unspec;

import au.com.dius.pact.consumer.PactVerificationResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prd.client.OrganisationApi;
import uk.gov.hmcts.reform.prd.model.ContactInformation;
import uk.gov.hmcts.reform.prd.model.DxAddress;
import uk.gov.hmcts.reform.prd.model.Organisation;
import uk.gov.hmcts.reform.unspec.config.PrdAdminUserConfiguration;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.sendgrid.SendGridClient;
import uk.gov.hmcts.reform.unspec.service.OrganisationService;
import uk.gov.hmcts.reform.unspec.service.flowstate.FlowState;
import uk.gov.hmcts.reform.unspec.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.unspec.service.robotics.mapper.EventHistoryMapper;
import uk.gov.hmcts.reform.unspec.service.robotics.mapper.RoboticsAddressMapper;
import uk.gov.hmcts.reform.unspec.service.robotics.mapper.RoboticsDataMapper;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.unspec.matcher.IsValidJson.validateJson;

@Slf4j
@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    StateFlowEngine.class,
    EventHistoryMapper.class,
    RoboticsDataMapper.class,
    RoboticsAddressMapper.class,
    OrganisationService.class
})
class RpaConsumerTest extends BaseRpaTest {

    private static final ContactInformation CONTACT_INFORMATION = ContactInformation.builder()
        .addressLine1("line 1")
        .addressLine2("line 2")
        .postCode("AB1 2XY")
        .county("My county")
        .dxAddress(List.of(DxAddress.builder()
                               .dxNumber("DX 12345")
                               .build()))
        .build();
    private static final Organisation ORGANISATION = Organisation.builder()
        .organisationIdentifier("QWERTY")
        .name("Org Name")
        .contactInformation(List.of(CONTACT_INFORMATION))
        .build();

    @Autowired
    RoboticsDataMapper roboticsDataMapper;

    @MockBean
    SendGridClient sendGridClient;
    @MockBean
    OrganisationApi organisationApi;
    @MockBean
    AuthTokenGenerator authTokenGenerator;
    @MockBean
    IdamClient idamClient;
    @MockBean
    PrdAdminUserConfiguration userConfig;

    @BeforeEach
    void setUp() {
        given(organisationApi.findOrganisationById(any(), any(), any())).willReturn(ORGANISATION);
    }

    @Nested
    class UnrepresentedDefendant {

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenClaimAgainstUnrepresentedDefendant() {
            CaseData caseData = CaseDataBuilder.builder()
                .atState(FlowState.Main.PROCEEDS_OFFLINE_UNREPRESENTED_DEFENDANT)
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for claim against unrepresented defendant";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }
    }

    @Nested
    class UnregisteredDefendant {

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenClaimAgainstUnregisteredDefendant() {
            CaseData caseData = CaseDataBuilder.builder()
                .atState(FlowState.Main.PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT)
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for claim against unrepresented defendant";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }
    }

    @Nested
    class AdmissionsOrCounterClaim {

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenDefendantRespondedWithPartAdmission() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentPartAdmission().build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for defendant responded with part admission";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenDefendantRespondedWithFullAdmission() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmission().build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for defendant responded with full admission";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenDefendantRespondedWithCounterClaim() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentCounterClaim().build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for defendant responded with counter claim";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }
    }

    @Nested
    class FullDefenceNotProceed {

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenFullDefenceNotProceeds() {
            CaseData caseData = CaseDataBuilder.builder()
                .atState(FlowState.Main.FULL_DEFENCE_NOT_PROCEED)
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for defendant responded with full defence not proceeds";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }
    }

    @Nested
    class FullDefenceProceed {

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenFullDefenceProceeds() {
            CaseData caseData = CaseDataBuilder.builder()
                .atState(FlowState.Main.FULL_DEFENCE_PROCEED)
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for defendant responded with full defence confirms to proceeds";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }
    }
}

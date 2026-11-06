package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.notification.robotics.RoboticsNotifier;
import uk.gov.hmcts.reform.civil.service.robotics.JsonSchemaValidationService;
import uk.gov.hmcts.reform.civil.service.robotics.RoboticsNotificationService;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsCaseDataSupport;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsPartyLookup;
import uk.gov.hmcts.reform.civil.utils.LocationRefDataUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

class RoboticsAddressPayloadTest {

    private static final String AUTH_TOKEN = "test-token";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonSchemaValidationService schemaValidator = new JsonSchemaValidationService();
    private RoboticsDataMapperForSpec specMapper;
    private RoboticsDataMapperForUnspec unspecMapper;

    @BeforeEach
    void setUp() {
        RoboticsAddressMapper addressMapper = new RoboticsAddressMapper(new AddressLinesMapper());
        RoboticsCaseDataSupport support = new RoboticsCaseDataSupport(addressMapper, new RoboticsPartyLookup());
        OrganisationService organisationService = mock(OrganisationService.class);

        // Fix unrelated event history so these tests exercise address mapping without depending on state routing.
        EventHistoryMapper eventHistoryMapper = mock(EventHistoryMapper.class);
        when(eventHistoryMapper.buildEvents(any(), anyString())).thenReturn(issuedEventHistory());
        specMapper = new RoboticsDataMapperForSpec(addressMapper, eventHistoryMapper, organisationService, support);

        LocationRefDataUtil locationRefDataUtil = mock(LocationRefDataUtil.class);
        when(locationRefDataUtil.getPreferredCourtData(any(), anyString(), anyBoolean())).thenReturn("121");
        unspecMapper = new RoboticsDataMapperForUnspec(
            addressMapper, eventHistoryMapper, organisationService, locationRefDataUtil, support
        );
    }

    @ParameterizedTest
    @CsvSource({
        "SPEC_CLAIM, 001", "SPEC_CLAIM, 002", "SPEC_CLAIM, 003", "SPEC_CLAIM, 004",
        "UNSPEC_CLAIM, 001", "UNSPEC_CLAIM, 002", "UNSPEC_CLAIM, 003", "UNSPEC_CLAIM, 004"
    })
    void shouldPreservePayloadAndNotify_whenPartyPrimaryAddressIsMissing(CaseCategory category, String partyId)
        throws JsonProcessingException {
        CaseData caseData = caseDataWithParty(category, partyId);
        String populatedPayload = mapPayload(caseData);
        assertThat(schemaValidator.validate(populatedPayload)).isEmpty();

        JsonNode expectedPayload = objectMapper.readTree(populatedPayload);
        ObjectNode expectedParty = findParty(expectedPayload, partyId);
        assertThat(expectedParty.get("addresses").isObject()).isTrue();
        expectedParty.putNull("addresses");

        party(caseData, partyId).setPrimaryAddress(null);
        String payload = mapPayload(caseData);

        assertThat(schemaValidator.validate(payload)).isEmpty();
        assertThat(objectMapper.readTree(payload)).isEqualTo(expectedPayload);

        RoboticsNotificationService notificationService = mock(RoboticsNotificationService.class);
        RoboticsNotifier notifier = new RoboticsNotifier(notificationService, schemaValidator, unspecMapper, specMapper);
        notifier.notifyRobotics(caseData, AUTH_TOKEN);

        verify(notificationService).notifyRobotics(eq(caseData), eq(isMultiPartyScenario(caseData)), eq(AUTH_TOKEN));
    }

    private CaseData caseDataWithParty(CaseCategory category, String partyId) {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued()
            .caseAccessCategory(category)
            .specRespondent1Represented(YES)
            .totalClaimAmount(BigDecimal.valueOf(1000))
            .build();
        caseData.setCcdCaseReference(1234567890123456L);
        if ("003".equals(partyId)) {
            caseData.setAddRespondent2(YES);
            caseData.setRespondent2(new PartyBuilder().individual().build());
            caseData.setRespondent2Represented(YES);
            caseData.setSpecRespondent2Represented(YES);
            caseData.setRespondent2SameLegalRepresentative(YES);
        } else if ("004".equals(partyId)) {
            caseData.setAddApplicant2(YES);
            caseData.setApplicant2(new PartyBuilder().individual().build());
        }
        return caseData;
    }

    private Party party(CaseData caseData, String partyId) {
        return switch (partyId) {
            case "001" -> caseData.getApplicant1();
            case "002" -> caseData.getRespondent1();
            case "003" -> caseData.getRespondent2();
            case "004" -> caseData.getApplicant2();
            default -> throw new IllegalArgumentException("Unexpected party: " + partyId);
        };
    }

    private ObjectNode findParty(JsonNode payload, String partyId) {
        for (JsonNode party : payload.get("litigiousParties")) {
            if (partyId.equals(party.get("ID").asText())) {
                return (ObjectNode) party;
            }
        }
        throw new AssertionError("Missing party: " + partyId);
    }

    private String mapPayload(CaseData caseData) throws JsonProcessingException {
        return SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            ? specMapper.toRoboticsCaseData(caseData, AUTH_TOKEN).toJsonString()
            : unspecMapper.toRoboticsCaseData(caseData, AUTH_TOKEN).toJsonString();
    }

    private EventHistory issuedEventHistory() {
        Event issued = new Event();
        issued.setEventCode("999");
        issued.setDateReceived(LocalDateTime.of(2026, 1, 1, 12, 0));
        issued.setEventDetailsText("Claim issued in CCD.");
        issued.setEventDetails(new EventDetails().setMiscText("Claim issued in CCD."));
        EventHistory history = new EventHistory();
        history.setMiscellaneous(List.of(issued));
        return new EventHistorySequencer().sortEvents(history);
    }
}

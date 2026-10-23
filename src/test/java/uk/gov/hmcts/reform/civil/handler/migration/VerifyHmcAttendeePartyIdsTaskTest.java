package uk.gov.hmcts.reform.civil.handler.migration;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PartyFlagStructure;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.hmc.model.hearing.Attendees;
import uk.gov.hmcts.reform.hmc.model.hearing.CaseDetailsHearing;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingRequestDetails;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingSubChannel;
import uk.gov.hmcts.reform.hmc.model.hearing.IndividualDetailsModel;
import uk.gov.hmcts.reform.hmc.model.hearing.PartyDetailsModel;
import uk.gov.hmcts.reform.hmc.model.hearings.CaseHearing;
import uk.gov.hmcts.reform.hmc.model.hearings.HearingsResponse;
import uk.gov.hmcts.reform.hmc.service.HearingsService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

class VerifyHmcAttendeePartyIdsTaskTest {

    private static final String CASE_ID = "1732030337525703";
    private static final Long HEARING_ID = 2000082695L;

    private HearingsService hearingsService;
    private VerifyHmcAttendeePartyIdsTask task;
    private Logger logger;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void setUp() {
        hearingsService = mock(HearingsService.class);
        UserService userService = mock(UserService.class);
        SystemUpdateUserConfiguration userConfig = mock(SystemUpdateUserConfiguration.class);
        task = new VerifyHmcAttendeePartyIdsTask(hearingsService, userService, userConfig);

        when(userConfig.getUserName()).thenReturn("system-user");
        when(userConfig.getPassword()).thenReturn("pass");
        when(userService.getAccessToken("system-user", "pass")).thenReturn("Bearer token");

        logger = (Logger) LoggerFactory.getLogger(VerifyHmcAttendeePartyIdsTask.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
        appender.stop();
    }

    @Test
    void shouldReportNameDiffers_whenHmcHoldsOurPartyIdAgainstAnotherName() {
        // shape A: the party ID is ours, only the name HMC returns is wrong, so a case data lookup fixes it
        givenHearing(hearing("app-1", "Charlie", "Sansom", "app-1", HearingSubChannel.INTER));

        task.migrateCaseData(caseWithClaimant("app-1", "Aslesh", "Narra"), caseReference(CASE_ID));

        assertTrue(loggedContains("verdict=ID_IN_CCD_NAME_DIFFERS"), logs());
        assertTrue(loggedContains("hmcName=\"Charlie Sansom\" ccdName=\"Aslesh Narra\""), logs());
        assertTrue(loggedContains("result=HEARING_PARTIES_ALL_KNOWN"), logs());
    }

    @Test
    void shouldReportIdNotInCcd_whenAttendeeBelongsToAnotherCase() {
        // shape B: the attendee's party ID was never sent by this case, so there is nothing to resolve
        givenHearing(hearing("other-9", "Charlie", "Sansom", "other-9", HearingSubChannel.INTER));

        task.migrateCaseData(caseWithClaimant("app-1", "Aslesh", "Narra"), caseReference(CASE_ID));

        assertTrue(loggedContains("verdict=ID_NOT_IN_CCD"), logs());
        assertTrue(loggedContains("result=HEARING_PARTIES_ALL_FOREIGN"), logs());
        assertTrue(loggedContains("idOverlap=0/1"), logs());
    }

    @Test
    void shouldReportNameMatches_whenHmcAgreesWithCaseData() {
        givenHearing(hearing("app-1", "Aslesh", "Narra", "app-1", HearingSubChannel.VIDCVP));

        task.migrateCaseData(caseWithClaimant("app-1", "Aslesh", "Narra"), caseReference(CASE_ID));

        assertTrue(loggedContains("verdict=ID_IN_CCD_NAME_MATCHES"), logs());
        assertTrue(loggedContains("channel=VIDCVP"), logs());
    }

    @Test
    void shouldFlagCaseRefMismatch_whenHmcHearingIsLinkedToAnotherCase() {
        HearingGetResponse hearing = hearing("other-9", "Charlie", "Sansom", "other-9", HearingSubChannel.INTER)
            .setCaseDetails(new CaseDetailsHearing().setCaseRef("1600000000000000")
                                .setHmctsInternalCaseName("Someone Else v Another Company"));
        givenHearing(hearing);

        task.migrateCaseData(caseWithClaimant("app-1", "Aslesh", "Narra"), caseReference(CASE_ID));

        assertTrue(loggedContains("caseRefMatches=false"), logs());
        assertTrue(loggedContains("hmcInternalCaseName=\"Someone Else v Another Company\""), logs());
    }

    @Test
    void shouldReportNoAttendees_whenTheHearingDayHasNone() {
        HearingGetResponse hearing = hearing("app-1", "Aslesh", "Narra", null, null);
        givenHearing(hearing);

        task.migrateCaseData(caseWithClaimant("app-1", "Aslesh", "Narra"), caseReference(CASE_ID));

        assertTrue(loggedContains("result=NO_ATTENDEES"), logs());
    }

    @Test
    void shouldReportNoHearings_whenHmcHasNoneForTheCase() {
        when(hearingsService.getHearings(anyString(), anyLong(), isNull()))
            .thenReturn(new HearingsResponse().setCaseHearings(List.of()));

        task.migrateCaseData(caseWithClaimant("app-1", "Aslesh", "Narra"), caseReference(CASE_ID));

        assertTrue(loggedContains("result=NO_HEARINGS"), logs());
    }

    @Test
    void shouldLogErrorAndContinue_whenHmcCallFails() {
        when(hearingsService.getHearings(anyString(), anyLong(), isNull()))
            .thenThrow(new RuntimeException("hmc unavailable"));

        CaseData caseData = caseWithClaimant("app-1", "Aslesh", "Narra");
        CaseData result = task.migrateCaseData(caseData, caseReference(CASE_ID));

        assertEquals(caseData, result, "task must return the case unchanged");
        assertTrue(loggedContains("stage=listHearings"), logs());
    }

    @Test
    void shouldCollectEveryPartyIdCcdWouldSendToHmc() {
        CaseData caseData = caseWithSolicitorOrganisation("ORG123");
        caseData.setApplicant1LitigationFriend(new LitigationFriend().setPartyID("lf-1")
                                                   .setFirstName("Litigation").setLastName("Friend"));
        caseData.setApplicantExperts(wrapElements(new PartyFlagStructure().setPartyID("exp-1")
                                                      .setFirstName("Expert").setLastName("One")));
        caseData.setRespondent1Witnesses(wrapElements(new PartyFlagStructure().setPartyID("wit-1")
                                                          .setFirstName("Witness").setLastName("One")));
        caseData.setApplicant1LRIndividuals(wrapElements(new PartyFlagStructure().setPartyID("lr-1")
                                                             .setFirstName("Legal").setLastName("Rep")));
        caseData.setRespondent1OrgIndividuals(wrapElements(new PartyFlagStructure().setPartyID("org-ind-1")
                                                               .setFirstName("Org").setLastName("Individual")));

        Map<String, String> parties = task.ccdPartyIdsToNames(caseData);

        assertEquals("Aslesh Narra", parties.get("app-1"));
        assertEquals("Litigation Friend", parties.get("lf-1"));
        assertEquals("Expert One", parties.get("exp-1"));
        assertEquals("Witness One", parties.get("wit-1"));
        assertEquals("Legal Rep", parties.get("lr-1"));
        assertEquals("Org Individual", parties.get("org-ind-1"));
        assertTrue(parties.containsKey("ORG123"), "solicitor organisation ID is the party ID HMC receives");
    }

    @Test
    void shouldUseSoleTraderNames_whenPartyIsASoleTrader() {
        Party soleTrader = new Party().setType(Party.Type.SOLE_TRADER).setPartyID("res-1")
            .setSoleTraderFirstName("William").setSoleTraderLastName("Beckett")
            .setSoleTraderTradingAs("Beckett Joinery");

        CaseData caseData = caseWithClaimant("app-1", "Aslesh", "Narra");
        caseData.setRespondent1(soleTrader);

        Map<String, String> parties = task.ccdPartyIdsToNames(caseData);

        assertEquals("William Beckett", parties.get("res-1"));
    }

    @Test
    void shouldBeReadOnlyAndRejectMissingInput() {
        assertTrue(task.isReadOnly(), "diagnostic must not write a CCD event");
        assertThrows(IllegalArgumentException.class, () -> task.migrateCaseData(null, caseReference(CASE_ID)));
        assertThrows(IllegalArgumentException.class,
                     () -> task.migrateCaseData(caseWithClaimant("app-1", "A", "B"), null));
    }

    private void givenHearing(HearingGetResponse hearing) {
        when(hearingsService.getHearings(anyString(), anyLong(), isNull()))
            .thenReturn(new HearingsResponse().setCaseHearings(List.of(new CaseHearing().setHearingId(HEARING_ID))));
        when(hearingsService.getHearingResponse(anyString(), eq(String.valueOf(HEARING_ID)))).thenReturn(hearing);
    }

    private HearingGetResponse hearing(String hmcPartyId, String first, String last,
                                       String attendeePartyId, HearingSubChannel channel) {
        PartyDetailsModel party = new PartyDetailsModel();
        party.setPartyID(hmcPartyId);
        IndividualDetailsModel individual = new IndividualDetailsModel();
        individual.setFirstName(first);
        individual.setLastName(last);
        party.setIndividualDetails(individual);

        HearingDaySchedule day = new HearingDaySchedule()
            .setHearingStartDateTime(LocalDateTime.of(2026, 3, 2, 10, 0));
        if (attendeePartyId != null) {
            day.setAttendees(List.of(new Attendees().setPartyID(attendeePartyId).setHearingSubChannel(channel)));
        }

        return new HearingGetResponse()
            .setRequestDetails(new HearingRequestDetails().setVersionNumber(3L))
            .setCaseDetails(new CaseDetailsHearing().setCaseRef(CASE_ID))
            .setPartyDetails(List.of(party))
            .setHearingResponse(new HearingResponse()
                                    .setReceivedDateTime(LocalDateTime.of(2026, 2, 1, 9, 0))
                                    .setHearingDaySchedule(List.of(day)));
    }

    private CaseData caseWithClaimant(String partyId, String first, String last) {
        return CaseData.builder()
            .applicant1(new Party().setType(Party.Type.INDIVIDUAL).setPartyID(partyId)
                            .setIndividualFirstName(first).setIndividualLastName(last))
            .respondent1(new Party().setType(Party.Type.ORGANISATION).setPartyID("res-1")
                             .setOrganisationName("IQUW Syndicate Management Limited"))
            .build();
    }

    private CaseData caseWithSolicitorOrganisation(String organisationId) {
        OrganisationPolicy policy = new OrganisationPolicy();
        policy.setOrganisation(new Organisation().setOrganisationID(organisationId));
        return CaseData.builder()
            .applicant1(new Party().setType(Party.Type.INDIVIDUAL).setPartyID("app-1")
                            .setIndividualFirstName("Aslesh").setIndividualLastName("Narra"))
            .applicant1OrganisationPolicy(policy)
            .build();
    }

    private CaseReference caseReference(String caseId) {
        CaseReference caseReference = new CaseReference();
        caseReference.setCaseReference(caseId);
        return caseReference;
    }

    private boolean loggedContains(String fragment) {
        return appender.list.stream().anyMatch(event -> event.getFormattedMessage().contains(fragment));
    }

    private String logs() {
        return appender.list.stream().map(ILoggingEvent::getFormattedMessage).toList().toString();
    }
}

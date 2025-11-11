package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.dj.DefaultJudgmentSDOOrderForm;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DjTrialTemplateServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private DocumentHearingLocationHelper locationHelper;

    private DjTrialTemplateService service;
    private DjTemplateFieldService templateFieldService;
    private DjPartyFieldService partyFieldService;
    private DjHearingMethodFieldService hearingMethodFieldService;
    private DjTrialTemplateFieldService trialTemplateFieldService;

    @BeforeEach
    void setUp() {
        templateFieldService = new DjTemplateFieldService();
        partyFieldService = new DjPartyFieldService();
        hearingMethodFieldService = new DjHearingMethodFieldService();
        trialTemplateFieldService = new DjTrialTemplateFieldService();
        service = new DjTrialTemplateService(
            userService,
            locationHelper,
            templateFieldService,
            partyFieldService,
            hearingMethodFieldService,
            trialTemplateFieldService
        );

        when(userService.getUserDetails(any())).thenReturn(UserDetails.builder()
            .forename("Judge")
            .surname("Dredd")
            .roles(Collections.singletonList("judge"))
            .build());
    }

    @Test
    void shouldPopulateTrialTemplate() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .atStateClaimIssuedTrialHearing()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .atStateClaimIssuedTrialSDOInPersonHearing()
            .atStateClaimIssuedTrialLocationInPerson()
            .atStateSdoTrialDj()
            .build();

        LocationRefData location = LocationRefData.builder()
            .epimmsId("321")
            .siteName("Court B")
            .build();
        when(locationHelper.getHearingLocation(any(), eq(caseData), any())).thenReturn(location);

        DefaultJudgmentSDOOrderForm result = service.buildTemplate(caseData, "token");

        assertThat(result.getCaseNumber()).isEqualTo(caseData.getLegacyCaseReference());
        assertThat(result.getHearingLocation()).isEqualTo(location);
        assertThat(result.isHasTrialHearingWelshSectionDJ()).isFalse();
    }
}

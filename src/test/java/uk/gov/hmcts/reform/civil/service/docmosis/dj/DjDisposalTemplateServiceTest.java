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
class DjDisposalTemplateServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private DocumentHearingLocationHelper locationHelper;

    private DjDisposalTemplateService service;

    private DjAuthorisationFieldService authorisationFieldService;
    private DjBundleFieldService bundleFieldService;
    private DjDirectionsToggleService directionsToggleService;
    private DjPartyFieldService partyFieldService;
    private DjHearingMethodFieldService hearingMethodFieldService;
    private DjDisposalTemplateFieldService disposalTemplateFieldService;

    @BeforeEach
    void setUp() {
        authorisationFieldService = new DjAuthorisationFieldService();
        bundleFieldService = new DjBundleFieldService();
        directionsToggleService = new DjDirectionsToggleService();
        partyFieldService = new DjPartyFieldService();
        hearingMethodFieldService = new DjHearingMethodFieldService();
        disposalTemplateFieldService = new DjDisposalTemplateFieldService();
        service = new DjDisposalTemplateService(
            userService,
            locationHelper,
            authorisationFieldService,
            bundleFieldService,
            directionsToggleService,
            partyFieldService,
            hearingMethodFieldService,
            disposalTemplateFieldService
        );

        when(userService.getUserDetails(any())).thenReturn(UserDetails.builder()
            .forename("Judge")
            .surname("Dredd")
            .roles(Collections.singletonList("judge"))
            .build());
    }

    @Test
    void shouldPopulateDisposalTemplate() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .atStateClaimIssuedDisposalHearing()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .atStateClaimIssuedDisposalSDOVideoCall()
            .atStateClaimIssuedDisposalHearingInPersonDJ()
            .build();

        LocationRefData location = LocationRefData.builder()
            .epimmsId("123")
            .siteName("Court A")
            .build();

        when(locationHelper.getHearingLocation(any(), eq(caseData), any())).thenReturn(location);

        DefaultJudgmentSDOOrderForm result = service.buildTemplate(caseData, "token");

        assertThat(result.getCaseNumber()).isEqualTo(caseData.getLegacyCaseReference());
        assertThat(result.getHearingLocation()).isEqualTo(location);
        assertThat(result.isHasDisposalHearingWelshSectionDJ()).isFalse();
    }
}

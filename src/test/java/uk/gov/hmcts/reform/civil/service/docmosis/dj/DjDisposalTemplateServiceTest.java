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

    @BeforeEach
    void setUp() {
        DjAuthorisationFieldService authorisationFieldService = new DjAuthorisationFieldService();
        DjBundleFieldService bundleFieldService = new DjBundleFieldService();
        DjDirectionsToggleService directionsToggleService = new DjDirectionsToggleService();
        DjPartyFieldService partyFieldService = new DjPartyFieldService();
        DjHearingMethodFieldService hearingMethodFieldService = new DjHearingMethodFieldService();
        DjDisposalTemplateFieldService disposalTemplateFieldService = new DjDisposalTemplateFieldService();
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

        LocationRefData location = new LocationRefData();
        location.setEpimmsId("123");
        location.setSiteName("Court A");

        when(locationHelper.getHearingLocation(any(), eq(caseData), any())).thenReturn(location);

        DefaultJudgmentSDOOrderForm result = service.buildTemplate(caseData, "token");

        assertThat(result.getCaseNumber()).isEqualTo(caseData.getLegacyCaseReference());
        assertThat(result.getHearingLocation()).isEqualTo(location);
        assertThat(result.isHasDisposalHearingWelshSectionDJ()).isFalse();
    }
}

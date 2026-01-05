package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinetests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.RespondToClaimSpecUtils;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinespec.Respondent2CaseDataUpdater;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Respondent2CaseDataUpdaterTest {

    @Mock
    private Time time;

    @InjectMocks
    private Respondent2CaseDataUpdater updater;

    @Mock
    private RespondToClaimSpecUtils respondToClaimSpecUtils;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseDataBuilder.builder().build();
        Party individual = new Party();
        individual.setType(Party.Type.INDIVIDUAL);
        individual.setPartyName("RESPONDENT_INDIVIDUAL");
        caseData.setRespondent2(individual);
        Party party = new Party();
        party.setPartyName("Party 2");
        Address address = new Address();
        address.setAddressLine1("Triple street");
        address.setPostCode("Postcode");
        party.setPrimaryAddress(address);
        caseData.setRespondent2Copy(party);
        caseData.setRespondentResponseIsSame(YesOrNo.YES);
    }

    @Test
    void shouldUpdateCaseDataWhenRespondent2HasSameLegalRep() {
        updater.update(caseData);

        assertThat(caseData.getRespondent2()).isNotNull();
        assertThat(caseData.getRespondent2().getPrimaryAddress()).isNotNull();
        assertThat(caseData.getRespondent2().getPrimaryAddress().getAddressLine1()).isEqualTo("Triple street");
        assertThat(caseData.getRespondent2().getPrimaryAddress().getPostCode()).isEqualTo("Postcode");
        assertThat(caseData.getRespondent2Copy()).isNull();
    }

    @Test
    void shouldNotUpdateCaseDataWhenRespondent2HasDifferentLegalRep() {
        caseData.setRespondentResponseIsSame(YesOrNo.NO);

        updater.update(caseData);

        assertThat(caseData.getRespondent2()).isNotNull();
        assertThat(caseData.getRespondent2().getPrimaryAddress()).isNotNull();
        assertThat(caseData.getRespondent2().getPrimaryAddress().getAddressLine1()).isEqualTo("Triple street");
        assertThat(caseData.getRespondent2().getPrimaryAddress().getPostCode()).isEqualTo("Postcode");
    }

    @Test
    void shouldUpdateRespondent2ClaimResponseTypeAndResponseDateWhenConditionsAreMet() {
        LocalDateTime responseDate = LocalDateTime.now();
        caseData.setRespondentResponseIsSame(YesOrNo.YES);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);

        when(time.now()).thenReturn(responseDate);
        when(respondToClaimSpecUtils.isRespondent2HasSameLegalRep(caseData)).thenReturn(true);

        updater.update(caseData);

        assertThat(caseData.getRespondent2ClaimResponseTypeForSpec()).isEqualTo(RespondentResponseTypeSpec.FULL_DEFENCE);
        assertThat(caseData.getRespondent2ResponseDate()).isEqualTo(responseDate);
    }
}

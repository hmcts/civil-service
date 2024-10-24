package uk.gov.hmcts.reform.civil.service.mediation.helpers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.Party.Type;
import uk.gov.hmcts.reform.civil.model.caseflags.PartyFlags;
import uk.gov.hmcts.reform.civil.service.mediation.MediationLitigant;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class PartyDetailsPopulatorTest {

    @InjectMocks
    private PartyDetailsPopulator partyDetailsPopulator;

    @Test
    void shouldPopulateLitigantWithCorrectInfo_whenAllFieldsProvided() {
        PartyFlags flags = PartyFlags.builder().roleOnCase("Defendant").build();
        Party party = Party.builder()
            .partyID("P1")
            .type(Type.INDIVIDUAL)
            .individualFirstName("John")
            .individualLastName("Doe")
            .flags(flags)
            .build();

        MediationLitigant.MediationLitigantBuilder builder = MediationLitigant.builder();
        MediationLitigant litigant = partyDetailsPopulator.populator(builder, party).build();

        assertThat(litigant.getPartyID()).isEqualTo("P1");
        assertThat(litigant.getPartyRole()).isEqualTo("Defendant");
        assertThat(litigant.getPartyType()).isEqualTo(Type.INDIVIDUAL);
        assertThat(litigant.getPartyName()).isEqualTo("John Doe");
        assertThat(litigant.getPaperResponse()).isEqualTo("N");
    }

    @Test
    void shouldHandleNullFlags_whenFlagsAreNotProvided() {
        Party party = Party.builder()
            .partyID("P2")
            .type(Type.COMPANY)
            .individualFirstName("John")
            .individualLastName("Doe")
            .flags(null)
            .build();

        MediationLitigant.MediationLitigantBuilder builder = MediationLitigant.builder();
        MediationLitigant litigant = partyDetailsPopulator.populator(builder, party).build();

        assertThat(litigant.getPartyID()).isEqualTo("P2");
        assertThat(litigant.getPartyRole()).isNull();
        assertThat(litigant.getPartyType()).isEqualTo(Type.COMPANY);
        assertThat(litigant.getPaperResponse()).isEqualTo("N");
    }
}

package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinetests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.RespondToClaimSpecUtils;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinespec.Respondent2CaseDataUpdater;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class Respondent2CaseDataUpdaterTest {

    @InjectMocks
    private Respondent2CaseDataUpdater updater;

    @Mock
    private RespondToClaimSpecUtils respondToClaimSpecUtils;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder()
                .respondent2(Party.builder().type(Party.Type.INDIVIDUAL).partyName("RESPONDENT_INDIVIDUAL").build())
                .respondent2Copy(Party.builder().partyName("Party 2").primaryAddress(Address.builder()
                                .addressLine1("Triple street")
                                .postCode("Postcode")
                                .build())
                        .build())
                .respondentResponseIsSame(YesOrNo.YES)
                .build();
    }

    @Test
    void shouldUpdateCaseDataWhenRespondent2HasSameLegalRep() {
        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();
        updater.update(caseData, updatedData);

        CaseData updatedCaseData = updatedData.build();
        assertThat(updatedCaseData.getRespondent2().getPrimaryAddress().getAddressLine1()).isEqualTo("Triple street");
        assertThat(updatedCaseData.getRespondent2().getPrimaryAddress().getPostCode()).isEqualTo("Postcode");
        assertThat(updatedCaseData.getRespondent2Copy()).isNull();
    }

    @Test
    void shouldNotUpdateCaseDataWhenRespondent2HasDifferentLegalRep() {
        caseData = caseData.toBuilder().respondentResponseIsSame(YesOrNo.NO).build();

        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();
        updater.update(caseData, updatedData);

        CaseData updatedCaseData = updatedData.build();
        assertThat(updatedCaseData.getRespondent2().getPrimaryAddress().getAddressLine1()).isEqualTo("Triple street");
        assertThat(updatedCaseData.getRespondent2().getPrimaryAddress().getPostCode()).isEqualTo("Postcode");
    }
}
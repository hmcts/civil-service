package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.fasttracktests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fasttrack.FastTrackClinicalNegligenceFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackClinicalNegligence;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class FastTrackClinicalNegligenceFieldBuilderTest {

    @InjectMocks
    private FastTrackClinicalNegligenceFieldBuilder fastTrackClinicalNegligenceFieldBuilder;

    @Test
    void shouldBuildFastTrackClinicalNegligenceFields() {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        fastTrackClinicalNegligenceFieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        FastTrackClinicalNegligence clinicalNegligence = caseData.getFastTrackClinicalNegligence();
        assertThat(clinicalNegligence).isNotNull();
        assertThat(clinicalNegligence.getInput1()).isEqualTo("Documents should be retained as follows:");
        assertThat(clinicalNegligence.getInput2()).isEqualTo("a) The parties must retain all electronically stored documents relating to the issues in this claim.");
        assertThat(clinicalNegligence.getInput3()).isEqualTo("b) the defendant must retain the original clinical notes relating to the issues in this claim. The defendant must " +
                "give facilities for inspection by the claimant, the claimant's legal advisers and experts of these original notes on 7 days written notice.");
        assertThat(clinicalNegligence.getInput4()).isEqualTo("c) Legible copies of the medical and educational records of the claimant are to be placed in a separate paginated " +
                "bundle by the claimant's solicitors and kept up to date. All references to medical notes are to be made by reference to the pages in that bundle.");
    }
}
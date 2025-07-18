package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fasttrack;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SdoCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackClinicalNegligence;

@Slf4j
@Component
public class FastTrackClinicalNegligenceFieldBuilder implements SdoCaseFieldBuilder {

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.fastTrackClinicalNegligence(FastTrackClinicalNegligence.builder()
                .input1("Documents should be retained as follows:")
                .input2(
                        "a) The parties must retain all electronically stored documents relating to the issues in this claim.")
                .input3(
                        "b) the defendant must retain the original clinical notes relating to the issues in this claim." +
                                " The defendant must give facilities for inspection by the claimant," +
                                " the claimant's legal advisers and experts of these original notes on 7 days written notice.")
                .input4(
                        "c) Legible copies of the medical and educational records of the claimant are to be placed in a" +
                                " separate paginated bundle by the claimant's solicitors and kept up to date." +
                                " All references to medical notes are to be made by reference to the pages in that bundle.")
                .build());
    }
}

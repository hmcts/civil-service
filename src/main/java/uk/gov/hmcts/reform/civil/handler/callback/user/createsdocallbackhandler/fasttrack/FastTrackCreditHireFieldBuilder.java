package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fasttrack;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.enums.sdo.AddOrRemoveToggle;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SdoCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackCreditHireDetails;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FastTrackCreditHireFieldBuilder implements SdoCaseFieldBuilder {

    private final FeatureToggleService featureToggleService;
    private final WorkingDayIndicator workingDayIndicator;
    static final String WITNESS_STATEMENT_STRING = "This witness statement is limited to 10 pages per party, including any appendices.";
    static final String LATER_THAN_FOUR_PM_STRING = "later than 4pm on";
    static final String CLAIMANT_EVIDENCE_STRING = "and the claimant's evidence in reply if so advised to be uploaded by 4pm on";

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (featureToggleService.isSdoR2Enabled()) {
            updatedData.sdoR2FastTrackCreditHire(getSdoR2FastTrackCreditHire());
        }
        updatedData.fastTrackCreditHire(getFastTrackCreditHire());
    }

    private FastTrackCreditHire getFastTrackCreditHire() {
        log.debug("Building FastTrackCreditHire");
        String partiesLiaseString = "The parties are to liaise and use reasonable endeavours to agree the basic hire rate no ";
        return FastTrackCreditHire.builder()
                .input1("""
                        If impecuniosity is alleged by the claimant and not admitted by the defendant, the claimant's disclosure as ordered earlier in this Order must include:
                        a) Evidence of all income from all sources for a period of 3 months prior to the commencement of hire until the earlier of:
                              i) 3 months after cessation of hire
                             ii) the repair or replacement of the claimant's vehicle
                        b) Copies of all bank, credit card, and saving account statements for a period of 3 months prior to the commencement of hire until the earlier of:
                             i) 3 months after cessation of hire
                             ii) the repair or replacement of the claimant's vehicle
                        c) Evidence of any loan, overdraft or other credit facilities available to the claimant.""")
                .input2("""
                        The claimant must upload to the Digital Portal a witness statement addressing
                        a) the need to hire a replacement vehicle; and
                        b) impecuniosity""")
                .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(4)))
                .input3(
                        "A failure to comply with the paragraph above will result in the claimant being debarred from asserting need or relying on impecuniosity" +
                                " as the case may be at the final hearing, save with permission of the Trial Judge.")
                .input4(partiesLiaseString + LATER_THAN_FOUR_PM_STRING)
                .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(6)))
                .input5(
                        "If the parties fail to agree rates subject to liability and/or other issues pursuant to the paragraph above," +
                                " each party may rely upon written evidence by way of witness statement of one witness to provide evidence of" +
                                " basic hire rates available within the claimant's geographical location," +
                                " from a mainstream supplier, or a local reputable supplier if none is available.")
                .input6("The defendant's evidence is to be uploaded to the Digital Portal by 4pm on")
                .date3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)))
                .input7(CLAIMANT_EVIDENCE_STRING)
                .date4(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)))
                .input8(WITNESS_STATEMENT_STRING)
                .build();
    }

    private SdoR2FastTrackCreditHire getSdoR2FastTrackCreditHire() {
        log.debug("Building SdoR2FastTrackCreditHire");
        List<AddOrRemoveToggle> addOrRemoveToggleList = List.of(AddOrRemoveToggle.ADD);
        return SdoR2FastTrackCreditHire.builder()
                .input1("""
                        If impecuniosity is alleged by the claimant and not admitted by the defendant, the claimant's \
                        disclosure as ordered earlier in this Order must include:
                        a) Evidence of all income from all sources for a period of 3 months prior to the \
                        commencement of hire until the earlier of:
                        
                             i) 3 months after cessation of hire
                             ii) the repair or replacement of the claimant's vehicle
                        b) Copies of all bank, credit card, and saving account statements for a period of 3 months \
                        prior to the commencement of hire until the earlier of:
                             i) 3 months after cessation of hire
                             ii) the repair or replacement of the claimant's vehicle
                        c) Evidence of any loan, overdraft or other credit facilities available to the claimant.""")
                .input5(
                        "If the parties fail to agree rates subject to liability and/or other issues pursuant to the paragraph above," +
                                " each party may rely upon written evidence by way of witness statement of" +
                                " one witness to provide evidence of basic hire rates available within the claimant's geographical location," +
                                " from a mainstream supplier, or a local reputable supplier if none is available.")
                .input6("The defendant's evidence is to be uploaded to the Digital Portal by 4pm on")
                .date3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)))
                .input7(CLAIMANT_EVIDENCE_STRING)
                .date4(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(10)))
                .input8(WITNESS_STATEMENT_STRING)
                .detailsShowToggle(addOrRemoveToggleList)
                .sdoR2FastTrackCreditHireDetails(SdoR2FastTrackCreditHireDetails.builder()
                        .input2("""
                                The claimant must upload to the Digital Portal a witness statement addressing
                                a) the need to hire a replacement vehicle; and
                                b) impecuniosity""")
                        .date1(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(
                                4)))
                        .input3(
                                "A failure to comply with the paragraph above will result in the claimant being debarred" +
                                        " from asserting need or relying on impecuniosity as the case may be at the final hearing," +
                                        " save with permission of the Trial Judge.")
                        .input4(
                                "The parties are to liaise and use reasonable endeavours to agree the basic hire rate no later than 4pm on")
                        .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(
                                6)))
                        .build())
                .build();
    }
}

package uk.gov.hmcts.reform.civil.notification.handlers.djnondivergent;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class DjNonDivergentAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public DjNonDivergentAllPartiesEmailGenerator(
        DjNonDivergentApplicantLREmailDTOGenerator applicantLRGenerator,
        DjNonDivergentApplicantLipEmailDTOGenerator applicantLipGenerator,
        DjNonDivergentDefendant1LREmailDTOGenerator defendant1LRGenerator,
        DjNonDivergentDefendant1LipEmailDTOGenerator defendant1LipGenerator,
        DjNonDivergentDefendant2LREmailDTOGenerator defendant2LRGenerator) {

        super(List.of(
            applicantLRGenerator,
            applicantLipGenerator,
            defendant1LRGenerator,
            defendant1LipGenerator,
            defendant2LRGenerator
        ));
    }
}

package uk.gov.hmcts.reform.civil.service.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialPPI;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DjPpiDirectionsService {

    public TrialPPI buildTrialPPI() {
        return new TrialPPI()
            .setPpiDate(LocalDate.now().plusDays(28))
            .setText(SdoR2UiConstantSmallClaim.PPI_DESCRIPTION);
    }
}


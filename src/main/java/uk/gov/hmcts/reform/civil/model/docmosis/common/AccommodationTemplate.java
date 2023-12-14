package uk.gov.hmcts.reform.civil.model.docmosis.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.HomeTypeOptionLRspec;
import uk.gov.hmcts.reform.civil.model.dq.HomeDetails;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.HomeTypeOptionLRspec.ASSOCIATION_HOME;
import static uk.gov.hmcts.reform.civil.enums.HomeTypeOptionLRspec.JOINTLY_OWNED_HOME;
import static uk.gov.hmcts.reform.civil.enums.HomeTypeOptionLRspec.OTHER;
import static uk.gov.hmcts.reform.civil.enums.HomeTypeOptionLRspec.OWNED_HOME;
import static uk.gov.hmcts.reform.civil.enums.HomeTypeOptionLRspec.PRIVATE_RENTAL;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class AccommodationTemplate {

    private static final Map<HomeTypeOptionLRspec, String> WHERE_DO_THEY_LIVE_MAP = Map.of(OWNED_HOME,
                                                                                           "Owned outright",
                                                                                           PRIVATE_RENTAL,
                                                                                           "Rent",
                                                                                           JOINTLY_OWNED_HOME,
                                                                                           "Jointly owned home",
                                                                                           ASSOCIATION_HOME,
                                                                                           "Council or housing"
    );
    private HomeDetails homeDetails;

    public String getDisplayValue() {
        return Optional.ofNullable(homeDetails)
            .map(home -> getHomeTypeAsString()).orElse("");
    }

    private String getHomeTypeAsString() {
        if (OTHER == homeDetails.type()) {
            return homeDetails.typeOtherDetails();
        }
        return WHERE_DO_THEY_LIVE_MAP.get(homeDetails.type());
    }
}

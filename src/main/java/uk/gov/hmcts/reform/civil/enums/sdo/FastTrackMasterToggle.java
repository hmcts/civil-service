package uk.gov.hmcts.reform.civil.enums.sdo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FastTrackMasterToggle {
    FastTrackDisclosureOfDocumentsToggle fastTrackDisclosureOfDocumentsToggle;
    FastTrackAnotherFieldSetToggle fastTrackAnotherFieldSetToggle;
}

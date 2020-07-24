package uk.gov.hmcts.reform.unspec.service;

import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static java.util.Map.of;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseSearchService {

    private final CoreCaseDataService coreCaseDataService;

    public List<CaseDetails> getCasesToBeStayed() {
        return coreCaseDataService.searchCases(dateQuery()).getCases();
    }

    private String dateQuery() {
        return new JSONObject(
            of("query",
                of("range",
                    of("data.claimIssuedDate",
                        of("lt", "now-112d")
                    )
                ),
                "_source", List.of("reference")
            )).toString();
    }
}

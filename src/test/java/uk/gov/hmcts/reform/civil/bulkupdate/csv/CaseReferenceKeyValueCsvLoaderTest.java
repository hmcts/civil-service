package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class CaseReferenceKeyValueCsvLoaderTest {

    CaseReferenceKeyValueCsvLoader caseReferenceCsvLoader = new CaseReferenceKeyValueCsvLoader();

    @Test
    public void shouldLoadCaseRefsCsvFile() {
        List<CaseReferenceKeyValue> caseReferences = caseReferenceCsvLoader.loadCaseReferenceList("caserefskeyvalue-test.csv");

        assertThat(caseReferences.isEmpty(), equalTo(Boolean.FALSE));
        assertThat(caseReferences.size(), equalTo(4));
    }

    @Test
    public void shouldReturnEmptyCollectionWhenFileNotFound() {
        List<CaseReferenceKeyValue> caseReferences = caseReferenceCsvLoader.loadCaseReferenceList("caserefs-test-file-does-not-exist.csv");

        assertThat(caseReferences.isEmpty(), equalTo(Boolean.TRUE));
    }
}

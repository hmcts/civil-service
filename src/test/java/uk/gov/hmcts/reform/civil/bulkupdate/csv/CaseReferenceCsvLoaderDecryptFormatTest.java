package uk.gov.hmcts.reform.civil.bulkupdate.csv;

import org.junit.jupiter.api.Test;

import java.util.List;
import javax.crypto.SecretKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CaseReferenceCsvLoaderDecryptFormatTest {

    private static final String SECRET = "mySecretKey";
    private static final String ORIGINAL = "This is a test string";

    private final CaseReferenceCsvLoader loader = new CaseReferenceCsvLoader();

    @Test
    void shouldThrowWhenEncryptedDataHasNoSeparator() throws Exception {
        SecretKey key = CaseReferenceCsvLoader.getKeyFromString(SECRET);

        assertThatThrownBy(() -> CaseReferenceCsvLoader.decrypt("noSeparatorHere", key))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid encrypted data format, expected <iv>:<cipherText>");
    }

    @Test
    void shouldThrowWhenCipherTextIsMissingAfterSeparator() throws Exception {
        SecretKey key = CaseReferenceCsvLoader.getKeyFromString(SECRET);
        String ivOnly = CaseReferenceCsvLoader.encrypt(ORIGINAL, key).split(":")[0] + ":";

        assertThatThrownBy(() -> CaseReferenceCsvLoader.decrypt(ivOnly, key))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldStillDecryptWhenThereAreMoreThanTwoParts() throws Exception {
        SecretKey key = CaseReferenceCsvLoader.getKeyFromString(SECRET);
        String encryptedWithExtraPart = CaseReferenceCsvLoader.encrypt(ORIGINAL, key) + ":ignored";

        assertThat(CaseReferenceCsvLoader.decrypt(encryptedWithExtraPart, key)).isEqualTo(ORIGINAL);
    }

    @Test
    void shouldStillDecryptValidData() throws Exception {
        SecretKey key = CaseReferenceCsvLoader.getKeyFromString(SECRET);

        assertThat(CaseReferenceCsvLoader.decrypt(CaseReferenceCsvLoader.encrypt(ORIGINAL, key), key))
            .isEqualTo(ORIGINAL);
    }

    @Test
    void shouldReturnEmptyListWhenEncryptedFileIsMalformed() {
        List<CaseReference> result = loader.loadCaseReferenceList(
            CaseReference.class, "caserefs-test-encrypted-malformed.csv", "DUMMY_SECRET");

        assertThat(result).isEmpty();
    }
}

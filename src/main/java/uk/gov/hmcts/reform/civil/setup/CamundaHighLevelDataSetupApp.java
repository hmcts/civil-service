package uk.gov.hmcts.reform.civil.setup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public final class CamundaHighLevelDataSetupApp {

    private static final Logger logger = LoggerFactory.getLogger(CamundaHighLevelDataSetupApp.class);

    private CamundaHighLevelDataSetupApp() {
    }

    public static void main(String[] args) throws Exception {
        configureRelaxedSsl();

        CamundaImportConfiguration configuration = CamundaImportConfiguration.fromEnvironment(
            System.getenv(),
            Path.of(".").toAbsolutePath().normalize()
        );

        logger.info(
            "Running Camunda high level data setup for {} against {}",
            args.length > 0 ? args[0] : "unspecified",
            configuration.getCamundaBaseUrl()
        );

        new CamundaDefinitionImporter(configuration).importDefinitions();
    }

    private static void configureRelaxedSsl() throws GeneralSecurityException {
        TrustManager[] trustAllManagers = new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }};

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllManagers, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

        HostnameVerifier allowAnyHostname = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allowAnyHostname);
    }
}

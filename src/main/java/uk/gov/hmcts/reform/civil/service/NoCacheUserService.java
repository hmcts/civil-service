package uk.gov.hmcts.reform.civil.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamClient;

@Service
@Slf4j
public class NoCacheUserService extends UserService {

    private final IdamClient idamClient;

    public NoCacheUserService(IdamClient idamClient) {
        super(idamClient, false);
        this.idamClient = idamClient;
    }

    /**
     * Retrieves an access token for the given username and password.
     * This method is not cached. Purpose for this method to use in bundle creation only.
     *
     * @param username The username of the user.
     * @param password The password of the user.
     * @return The access token.
     */
    @Override
    public String getAccessToken(String username, String password) {
        return this.idamClient.getAccessToken(username, password);
    }
}

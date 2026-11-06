package uk.gov.hmcts.reform.civil.config;

import org.camunda.bpm.client.ExternalTaskClient;

import java.util.List;

/**
 * The case driven external task clients, held in a type of their own rather than exposed as a
 * {@code List<ExternalTaskClient>} bean.
 *
 * <p>The distinct type is the point. A bean whose type is {@code List<ExternalTaskClient>} competes
 * with Spring's own collection injection, which gathers every bean of the element type: an injection
 * point asking for {@code List<ExternalTaskClient>} could be handed the router and the scheduler
 * client instead of these, wiring the router into itself. A wrapper cannot be resolved that way.
 *
 * <p>It also keeps these clients out of {@code getBeanNamesForType(ExternalTaskClient.class)}, so
 * the 39 listeners that inject a bare {@code ExternalTaskClient} still resolve the router
 * unambiguously however many case driven clients are configured.
 *
 * @param clients one client, and therefore one subscription thread, per configured slot
 */
public record CaseDrivenExternalTaskClients(List<ExternalTaskClient> clients) {

    public CaseDrivenExternalTaskClients(List<ExternalTaskClient> clients) {
        if (clients.isEmpty()) {
            throw new IllegalArgumentException("At least one case driven external task client is required");
        }
        this.clients = List.copyOf(clients);
    }

    public int size() {
        return clients.size();
    }
}

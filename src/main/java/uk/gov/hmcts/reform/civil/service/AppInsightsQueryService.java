package uk.gov.hmcts.reform.civil.service;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.monitor.query.LogsQueryClient;
import com.azure.monitor.query.LogsQueryClientBuilder;
import com.azure.monitor.query.models.LogsQueryResult;
import com.azure.monitor.query.models.LogsTable;
import com.azure.monitor.query.models.LogsTableCell;
import com.azure.monitor.query.models.LogsTableRow;
import com.azure.monitor.query.models.QueryTimeInterval;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AppInsightsQueryService {

    private static final String QUERY = """
        customEvents
        | where customDimensions.eventType == '%s'
        | where customDimensions.caseId == '%s'
        | project timestamp, customDimensions.eventName, customDimensions.caseId, customDimensions.nextSteps
        """;

    private final LogsQueryClient logsQueryClient;
    private final String workspaceId;

    public AppInsightsQueryService(@Value("%{azure.appinsights.workspaceId}") String workspaceId) {
        this.workspaceId = workspaceId;

        TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        this.logsQueryClient = new LogsQueryClientBuilder()
            .credential(credential)
            .buildClient();
    }

    public List<Map<String, Object>> queryCustomEvents(String eventType, String caseId) {
        QueryTimeInterval timeRange = new QueryTimeInterval(Duration.ofDays(30));

        LogsQueryResult queryResult = logsQueryClient.queryWorkspace(
            workspaceId,
            QUERY.formatted(eventType, caseId),
            timeRange
        );

        LogsTable table = queryResult.getTable();

        List<Map<String, Object>> results = new ArrayList<>();

        for (LogsTableRow row : table.getRows()) {
            Map<String, Object> rowData = new HashMap<>();
            List<LogsTableCell> cells = row.getRow();
            for (int i = 0; i < table.getColumns().size(); i++) {
                String columnName = table.getColumns().get(i).getColumnName();
                Object value = cells.get(i).getValueAsDynamic();
                rowData.put(columnName, value);
            }
            results.add(rowData);
        }

        return results;
    }
}

package uk.gov.hmcts.reform.roleassignmentbatch.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class EmailData {
    private List<String> emailTo;
    private String emailSubject;
    private String runId;
    private String module;
    Map<String, Object> templateMap;
}

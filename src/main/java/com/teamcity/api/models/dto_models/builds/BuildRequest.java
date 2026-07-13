package com.teamcity.api.models.dto_models.builds;

import com.teamcity.api.generators.GeneratingRule;
import com.teamcity.api.models.BaseModel;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BuildRequest extends BaseModel {
    @GeneratingRule(regex = "^[A-Za-z0-9_-]{3,20}$")
    private String buildTypeId;

    @GeneratingRule(regex = "^(main|develop|feature/[A-Za-z0-9_-]{3,15}|hotfix/[A-Za-z0-9_-]{3,15})$")
    private String branchName;

    private String comment;

    @Builder.Default
    private Boolean cleanSources = false;

    @Builder.Default
    private Boolean rebuildAllDependencies = false;

    // Параметры билда (ключ-значение)
    private java.util.Map<String, String> parameters;

    // Методы-помощники
    public void addParameter(String key, String value) {
        if (parameters == null) {
            parameters = new java.util.HashMap<>();
        }
        parameters.put(key, value);
    }

    public String getParameter(String key) {
        if (parameters == null) return null;
        return parameters.get(key);
    }

    public boolean hasParameter(String key) {
        return parameters != null && parameters.containsKey(key);
    }
}

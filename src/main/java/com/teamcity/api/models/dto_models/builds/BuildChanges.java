package com.teamcity.api.models.dto_models.builds;

import com.teamcity.api.models.BaseModel;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BuildChanges extends BaseModel {
    private String buildId;
    private List<Change> changes;
    private Integer totalChanges;

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Change extends BaseModel {
        private String id;
        private String version;
        private String username;
        private String timestamp;
        private String comment;
        private String vcsRootId;
        private List<String> files;

        @Builder.Default
        private String changeType = "MODIFIED";  // ADDED, MODIFIED, DELETED

        public boolean isFileChanged(String fileName) {
            return files != null && files.contains(fileName);
        }

        public boolean containsComment(String keyword) {
            return comment != null && comment.toLowerCase().contains(keyword.toLowerCase());
        }
    }

    // Методы-помощники
    public boolean hasChanges() {
        return changes != null && !changes.isEmpty();
    }

    public List<Change> getChangesByUser(String username) {
        if (changes == null) return List.of();
        return changes.stream()
                .filter(c -> username.equalsIgnoreCase(c.getUsername()))
                .toList();
    }

    public List<Change> getRecentChanges(int limit) {
        if (changes == null) return List.of();
        return changes.stream()
                .limit(limit)
                .toList();
    }
}

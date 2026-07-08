package com.teamcity.api.admin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AdminArtifactsTest {
    @Test
    @DisplayName("Получить список артефактов сборки")
    public void adminCanGetBuildArtifactsListTest() {
        // Expected: 200 OK
    }

    @Test
    @DisplayName("Получить артефакты несуществующей сборки")
    public void adminCanNotGetArtifactsOfNonExistingBuildTest() {
        // Expected: 404 Not Found
    }

    @Test
    @DisplayName("Скачать конкретный артефакт")
    public void adminCanDownloadArtifactTest() {
        // Expected: 200 OK
    }

    @Test
    @DisplayName("Скачать несуществующий артефакт")
    public void adminCanNotDownloadNonExistingArtifactTest() {
        // Expected: 404 Not Found
    }

    @Test
    @DisplayName("Скачать все артефакты архивом")
    public void adminCanDownloadAllArtifactsAsArchiveTest() {
        // Expected: 200 OK
    }

    @Test
    @DisplayName("Получить метаданные артефакта")
    public void adminCanGetArtifactMetadataTest() {
        // Expected: 200 OK
    }
}

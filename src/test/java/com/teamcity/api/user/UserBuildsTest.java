package com.teamcity.api.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UserBuildsTest {

    @Test
    @DisplayName("Запустить сборку в своём проекте")
    public void userCanRunBuildInOwnProjectTest() {
        // Expected: 200 OK
    }

    @Test
    @DisplayName("Запустить сборку в чужом проекте")
    public void userCanNotRunBuildInAnotherProjectTest() {
        // Expected: Access denied
    }

    @Test
    @DisplayName("Получить статус своей сборки")
    public void userCanGetOwnBuildStatusTest() {
        // Expected: 200 OK
    }

    @Test
    @DisplayName("Получить детали своей сборки")
    public void userCanGetOwnBuildDetailsTest() {
        // Expected: 200 OK
    }

    @Test
    @DisplayName("Отменить свою сборку в очереди")
    public void userCanCancelOwnQueuedBuildTest() {
        // Expected: 200 OK
    }

    @Test
    @DisplayName("Отменить чужую сборку в очереди")
    public void userCanNotCancelAnotherUserBuildTest() {
        // Expected: Access denied
    }
// Не существует ручки для получения лога. Тест несуществуюшего API
//    @Test
//    @DisplayName("Получить лог своей сборки")
//    public void userCanGetOwnBuildLogTest() {
//        // Expected: 200 OK
//    }

    @Test
    @DisplayName("Удалить свою завершённую сборку")
    public void userCanNotDeleteOwnFinishedBuildTest() {
        // Expected: Access denied
    }
}
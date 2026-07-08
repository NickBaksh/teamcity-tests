# TeamCity API Tests

Автотесты для TeamCity REST API с использованием 5-слойной архитектуры.

---

## 📋 О проекте

Проект содержит автоматизированные тесты для проверки TeamCity REST API.  
Архитектура построена на 5 слоях, что обеспечивает **масштабируемость**, **поддерживаемость** и **переиспользуемость** кода.

**Всего тестов:** 211  
**API-тесты:** 140 (88 админ + 52 пользователь)  
**UI-тесты:** 61  
**E2E-тесты:** 3

---

## 🏗️ Архитектура (5 слоёв)

| Слой | Назначение | Пример |
| :--- | :--- | :--- |
| **1. Тесты** | Тестовые сценарии с проверками | `AuthTest.java`, `ProjectsTest.java` |
| **2. Шаги (Steps)** | Бизнес-логика, композиция операций | `UserSteps.createUser()` |
| **3. Клиенты (Clients)** | HTTP-запросы к TeamCity REST API | `RestClient.post()` |
| **4. Модели (Models)** | Java-объекты для JSON-сериализации | `User.java`, `Project.java` |
| **5. Утилиты (Utils)** | Конфигурация, генерация данных, логирование | `ConfigManager`, `TestDataFactory` |

---

## 🚀 Быстрый старт

### 1️⃣ Установить Java 21

# 2️⃣ Клонировать репозиторий:

git clone https://github.com/NickBaksh/teamcity-tests.git
cd teamcity-tests

## 3️⃣ Запустить TeamCity локально (через Docker):

docker-compose up -d

TeamCity будет доступен по адресу: http://localhost:8111

## 4️⃣ Настроить конфигурацию

Создай файл src/test/resources/config/local.properties:

# TeamCity API Configuration
api.base.url=http://localhost:8111
admin.login=admin
admin.password=admin
user.login=user
user.password=user123
api.timeout=30000
browser=chrome
browser.headless=false

# 5️⃣ **Запустить тесты:**

### Все тесты
./mvnw clean test

### Только API-тесты
./mvnw test -Dgroups=api

### Только Smoke-тесты
./mvnw test -Dgroups=smoke

### С Allure-отчётом
./mvnw clean test allure:report

# 6️⃣ Открыть Allure-отчёт:

./mvnw allure:serve

# Структура проекта

## 📁 Структура проекта

| Папка / Файл                                                                    | Назначение                                                      |
|:--------------------------------------------------------------------------------|:----------------------------------------------------------------|
| **`.github/workflows/`**                                                        | GitHub Actions пайплайн для CI/CD                               |
| **`.mvn/wrapper/`**                                                             | Maven Wrapper (фиксированная версия Maven)                      |
|                                                                                 |                                                                 |
| **`src/test/java/com/teamcity/api/`** <br/>**`src/test/java/com/teamcity/ui/`** | Базовые классы для тестов API <br/>Базовые классы для тестов UI |
| **`src/main/java/com/teamcity/core/client/`**                                   | HTTP-клиенты (`RestClient`)                                     |
| **`src/main/java/com/teamcity/core/models/`**                                   | Модели данных (`User`, `Project`)                               |
| **`src/main/java/com/teamcity/core/models/dto/`**                               | DTO для запросов                                                |
| **`src/main/java/com/teamcity/core/steps/`**                                    | Бизнес-шаги (`UserSteps`)                                       |
| **`src/main/java/com/teamcity/core/utils/`**                                    | Утилиты (`ConfigManager`, `TestDataFactory`)                    |
|                                                                                 |                                                                 |
| **`src/test/java/com/teamcity/api/admin/`**                                     | API-тесты (администратор)                                       |
| **`src/test/java/com/teamcity/api/user/`**                                      | API-тесты (пользователь)                                        |
| **`src/test/java/com/teamcity/api/smoke/`**                                     | Smoke-тесты                                                     |
| **`src/test/java/com/teamcity/ui/`**                                            | UI-тесты                                                        |
| **`src/test/java/com/teamcity/e2e/`**                                           | E2E-тесты (сквозные сценарии)                                   |
| **`src/test/java/com/teamcity/listeners/`**                                     | Listeners (скриншоты, ретраи)                                   |
|                                                                                 |                                                                 |
| **`src/test/resources/config/`**                                                | Конфигурации окружений                                          |
| **`src/test/resources/test-data/`**                                             | Тестовые данные (JSON, CSV)                                     |
|                                                                                 |                                                                 |
| **`checkstyle.xml`**                                                            | Правила стиля кода                                              |
| **`pom.xml`**                                                                   | Maven-зависимости                                               |
| **`docker-compose.yml`**                                                        | Локальный запуск TeamCity                                       |
| **`README.md`**                                                                 | Документация                                                    |
| **`.gitignore`**                                                                | Исключения для Git                                              |



## Как написать новый тест

package com.teamcity.api.admin;

import com.teamcity.api.BaseApiTest;
import com.teamcity.core.models.User;
import com.teamcity.core.steps.UserSteps;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Epic("Admin API")
@Feature("User Management")
public class MyTest extends BaseApiTest {

    private UserSteps userSteps;

    @BeforeEach
    public void initSteps() {
        userSteps = new UserSteps(adminClient);
    }

    @Test
    @DisplayName("Create user with valid credentials")
    @Description("Проверяет, что пользователь создаётся с корректными данными")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Create user")
    public void testCreateUserPositive() {
        // 1. Arrange — подготовка данных
        User user = dataFactory.createRandomUser();

        // 2. Act — выполнение действия
        User created = userSteps.createUser(user);
        trackUser(created.getUsername());

        // 3. Assert — проверка результата
        assertAll("User creation validation",
            () -> assertNotNull(created.getUsername()),
            () -> assertEquals(user.getUsername(), created.getUsername()),
            () -> assertEquals(user.getEmail(), created.getEmail())
        );

        LOG.info("✅ User created: {}", created.getUsername());
    }
}

###  Добавь Allure-аннотации

## 🏷️ Allure-аннотации

| Аннотация | Назначение | Пример |
| :--- | :--- | :--- |
| **`@Epic`** | Группирует тесты по крупным модулям | `@Epic("Admin API")` |
| **`@Feature`** | Группирует тесты по функциональности | `@Feature("User Management")` |
| **`@Story`** | Группирует тесты по конкретным сценариям | `@Story("Create user")` |
| **`@Description`** | Описание теста (видно в Allure) | `@Description("Проверяет создание пользователя")` |
| **`@Severity`** | Важность теста | `@Severity(SeverityLevel.CRITICAL)` |
| **`@DisplayName`** | Человекочитаемое имя теста | `@DisplayName("Create user with valid credentials")` |


### Открыть отчёт:

./mvnw allure:serve

## Code Style (Checkstyle)

Проект использует Checkstyle для единого стиля кода.

Основные правила:

Отступ — 4 пробела (не Tab)

Длина строки — ≤ 120 символов

Длина метода — ≤ 60 строк

Не более 7 параметров у метода

## Проверить стиль:
./mvnw checkstyle:check


# Частые проблемы и решения

## 1. TeamCity не поднимается локально

### Проверь, что Docker запущен
docker ps

### Перезапусти контейнеры
docker-compose down
docker-compose up -d

### Проверь логи
docker-compose logs -f

## 2. Тесты падают с "Connection refused"
Проверь local.properties:
api.base.url=http://localhost:8111  

## 3. Allure не открывается

### 1. Убедись, что тесты запускались
ls target/allure-results

### 2. Перегенерировать отчёт
./mvnw allure:clean
./mvnw allure:serve

### 4. Checkstyle ругается
#### Посмотреть все ошибки
./mvnw checkstyle:check

#### Исправить автоформатированием (IDEA)
Ctrl + Alt + L


## Контакты:

| Роль                            | Ответственный      |
|:--------------------------------|:-------------------|
| Smoke-тесты                     | Инженер 1          |
| **API-тесты (админ)**           | Инженер 1, 2, 3, 4 |
| **API-тесты (пользователь)**    | Инженер 2, 3, 4    |
| **UI-тесты** | Инженер 1, 2, 3, 4 |
| **CI/CD**                       | Инженер 1, 2      |

---

## 📚 Полезные ссылки

| Ресурс | Ссылка |
| :--- | :--- |
| **TeamCity REST API** | [https://www.jetbrains.com/help/teamcity/rest-api.html](https://www.jetbrains.com/help/teamcity/rest-api.html) |
| **Allure Documentation** | [https://docs.qameta.io/allure/](https://docs.qameta.io/allure/) |
| **JUnit 5 User Guide** | [https://junit.org/junit5/docs/current/user-guide/](https://junit.org/junit5/docs/current/user-guide/) |
| **REST Assured Documentation** | [https://rest-assured.io/](https://rest-assured.io/) |
| **Lombok Documentation** | [https://projectlombok.org/](https://projectlombok.org/) |
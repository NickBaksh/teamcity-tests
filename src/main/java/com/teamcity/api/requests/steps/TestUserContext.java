package com.teamcity.api.requests.steps;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class TestUserContext {
    private String username;
    private String password;
    private String token;
    private Long userId;
    private String role;

    @Builder.Default
    private Map<Long, String> accounts = new LinkedHashMap<>();

    // ========== Методы для работы с аккаунтами ==========

    /**
     * Получить список ID аккаунтов
     */
    public List<Long> getAccountsIds() {
        return new ArrayList<>(accounts.keySet());
    }

    /**
     * Добавить аккаунт с ID и номером
     */
    public void addAccount(Long accountId, String accountNumber) {
        accounts.put(accountId, accountNumber);
    }

    /**
     * Добавить аккаунт только с ID (для обратной совместимости)
     */
    public void addAccount(Long accountId) {
        accounts.put(accountId, null);
    }

    /**
     * Получить номер аккаунта по ID
     */
    public String getAccountNumber(Long accountId) {
        return accounts.get(accountId);
    }

    /**
     * Проверить, есть ли у аккаунта номер
     */
    public boolean hasAccountNumber(Long accountId) {
        String number = accounts.get(accountId);
        return number != null && !number.isEmpty();
    }

    // ========== Удобные методы для доступа к аккаунтам ==========

    /**
     * Получить первый аккаунт
     */
    public Long getFirstAccountId() {
        if (accounts.isEmpty()) {
            throw new IllegalStateException("User [" + username + "] has no accounts");
        }
        return new ArrayList<>(accounts.keySet()).get(0);
    }

    /**
     * Получить второй аккаунт
     */
    public Long getSecondAccountId() {
        if (accounts.size() < 2) {
            throw new IllegalStateException("User [" + username + "] has less than 2 accounts");
        }
        return new ArrayList<>(accounts.keySet()).get(1);
    }

    /**
     * Получить аккаунт по индексу
     */
    public Long getAccountIdByIndex(int index) {
        if (index >= accounts.size()) {
            throw new IllegalStateException("User [" + username + "] has no account at index " + index);
        }
        return new ArrayList<>(accounts.keySet()).get(index);
    }

    /**
     * Проверить наличие аккаунтов
     */
    public boolean hasAccounts() {
        return !accounts.isEmpty();
    }

    /**
     * Получить количество аккаунтов
     */
    public int getAccountsCount() {
        return accounts.size();
    }

    // ========== Методы для проверки роли ==========

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public boolean isUser() {
        return "USER".equalsIgnoreCase(role);
    }

    // ========== Вспомогательные методы ==========

    public String getDisplayName() {
        return username + " (ID: " + userId + ", role: " + role + ", accounts: " + accounts.size() + ")";
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
package com.teamcity.ui.pages.elements;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;

public class ConfirmDialog {

    private final SelenideElement dialog = $(
            "[data-test='ring-dialog'], .modalDialog, .ring-dialog, #deleteProjectForm, #pauseBuildTypeForm"
    );
    private final SelenideElement confirmButton = $x(
            "//div[contains(@class,'modalDialog') or contains(@class,'ring-dialog') or @id='deleteProjectForm' or @id='pauseBuildTypeForm']"
                    + "//*[self::button or self::input or self::a]"
                    + "[contains(@value,'Delete') or contains(.,'Delete') or contains(@value,'Pause') "
                    + "or contains(.,'Pause') or contains(@value,'Remove') or contains(.,'Remove') "
                    + "or contains(@value,'OK') or contains(.,'OK') or contains(@value,'Yes') or contains(.,'Yes')]"
    );
    private final SelenideElement cancelButton = $x(
            "//button[contains(.,'Cancel') or contains(.,'No')] | //input[@value='Cancel']"
    );

    @Step("Confirm dialog action")
    public void confirm() {
        if (dialog.exists() && dialog.is(visible)) {
            confirmButton.shouldBe(visible).click();
            return;
        }
        if (confirmButton.exists()) {
            confirmButton.click();
        }
    }

    @Step("Cancel dialog action")
    public void cancel() {
        cancelButton.shouldBe(visible).click();
    }

    @Step("Check confirm dialog is visible")
    public boolean isVisible() {
        return (dialog.exists() && dialog.is(visible)) || (confirmButton.exists() && confirmButton.is(visible));
    }
}

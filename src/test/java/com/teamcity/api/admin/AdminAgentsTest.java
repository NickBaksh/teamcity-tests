package com.teamcity.api.admin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AdminAgentsTest {
    @Test
    @DisplayName("Юзер может получить всех агентов")
    public void adminCanGetAllAgentsTest (){
     //Expected: 200 OK
    }

    @Test
    @DisplayName("Юзер может получить агента по ID")
    public void adminCanGetAgentByIdTest(){
        //Expected: 200 OK
    }

    @Test
    @DisplayName("Юзер не может получить несуществующего агента ")
    public void adminCanNotGetNonExistingAgentTest(){
        //Expected: 404 Not Found
    }

    @Test
    @DisplayName("Юзер может включить агента")
    public void adminCanEnableAgentTest(){
     //Expected: 200 OK
    }

    @Test
    @DisplayName("Юзер может выключить агента")
    public void adminCanDisableAgentTest() {
        //Expected: 200 OK
    }

    @Test
    @DisplayName("Юзер может авторизовать агента")
    public void adminCanAuthorizeAgentTest(){
    //Expected: 200 OK
    }
}

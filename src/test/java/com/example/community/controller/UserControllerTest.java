package com.example.community.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testLoginPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/login"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void testSignupPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/signup"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void testSignup() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/signup")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "controller_test")
                        .param("password", "password123!")
                        .param("name", "Controller Test")
                        .param("nickname", "ctrl_tester")
                        .param("email", "ctrl@test.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andDo(print());

        log.info("Signup test completed");

    }

    @Test
    public void testCheckUsername_Available() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/check-username")
                .param("username", "available_user")
                .with(SecurityMockMvcRequestPostProcessors.anonymous()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"))
                .andDo(print());

        log.info("Check username test completed");

    }

    @Test
    public void testCheckUsername_Duplicate() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/signup")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "duplicate_user")
                .param("password", "password123!")
                .param("name","Duplicate Test")
                .param("nickname","dup_nick")
                .param("email","dup@test.com"));

        mockMvc.perform(MockMvcRequestBuilders.get("/check-username")
                .param("username", "duplicate_user")
                .with(SecurityMockMvcRequestPostProcessors.anonymous()))
                .andExpect(status().isOk())
                .andExpect(content().string("false"))
                .andDo(print());

        log.info("Check username test completed");
    }

    @Test
    public void testCheckNickname_Available() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/check-nickname")
                        .param("nickname", "available_nick")
                        .with(SecurityMockMvcRequestPostProcessors.anonymous()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"))
                .andDo(print());

        log.info("Check nickname (available) test completed");
    }

    @Test
    public void testCheckNickname_Duplicate() throws Exception {
        // 먼저 회원가입
        mockMvc.perform(MockMvcRequestBuilders.post("/signup")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "nick_user")
                .param("password", "password123!")
                .param("name", "Nick Test")
                .param("nickname", "duplicate_nick")
                .param("email", "nick@test.com"));

        // 중복 체크
        mockMvc.perform(MockMvcRequestBuilders.get("/check-nickname")
                        .param("nickname", "duplicate_nick")
                        .with(SecurityMockMvcRequestPostProcessors.anonymous()))
                .andExpect(status().isOk())
                .andExpect(content().string("false"))
                .andDo(print());

        log.info("Check nickname (duplicate) test completed");
    }
}
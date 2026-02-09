package com.example.community.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.springframework.security.test.context.support.WithMockUser;

@WithMockUser
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
class PostControllerTest {
    @Autowired
    private MockMvc mockMvc;

    // 테스트용
    private static final String BOARD_ID = "1";
    private static final String POST_ID = "1";

    @Test
    public void testList() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get("/board/{boardId}/post", BOARD_ID);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void testListWithPaging() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/board/{boardId}/post/list", BOARD_ID)
                .param("page", "1")
                .param("size", "10");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void testWrite() throws Exception {

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/board/{boardId}/post/write", BOARD_ID)
                .with(csrf())
                .param("title", "[TEST] PostControllerTest#testWrite")
                .param("content", "[TEST] PostControllerTest#testWrite");

        mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().is3xxRedirection());
    }
}
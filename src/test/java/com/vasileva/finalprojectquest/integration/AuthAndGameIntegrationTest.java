package com.vasileva.finalprojectquest.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vasileva.finalprojectquest.dto.LoginRequest;
import com.vasileva.finalprojectquest.dto.RegisterRequest;
import com.vasileva.finalprojectquest.entity.Quest;
import com.vasileva.finalprojectquest.repository.QuestRepository;
import com.vasileva.finalprojectquest.repository.QuestionRepository;
import com.vasileva.finalprojectquest.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Integration testing for authorization and game process")
public class AuthAndGameIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private QuestRepository questRepository;
    @Autowired
    private QuestionRepository questionRepository;

    private Long testQuestId;

    @BeforeEach
    void initDatabase() {
        Quest savedQuest = TestDataFactory.createAndSaveTestQuest(questRepository, questionRepository);
        testQuestId = savedQuest.getId();
    }

    @Test
    @DisplayName("Scenario: register, successful login, get JWT and start game")
    void fullUserScenario() throws Exception {

        mockMvc.perform(post("/game/start")
                        .param("questId", String.valueOf(testQuestId)))
                .andExpect(status().isForbidden());

        RegisterRequest registerData = TestDataFactory.createRegisterRequest(
                "testplayer", "player@quest.com", "secret123"
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerData)))
                .andExpect(status().isOk());

        LoginRequest loginData = TestDataFactory.createLoginRequest(
                "testplayer", "secret123");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        String jsonResponse = loginResult.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(jsonResponse).get("accessToken").asText();

        mockMvc.perform(post("/game/start")
                        .header("Authorization", "Bearer " + accessToken) // Имитируем фронтенд
                        .param("questId", String.valueOf(testQuestId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Start?"))
                .andExpect(jsonPath("$.label").value("1"));

        mockMvc.perform(get("/stats")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userLogin").value("testplayer"))
                .andExpect(jsonPath("$.totalGames").value(0)); // Пока 0, игра еще не завершена
    }
}


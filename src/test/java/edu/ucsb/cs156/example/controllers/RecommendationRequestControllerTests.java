package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = RecommendationRequestController.class)
@Import(TestConfig.class)
public class RecommendationRequestControllerTests extends ControllerTestCase {

        @MockBean
        RecommendationRequestRepository RecommendationRequestRepository;

        @MockBean
        UserRepository userRepository;

        // Tests for GET /api/RecommendationRequest/all
        
        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/RecommendationRequest/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/RecommendationRequest/all"))
                                .andExpect(status().is(200)); // logged
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_recommendationrequests() throws Exception {

                // arrange
                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime ldt1b = LocalDateTime.parse("2022-02-03T00:00:00");

                RecommendationRequest RecommendationRequest1 = RecommendationRequest.builder()
                                .requesterEmail("student@ucsb.edu")
                                .professorEmail("professor@ucsb.edu")
                                .explanation("Letter of Rec")
                                .dateRequested(ldt1)
                                .dateNeeded(ldt1b)
                                .done(false)
                                .build();

                LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");
                LocalDateTime ldt2b = LocalDateTime.parse("2022-04-11T00:00:00");

                RecommendationRequest RecommendationRequest2 = RecommendationRequest.builder()
                                .requesterEmail("student2@ucsb.edu")
                                .professorEmail("professor2@ucsb.edu")
                                .explanation("Second Letter of Rec")
                                .dateRequested(ldt2)
                                .dateNeeded(ldt2b)
                                .done(true)
                                .build();

                ArrayList<RecommendationRequest> expectedRequests = new ArrayList<>();
                expectedRequests.addAll(Arrays.asList(RecommendationRequest1, RecommendationRequest2));

                when(RecommendationRequestRepository.findAll()).thenReturn(expectedRequests);

                // act
                MvcResult response = mockMvc.perform(get("/api/RecommendationRequest/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(RecommendationRequestRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedRequests);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for POST /api/RecommendationRequest/post...

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/RecommendationRequest/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/RecommendationRequest/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_recommendationrequest() throws Exception {
                // arrange

                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime ldt1b = LocalDateTime.parse("2022-02-03T00:00:00");

                RecommendationRequest RecommendationRequest1 = RecommendationRequest.builder()
                                .requesterEmail("student@ucsb.edu")
                                .professorEmail("professor@ucsb.edu")
                                .explanation("Letter of Rec")
                                .dateRequested(ldt1)
                                .dateNeeded(ldt1b)
                                .done(true)
                                .build();

                when(RecommendationRequestRepository.save(eq(RecommendationRequest1))).thenReturn(RecommendationRequest1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/RecommendationRequest/post?requesterEmail=student@ucsb.edu&professorEmail=professor@ucsb.edu&explanation=Letter of Rec&dateRequested=2022-01-03T00:00:00&dateNeeded=2022-02-03T00:00:00&done=true")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(RecommendationRequestRepository, times(1)).save(RecommendationRequest1);
                String expectedJson = mapper.writeValueAsString(RecommendationRequest1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // Tests for GET /api/RecommendationRequest?id=...

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/RecommendationRequest?id=7"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange
                LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
                LocalDateTime ldt1b = LocalDateTime.parse("2022-02-03T00:00:00");

                RecommendationRequest RecommendationRequest1 = RecommendationRequest.builder()
                                .requesterEmail("student@ucsb.edu")
                                .professorEmail("professor@ucsb.edu")
                                .explanation("Letter of Rec")
                                .dateRequested(ldt1)
                                .dateNeeded(ldt1b)
                                .done(false)
                                .build();

                when(RecommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.of(RecommendationRequest1));

                // act
                MvcResult response = mockMvc.perform(get("/api/RecommendationRequest?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(RecommendationRequestRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(RecommendationRequest1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(RecommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/RecommendationRequest?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(RecommendationRequestRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("RecommendationRequest with id 7 not found", json.get("message"));
        }
}
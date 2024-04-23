package com.vioc.central.motor.controller;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;

import jakarta.servlet.ServletException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.annotation.RequestMethod;

import com.vioc.central.motor.IntegrationTest;
import com.vioc.core.test.controller.AbstractControllerTest;
import com.vioc.core.test.controller.ControllerTest;
import com.vioc.core.test.security.WithMockViocUser;
import com.vioc.pos.vcdb.service.vin.InvalidVinException;

class MotorControllerTest {

    @Nested
    @AutoConfigureMockMvc
    @IntegrationTest
    @ControllerTest(controllerClass = MotorController.class, controllerMethod = FindVehicleToEngineConfigs.FIND_METHOD,
            requestMethod = RequestMethod.GET, uriTemplate = FindVehicleToEngineConfigs.PATH)
    class FindVehicleToEngineConfigs extends AbstractControllerTest {

        private static final String PATH = "/v1/vehicles";

        private static final String FIND_METHOD = "findVehicleToEngineConfigs";

        @Test
        @WithMockViocUser
        void testfindVehicleToEngineConfigs_notFound() throws Exception {
            performRequest("[]", Map.of("vin", "137DA8331A1111111"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0]").doesNotHaveJsonPath());
        }

        @Test
        @WithMockViocUser
        void testfindVehicleToEngineConfigs_invalidVin() {
            final var vin = "123";
            // Note: don't use this as a pattern!  The InvalidVinException should be caught and rewrapped so that we 
            //  throw an ApiErrorException instead.  That is out of scope for this change.
            assertThatExceptionOfType(ServletException.class)
                    .isThrownBy(() -> performRequest("[]", Map.of("vin", vin)))
                    .withCauseInstanceOf(InvalidVinException.class)
                    .withMessageContaining(
                            "Invalid vin passed.  VINs are usually 17-18 characters and are validated via a check digit. VIN=\"" + vin + "\"");
        }

        @Test
        @WithMockViocUser
        void testfindVehicleToEngineConfigs() throws Exception {

            performRequest("[]", Map.of("vin", "JN1CA21D7TT174600"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(46497))
                    .andExpect(jsonPath("$[0].vehicle.id").value(16201))
                    .andExpect(jsonPath("$[0].engineConfig.id").value(3294));
        }

        @Test
        @DisplayName("Should require authentication when not running with the store or local profiles")
        void accessUnauthenticated() throws Exception {
            RequestBuilder request = MockMvcRequestBuilders.request(HttpMethod.GET, PATH)
                    .param("vin", "JN1CA21D7TT174600");

            mockMvc.perform(request)
                    .andExpect(status().isUnauthorized());

        }

    }

}

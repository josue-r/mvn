package com.vioc.central.motor.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.web.bind.annotation.RequestMethod;

import com.vioc.central.motor.IntegrationTest;
import com.vioc.core.test.controller.AbstractControllerTest;
import com.vioc.core.test.controller.ControllerTest;
import com.vioc.core.test.security.WithMockViocUser;

public class VehicleSpecificationControllerTest {

    @Nested
    @AutoConfigureMockMvc
    @IntegrationTest
    @WithMockViocUser
    @ControllerTest(controllerClass = VehicleSpecificationController.class,
            controllerMethod = VehicleSpecificationControllerTest.GetVehicleSpecifications.FIND_METHOD,
            requestMethod = RequestMethod.GET, uriTemplate = GetVehicleSpecifications.PATH)
    class GetVehicleSpecifications extends AbstractControllerTest {

        private static final String PATH = "/v1/vehicle-specifications/{0}";

        private static final String FIND_METHOD = "getVehicleSpecifications";

        @Test
        @DisplayName("Should fetch vehicle specifications for the given vehicleToEngineConfigId")
        void testGetVehicleSpecifications_success() throws Exception {
            final String vehicleToEngineConfigId = "175297";
            performRequest(null, List.of(vehicleToEngineConfigId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.year").value(2004))
                    .andExpect(jsonPath("$.vehicleToEngineConfigId").value(vehicleToEngineConfigId))
                    .andExpect(jsonPath("$.makeName").value("Ford"))
                    .andExpect(jsonPath("$.description").value("F-150 Heritage XL, 8CYL 4.6L, Triton (Romeo), MFI (W)"))
                    .andExpect(jsonPath("$.engine").value("2004 Ford F-150 Heritage"))
                    .andExpect(jsonPath("$.model").value("F-150 Heritage"))
                    .andExpect(jsonPath("$.displayText").value("XL, 8CYL 4.6L, Triton (Romeo), MFI (W)"));
        }

        @Test
        @DisplayName("Should find no vehicle specifications for the given vehicleToEngineConfigId")
        void testGetVehicleSpecification_notFound() throws Exception {
            final String notFoundVehicleToEngineConfigId = "175297777";
            performRequest(null, List.of(notFoundVehicleToEngineConfigId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should trigger a bad request for the given ID as it is too big to be an Integer")
        void testGetVehicleSpecification_badRequest() throws Exception {
            final String badVehicleToEngineConfigId = "17529777777777";
            performRequest(null, List.of(badVehicleToEngineConfigId))
                    .andExpect(status().isBadRequest());
        }

    }


    @Nested
    @AutoConfigureMockMvc
    @IntegrationTest
    @ControllerTest(controllerClass = VehicleSpecificationController.class,
            controllerMethod = GetPartsSpecifications.METHOD,
            requestMethod = RequestMethod.GET, uriTemplate = GetPartsSpecifications.PATH)
    class GetPartsSpecifications extends AbstractControllerTest {

        private static final String METHOD = "getPartsSpecifications";

        // Used a vehicle that has the three categories.
        private static final String VEHICLE_TO_ENGINE_CONFIG_ID = "641917";

        private static final String PATH = "/v1/vehicle-specifications/{0}/parts/{1}";

        private static Stream<Arguments> provideProductCategories() {
            return Stream.of(  // Category & Status
                    Arguments.of("Valid air filter", VEHICLE_TO_ENGINE_CONFIG_ID, "AIR_FILTER"),
                    Arguments.of("Valid cabin air", VEHICLE_TO_ENGINE_CONFIG_ID, "CABIN_AIR"),
                    Arguments.of("Valid oil filter", VEHICLE_TO_ENGINE_CONFIG_ID, "OIL_FILTER"),
                    Arguments.of("Valid Adv Synth oil filter", VEHICLE_TO_ENGINE_CONFIG_ID, "ADVFULLSYN"),
                    Arguments.of("Valid diesel fuel filter", VEHICLE_TO_ENGINE_CONFIG_ID, "DIESEL_FUEL_FILTER"),
                    Arguments.of("Valid serpentine belt", VEHICLE_TO_ENGINE_CONFIG_ID, "SERP_BELT"),
                    Arguments.of("Valid fuel filter", VEHICLE_TO_ENGINE_CONFIG_ID, "FUEL_FILTER"),
                    Arguments.of("Valid breather", VEHICLE_TO_ENGINE_CONFIG_ID, "BREATHER"),
                    Arguments.of("Valid  PC valve", VEHICLE_TO_ENGINE_CONFIG_ID,  "PCV")
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("provideProductCategories")
        @WithMockViocUser
        @DisplayName("Should get motor parts.")
        void testGetPartsSpecification_validCategories(String testName, String vehicleToEngineConfigId,
                String category) throws Exception {

            performRequest(null, List.of(vehicleToEngineConfigId, category))
                    .andExpect(status().isOk());
        }

        private static Stream<Arguments> provideInvalidParameters() {
            return Stream.of(
                    Arguments.of("Non numeric vehicle to engine config id", "AAAA", "AIR_FILTER"),
                    Arguments.of("Invalid integer vehicle to engine config id", "17529777777", "AIR_FILTER")
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("provideInvalidParameters")
        @WithMockViocUser
        @DisplayName("Invalid inputs test")
        void testGetPartsSpecification_badRequests(String testName, String vehicleToEngineConfigId,
                String category) throws Exception {
            performRequest(null, List.of(vehicleToEngineConfigId, category))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockViocUser
        @DisplayName("Expect empty responses - invalid category")
        void testGetPartsSpecifications_invalidCategory() throws Exception {
            performRequest(null, List.of(VEHICLE_TO_ENGINE_CONFIG_ID, "INVALID_CATEGORY"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }

    }

}


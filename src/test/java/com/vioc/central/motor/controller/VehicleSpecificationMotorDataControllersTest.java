package com.vioc.central.motor.controller;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.vioc.pos.vcdb.EnableMotor;
import com.vioc.pos.vcdb.external.domain.DrainPlug;
import jakarta.inject.Inject;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.annotation.AliasFor;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import com.vioc.central.motor.IntegrationTest;
import com.vioc.core.test.controller.AbstractControllerTest;
import com.vioc.core.test.controller.FunctionalControllerTest;
import com.vioc.core.test.security.WithMockViocUser;
import com.vioc.pos.vcdb.aces.domain.VehicleToEngineConfigAces;
import com.vioc.pos.vcdb.motor.MotorApplication;
import com.vioc.pos.vcdb.motor.MotorApplicationMap;
import com.vioc.pos.vcdb.motor.MotorData;
import com.vioc.pos.vcdb.motor.quicklube.domain.EngineDrainPlugTorque;
import com.vioc.pos.vcdb.motor.quicklube.domain.FinalDriveTorque;
import com.vioc.pos.vcdb.motor.quicklube.domain.ManualTransmissionTorque;
import com.vioc.pos.vcdb.motor.quicklube.domain.OilFilterProcedure;
import com.vioc.pos.vcdb.motor.quicklube.domain.OilFilterTorque;
import com.vioc.pos.vcdb.motor.quicklube.domain.OilFilterType;
import com.vioc.pos.vcdb.motor.quicklube.domain.QuickLubeNote;
import com.vioc.pos.vcdb.motor.quicklube.domain.TransferCaseTorque;
import com.vioc.pos.vcdb.service.MotorDataService;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.Operation;

/**
 * Tests the functional spring-mvc endpoints.
 * <p>
 * To create a test for a new endpoint, extend the {@link AbstractMotorDataTest} and annotate with
 * {@link MotorDataTest}, with the url pattern in the set in this annotation.  Then, implement the
 * {@link AbstractMotorDataTest#createMockData()} to build the expected motor data to return, then implement
 * {@link AbstractMotorDataTest#getExpectedSuccessJson()} to return the expected result JSON.
 */
class VehicleSpecificationMotorDataControllersTest {

    private static final String FT_LBS = "10 Ft-lbs";

    private static final String NEWTON_METERS = "13.56 Nm";

    //
    // AbstractMotorDataTest implementations
    // 

    @Nested
    @MotorDataTest("/v1/vehicle-specifications/{0}/oil-filter-torque")
    class GetOilFilterTorque extends AbstractMotorDataTest<OilFilterTorque> {

        @Override
        List<OilFilterTorque> createMockData() {
            final var oilFilterTorque = new OilFilterTorque();
            oilFilterTorque.setId(10001);
            oilFilterTorque.setOilFilterProcedure(new OilFilterProcedure("Procedure Id", "Procedure Description"));
            oilFilterTorque.setOilFilterType(new OilFilterType("Type ID", "Type Description"));
            oilFilterTorque.setQualifier("Qualifier");
            oilFilterTorque.setTorqueFtLbs(FT_LBS);
            oilFilterTorque.setTorqueNm(NEWTON_METERS);
            oilFilterTorque.setNote(new QuickLubeNote("Note Id", "Note Description"));
            return List.of(oilFilterTorque);
        }

        @Override
        String getExpectedSuccessJson() {
            return """
                    [
                      {
                        "id": 10001,
                        "qualifier": "Qualifier",
                        "torqueFtLbs": "10 Ft-lbs",
                        "torqueNm": "13.56 Nm",
                        "oilFilterProcedure": {
                          "id": "Procedure Id",
                          "value": "Procedure Description"
                        },
                        "oilFilterType": {
                          "id": "Type ID",
                          "value": "Type Description"
                        },
                        "notes": [
                          {
                            "id": "Note Id",
                            "value": "Note Description"
                          }
                        ]
                      }
                    ]
                    """;
        }

    }

    @Nested
    @MotorDataTest("/v1/vehicle-specifications/{0}/engine-drain-plug-torque")
    class GetEngineDrainOilDrainPlugTorque extends AbstractMotorDataTest<EngineDrainPlugTorque> {

        @Override
        List<EngineDrainPlugTorque> createMockData() {
            EngineDrainPlugTorque engineDrainPlugTorque = new EngineDrainPlugTorque();
            engineDrainPlugTorque.setId(537152);
            engineDrainPlugTorque.setType("Type");
            engineDrainPlugTorque.setTorqueFtLbs("10 ft-lbs");
            engineDrainPlugTorque.setNote(new QuickLubeNote("note1", "Example of a Note value"));
            return List.of(engineDrainPlugTorque);
        }

        @Override
        String getExpectedSuccessJson() {
            return """
                    [
                       {
                         "id": 537152,
                         "type": "Type",
                         "torqueFtLbs": "10 ft-lbs",
                         "notes": [
                           {
                             "id": "note1",
                             "value": "Example of a Note value"
                           }
                         ]
                       }
                     ]
                    """;
        }

    }

    @Nested
    @MotorDataTest("/v1/vehicle-specifications/{0}/final-drive-torque")
    class GetVehicleSpecificationsFinalDriveTorque extends AbstractMotorDataTest<FinalDriveTorque> {

        @Override
        List<FinalDriveTorque> createMockData() {
            final var finalDriveTorque = new FinalDriveTorque();
            finalDriveTorque.setId(9570);
            finalDriveTorque.setType("Type");
            finalDriveTorque.setDrainPlugTorqueFtLbs(FT_LBS);
            finalDriveTorque.setFillPlugTorqueFtLbs(NEWTON_METERS);
            finalDriveTorque.setNote(new QuickLubeNote("note1", "Note"));
            return List.of(finalDriveTorque);
        }

        @Override
        String getExpectedSuccessJson() {
            return """
                    [
                      {
                        "id": 9570,
                        "type": "Type",
                        "fillPlugTorqueFtLbs": "13.56 Nm",
                        "drainPlugTorqueFtLbs": "10 Ft-lbs",
                        "notes": [
                          {
                            "id": "note1",
                            "value": "Note"
                          }
                        ]
                      }
                    ]
                    """;
        }

    }


    @Nested
    @MotorDataTest("/v1/vehicle-specifications/{0}/manual-transmission-torque")
    class GetVehicleSpecificationsManualTransmissionTorque extends AbstractMotorDataTest<ManualTransmissionTorque> {

        @Override
        List<ManualTransmissionTorque> createMockData() {
            final var manualTransmissionTorque = new ManualTransmissionTorque();
            manualTransmissionTorque.setId(4985);
            manualTransmissionTorque.setDrainPlugTorqueFtLbs(FT_LBS);
            manualTransmissionTorque.setFillPlugTorqueFtLbs(NEWTON_METERS);
            manualTransmissionTorque.setType("Torque Type");
            manualTransmissionTorque.setNote(new QuickLubeNote("n1", "Note"));
            return List.of(manualTransmissionTorque);
        }

        @Override
        String getExpectedSuccessJson() {
            return """
                    [
                      {
                        "id": 4985,
                        "type": "Torque Type",
                        "fillPlugTorqueFtLbs": "13.56 Nm",
                        "drainPlugTorqueFtLbs": "10 Ft-lbs",
                        "notes": [
                          {
                            "id": "n1",
                            "value": "Note"
                          }
                        ]
                      }
                    ]
                    """;
        }

    }

    @Nested
    @MotorDataTest("/v1/vehicle-specifications/{0}/transfer-case-torque")
    class GetVehicleSpecificationsTransferCaseTorque extends AbstractMotorDataTest<TransferCaseTorque> {

        @Override
        List<TransferCaseTorque> createMockData() {
            final var transferCaseTorque = new TransferCaseTorque();
            transferCaseTorque.setId(1884);
            transferCaseTorque.setType("Type");
            transferCaseTorque.setDrainPlugTorqueFtLbs(FT_LBS);
            transferCaseTorque.setFillPlugTorqueFtLbs(NEWTON_METERS);
            transferCaseTorque.setNote(new QuickLubeNote("note1", "Note"));
            return List.of(transferCaseTorque);
        }

        @Override
        String getExpectedSuccessJson() {
            return """
                    [
                      {
                        "id": 1884,
                        "type": "Type",
                        "fillPlugTorqueFtLbs": "13.56 Nm",
                        "drainPlugTorqueFtLbs": "10 Ft-lbs",
                        "notes": [
                          {
                            "id": "note1",
                            "value": "Note"
                          }
                        ]
                      }
                    ]
                    """;
        }

    }

    @Nested
    @MotorDataTest("/v1/vehicle-specifications/{0}/drain-plug")
    class GetVehicleSpecificationsDrainPlug extends AbstractMotorDataTest<DrainPlug> {

        @Override
        List<DrainPlug> createMockData() {
            final var drainPlug = new DrainPlug();
            drainPlug.setId(4);
            drainPlug.setProductCode("11673");
            drainPlug.setSize("SIZE");
            return List.of(drainPlug);
        }

        @Override
        String getExpectedSuccessJson() {
            return """
                    [
                      {
                        "id": 4,
                        "productCode": "11673",
                        "size": "SIZE"
                      }
                    ]
                    """;
        }

    }

    //
    // Other API tests
    //

    /**
     * Verify Swagger doc is generated
     */
    @Nested
    @IntegrationTest
    @AutoConfigureMockMvc
    class SwaggerGenerationTest {

        @Inject
        private MockMvc mockMvc;

        @Test
        void testSwaggerGenerated() throws Exception {
            final var swagger = mockMvc.perform(MockMvcRequestBuilders.get("/v3/api-docs"))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse().getContentAsString();
            final var parser = new OpenAPIParser().readContents(swagger, null, null);

            final Operation oilDrainPlugTorque = parser.getOpenAPI().getPaths().get(
                    "/v1/vehicle-specifications/{vehicleToEngineConfigId}/oil-filter-torque").getGet();
            assertThat(oilDrainPlugTorque.getParameters())
                    .extracting("name", "in", "required", "schema.type")
                    .containsExactly(tuple("vehicleToEngineConfigId", "path", true, "integer"));

            final var response200 = oilDrainPlugTorque.getResponses().get("200");
            assertThat(response200.getDescription()).isEqualTo("OK");
            assertThat(response200.getContent().get("application/json"))
                    .extracting("schema.type", "schema.items.$ref")
                    .containsExactly("array", "#/components/schemas/OilFilterTorque");

            assertStatusCode(oilDrainPlugTorque, "400", "Bad Request");
            assertStatusCode(oilDrainPlugTorque, "403", "Forbidden");
            assertStatusCode(oilDrainPlugTorque, "404", "Not Found");
            assertStatusCode(oilDrainPlugTorque, "500", "Internal Server Error");
        }

        private static void assertStatusCode(Operation oilDrainPlugTorque, String statusCode,
                String expectedDescription) {
            final var response400 = oilDrainPlugTorque.getResponses().get(statusCode);
            assertThat(response400.getDescription()).isEqualTo(expectedDescription);
            assertThat(response400.getContent().get("application/json"))
                    .extracting("schema.type", "schema.$ref")
                    .containsExactly(null, "#/components/schemas/ApiErrorResponse");
        }

    }

    //
    // Support Classes below
    //

    @Documented
    @Retention(RUNTIME)
    @Target(TYPE)
    @Inherited
    @FunctionalControllerTest(requestMethod = RequestMethod.GET, uriTemplate = "")
    @interface MotorDataTest {

        @AliasFor(annotation = FunctionalControllerTest.class, value = "uriTemplate")
        String value();

    }

    @AutoConfigureMockMvc
    @IntegrationTest
    @WithMockViocUser
    abstract static class AbstractMotorDataTest<D extends MotorData> extends AbstractControllerTest {

        /**
         * A map of {@link MotorData} class to corresponding {@link MotorApplicationMap} class. {@snippet}
         */
        private static final Map<Class<?>, Class<?>> DATA_TO_MAP = buildDataToMapMapping();

        @MockBean
        private MotorDataService mockMotorDataService;

        private final Integer vehicleToEngineConfigId = 537152;

        abstract List<D> createMockData();

        abstract String getExpectedSuccessJson();

        /**
         * Builds a map of {@link MotorData} class to corresponding {@link MotorApplicationMap} class.  This is done by
         * finding all {@link MotorApplicationMap} type declarations, the interrogating the generics to find the
         * associated {@link MotorData} type.
         */
        private static Map<Class<?>, Class<?>> buildDataToMapMapping() {
            @SuppressWarnings("unchecked")
            final Function<Class<?>, Class<? extends MotorData>> findMotorDataClass =
                    mapClass -> (Class<? extends MotorData>) ArrayUtils.get(
                            GenericTypeResolver.resolveTypeArguments(mapClass, MotorApplicationMap.class), 0);
            final var provider = new ClassPathScanningCandidateComponentProvider(false);
            provider.addIncludeFilter(new AssignableTypeFilter(MotorApplicationMap.class));
            return provider.findCandidateComponents(EnableMotor.class.getPackageName()).stream()
                    .map(BeanDefinition::getBeanClassName)
                    .filter(Objects::nonNull)
                    // ignore the abstract MotorApplicationMap class in favor of concrete implementations
                    .filter(className -> !Objects.equals(className, MotorApplicationMap.class.getTypeName()))
                    .map(className -> ClassUtils.resolveClassName(className, null))
                    .collect(Collectors.toMap(findMotorDataClass, Function.identity()));
        }

        @Test
        @DisplayName("Should return an empty list when not found")
        void testNotFound() throws Exception {
            performRequest(null, List.of(vehicleToEngineConfigId))
                    .andExpect(status().isOk())
                    .andExpect(content().json("[]"));
        }

        @Test
        @DisplayName("Should trigger a bad request if vehicleToEngineConfigId is not an integer")
        void testBadRequest() throws Exception {
            final String badVehicleToEngineConfigId = "123ABC";
            performRequest(null, List.of(badVehicleToEngineConfigId))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return data when found")
        <D2 extends MotorData, M2 extends MotorApplicationMap<D2, A>, A extends MotorApplication<D2>> void testSuccess() throws Exception {
            @SuppressWarnings("unchecked")
            final var dataClass = (Class<D2>)
                    Objects.requireNonNull(
                            GenericTypeResolver.resolveTypeArgument(this.getClass(), AbstractMotorDataTest.class),
                            "Could not find MotorData from annotation on AbstractMotorDataTest");
            @SuppressWarnings("unchecked")
            final var mapClass = (Class<M2>) Objects.requireNonNull(DATA_TO_MAP.get(dataClass),
                    () -> "Could not interrogate generics to find MotorApplicationMap class on " + dataClass.getTypeName());
            //noinspection unchecked
            when(mockMotorDataService.findMotorData(
                    eq(mapClass),
                    isA(VehicleToEngineConfigAces.class),
                    isNull()))
                    .thenReturn((List<D2>) createMockData());

            performRequest(null, List.of(vehicleToEngineConfigId))
                    .andExpect(status().isOk())
                    .andExpect(content().json(getExpectedSuccessJson()));
        }

    }

}
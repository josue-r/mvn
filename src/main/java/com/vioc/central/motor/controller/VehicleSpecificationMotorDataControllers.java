package com.vioc.central.motor.controller;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.web.servlet.function.RouterFunctions.*;
import static org.springframework.web.servlet.function.ServerResponse.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.vioc.pos.vcdb.external.domain.DrainPlugMap;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Sort;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import com.google.common.base.CaseFormat;
import com.vioc.central.motor.repository.VehicleToEngineConfigAcesRepository;
import com.vioc.core.web.request.error.domain.ApiErrorResponse;
import com.vioc.pos.vcdb.aces.domain.VehicleToEngineConfigAces;
import com.vioc.pos.vcdb.motor.MotorApplication;
import com.vioc.pos.vcdb.motor.MotorApplicationMap;
import com.vioc.pos.vcdb.motor.MotorData;
import com.vioc.pos.vcdb.motor.quicklube.domain.EngineDrainPlugTorqueMap;
import com.vioc.pos.vcdb.motor.quicklube.domain.FinalDriveTorqueMap;
import com.vioc.pos.vcdb.motor.quicklube.domain.ManualTransmissionTorqueMap;
import com.vioc.pos.vcdb.motor.quicklube.domain.OilFilterTorqueMap;
import com.vioc.pos.vcdb.motor.quicklube.domain.TransferCaseTorqueMap;
import com.vioc.pos.vcdb.service.MotorDataService;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

/**
 * Builds functional style controllers for data that is fetched using
 * {@link MotorDataService#findMotorData(Class, VehicleToEngineConfigAces, Sort)}.
 * <p>
 * Further data can be added to this method without having to create new methods.
 * {@snippet :
 *     @Bean
 *     public RouterFunction<ServerResponse> motorDataRoutes() {
 *         return route()
 *                 .add(motorDataFunction(OilFilterTorqueMap.class))
 *                 .add(motorDataFunction(EngineDrainPlugTorqueMap.class))
 *                 .build();
 *     }
 *} Swagger doc will be generated automatically by this code via
 * {@link #vehicleSpecificationControllersOpenApiCustomizer()}.
 */
@Configuration
public class VehicleSpecificationMotorDataControllers implements DefaultApiResponses {

    private static final String ID_VARIABLE_NAME = "vehicleToEngineConfigId";

    @Inject
    private MotorDataService motorDataService;

    @Inject
    private VehicleToEngineConfigAcesRepository vehicleToEngineConfigAcesRepository;

    @Inject
    private ConversionService conversionService;

    /**
     * Map of api paths to the data type they return.  This facilitates building the swagger docs.
     */
    private final Map<String, Class<? extends MotorData>> pathToMotorDataType = new LinkedHashMap<>();

    @Bean
    public RouterFunction<ServerResponse> motorDataRoutes() {
        return route()
                .add(motorDataFunction(EngineDrainPlugTorqueMap.class))
                .add(motorDataFunction(FinalDriveTorqueMap.class))
                .add(motorDataFunction(ManualTransmissionTorqueMap.class))
                .add(motorDataFunction(OilFilterTorqueMap.class))
                .add(motorDataFunction(TransferCaseTorqueMap.class))
                .add(motorDataFunction(DrainPlugMap.class))
                .build();
    }

    /**
     * Builds the swagger endpoints for this path. This depends on {@link #pathToMotorDataType}, which is populated by
     * {@link #motorDataRoutes()}
     */
    @Bean
    @DependsOn("motorDataRoutes")
    public OpenApiCustomizer vehicleSpecificationControllersOpenApiCustomizer() {
        return openApi ->
                pathToMotorDataType.forEach((subPath, motorDataType) -> {
                    // build the schema and related schema from the type
                    final ResolvedSchema motorDataTypeResolvedSchema = ModelConverters.getInstance()
                            .readAllAsResolvedSchema(motorDataType);
                    final var motorDataTypeSchema = motorDataTypeResolvedSchema.schema;

                    // build the default OK response
                    final var okResponse = new ApiResponse()
                            .description(OK.getReasonPhrase())
                            .content(new Content()
                                    .addMediaType(APPLICATION_JSON_VALUE,
                                            new MediaType()
                                                    .schema(new ArraySchema().items(
                                                            new Schema<>().$ref(motorDataTypeSchema.getName())))));
                    // Create the wrapper for the various responses.  Initially, this will just contain the OK response.
                    //  Error status responses will be added shortly
                    final var responses = new ApiResponses()
                            .addApiResponse(String.valueOf(OK.value()), okResponse);

                    // Add error handling responses
                    final ResolvedSchema apiErrorResponseResolvedSchema = ModelConverters.getInstance()
                            .readAllAsResolvedSchema(ApiErrorResponse.class);
                    // Note; Ideally, we should look at ApiResponses and merge the responses but this is way more difficult
                    //  than it sounds and the spring-doc api is undocumented (at the library level) and not intuitive.
                    // We could parse tha annotations programmatically, but it would be complex and difficult to support
                    Stream.of(NOT_FOUND, FORBIDDEN, BAD_REQUEST, INTERNAL_SERVER_ERROR)
                            .forEach(status -> {
                                // Build the error response 
                                final var errorResponseSchema = apiErrorResponseResolvedSchema.schema;
                                final var errorResponseRelatedSchemas = apiErrorResponseResolvedSchema.referencedSchemas;
                                final var errorApiResponse = new ApiResponse()
                                        .description(status.getReasonPhrase())
                                        .content(new Content()
                                                .addMediaType(APPLICATION_JSON_VALUE,
                                                        new MediaType().schema(new ObjectSchema().$ref(
                                                                errorResponseSchema.getName()))));

                                // Add it to the responses for this specific path
                                responses.addApiResponse(String.valueOf(status.value()), errorApiResponse);

                                // Then ensure that the error response schemas are present. 
                                openApi.getComponents().getSchemas()
                                        .put(errorResponseSchema.getName(), errorResponseSchema);
                                openApi.getComponents().getSchemas().putAll(errorResponseRelatedSchemas);
                            });

                    // finally, register the path along with all of its response
                    final var responsePathItem = new PathItem()
                            .get(new Operation()
                                    .operationId(subPath)
                                    .parameters(List.of(
                                            new Parameter()
                                                    .name(ID_VARIABLE_NAME)
                                                    .in(ParameterIn.PATH.toString())
                                                    .required(true)
                                                    .schema(new IntegerSchema())
                                    ))
                                    .responses(responses));
                    openApi.getPaths().addPathItem(subPath, responsePathItem);

                    // and the main schema
                    openApi.getComponents().getSchemas()
                            .put(motorDataTypeSchema.getName(), motorDataTypeSchema);

                    // and any related schemas
                    openApi.getComponents().getSchemas().putAll(motorDataTypeResolvedSchema.referencedSchemas);
                });
    }

    /**
     * Build the {@link RouterFunction} with the path generated by convention (kebab-casing the class name).
     */
    private <D extends MotorData, A extends MotorApplication<D>, M extends MotorApplicationMap<D, A>> RouterFunction<ServerResponse> motorDataFunction(
            Class<M> mapClass) {
        final var simpleName = mapClass.getSimpleName();
        final var mapSuffixTrimmed = simpleName.substring(0, simpleName.length() - 3);
        final var kebabCased = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, mapSuffixTrimmed);
        return motorDataFunction(kebabCased, mapClass);
    }

    /**
     * Build the {@link RouterFunction} with a specific the path to return a list of the passed data type.
     */
    private <D extends MotorData, A extends MotorApplication<D>, M extends MotorApplicationMap<D, A>> RouterFunction<ServerResponse> motorDataFunction(
            String subPath, Class<M> mapClass) {
        final var path = "/v1/vehicle-specifications/{" + ID_VARIABLE_NAME + "}/" + subPath;

        final var motorDataType = getMotorDataType(mapClass);
        pathToMotorDataType.put(path, motorDataType);

        return RouterFunctions.route(RequestPredicates.GET(path), req -> {
            final var vehicleToEngineConfigId = extractVehicleToEngineConfigId(req);
            final var vehicleToEngineConfig = vehicleToEngineConfigAcesRepository.getReferenceById(
                    vehicleToEngineConfigId);
            final List<D> data = motorDataService.findMotorData(mapClass, vehicleToEngineConfig, null);
            return ok().body(data);
        });
    }

    @SuppressWarnings("unchecked")
    private static <D extends MotorData, A extends MotorApplication<D>, M extends MotorApplicationMap<D, A>> Class<D> getMotorDataType(
            Class<M> mapClass) {
        return (Class<D>) Objects.requireNonNull(GenericTypeResolver.resolveTypeArguments(mapClass,
                        MotorApplicationMap.class),
                () -> "Could not determine MotorData type from " + mapClass.getTypeName())[0];
    }

    private int extractVehicleToEngineConfigId(ServerRequest req) {
        final int vehicleToEngineConfigId;
        final var idPathVariableAsString = req.pathVariable(ID_VARIABLE_NAME);
        try {
            vehicleToEngineConfigId = Objects.requireNonNull(
                    conversionService.convert(idPathVariableAsString, Integer.class));
        } catch (ConversionFailedException e) {
            // Wrap in the equivalent exception handled by ApiGlobalExceptionHandler 
            throw new TypeMismatchException(idPathVariableAsString, Integer.class, e);
        }
        return vehicleToEngineConfigId;
    }

}

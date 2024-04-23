package com.vioc.central.motor.controller;

import java.util.List;
import java.util.Optional;

import jakarta.validation.constraints.NotEmpty;
import javax.inject.Inject;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vioc.central.motor.configuration.PartsCategoryMappingProperties;
import com.vioc.central.motor.projections.VehicleSpecificationProjection;
import com.vioc.central.motor.repository.VehicleToEngineConfigAcesRepository;
import com.vioc.core.web.projections.Projections;
import com.vioc.pos.vcdb.aces.domain.VehicleToEngineConfigAces;
import com.vioc.pos.vcdb.motor.parts.MotorPart;
import com.vioc.pos.vcdb.service.MotorDataService;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


@Tag(name = "Vehicle", description = "Retrieves vehicle specification for the given vehicleToEngineConfigId")
@RestController
@EnableConfigurationProperties(PartsCategoryMappingProperties.class)
@RequestMapping(path = "/v1/vehicle-specifications")
public class VehicleSpecificationController implements DefaultApiResponses {

    @Inject
    private VehicleToEngineConfigAcesRepository vehicleToEngineConfigAcesRepository;

    @Inject
    private MotorDataService motorDataService;

    @Inject
    private Projections projections;

    @Inject
    private PartsCategoryMappingProperties partsCategoryMappingProperties;

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = VehicleSpecificationProjection.class)
                            )
                    )
            )
    })
    @GetMapping("/{id}")
    public Optional<VehicleSpecificationProjection> getVehicleSpecifications(@PathVariable Integer id ) {
        Optional<VehicleToEngineConfigAces>  vehicleToEngineConfigAces = vehicleToEngineConfigAcesRepository.findById(id);
        return vehicleToEngineConfigAces.map(projections.mapTo(VehicleSpecificationProjection.class));
    }

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = MotorPart.class)
                            )
                    )
            )
    })
    @Validated
    @GetMapping("/{vehicleToEngineConfigId}/parts/{productCategoryCode}")
    public List<MotorPart> getPartsSpecifications(
            @PathVariable("vehicleToEngineConfigId") Integer vehicleToEngineConfigId,
            @PathVariable("productCategoryCode") @NotEmpty String productCategoryCode) {

        final var parts = partsCategoryMappingProperties.getParts().getOrDefault(productCategoryCode, List.of());
        return motorDataService.findPartsByEngineAndMotorCategory(vehicleToEngineConfigAcesRepository
                .getReferenceById(vehicleToEngineConfigId), parts);

    }

}

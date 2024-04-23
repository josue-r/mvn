package com.vioc.central.motor.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vioc.central.motor.projections.VehicleToEngineConfigAcesProjection;
import com.vioc.core.web.projections.Projections;
import com.vioc.pos.vcdb.service.MotorDataService;
import com.vioc.pos.vcdb.service.vin.InvalidVinException;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;

@Tag(name = "Motor", description = "Retrieves vehicle engine configuration for the given vin")
@RestController
@RequestMapping(path = "/v1/vehicles")
public class MotorController implements DefaultApiResponses {

    @Inject
    private Projections projections;

    @Inject
    private MotorDataService motorDataService;

    /**
     * As of now, this method is not used. Leaving in for future use
     */
    // Not secured at the role level. Just requires authentication.
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = VehicleToEngineConfigAcesProjection.class)
                            )
                    )
            )
    })
    @GetMapping(params = { "vin" })
    public List<VehicleToEngineConfigAcesProjection> findVehicleToEngineConfigs(@RequestParam("vin") String vin)
            throws InvalidVinException {
        return motorDataService.findVehicleToEngineConfigs(vin).stream()
                .map(projections.mapTo(VehicleToEngineConfigAcesProjection.class))
                .toList();
    }

}
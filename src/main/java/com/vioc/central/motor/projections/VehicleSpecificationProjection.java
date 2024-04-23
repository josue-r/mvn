package com.vioc.central.motor.projections;

import org.springframework.beans.factory.annotation.Value;

public interface VehicleSpecificationProjection {

    @Value("#{target.id}")
    Integer getVehicleToEngineConfigId();

    @Value("#{target.vehicle.baseVehicle.year.id}")
    Integer getYear();

    @Value("#{target.vehicle.baseVehicle.model.modelName}")
    String getModel();

    @Value("#{target.vehicle.baseVehicle.make.makeName}")
    String getMakeName();

    @Value("#{target.getTrimDescription}")
    String getDescription();

    @Value("#{target.vehicle.baseVehicle.getDisplayString}")
    String getEngine();

    @Value("#{target.getDisplayString}")
    String getDisplayText();

}
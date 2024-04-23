package com.vioc.central.motor.projections;

import com.vioc.core.data.domain.Identified;

public interface VehicleToEngineConfigAcesProjection extends Identified<Integer> {

    VehicleAcesProjection getVehicle();

    EngineConfigAcesProjection getEngineConfig();

    interface VehicleAcesProjection extends Identified<Integer> {

    }

    interface EngineConfigAcesProjection extends Identified<Integer> {

    }

}
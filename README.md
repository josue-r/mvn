# Introduction

This app is intended to provide access to the `vioc_vcdb_motor` database

It can either be run in AWS (default) or at the store (`store` profile). The `local` profile (activated by default) is
intended to behave like the store.

## AWS

- This is the default configuration
- Redis is used as a cache provider. This cache is configured as documented in `vioc-core-microservice`
- All endpoints require authentication
- The AWS `vioc_vcdb_motor` database is used

## Store

- Activated with the `store` profile.
- Caffeine is used as the cache provider. Configuration is done via the `application.conf`
- All endpoints are unsecured
- The local `vioc_vcdb_motor` database is used
- Database credentials must be configured via environment variables or other means
  - `spring.datasource.username`
  - `spring.datasource.password`

# Developer Notes

## ACES Data

Fetching ACES data should be done in the standard annotation-based controller pattern (`@RestController`
and `@GetMapping`), in the `VehicleSpecificationController`

## Motor Data

Most motor data is fetched in a very generic way, via `MotorDataService`. Implementing this with the annotation based
approach is going to be very verbose, with the only differences between the type parameter passed to the service and the
return type. We'll likely eventually have over 60 of these boilerplate controller methods.

To make this cleaner, we use the functional controller
approach ([WebMvc.fn](https://docs.spring.io/spring-framework/reference/web/webmvc-functional.html))
in `VehicleSpecificationMotorDataControllers`. This allows us to add a single line of code to provide access to the
data.

For example, this code serves up 4 endpoints based on convention:

 ```java

@Bean
public RouterFunction<ServerResponse> motorDataRoutes() {
  return route()
          .add(motorDataFunction(EngineDrainPlugTorqueMap.class))
          .add(motorDataFunction(FinalDriveTorqueMap.class))
          .add(motorDataFunction(ManualTransmissionTorqueMap.class))
          .add(motorDataFunction(OilFilterTorqueMap.class))
          .build();
}
 ```

- `/v1/vehicle-specifications/{vehicleToEngineConfigId}/engine-drain-plug-torque`
- `/v1/vehicle-specifications/{vehicleToEngineConfigId}/final-drive-torque`
- `/v1/vehicle-specifications/{vehicleToEngineConfigId}/manual-transmission-torque`
- `/v1/vehicle-specifications/{vehicleToEngineConfigId}/oil-filter-torque`

Since these endpoints are so similar, we can test things in a more generic way as well. To add a new test, all we have
to do is:

- Add a new class to `VehicleSpecificationMotorDataControllersTest` that extends `AbstractMotorDataTest`, defining the
  type of data that we're fetching in the generic
- add the `@MotorDataTest` annotation with the expected uri
- implement the `createMockData()` method to define the data to return
- implement the `getExpectedSuccessJson()` to define the expected json that `createMockData()` with serialize to

Example:

```java

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
```

This will generate the following tests:
![image](https://github.com/valvoline-llc/vioc-central-api-motor/assets/83016568/33265259-a253-480f-b03c-f8f6ecf728fd)
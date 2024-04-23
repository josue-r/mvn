package com.vioc.central.motor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.vioc.core.test.StandardIntegrationTestInMemory;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@StandardIntegrationTestInMemory(classes = MotorApiApplication.class)
public @interface IntegrationTestInMemory {
}

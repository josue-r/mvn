package com.vioc.central.motor.configuration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.vioc.pos.vcdb.motor.ProductCategoryTableMap;

@ConfigurationProperties("vioc.motor")
public class PartsCategoryMappingProperties {
    private final Map<String, List<ProductCategoryTableMap>> partsCateogryMap = new LinkedHashMap<>();

    @Bean
    public Map<String, List<ProductCategoryTableMap>> getParts() {
        return partsCateogryMap;
    }
}

package com.vioc.central.motor;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Import;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.vioc.core.autoconfigure.converter.ConverterRegistrar;
import com.vioc.core.autoconfigure.cors.ViocCorsConfiguration;
import com.vioc.core.data.domain.Identified;
import com.vioc.core.data.repository.CentralJpaRepositoryFactoryBean;
import com.vioc.core.messaging.cache.CacheDirtyEventConsumerSupplier;
import com.vioc.core.security.config.EnableCentralApiSecurity;
import com.vioc.core.web.EnableCentralProjections;
import com.vioc.core.web.EnableCentralStandardRestComponents;
import com.vioc.pos.vcdb.EnableMotor;

@SpringBootApplication
@EnableJpaAuditing
@EnableCentralApiSecurity
@EnableCentralProjections
@EnableCentralStandardRestComponents
@EnableJpaRepositories(repositoryFactoryBeanClass = CentralJpaRepositoryFactoryBean.class)
@Import({ ViocCorsConfiguration.class, ConverterRegistrar.class })
@EnableMotor
public class MotorApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MotorApiApplication.class, args);
    }

    /**
     * Setup not to evict the cached entities, because data doesn't change.
     */
    @Bean
    CacheDirtyEventConsumerSupplier cacheEventConsumerSupplier(CacheManager cacheManager) {
        final var cacheEventConsumerSupplier = CacheDirtyEventConsumerSupplier.factory();
        findCachedEntityClasses().forEach(cacheEventConsumerSupplier::withNoEviction);
        return cacheEventConsumerSupplier;
    }

    protected static <T extends Serializable> Collection<Class<? extends Identified<T>>> findCachedEntityClasses() {
        //used to fetch all the cached entities.
        Collection<Class<? extends Identified<T>>> classes = new LinkedHashSet<>();
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Cache.class));
        Set<BeanDefinition> beanDefs = scanner.findCandidateComponents("com.vioc");
        for (BeanDefinition bd : beanDefs) {
            try {
                classes.add((Class<? extends Identified<T>>) Class.forName(bd.getBeanClassName()));

            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Can't load class: " + bd.getBeanClassName(), e);
            }
        }
        return classes;
    }

}

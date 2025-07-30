package pl.kurs.test3roz.config;

import com.fasterxml.jackson.databind.jsontype.NamedType;
import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.kurs.test3roz.commands.CreatePersonCommand;
import pl.kurs.test3roz.filters.PersonFilterExtension;
import pl.kurs.test3roz.models.PersonType;
import pl.kurs.test3roz.repositories.specifications.PersonSummaryViewSpecifications;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Configuration
public class BeansConfig {

    @Autowired
    private List<PersonFilterExtension> extensions;


    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper;
    }

    @Bean
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(1);
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer registerSubtypesDynamically() {
        return builder -> builder.postConfigurer(mapper -> {
            Reflections reflections = new Reflections(
                    new ConfigurationBuilder()
                            .forPackage("")
                            .addScanners(Scanners.TypesAnnotated)
            );

            Set<Class<?>> subtypes = reflections.getTypesAnnotatedWith(PersonType.class);

            for (Class<?> subtype : subtypes) {
                PersonType annotation = subtype.getAnnotation(PersonType.class);
                if (annotation != null) {
                    mapper.registerSubtypes(new NamedType(subtype, annotation.value()));
                }
            }
        });
    }

    @Bean
    public Map<String, Class<? extends CreatePersonCommand>> personCommandTypeMap() {
        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .forPackage("")
                        .addScanners(Scanners.TypesAnnotated)
        );

        Set<Class<?>> subtypes = reflections.getTypesAnnotatedWith(PersonType.class);

        Map<String, Class<? extends CreatePersonCommand>> map = new HashMap<>();
        for (Class<?> subtype : subtypes) {
            PersonType annotation = subtype.getAnnotation(PersonType.class);
            if (annotation != null) {
                map.put(annotation.value(), (Class<? extends CreatePersonCommand>) subtype);
            }
        }

        return map;
    }
    @PostConstruct
    public void wireExtensions() {
        Map<String, PersonFilterExtension> map = extensions.stream()
                .collect(Collectors.toMap(
                        e -> {
                            Class<?> targetClass = AopProxyUtils.ultimateTargetClass(e);
                            PersonType pt = targetClass.getAnnotation(PersonType.class);
                            if (pt == null)
                                throw new IllegalStateException("Missing @PersonType on " + targetClass);
                            return pt.value().toUpperCase();
                        },
                        e -> e
                ));

        PersonSummaryViewSpecifications.setExtensions(map);
    }
}
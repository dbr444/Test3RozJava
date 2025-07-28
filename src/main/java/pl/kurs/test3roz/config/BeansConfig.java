package pl.kurs.test3roz.config;

import com.fasterxml.jackson.databind.jsontype.NamedType;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.reflections.Reflections;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.kurs.test3roz.commands.CreatePersonCommand;
import pl.kurs.test3roz.models.PersonType;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Configuration
public class BeansConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper;
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer registerSubtypesDynamically() {
        return builder -> builder.postConfigurer(mapper -> {
            Reflections reflections = new Reflections("pl.kurs.test3roz.commands");
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
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(1);
    }

    @Bean
    public Map<String, Class<? extends CreatePersonCommand>> personCommandTypeMap() {
        Reflections reflections = new Reflections("pl.kurs.test3roz.commands");
        return reflections.getSubTypesOf(CreatePersonCommand.class).stream()
                .filter(c -> c.isAnnotationPresent(PersonType.class))
                .collect(Collectors.toMap(
                        c -> c.getAnnotation(PersonType.class).value().toUpperCase(),
                        c -> c
                ));
    }
}
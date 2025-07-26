package pl.kurs.test3roz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@SpringBootApplication
@ComponentScan("pl.kurs")
public class Test3rozApplication {

    public static void main(String[] args) {
        SpringApplication.run(Test3rozApplication.class, args);
    }

}

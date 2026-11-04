package ua.foxminded.carrestservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;

@SpringBootApplication
@EnableSpringDataWebSupport(
        pageSerializationMode = PageSerializationMode.VIA_DTO
)
public class CarRestServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CarRestServiceApplication.class, args);
	}

}

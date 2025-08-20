package eva.platzda.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PlatzdaApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlatzdaApplication.class, args);
	}

}

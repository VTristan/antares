package vt.project.antares.ntiers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AntaresApplication {

	private static final Logger logger = LoggerFactory.getLogger(AntaresApplication.class);


	static void main(String[] args) {
		SpringApplication.run(AntaresApplication.class, args);
	}
}

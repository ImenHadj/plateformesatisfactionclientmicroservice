package satisfactionclient.reclamation_service.reclamation_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients

public class ReclamationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReclamationServiceApplication.class, args);
	}

}

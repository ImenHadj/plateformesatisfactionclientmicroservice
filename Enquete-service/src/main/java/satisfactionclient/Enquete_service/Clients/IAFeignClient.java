package satisfactionclient.Enquete_service.Clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import satisfactionclient.Enquete_service.Dto.EnqueteIARequest;
import satisfactionclient.Enquete_service.Dto.EnqueteIAResponse;

@FeignClient(name = "ia-service", url = "http://localhost:8000") // URL directe !

public interface IAFeignClient {

    @PostMapping("/generate-questions")
    EnqueteIAResponse generateQuestions(@RequestBody EnqueteIARequest request);
}


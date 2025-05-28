package satisfactionclient.reclamation_service.reclamation_service.Clients;


import feign.Headers;
import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import satisfactionclient.reclamation_service.reclamation_service.Dtos.UserDto;

import java.util.List;

@FeignClient(name = "USER-SERVICE")
public interface UserServiceClient {



    @GetMapping("/api/auth/role/{role}")
    List<UserDto> getUsersByRole(@PathVariable String role);
    @GetMapping("/api/auth/users/{id}")
    UserDto getUserById(@PathVariable("id") Long id);

}


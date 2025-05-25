package satisfactionclient.reclamation_service.reclamation_service.Clients;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import satisfactionclient.reclamation_service.reclamation_service.Dtos.UserDto;

import java.util.List;

@FeignClient(name = "USER-SERVICE", url = "http://localhost:8081")
public interface UserServiceClient {



    @GetMapping("/api/auth/role/{role}")
    List<UserDto> getUsersByRole(@PathVariable String role);
    @GetMapping("/api/auth/users/{id}")
    UserDto getUserById(@PathVariable("id") Long id);
}

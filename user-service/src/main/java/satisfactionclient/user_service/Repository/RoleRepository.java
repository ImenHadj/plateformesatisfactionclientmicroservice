package satisfactionclient.user_service.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import satisfactionclient.user_service.Entity.ERole;
import satisfactionclient.user_service.Entity.Role;


import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}

package satisfactionclient.user_service.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import satisfactionclient.user_service.Entity.ERole;
import satisfactionclient.user_service.Entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);



    User findByEmail(String email);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    public List<User> findByRoles_Name(ERole role);
}

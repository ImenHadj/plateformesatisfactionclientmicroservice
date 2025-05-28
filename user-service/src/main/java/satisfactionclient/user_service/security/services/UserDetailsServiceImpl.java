package satisfactionclient.user_service.security.services;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import satisfactionclient.user_service.Entity.User;
import satisfactionclient.user_service.Repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired  // injection via le constructeur
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {
        // 1. Essayer de trouver par username (email, login)
        return userRepository.findByUsername(input)
                .map(UserDetailsImpl::build)
                .orElseGet(() -> {
                    try {
                        // 2. Sinon, essayer par ID (convertir input en Long)
                        Long userId = Long.parseLong(input);
                        User user = userRepository.findById(userId)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID or username: " + input));
                        return UserDetailsImpl.build(user);
                    } catch (NumberFormatException e) {
                        throw new UsernameNotFoundException("Invalid input (not a username or ID): " + input);
                    }
                });
    }

}

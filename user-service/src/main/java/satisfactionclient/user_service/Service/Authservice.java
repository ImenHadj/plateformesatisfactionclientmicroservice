package satisfactionclient.user_service.Service;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import satisfactionclient.user_service.Dtos.UserDto;
import satisfactionclient.user_service.Entity.ERole;
import satisfactionclient.user_service.Entity.Role;
import satisfactionclient.user_service.Entity.User;
import satisfactionclient.user_service.Repository.RoleRepository;
import satisfactionclient.user_service.Repository.UserRepository;
import satisfactionclient.user_service.payload.request.LoginRequest;
import satisfactionclient.user_service.payload.request.SignupRequest;
import satisfactionclient.user_service.security.jwt.JwtUtil;
import satisfactionclient.user_service.security.services.UserDetailsServiceImpl;

import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class Authservice {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // 🔐 Authenticate User & Generate JWT Token
    /*public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtil.generateToken(loginRequest.getUsername());

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        // ✅ Convert Set<Role> to List<String>
        List<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        return new JwtResponse(jwt, user.getId(), user.getUsername(), user.getEmail(), roleNames);
    }*/
    /*public void authenticateUser(LoginRequest loginRequest, HttpServletResponse response) {
        // Authentification de l'utilisateur
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // Contexte de sécurité
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Génération du JWT
        String jwt = jwtUtil.generateToken(loginRequest.getUsername());

        // Création du cookie JWT
        Cookie cookie = new Cookie("jwt", jwt);
        cookie.setHttpOnly(true);  // Empêche l'accès au cookie via JavaScript
        cookie.setSecure(true);    // Doit être activé en production (HTTPS)
        cookie.setPath("/");       // Accessible sur toute l'application
        cookie.setMaxAge(24 * 60 * 60);  // Expire après 24h

        // Ajout du cookie à la réponse
        response.addCookie(cookie);
    }*/

    public String authenticateUser(LoginRequest loginRequest, HttpServletResponse response) {
        try {
            System.out.println("Tentative de connexion avec username: " + loginRequest.getUsername());

            // Authentifie l'utilisateur
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // Stocke l'utilisateur authentifié dans le contexte de sécurité
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Récupération de l'utilisateur depuis la base de données
            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // Récupération des rôles (ex : ["ROLE_ADMIN", "ROLE_USER"])
            List<String> roles = user.getRoles().stream()
                    .map(role -> role.getName().name()) // ✅ ERole → String
                    .collect(Collectors.toList());

            String jwt = jwtUtil.generateToken(user.getId(), user.getUsername(), roles);

            // Ajoute un cookie d'information utilisateur (facultatif)
            String userInfo = "id=" + user.getId() + "&username=" + user.getUsername();
            String encodedUserInfo = URLEncoder.encode(userInfo, "UTF-8");

            Cookie userCookie = new Cookie("user_info", encodedUserInfo);
            userCookie.setHttpOnly(true);
            userCookie.setSecure(false); // ➕ Met à true si HTTPS
            userCookie.setPath("/");
            userCookie.setMaxAge(24 * 60 * 60);
            response.addCookie(userCookie);

            // Ajoute le JWT dans un cookie sécurisé
            Cookie jwtCookie = new Cookie("jwt", jwt);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(false); // ➕ Met à true si HTTPS
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(24 * 60 * 60);
            response.addCookie(jwtCookie);

            // Renvoie le JWT en réponse (si tu veux l’utiliser côté frontend)
            return jwt;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur d'authentification");
        }
    }




    private boolean isSecureEnvironment(HttpServletRequest request) {
        return "https".equalsIgnoreCase(request.getScheme());
    }


    // 📝 Register New User
    public String registerUser(SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return "Error: Username is already taken!";
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return "Error: Email is already in use!";
        }

        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            // Assigning a default role if none is provided
            Role userRole = roleRepository.findByName(ERole.ROLE_Client)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                Role foundRole = roleRepository.findByName(ERole.valueOf(role))
                        .orElseThrow(() -> new RuntimeException("Error: Role not found."));
                roles.add(foundRole);
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return "User registered successfully!";
    }

    public String resetPassword(String email, String newPassword) {
        // Vérifier si l'utilisateur existe avec cet email
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("Utilisateur non trouvé avec cet email.");
        }

        // Mettre à jour le mot de passe de l'utilisateur
        user.setPassword(passwordEncoder.encode(newPassword));  // N'oubliez pas de crypter le mot de passe
        userRepository.save(user);  // Enregistrer les changements dans la base de données

        return "Mot de passe réinitialisé avec succès.";
    }

    public List<User> getUsersByRole(ERole role) {
        return userRepository.findByRoles_Name(role); // Utilise ERole au lieu de String
    }



    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public UserDto createUser(UserDto dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode("default123")); // ou dto.getPassword() si tu veux gérer un champ password
        user.setActive(dto.isActive());

        Role role = roleRepository.findByName(ERole.valueOf(dto.getRole()))
                .orElseThrow(() -> new RuntimeException("Rôle introuvable"));
        user.setRoles(Set.of(role));

        return convertToDto(userRepository.save(user));
    }

    public UserDto updateUser(Long id, UserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setActive(dto.isActive());

        Role role = roleRepository.findByName(ERole.valueOf(dto.getRole()))
                .orElseThrow(() -> new RuntimeException("Rôle introuvable"));
        Set<Role> newRoles = new HashSet<>();
        newRoles.add(role);
        user.setRoles(newRoles);
        user.setActive(dto.isActive()); // ✅ Ne pas oublier

        return convertToDto(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public UserDto convertToDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setActive(user.getActive() != null && user.getActive());


        // ✅ Convertir les rôles (on prend le premier pour simplifier, sinon on peut en mettre plusieurs)
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            String rolesString = user.getRoles().stream()
                    .map(role -> role.getName().name()) // getName() retourne ERole
                    .findFirst()
                    .orElse("ROLE_ADMIN");  // ou une valeur par défaut
            userDto.setRole(rolesString);
        }

        return userDto;
    }
}

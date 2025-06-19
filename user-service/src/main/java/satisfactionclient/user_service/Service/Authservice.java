package satisfactionclient.user_service.Service;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.time.LocalDateTime;
import java.util.*;
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
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);


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

        // Si aucun rôle n’est fourni, renvoyer une erreur
        Set<String> strRoles = signUpRequest.getRole();
        if (strRoles == null || strRoles.isEmpty()) {
            return "Error: No role provided!";
        }

        Set<Role> roles = new HashSet<>();

        for (String role : strRoles) {
            try {
                ERole enumRole = ERole.valueOf(role.trim()); // s'assurer que la casse est correcte
                Role foundRole = roleRepository.findByName(enumRole)
                        .orElseThrow(() -> new RuntimeException("Error: Role not found in DB: " + role));
                roles.add(foundRole);
            } catch (IllegalArgumentException e) {
                return "Error: Invalid role name: " + role;
            }
        }

        // Création de l’utilisateur
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
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




    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public UserDto createUser(UserDto dto) {
        System.out.println("Role reçu : " + dto.getRole());

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode("default123"));
        user.setActive(dto.isActive());

        // Trouver la valeur enum
        ERole eRole;
        try {
            eRole = ERole.valueOf(dto.getRole());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Rôle invalide reçu: " + dto.getRole());
        }

        Role role = roleRepository.findByName(eRole)
                .orElseThrow(() -> new RuntimeException("Rôle introuvable"));

        Set<Role> newRoles = new HashSet<>();
        newRoles.add(role);
        user.setRoles(newRoles);
        user.setActive(dto.isActive());

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
        userDto.setFcmToken(user.getFcmToken());


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

    public List<UserDto> getUsersByRole(String roleStr) {
        // ✅ Convertir String vers Enum
        ERole roleEnum = ERole.valueOf(roleStr); // Attention à la casse (ex: "ROLE_ADMIN")

        // ✅ Appeler le repository
        List<User> users = userRepository.findByRoles_Name(roleEnum);

        // ✅ Mapper User → UserDto
        return users.stream()
                .map(user -> {
                    UserDto dto = new UserDto();
                    dto.setId(user.getId());
                    dto.setUsername(user.getUsername());
                    dto.setEmail(user.getEmail());
                    dto.setActive(user.isActive());
                    dto.setFcmToken(user.getFcmToken());

                    // Extraire le premier rôle (enum ERole) → String
                    if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                        dto.setRole(user.getRoles().stream()
                                .map(Role::getName)
                                .map(Enum::name)
                                .findFirst()
                                .orElse(null));
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDto getUserdtoById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Force chargement des rôles
        user.getRoles().size();

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());

        // Prend le premier rôle s’il existe
        if (!user.getRoles().isEmpty()) {
            dto.setRole(user.getRoles().iterator().next().getName().name());
        }

        dto.setActive(user.isActive());
        dto.setFcmToken(user.getFcmToken());
        return dto;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }
    public long countNewUsersThisWeek() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        return userRepository.countByCreatedAtAfter(oneWeekAgo);
    }
    public Map<String, Long> countUsersByRole() {
        return userRepository.findAll().stream()
                .flatMap(user -> user.getRoles().stream())
                .map(role -> role.getName().name())
                .collect(Collectors.groupingBy(r -> r, Collectors.counting()));
    }
    public long countActiveUsers() {
        return userRepository.countByActiveTrue();
    }

    public long countInactiveUsers() {
        return userRepository.countByActiveFalse();
    }

}

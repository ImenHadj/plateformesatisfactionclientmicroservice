package satisfactionclient.Enquete_service.Service;


import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import satisfactionclient.Enquete_service.Clients.IAFeignClient;
import satisfactionclient.Enquete_service.Clients.UserServiceClient;
import satisfactionclient.Enquete_service.Dto.*;
import satisfactionclient.Enquete_service.Entity.*;
import satisfactionclient.Enquete_service.Repository.EnqueteRepository;
import satisfactionclient.Enquete_service.Repository.QuestionRepository;
import satisfactionclient.Enquete_service.Repository.ReponseRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class EnqueteService {

    @Autowired
    private EnqueteRepository enqueteRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private Emailservice emailService;
    @Autowired
    private ReponseRepository reponseRepository;
    @Autowired

    private  IAFeignClient iaFeignClient;
    @Autowired

    private RestTemplate restTemplate;

    public EnqueteService(
            EnqueteRepository enqueteRepository,
            IAFeignClient iaFeignClient,RestTemplate restTemplate
    ) {
        this.enqueteRepository = enqueteRepository;
        this.iaFeignClient = iaFeignClient;
        this.restTemplate = restTemplate;

    }


    public Enquete creerEnqueteAvecQuestionsEtOptions(String titre, String description, LocalDateTime datePublication, LocalDateTime dateExpiration, UserDto admin, List<Question> questions) {
        Enquete enquete = new Enquete();
        enquete.setTitre(titre);
        enquete.setDescription(description);
        enquete.setDateCreation(LocalDateTime.now());
        enquete.setDatePublication(datePublication);
        enquete.setDateExpiration(dateExpiration);
        enquete.setStatut(StatutEnquete.BROUILLON);
        enquete.setAdminId(admin.getId());

        if (LocalDateTime.now().isAfter(datePublication) || LocalDateTime.now().isEqual(datePublication)) {
            enquete.setStatut(StatutEnquete.PUBLIEE);
        }

        for (Question question : questions) {
            question.setEnquete(enquete);
        }

        enquete.setQuestions(questions);

        Enquete savedEnquete = enqueteRepository.save(enquete);



        return savedEnquete;
    }

    public Enquete getEnqueteWithQuestions(Long id) {
        return enqueteRepository.findById(id).map(enquete -> {
            enquete.getQuestions().forEach(question -> {
                if (question.getOptions() != null) {
                    question.getOptions().size();
                }
            });
            return enquete;
        }).orElseThrow(() -> new NoSuchElementException("Enquête non trouvée avec l'id : " + id));
    }

    // Modifier une enquête


    // Supprimer une enquête
    @Transactional
    public void deleteEnquete(Long id) {
        Enquete enquete = enqueteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enquête non trouvée"));

        // Supprimer les questions associées (si cascade pas activée)
        questionRepository.deleteAll(enquete.getQuestions());

        // Supprimer l’enquête
        enqueteRepository.delete(enquete);
    }


    public Question ajouterQuestion(Long enqueteId, String texte, List<String> options, TypeQuestion type) {
        Enquete enquete = enqueteRepository.findById(enqueteId).orElseThrow();
        Question question = new Question();
        question.setTexte(texte);
        question.setOptions(options);
        question.setType(type);
        question.setEnquete(enquete);

        return questionRepository.save(question);
    }

    public List<Enquete> getEnquetesAdmin(Long adminId) {
        return enqueteRepository.findByAdminId(adminId);
    }


    public List<Question> getQuestionsForEnquete(Long enqueteId) {
        return questionRepository.findByEnqueteId(enqueteId);
    }


    @Transactional
    public void enregistrerReponses(Long enqueteId, Long userId, List<ReponseDTO> reponsesDTO) {
        // Récupération de l'enquête
        Enquete enquete = enqueteRepository.findById(enqueteId)
                .orElseThrow(() -> new RuntimeException("Enquête non trouvée"));

        // Récupération de l'utilisateur
        UserDto utilisateur = userServiceClient.getUserById(userId);

        // Récupération des réponses et conversion de chaque DTO en entité Reponse
        List<Reponse> reponses = reponsesDTO.stream()
                .map(dto -> convertirReponse(dto, enquete, utilisateur))
                .collect(Collectors.toList());

        // Sauvegarde des réponses dans la base de données
        reponseRepository.saveAll(reponses);
    }


    private Reponse convertirReponse(ReponseDTO dto, Enquete enquete, UserDto utilisateur) {
        Question question = questionRepository.findById(dto.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question non trouvée"));

        Reponse reponse = new Reponse();
        reponse.setTypeReponse(dto.getTypeReponse());
        reponse.setEnquete(enquete);
        reponse.setUserId(utilisateur.getId());
        reponse.setQuestion(question);

        switch (dto.getTypeReponse()) {
            case TEXTE:
            case EMAIL:
            case TELEPHONE:



            case CHOIX_SIMPLE:
            case OUI_NON:
                if (dto.getChoixReponses() != null && !dto.getChoixReponses().isEmpty()) {
                    reponse.setValeursChoixFromList(List.of(dto.getChoixReponses().get(0)));
                }
                break;

            case CHOIX_MULTIPLE:
                if (dto.getChoixReponses() != null && !dto.getChoixReponses().isEmpty()) {
                    reponse.setValeursChoixFromList(dto.getChoixReponses());
                }
                break;

            case NUMERIQUE:
            case NOTE:
            case SLIDER:
            case POURCENTAGE:
            case DEVISE:
                if (dto.getValeurNumerique() != null) {
                    reponse.setValeurNumerique(dto.getValeurNumerique());
                } else if (dto.getTexteReponse() != null) {
                    try {
                        reponse.setValeurNumerique(Double.parseDouble(dto.getTexteReponse()));
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("Valeur numérique invalide : " + dto.getTexteReponse());
                    }
                }
                break;

            case DATE_HEURE:
            case DATE:
            case HEURE:
                reponse.setValeurTexte(dto.getTexteReponse());
                break;

            case FICHIER:
            case IMAGE:

            case SIGNATURE:
            case DESSIN:
                reponse.setValeurTexte(dto.getTexteReponse());
                break;


            case LOCALISATION:
                reponse.setValeurTexte(dto.getTexteReponse());
                break;

            case MATRICE:
            case CLASSEMENT:
                reponse.setValeurTexte(dto.getTexteReponse());
                break;


            case CODE_PIN:
                reponse.setValeurTexte(dto.getTexteReponse());
                break;

            default:
                throw new RuntimeException("Type de réponse non supporté : " + dto.getTypeReponse());
        }

        return reponse;
    }


    public List<EnqueteResponseDTO> getAllEnquetes() {
        List<Enquete> enquetes = enqueteRepository.findAll();

        return enquetes.stream().map(enquete -> {
            EnqueteResponseDTO dto = new EnqueteResponseDTO();
            dto.setId(enquete.getId()); // ✅ AJOUT ICI

            dto.setTitre(enquete.getTitre());
            dto.setDescription(enquete.getDescription());
            dto.setDateCreation(enquete.getDateCreation());
            dto.setDateExpiration(enquete.getDateExpiration());
            dto.setDatePublication(enquete.getDatePublication());
            dto.setStatut(enquete.getStatut());

            // Mapper les questions
            List<QuestionDTO> questions = enquete.getQuestions().stream().map(question -> {
                QuestionDTO qDto = new QuestionDTO();
                qDto.setId(question.getId());
                qDto.setTexte(question.getTexte());
                qDto.setType(question.getType());
                qDto.setOptions(question.getOptions());
                return qDto;
            }).toList();

            dto.setQuestions(questions);

            return dto;
        }).collect(Collectors.toList());
    }

    public EnqueteResponseDTO getEnqueteById(Long id) {
        Enquete enquete = enqueteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Enquête non trouvée avec l'id : " + id));

        return mapToDto(enquete);
    }

    private EnqueteResponseDTO mapToDto(Enquete enquete) {
        EnqueteResponseDTO dto = new EnqueteResponseDTO();
        dto.setId(enquete.getId());
        dto.setTitre(enquete.getTitre());
        dto.setDescription(enquete.getDescription());
        dto.setDateCreation(enquete.getDateCreation());
        dto.setDateExpiration(enquete.getDateExpiration());
        dto.setDatePublication(enquete.getDatePublication());
        dto.setStatut(enquete.getStatut()); // Directement l'enum

        dto.setQuestions(mapQuestionsToDto(enquete.getQuestions()));
        return dto;
    }

    private List<QuestionDTO> mapQuestionsToDto(List<Question> questions) {
        return questions.stream()
                .map(this::mapQuestionToDto)
                .collect(Collectors.toList());
    }

    private QuestionDTO mapQuestionToDto(Question question) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getId());
        dto.setTexte(question.getTexte());
        dto.setType(question.getType()); // Directement l'enum
        dto.setOptions(question.getOptions() != null ?
                new ArrayList<>(question.getOptions()) :
                Collections.emptyList());
        return dto;
    }
    @Transactional
    public void updateEnquete(Long id, EnqueteResponseDTO updatedDto) {
        try {
            Enquete enquete = enqueteRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Enquête non trouvée"));

            // ✅ Mise à jour des champs simples
            enquete.setTitre(updatedDto.getTitre());
            enquete.setDescription(updatedDto.getDescription());
            enquete.setDateExpiration(updatedDto.getDateExpiration());
            enquete.setDatePublication(updatedDto.getDatePublication());
            enquete.setStatut(updatedDto.getStatut());

            // ✅ Étape cruciale : supprimer les anciennes questions proprement
            List<Question> existingQuestions = new ArrayList<>(enquete.getQuestions());
            for (Question q : existingQuestions) {
                q.setEnquete(null); // rompre la liaison pour éviter l’exception Hibernate
            }
            enquete.getQuestions().clear();
            questionRepository.deleteAll(existingQuestions); // supprimer les anciennes

            // ✅ Ajouter les nouvelles questions
            List<Question> newQuestions = updatedDto.getQuestions().stream().map(qDto -> {
                Question q = new Question();
                q.setTexte(qDto.getTexte());
                q.setType(qDto.getType());
                q.setOptions(qDto.getOptions() != null ? qDto.getOptions() : new ArrayList<>());
                q.setEnquete(enquete); // important !
                return q;
            }).collect(Collectors.toList());

            enquete.getQuestions().addAll(newQuestions);

            // ✅ Sauvegarder (Hibernate va gérer les enfants avec cascade)
            enqueteRepository.save(enquete);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour de l’enquête : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur mise à jour enquête");
        }
    }
    public Enquete creerEnqueteAvecIA(String titre, String description,
                                      LocalDateTime datePublication,
                                      LocalDateTime dateExpiration,
                                      UserDto admin) {

        Enquete enquete = new Enquete();
        enquete.setTitre(titre);
        enquete.setDescription(description);
        enquete.setDateCreation(LocalDateTime.now());
        enquete.setDatePublication(datePublication);
        enquete.setDateExpiration(dateExpiration);
        enquete.setStatut(datePublication.isBefore(LocalDateTime.now())
                ? StatutEnquete.PUBLIEE
                : StatutEnquete.BROUILLON);
        enquete.setAdminId(admin.getId());

        // === Appel IA avec RestTemplate au lieu de iaFeignClient ===
        EnqueteIARequest iaRequest = new EnqueteIARequest();
        iaRequest.setTitre(titre);
        iaRequest.setDescription(description);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EnqueteIARequest> request = new HttpEntity<>(iaRequest, headers);

        String url = "http://localhost:8000/generate-questions";

        ResponseEntity<EnqueteIAResponse> response = restTemplate.postForEntity(
                url, request, EnqueteIAResponse.class);

        EnqueteIAResponse iaResponse = response.getBody();

        List<Question> questions = iaResponse.getQuestions().stream().map(qia -> {
            Question q = new Question();
            q.setTexte(qia.getQuestion());
            q.setType(mapTypeToEnum(qia.getType())); // conversion String -> Enum TypeQuestion
            q.setOptions(qia.getChoices());
            q.setEnquete(enquete);
            return q;
        }).collect(Collectors.toList());

        enquete.setQuestions(questions);
        return enqueteRepository.save(enquete);
    }

    private TypeQuestion mapTypeToEnum(String type) {
        switch (type.toLowerCase()) {
            case "question ouverte": return TypeQuestion.OUVERT;
            case "échelle de satisfaction": return TypeQuestion.LIKERT;
            case "choix simple": return TypeQuestion.CHOIX_SIMPLE;
            case "choix multiple": return TypeQuestion.CHOIX_MULTIPLE;
            case "notation": return TypeQuestion.NOTE;
            case "oui/non": return TypeQuestion.OUI_NON;
            default: return TypeQuestion.OUVERT;
        }
    }
    public List<QuestionDTO> genererQuestionsAvecIA(String titre, String description) {
        // === Préparer la requête IA ===
        EnqueteIARequest iaRequest = new EnqueteIARequest();
        iaRequest.setTitre(titre);
        iaRequest.setDescription(description);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EnqueteIARequest> request = new HttpEntity<>(iaRequest, headers);

        String url = "http://localhost:8000/generate-questions";

        // === Appel IA ===
        ResponseEntity<EnqueteIAResponse> response = restTemplate.postForEntity(
                url, request, EnqueteIAResponse.class);

        EnqueteIAResponse iaResponse = response.getBody();

        // === Convertir en DTO sans toucher à l'entité Enquete ===
        List<QuestionDTO> questions = iaResponse.getQuestions().stream().map(qia -> {
            QuestionDTO dto = new QuestionDTO();
            dto.setTexte(qia.getQuestion());
            dto.setType(mapTypeToEnum(qia.getType()));
            dto.setOptions(qia.getChoices());
            return dto;
        }).collect(Collectors.toList());

        return questions;
    }

}

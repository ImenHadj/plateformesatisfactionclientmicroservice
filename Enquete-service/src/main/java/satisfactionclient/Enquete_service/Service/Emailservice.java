package satisfactionclient.Enquete_service.Service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class Emailservice {




    @Autowired
    private JavaMailSender emailSender;



    public void sendEnqueteLink(String toEmail, String enqueteLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Participez à notre enquête");
        message.setText("Veuillez participer à notre enquête en cliquant sur le lien suivant : " + enqueteLink);
        emailSender.send(message);
    }
}

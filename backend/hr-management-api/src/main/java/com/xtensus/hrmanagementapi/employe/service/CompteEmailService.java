package com.xtensus.hrmanagementapi.employe.service;

import com.xtensus.hrmanagementapi.domain.entity.Employe;
import com.xtensus.hrmanagementapi.variable.VariableSystemeService;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class CompteEmailService {
    private static final Logger log = LoggerFactory.getLogger(CompteEmailService.class);
    private final VariableSystemeService variables;

    public CompteEmailService(VariableSystemeService variables) { this.variables = variables; }

    public void envoyerBienvenue(Employe employe, String motDePasseTemporaire) {
        try {
            JavaMailSender sender = expediteur();
            var message = sender.createMimeMessage();
            var helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(emailExpediteur()); helper.setTo(employe.getEmail());
            helper.setSubject("Bienvenue chez XTENSUS - votre compte RH");
            helper.setText(htmlBienvenue(employe, motDePasseTemporaire), true);
            sender.send(message);
        } catch (Exception ex) {
            // La creation du compte reste valide meme si le serveur SMTP est temporairement indisponible.
            log.error("Echec d'envoi de l'email de bienvenue a {}", employe.getEmail(), ex);
        }
    }

    public void envoyerCodeReinitialisation(Employe employe, String code) {
        try {
            JavaMailSender sender = expediteur();
            var message = sender.createMimeMessage();
            var helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(emailExpediteur()); helper.setTo(employe.getEmail());
            helper.setSubject("Code de reinitialisation de votre mot de passe");
            helper.setText(htmlCode(employe, code), true);
            sender.send(message);
        } catch (Exception ex) {
            log.error("Echec d'envoi du code de reinitialisation a {}", employe.getEmail(), ex);
            throw new IllegalStateException("Impossible d'envoyer l'email de reinitialisation. Verifiez la configuration SMTP.");
        }
    }

    public void envoyerHtml(String destinataire, String sujet, String contenuHtml) throws Exception {
        JavaMailSender sender = expediteur();
        var message = sender.createMimeMessage();
        var helper = new MimeMessageHelper(message, "UTF-8");
        helper.setFrom(emailExpediteur()); helper.setTo(destinataire); helper.setSubject(sujet); helper.setText(contenuHtml, true);
        sender.send(message);
    }

    private JavaMailSender expediteur() {
        String username = emailExpediteur();
        String password = variables.valeur("smtp_mot_de_passe_application", "").replace(" ", "");
        if (username.isBlank() || password.isBlank()) throw new IllegalStateException("La configuration SMTP est incomplete");
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost("smtp.gmail.com"); sender.setPort(587);
        sender.setUsername(username); sender.setPassword(password);
        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true"); props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.connectiontimeout", "10000"); props.put("mail.smtp.timeout", "10000");
        return sender;
    }

    private String emailExpediteur() { return variables.valeur("smtp_email_expediteur", "").trim(); }

    private String htmlBienvenue(Employe e, String code) {
        return """
          <div style='background:#f7f4ef;padding:32px;font-family:Arial,sans-serif;color:#3d3935'><div style='max-width:620px;margin:auto;background:white;border-radius:18px;overflow:hidden;border:1px solid #eadfd3'><div style='padding:28px;background:linear-gradient(135deg,#fff4e8,#ffe0bd)'><h1 style='margin:0;color:#bd5a00'>Bienvenue %s</h1><p>Votre espace RH est pret.</p></div><div style='padding:28px'><p><b>Identifiant :</b> %s</p><p><b>Mot de passe temporaire :</b> %s</p><p>Le changement du mot de passe sera obligatoire apres votre connexion.</p><a href='http://localhost:4200/login' style='display:inline-block;background:#f58220;color:white;text-decoration:none;padding:13px 22px;border-radius:10px;font-weight:bold'>Se connecter</a></div></div></div>
          """.formatted(e.getPrenom(), e.getUsername(), code);
    }

    private String htmlCode(Employe e, String code) {
        return """
          <div style='background:#f7f4ef;padding:32px;font-family:Arial,sans-serif;color:#3d3935'><div style='max-width:620px;margin:auto;background:white;border-radius:18px;overflow:hidden;border:1px solid #eadfd3'><div style='padding:28px;background:linear-gradient(135deg,#fff4e8,#ffe0bd)'><h1 style='margin:0;color:#bd5a00'>Reinitialisation du mot de passe</h1></div><div style='padding:28px'><p>Bonjour %s,</p><p>Voici votre code de verification XTENSUS :</p><div style='font-size:30px;letter-spacing:8px;font-weight:bold;text-align:center;background:#fff8f0;border:1px solid #f5c99e;border-radius:12px;padding:20px'>%s</div><p style='color:#6f675f'>Ce code expire dans 15 minutes. Si vous n'etes pas a l'origine de cette demande, ignorez cet email.</p></div></div></div>
          """.formatted(e.getPrenom(), code);
    }
}

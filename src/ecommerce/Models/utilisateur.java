package ecommerce.Models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Utilisateur {
    private String id;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasseHash;
    private Role role;
    private boolean actif;
    private LocalDateTime dateInscription;
    private List<Adresse> adresses;
    private String telephone;

    public Utilisateur() {
        this.id = UUID.randomUUID().toString();
        this.actif = true;
        this.dateInscription = LocalDateTime.now();
        this.adresses = new ArrayList<>();
        this.role = Role.CLIENT;
    }

    public Utilisateur(String nom, String prenom, String email, String motDePasseHash) {
        this();
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasseHash = motDePasseHash;
    }

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    public Adresse getAdressePrincipale() {
        return adresses.stream().filter(Adresse::isPrincipale).findFirst().orElse(
                adresses.isEmpty() ? null : adresses.get(0)
        );
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMotDePasseHash() { return motDePasseHash; }
    public void setMotDePasseHash(String motDePasseHash) { this.motDePasseHash = motDePasseHash; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }
    public List<Adresse> getAdresses() { return adresses; }
    public void setAdresses(List<Adresse> adresses) { this.adresses = adresses; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    @Override
    public String toString() {
        return "[" + role + "] " + getNomComplet() + " <" + email + ">";
    }
}
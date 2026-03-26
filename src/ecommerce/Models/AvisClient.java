package ecommerce.Models;

import java.time.LocalDateTime;
import java.util.UUID;

public class AvisClient {
    private String id;
    private String produitId;
    private String clientId;
    private String nomClient;
    private int note; // 1 à 5
    private String commentaire;
    private LocalDateTime dateAvis;

    public AvisClient() {
        this.id = UUID.randomUUID().toString();
        this.dateAvis = LocalDateTime.now();
    }

    public AvisClient(String produitId, String clientId, String nomClient, int note, String commentaire) {
        this();
        this.produitId = produitId;
        this.clientId = clientId;
        this.nomClient = nomClient;
        this.note = Math.max(1, Math.min(5, note));
        this.commentaire = commentaire;
    }

    public String getEtoiles() {
        return "★".repeat(note) + "☆".repeat(5 - note);
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProduitId() { return produitId; }
    public void setProduitId(String produitId) { this.produitId = produitId; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getNomClient() { return nomClient; }
    public void setNomClient(String nomClient) { this.nomClient = nomClient; }
    public int getNote() { return note; }
    public void setNote(int note) { this.note = note; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public LocalDateTime getDateAvis() { return dateAvis; }
    public void setDateAvis(LocalDateTime dateAvis) { this.dateAvis = dateAvis; }
}
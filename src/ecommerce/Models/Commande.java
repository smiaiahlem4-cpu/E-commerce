package ecommerce.Models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Commande {
    private String id;
    private String clientId;
    private List<LigneCommande> lignes;
    private double montantTotal;
    private double montantRemise;
    private StatutCommande statut;
    private LocalDateTime dateCommande;
    private LocalDateTime dateLivraison;
    private Adresse adresseLivraison;
    private String codePromo;
    private String notes;

    public Commande() {
        this.id = UUID.randomUUID().toString();
        this.dateCommande = LocalDateTime.now();
        this.statut = StatutCommande.EN_ATTENTE;
    }

    public Commande(String clientId, List<LigneCommande> lignes, Adresse adresseLivraison) {
        this();
        this.clientId = clientId;
        this.lignes = lignes;
        this.adresseLivraison = adresseLivraison;
        this.montantTotal = lignes.stream().mapToDouble(LigneCommande::getSousTotal).sum();
        this.montantRemise = 0;
    }

    public double getMontantAPayer() {
        return montantTotal - montantRemise;
    }

    public String getNumeroCommande() {
        return "CMD-" + id.substring(0, 8).toUpperCase();
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public List<LigneCommande> getLignes() { return lignes; }
    public void setLignes(List<LigneCommande> lignes) { this.lignes = lignes; }
    public double getMontantTotal() { return montantTotal; }
    public void setMontantTotal(double montantTotal) { this.montantTotal = montantTotal; }
    public double getMontantRemise() { return montantRemise; }
    public void setMontantRemise(double montantRemise) { this.montantRemise = montantRemise; }
    public StatutCommande getStatut() { return statut; }
    public void setStatut(StatutCommande statut) { this.statut = statut; }
    public LocalDateTime getDateCommande() { return dateCommande; }
    public void setDateCommande(LocalDateTime dateCommande) { this.dateCommande = dateCommande; }
    public LocalDateTime getDateLivraison() { return dateLivraison; }
    public void setDateLivraison(LocalDateTime dateLivraison) { this.dateLivraison = dateLivraison; }
    public Adresse getAdresseLivraison() { return adresseLivraison; }
    public void setAdresseLivraison(Adresse adresseLivraison) { this.adresseLivraison = adresseLivraison; }
    public String getCodePromo() { return codePromo; }
    public void setCodePromo(String codePromo) { this.codePromo = codePromo; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
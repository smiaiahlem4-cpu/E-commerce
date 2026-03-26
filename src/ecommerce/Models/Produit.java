package ecommerce.Models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Produit {
    private String id;
    private String nom;
    private String description;
    private double prix;
    private int quantiteStock;
    private String categorieId;
    private String vendeurId;
    private boolean disponible;
    private double noteMoyenne;
    private int nombreAvis;
    private LocalDateTime dateAjout;
    private String imageUrl;

    public Produit() {
        this.id = UUID.randomUUID().toString();
        this.disponible = true;
        this.dateAjout = LocalDateTime.now();
        this.noteMoyenne = 0.0;
        this.nombreAvis = 0;
    }

    public Produit(String nom, String description, double prix, int quantiteStock, String categorieId) {
        this();
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.quantiteStock = quantiteStock;
        this.categorieId = categorieId;
    }

    public boolean estEnStock() {
        return disponible && quantiteStock > 0;
    }

    public void diminuerStock(int quantite) {
        this.quantiteStock -= quantite;
        if (this.quantiteStock <= 0) {
            this.quantiteStock = 0;
        }
    }

    public void augmenterStock(int quantite) {
        this.quantiteStock += quantite;
    }

    public void mettreAJourNote(double nouvelleNote) {
        double total = this.noteMoyenne * this.nombreAvis + nouvelleNote;
        this.nombreAvis++;
        this.noteMoyenne = total / this.nombreAvis;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }
    public int getQuantiteStock() { return quantiteStock; }
    public void setQuantiteStock(int quantiteStock) { this.quantiteStock = quantiteStock; }
    public String getCategorieId() { return categorieId; }
    public void setCategorieId(String categorieId) { this.categorieId = categorieId; }
    public String getVendeurId() { return vendeurId; }
    public void setVendeurId(String vendeurId) { this.vendeurId = vendeurId; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
    public double getNoteMoyenne() { return noteMoyenne; }
    public void setNoteMoyenne(double noteMoyenne) { this.noteMoyenne = noteMoyenne; }
    public int getNombreAvis() { return nombreAvis; }
    public void setNombreAvis(int nombreAvis) { this.nombreAvis = nombreAvis; }
    public LocalDateTime getDateAjout() { return dateAjout; }
    public void setDateAjout(LocalDateTime dateAjout) { this.dateAjout = dateAjout; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @Override
    public String toString() {
        return String.format("%-30s | %.2f DT | Stock: %d | Note: %.1f/5", nom, prix, quantiteStock, noteMoyenne);
    }}
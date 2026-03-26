package ecommerce.Models;

import java.util.UUID;

public class Categorie {
    private String id;
    private String nom;
    private String description;

    public Categorie() {
        this.id = UUID.randomUUID().toString();
    }

    public Categorie(String nom, String description) {
        this();
        this.nom = nom;
        this.description = description;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() { return nom; }
}

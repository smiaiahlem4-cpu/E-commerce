package ecommerce.Models;

import java.util.UUID;

public class Adresse {
    private String id;
    private String rue;
    private String ville;
    private String codePostal;
    private String pays;
    private boolean principale;

    public Adresse() {
        this.id = UUID.randomUUID().toString();
    }

    public Adresse(String rue, String ville, String codePostal, String pays) {
        this();
        this.rue = rue;
        this.ville = ville;
        this.codePostal = codePostal;
        this.pays = pays;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRue() { return rue; }
    public void setRue(String rue) { this.rue = rue; }
    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }
    public String getCodePostal() { return codePostal; }
    public void setCodePostal(String codePostal) { this.codePostal = codePostal; }
    public String getPays() { return pays; }
    public void setPays(String pays) { this.pays = pays; }
    public boolean isPrincipale() { return principale; }
    public void setPrincipale(boolean principale) { this.principale = principale; }

    @Override
    public String toString() {
        return rue + ", " + codePostal + " " + ville + ", " + pays;
    }
}

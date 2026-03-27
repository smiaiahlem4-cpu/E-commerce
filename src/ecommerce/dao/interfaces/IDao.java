package ecommerce.dao.interfaces;
import java.util.List;
import java.util.Optional;

public interface IDao<T>{
    void ajouter(T entite);
    void modifier(T entite);
    void supprimer(String id);
    Optional<T> trouverParId(String id);
    List<T> trouverTous();
    void sauvegarder();
}

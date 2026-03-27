package ecommerce.dao.interfaces;

import ecommerce.Models.Promotion;
import java.util.Optional;

public interface IPromotionDAO extends IDao<Promotion> {
    Optional<Promotion> trouverParCode(String code);
}
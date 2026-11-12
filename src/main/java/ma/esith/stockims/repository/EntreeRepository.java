package ma.esith.stockims.repository;
import ma.esith.stockims.model.Entree;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface EntreeRepository
        extends JpaRepository<Entree, Long> {
    List<Entree> findByArticle_Id(Long articleId);
}

package ma.esith.stockims.repository;
import ma.esith.stockims.model.Article;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface ArticleRepository
        extends JpaRepository<Article, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Article a where a.id = :id")
    Optional<Article> verrouiller(@Param("id") Long id);
}
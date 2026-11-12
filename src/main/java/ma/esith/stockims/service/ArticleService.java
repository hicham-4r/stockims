package ma.esith.stockims.service;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ma.esith.stockims.model.Article;
import ma.esith.stockims.repository.ArticleRepository;
@Service @RequiredArgsConstructor
public class ArticleService {
    private final ArticleRepository repository;
    public List<Article> lister() {
        return repository.findAll();
    }
    public Article creer(String reference, String designation) {
        return repository.save(new Article(reference, designation));
    }
}

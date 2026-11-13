package ma.esith.stockims;

import ma.esith.stockims.model.Article;
import ma.esith.stockims.repository.ArticleRepository;
import ma.esith.stockims.repository.EntreeRepository;
import ma.esith.stockims.repository.SortieRepository;
import ma.esith.stockims.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class StockDeletionTests {
    @Autowired MockMvc mvc;
    @Autowired ArticleRepository articles;
    @Autowired EntreeRepository entrees;
    @Autowired SortieRepository sorties;
    @Autowired StockService stock;

    @BeforeEach
    void viderBaseDeTest() {
        sorties.deleteAllInBatch();
        entrees.deleteAllInBatch();
        articles.deleteAllInBatch();
    }

    private Article creerArticle(String reference) {
        return articles.saveAndFlush(new Article(reference, "Rouleau de coton"));
    }

    @Test
    void pagesPrincipalesEtEtatVideSontRendus() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk())
                .andExpect(content().string(containsString("/css/stockims.css")));
        mvc.perform(get("/articles")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Créer mon premier article")));
        mvc.perform(get("/articles/nouveau")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Informations de l’article")));
    }

    @Test
    void afficherLaConfirmationNeSupprimeRien() throws Exception {
        Article article = creerArticle("TIS-001");
        stock.entrer(article.getId(), 12);
        mvc.perform(get("/stock/{id}/supprimer", article.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Supprimer cet article ?")));
        assertThat(articles.existsById(article.getId())).isTrue();
        assertThat(stock.calculerStock(article.getId())).isEqualTo(12);
    }

    @Test
    void suppressionSansConfirmationEstRefusee() throws Exception {
        Article article = creerArticle("TIS-001");
        for (String confirmation : new String[] {"", "false"}) {
            mvc.perform(post("/stock/{id}/supprimer", article.getId())
                            .param("confirmation", confirmation))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/stock/" + article.getId() + "/supprimer"));
        }
        assertThat(articles.existsById(article.getId())).isTrue();
    }

    @Test
    void suppressionConfirmeeEffaceLesMouvementsEtPreserveLesAutresArticles() throws Exception {
        Article article = creerArticle("TIS-001");
        Article autre = creerArticle("TIS-002");
        stock.entrer(article.getId(), 12);
        stock.sortir(article.getId(), 3);
        stock.entrer(autre.getId(), 7);
        stock.sortir(autre.getId(), 2);

        mvc.perform(post("/stock/{id}/supprimer", article.getId()).param("confirmation", "true"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/articles"))
                .andExpect(flash().attribute("succes", "L’article et ses mouvements de stock ont été supprimés."));

        assertThat(articles.existsById(article.getId())).isFalse();
        assertThat(entrees.findByArticle_Id(article.getId())).isEmpty();
        assertThat(sorties.findByArticle_Id(article.getId())).isEmpty();
        assertThat(articles.existsById(autre.getId())).isTrue();
        assertThat(stock.calculerStock(autre.getId())).isEqualTo(5);
        mvc.perform(get("/stock/{id}", article.getId())).andExpect(status().isNotFound());
    }

    @Test
    void articleSansMouvementsPeutEtreSupprime() throws Exception {
        Article article = creerArticle("TIS-001");
        mvc.perform(post("/stock/{id}/supprimer", article.getId()).param("confirmation", "true"))
                .andExpect(status().is3xxRedirection());
        assertThat(articles.existsById(article.getId())).isFalse();
    }

    @Test
    void articleIntrouvableRenvoie404() throws Exception {
        mvc.perform(get("/stock/999999/supprimer")).andExpect(status().isNotFound());
        mvc.perform(post("/stock/999999/supprimer").param("confirmation", "true"))
                .andExpect(status().isNotFound());
    }

    @Test
    void creationEntreeSortieEtErreursDeValidationRestentFonctionnelles() throws Exception {
        mvc.perform(post("/articles").param("reference", "TIS-001")
                        .param("designation", "Rouleau de coton"))
                .andExpect(status().is3xxRedirection());
        Article article = articles.findAll().getFirst();
        mvc.perform(get("/articles")).andExpect(status().isOk())
                .andExpect(content().string(containsString("/stock/" + article.getId() + "/supprimer")));
        mvc.perform(get("/stock/{id}", article.getId())).andExpect(status().isOk())
                .andExpect(content().string(containsString("Épuisé")));
        mvc.perform(post("/stock/{id}/entrees", article.getId()).param("quantite", "12"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(post("/stock/{id}/sorties", article.getId()).param("quantite", "3"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(post("/stock/{id}/sorties", article.getId()).param("quantite", "20"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Stock insuffisant")));
        mvc.perform(post("/stock/{id}/entrees", article.getId()).param("quantite", "0"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("mouvement", "quantite"));
        mvc.perform(post("/articles").param("reference", "").param("designation", ""))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("article", "reference", "designation"));
        mvc.perform(post("/articles").param("reference", "TIS-001").param("designation", "Doublon"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("article", "reference"));
        assertThat(stock.calculerStock(article.getId())).isEqualTo(9);
    }
}

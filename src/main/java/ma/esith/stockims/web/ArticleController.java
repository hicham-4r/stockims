package ma.esith.stockims.web;
import ma.esith.stockims.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
@Controller @RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService service;
    @GetMapping
    public String liste(Model model) {
        model.addAttribute("articles", service.lister());
        return "articles/liste";
    }
    @GetMapping("/nouveau")
    public String formulaire(Model model) {
        model.addAttribute("article", new ArticleForm());
        return "articles/formulaire";
    }
    @PostMapping
    public String ajouter(@Valid @ModelAttribute("article") ArticleForm form,
                          BindingResult erreurs) {
        if (erreurs.hasErrors()) return "articles/formulaire";
        try {
            service.creer(form.getReference(), form.getDesignation());
        } catch (DataIntegrityViolationException e) {
            erreurs.rejectValue("reference", "doublon",
                    "Cette référence existe déjà.");
            return "articles/formulaire";
        }
        return "redirect:/articles";
    }
}

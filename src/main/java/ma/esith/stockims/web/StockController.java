package ma.esith.stockims.web;
import ma.esith.stockims.model.Article;
import ma.esith.stockims.service.StockService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@Controller @RequestMapping("/stock") @RequiredArgsConstructor
public class StockController {
    private final StockService service;
    @GetMapping("/{id}")
    public String fiche(@PathVariable("id") Long id, Model model) {
        model.addAttribute("mouvement", new MouvementForm());
        return preparer(id, model);
    }
    private String preparer(Long id, Model model) {
        Article article = service.chercher(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("article", article);
        model.addAttribute("stock", service.calculerStock(id));
        return "stock/fiche";
    }
    @PostMapping("/{id}/entrees")
    public String entree(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("mouvement") MouvementForm form,
                         BindingResult erreurs, Model model) {
        if (erreurs.hasErrors()) return preparer(id, model);
        try {
            service.entrer(id, form.getQuantite());
        } catch (IllegalArgumentException e) {
            erreurs.reject("metier", e.getMessage());
            return preparer(id, model);
        }
        return "redirect:/stock/" + id;
    }
    @PostMapping("/{id}/sorties")
    public String sortie(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("mouvement") MouvementForm form,
                         BindingResult erreurs, Model model) {
        if (erreurs.hasErrors()) return preparer(id, model);
        try {
            service.sortir(id, form.getQuantite());
        } catch (IllegalArgumentException e) {
            erreurs.reject("metier", e.getMessage());
            return preparer(id, model);
        }
        return "redirect:/stock/" + id;
    }
}

package ma.esith.stockims;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class AccueilController {
    @GetMapping("/")
    public String accueil(Model model) {
        var message = new MessageAccueil("Bienvenue dans StockIMS");
        model.addAttribute("message", message.getTexte());
        return "accueil";
    }
}

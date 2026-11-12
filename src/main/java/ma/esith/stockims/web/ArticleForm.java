package ma.esith.stockims.web;
import jakarta.validation.constraints.*;
import lombok.*;
@Getter @Setter
public class ArticleForm {
    @NotBlank(message = "La référence est obligatoire.")
    @Size(max = 20, message = "20 caractères au maximum.")
    private String reference;
    @NotBlank(message = "La désignation est obligatoire.")
    @Size(max = 80, message = "80 caractères au maximum.")
    private String designation;
}
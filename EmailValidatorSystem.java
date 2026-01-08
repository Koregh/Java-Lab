// EmailValidatorSystem.java

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class EmailValidatorSystem {

    public record ValidationReport(String email, boolean isValid) {}

    @FunctionalInterface
    public interface EmailValidationStrategy {
        boolean isValid(String email);
    }

    public static final class RegexValidationStrategy implements EmailValidationStrategy {
        private static final String EMAIL_PATTERN = 
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        
        private static final Pattern PATTERN = Pattern.compile(EMAIL_PATTERN);

        @Override
        public boolean isValid(final String email) {
            if (email == null || email.isBlank()) return false;
            return PATTERN.matcher(email).matches();
        }
    }

    public static final class CorporateDomainValidator implements EmailValidationStrategy {
        private final String domain;

        public CorporateDomainValidator(final String domain) {
            this.domain = Objects.requireNonNull(domain);
        }

        @Override
        public boolean isValid(final String email) {
            return email != null && email.toLowerCase().endsWith("@" + domain.toLowerCase());
        }
    }

    public static final class EmailVerificationService {
        private final List<EmailValidationStrategy> strategies;

        public EmailVerificationService(final List<EmailValidationStrategy> strategies) {
            this.strategies = List.copyOf(strategies); 
        }

        public boolean isValidEmail(final String email) {
            return strategies.stream().allMatch(s -> s.isValid(email));
        }

        public List<ValidationReport> processEmails(final List<String> emails) {
            return emails.parallelStream()
                         .map(email -> new ValidationReport(email, isValidEmail(email)))
                         .toList();
        }
    }

    public static void main(String[] args) {
        final var service = new EmailVerificationService(List.of(
            new RegexValidationStrategy(),
            new CorporateDomainValidator("empresa.com")
        ));

        final var emailsParaValidar = List.of(
            "contato@empresa.com", 
            "usuario.invalido@", 
            "hacker@gmail.com", 
            "diretoria@empresa.com"
        );

        final List<ValidationReport> relatorio = service.processEmails(emailsParaValidar);

        relatorio.forEach(res -> 
            System.out.printf("Email: %-25s | Válido: %b%n", res.email(), res.isValid())
        );
    }
}

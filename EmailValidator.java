// EmailValidator.java

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// EmailValitador, esse script é um teste e uma base para testes futuros utilizando algumas bases de código limpo.


public class EmailValidatorSystem {
  
    /**
     * Define o contrato para qualquer nova regra de validação.
     */
    public interface EmailValidationStrategy {
        boolean isValid(String email);
    }

    
    /**
     * Validador via Regex utilizando RFC 5322.
     */
    public static class RegexValidationStrategy implements EmailValidationStrategy {
        private static final String EMAIL_PATTERN = 
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        
        
        private static final Pattern PATTERN = Pattern.compile(EMAIL_PATTERN);

        @Override
        public boolean isValid(String email) {
            if (email == null || email.isBlank()) return false;
            return PATTERN.matcher(email).matches();
        }
    }

    
    public static class CorporateDomainValidator implements EmailValidationStrategy {
        private final String domain;

        public CorporateDomainValidator(String domain) {
            this.domain = domain;
        }

        @Override
        public boolean isValid(String email) {
            return email != null && email.toLowerCase().endsWith("@" + domain.toLowerCase());
        }
    }

    public static class EmailVerificationService {
        private final List<EmailValidationStrategy> strategies;

        public EmailVerificationService(List<EmailValidationStrategy> strategies) {
            this.strategies = Objects.requireNonNull(strategies, "Strategies cannot be null");
        }

        /**
         * Verifica um único e-mail contra todas as estratégias.
         */
        public boolean isValidEmail(String email) {
            return strategies.stream().allMatch(s -> s.isValid(email));
        }

        /**
         * Processamento paralelo para grandes volumes de dados.
         */
        public List<String> filterValidEmails(List<String> emails) {
            return emails.parallelStream() // Uso de ForkJoinPool para performance multithread
                         .filter(this::isValidEmail)
                         .collect(Collectors.toList());
        }
    }

    public static void main(String[] args) {
        // Configuração do sistema com injeção de dependência manual
        EmailVerificationService service = new EmailVerificationService(List.of(
            new RegexValidationStrategy(),
            new CorporateDomainValidator("empresa.com")
        ));

        List<String> emailsParaValidar = List.of(
            "contato@empresa.com", 
            "usuario.invalido@", 
            "hacker@gmail.com", 
            "diretoria@empresa.com"
        );

        List<String> validos = service.filterValidEmails(emailsParaValidar);

        System.out.println("Emails Válidos (Padrão + Domínio Corporativo):");
        validos.forEach(System.out::println);
    }
}


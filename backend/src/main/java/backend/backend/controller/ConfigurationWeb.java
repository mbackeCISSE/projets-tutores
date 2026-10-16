package backend.backend.controller;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
@Configuration
public class ConfigurationWeb implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registre) {
        registre.addMapping("/api/**").allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "X-Utilisateur-Id");
    }
}

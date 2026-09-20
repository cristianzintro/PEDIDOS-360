package cl.duoc.pedidos360.bff.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    @Qualifier("otRestClient")
    RestClient otRestClient(RestClient.Builder builder,
                            @Value("${services.ot.url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }

    @Bean
    @Qualifier("auditRestClient")
    RestClient auditRestClient(RestClient.Builder builder,
                               @Value("${services.audit.url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }
}

package commerce.gateway.logging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.observation.DefaultServerRequestObservationConvention;
import org.springframework.http.server.reactive.observation.ServerRequestObservationContext;

@Configuration
public class GatewayTracingConfig {

    @Bean
    public DefaultServerRequestObservationConvention gatewayObservationConvention() {
        return new DefaultServerRequestObservationConvention() {
            @Override
            public String getContextualName(ServerRequestObservationContext context) {
                String method = context.getCarrier().getMethod().name();
                String path = context.getCarrier().getPath().value();

                return String.format("%s %s", method, path);
            }

            @Override
            public String getName() {
                return "http.server.requests";
            }
        };
    }
}

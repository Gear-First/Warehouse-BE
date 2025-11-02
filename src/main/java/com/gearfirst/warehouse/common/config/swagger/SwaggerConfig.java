package com.gearfirst.warehouse.common.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Server server = new Server();
        server.setUrl("/warehouse");

        String description = """
            Gearfirst Warehouse REST API Document\n\n
            Timezone policy:\n
            - API inputs/outputs use KST (+09:00) formatted ISO-8601 strings.\n
            - Server persists and computes in UTC.\n
            Date filter semantics:\n
            - Parameters `date`, `dateFrom`, `dateTo` are interpreted as KST local-day(s).\n
            - Internally they are converted to UTC inclusive bounds.\n
            - When both `date` and a range are provided, the range takes precedence.\n
            Response envelope:\n
            - Success responses are wrapped with CommonApiResponse and lists use PageEnvelope<T>.\n
            - Error payloads include code/message/detail with standard error statuses.\n            """;

        return new OpenAPI()
                .info(new Info()
                        .title("Gearfirst-Warehouse")
                        .description(description)
                        .version("1.0.0"))
                .addServersItem(server);
    }
}

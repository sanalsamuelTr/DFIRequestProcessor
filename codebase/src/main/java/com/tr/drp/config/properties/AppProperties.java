package com.tr.drp.config.properties;

import com.tr.drp.config.DomainProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private List<DomainProperties> domains;
}

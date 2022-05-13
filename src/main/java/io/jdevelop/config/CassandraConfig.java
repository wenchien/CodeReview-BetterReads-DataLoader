package io.jdevelop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class CassandraConfig extends AbstractCassandraConfiguration {

    @Override
    protected String getKeyspaceName() {
        return "main";
    }

    @Override
    public String[] getEntityBasePackages() {
        log.debug("getEntityBasePackages");
		return new String[] {"io.jdevelop.DTO"};
	}

}

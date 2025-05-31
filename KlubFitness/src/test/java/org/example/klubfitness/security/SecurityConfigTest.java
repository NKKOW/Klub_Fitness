package org.example.klubfitness.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;

class SecurityConfigTest {

    private final ApplicationContextRunner ctx = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SecurityConfig.class));

    @Test
    void filterChainBeanIsCreated() {
        ctx.run(context -> {
            assertThat(context).hasSingleBean(SecurityFilterChain.class);
            Object bean = context.getBean("filterChain");
            assertThat(bean).isInstanceOf(SecurityFilterChain.class);
        });
    }

    @Test
    void passwordEncoderBeanIsBCrypt() {
        ctx.run(context -> {
            assertThat(context).hasSingleBean(PasswordEncoder.class);
            PasswordEncoder encoder = context.getBean(PasswordEncoder.class);
            assertThat(encoder).isInstanceOf(BCryptPasswordEncoder.class);
            // and it actually encodes
            String raw = "test123";
            String encoded = encoder.encode(raw);
            assertThat(encoder.matches(raw, encoded)).isTrue();
        });
    }
}



package com.bureureung.fo;

import com.bureureung.fo.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FoApplicationTests extends IntegrationTestSupport {

    @Test
    void contextLoads() {
    }

    @Test
    void 컨테이너_확인() {
        System.out.println("=== 컨테이너 정보 ===");
        System.out.println("Running: " + IntegrationTestSupport.MYSQL_CONTAINER.isRunning());
        System.out.println("JDBC URL: " + IntegrationTestSupport.MYSQL_CONTAINER.getJdbcUrl());
        System.out.println("Container ID: " + IntegrationTestSupport.MYSQL_CONTAINER.getContainerId());
        assertThat(IntegrationTestSupport.MYSQL_CONTAINER.isRunning()).isTrue();
    }
}

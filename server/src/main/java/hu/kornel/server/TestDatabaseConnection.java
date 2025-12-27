package hu.kornel.server;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TestDatabaseConnection {
    public static void main(String[] args) {
        SpringApplication.run(TestDatabaseConnection.class, args);   
    }

    @Bean
    public CommandLineRunner testConnection(DataSource dataSource) {
        return args -> {
            try (Connection connection = dataSource.getConnection()){
                System.out.println("Connection successful!");
                System.out.println("Database: "+connection.getCatalog());
                System.out.println("URL: "+connection.getMetaData().getURL());
                System.out.println("User: "+connection.getMetaData().getUserName());
                
            } catch (Exception e) {
                System.err.println("Connection failed");
                e.printStackTrace();
            }
        };
    }
}

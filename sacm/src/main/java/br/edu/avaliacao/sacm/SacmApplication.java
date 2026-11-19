package br.edu.avaliacao.sacm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import br.edu.avaliacao.sacm.console.SistemaConsole;

@SpringBootApplication
public class SacmApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(SacmApplication.class, args);
        ctx.getBean(SistemaConsole.class).exibirMenu();
    }
}

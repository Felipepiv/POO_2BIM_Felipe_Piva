package br.edu.avaliacao.sacm.console;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import br.edu.avaliacao.sacm.enums.Especialidade;
import br.edu.avaliacao.sacm.exception.RegraNegocioException;
import br.edu.avaliacao.sacm.model.Consulta;
import br.edu.avaliacao.sacm.model.Medico;
import br.edu.avaliacao.sacm.model.Paciente;
import br.edu.avaliacao.sacm.service.ConsultaService;
import java.util.Scanner;

@Component
public class SistemaConsole {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ConsultaService consultaService;
    private final Scanner scanner = new Scanner(System.in);

    public SistemaConsole(ConsultaService consultaService) { this.consultaService = consultaService; }

    public void exibirMenu() { // A console vira @Component, para o Spring injetar o service. Ela só lê dados e chama o service. Aqui ficam as duas constantes static final (RN10):
        while (true) {
            System.out.println("\n=== SACM ===\n1-Cadastrar médico\n2-Cadastrar paciente\n"
                + "3-Agendar consulta\n4-Listar consultas\n5-Alterar status\n0-Sair");
            System.out.print("Opção: ");
            try {
                switch (scanner.nextLine().trim()) {
                    case "1" -> cadastrarMedico();
                    case "2" -> cadastrarPaciente();
                    case "3" -> agendarConsulta();
                    case "4" -> listarConsultas();
                    case "5" -> alterarStatusConsulta();
                    case "0" -> { encerrar(); return; }
                    default  -> System.out.println("Opção inválida.");
                }
            } catch (RegraNegocioException e) {
                System.out.println("Erro: " + e.getMessage());
            } catch (DateTimeParseException | IllegalArgumentException e) {
                System.out.println("Dado em formato inválido. Tente novamente.");
            }
        }
    }

    private String ler(String rotulo) { System.out.print(rotulo + ": "); return scanner.nextLine().trim(); }

    public void cadastrarMedico() {
        Medico m = new Medico(ler("Nome"), ler("CPF"),
            LocalDate.parse(ler("Nascimento (dd/MM/yyyy)"), FORMATO_DATA),
            ler("Email"), ler("Telefone"), ler("Cidade"), ler("Estado"), ler("CRM"),
            Especialidade.valueOf(ler("Especialidade " + Arrays.toString(Especialidade.values())).toUpperCase()),
            new BigDecimal(ler("Valor da consulta")));
        System.out.println("Médico cadastrado com id " + consultaService.cadastrarMedico(m).getId());
    }

    public void cadastrarPaciente() {
        Paciente p = new Paciente(ler("Nome"), ler("CPF"),
            LocalDate.parse(ler("Nascimento (dd/MM/yyyy)"), FORMATO_DATA),
            ler("Email"), ler("Telefone"), ler("Cidade"), ler("Estado"),
            ler("Nº prontuário"), ler("Convênio"), ler("Contato de emergência"), ler("Telefone de emergência"));
        System.out.println("Paciente cadastrado com id " + consultaService.cadastrarPaciente(p).getId());
    }

    public void agendarConsulta() {
        Consulta c = consultaService.agendarConsulta(
            Long.parseLong(ler("Id do médico")), Long.parseLong(ler("Id do paciente")),
            LocalDateTime.parse(ler("Data/hora (dd/MM/yyyy HH:mm)"), FORMATO_DATA_HORA), ler("Motivo"));
        System.out.println("Agendada: " + c.getResumo());
    }

    public void listarConsultas() {
        boolean porMedico = ler("Listar por 1-médico ou 2-paciente").equals("1");
        Long id = Long.parseLong(ler("Id"));
        List<Consulta> lista = porMedico ? consultaService.listarConsultasPorMedico(id)
                                         : consultaService.listarConsultasPorPaciente(id);
        if (lista.isEmpty()) System.out.println("Nenhuma consulta encontrada.");
        lista.forEach(c -> System.out.println(c.getResumo()));
    }

    public void alterarStatusConsulta() {
        Long id = Long.parseLong(ler("Id da consulta"));
        switch (ler("1-Confirmar 2-Cancelar 3-Finalizar")) {
            case "1" -> consultaService.confirmarConsulta(id);
            case "2" -> consultaService.cancelarConsulta(id);
            case "3" -> consultaService.finalizarConsulta(id);
            default  -> { System.out.println("Opção inválida."); return; }
        }
        System.out.println("Status atualizado.");
    }

    public void encerrar() { System.out.println("Sistema encerrado."); scanner.close(); }
}
package br.edu.avaliacao.sacm.console;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
// + imports de model, enums, repository, service e exception

public class SistemaConsole {

    private ConsultaService consultaService;
    private MedicoRepository medicoRepository;
    private PacienteRepository pacienteRepository;
    private Scanner scanner = new Scanner(System.in);

    public SistemaConsole(ConsultaService consultaService,
                          MedicoRepository medicoRepository,
                          PacienteRepository pacienteRepository) {
        this.consultaService = consultaService;
        this.medicoRepository = medicoRepository;
        this.pacienteRepository = pacienteRepository;
    }

    public void exibirMenu() { // vai mostrar as opções, lê a escolha e chama o método certo, repetindo até a pessoa escolher sair
    while (true) {
        System.out.println("\n=== SACM ===");
        System.out.println("1 - Cadastrar médico");
        System.out.println("2 - Cadastrar paciente");
        System.out.println("3 - Agendar consulta");
        System.out.println("4 - Listar consultas");
        System.out.println("5 - Alterar status de consulta");
        System.out.println("0 - Sair");
        System.out.print("Opção: ");

        String opcao = scanner.nextLine();
        try {
            switch (opcao) {
                case "1": cadastrarMedico(); break;
                case "2": cadastrarPaciente(); break;
                case "3": agendarConsulta(); break;
                case "4": listarConsultas(); break;
                case "5": alterarStatusConsulta(); break;
                case "0": encerrar(); return;
                default: System.out.println("Opção inválida.");
            }
        } catch (RegraNegocioException e) {
            System.out.println("Erro: " + e.getMessage());
        }
     }
    }

    
}
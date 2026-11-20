package br.edu.avaliacao.sacm.console;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.springframework.stereotype.Component;

import br.edu.avaliacao.sacm.enums.Especialidade;
import br.edu.avaliacao.sacm.exception.RegraNegocioException;
import br.edu.avaliacao.sacm.model.Consulta;
import br.edu.avaliacao.sacm.model.Medico;
import br.edu.avaliacao.sacm.model.Paciente;
import br.edu.avaliacao.sacm.repository.MedicoRepository;
import br.edu.avaliacao.sacm.repository.PacienteRepository;
import br.edu.avaliacao.sacm.service.ConsultaService;

/**
 * Interface de console do sistema. Só lê o que o usuário digita, chama o serviço
 * (ou o repositório, no caso dos cadastros) e mostra o resultado.
 * Não contém regra de agendamento: isso fica no ConsultaService.
 */
@Component
public class SistemaConsole {

    // RN10: constantes static final. Evitam repetir o padrão de data em vários pontos
    // e o DateTimeFormatter é imutável, então é seguro compartilhar.
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Atributos de instância: o UML pede consultaService e scanner.
    // Os repositórios entram pelo construtor para permitir os cadastros.
    private final ConsultaService consultaService;
    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;
    private final Scanner scanner = new Scanner(System.in);

    public SistemaConsole(ConsultaService consultaService,
                          MedicoRepository medicoRepository,
                          PacienteRepository pacienteRepository) {
        this.consultaService = consultaService;
        this.medicoRepository = medicoRepository;
        this.pacienteRepository = pacienteRepository;
    }

    /**
     * Mostra o menu em loop e chama o método da opção escolhida.
     * O try/catch evita que um erro de regra ou um dado digitado errado derrube o programa:
     * mostra a mensagem e volta para o menu.
     */
    public void exibirMenu() {
        while (true) {
            System.out.println("\n=== SACM ===");
            System.out.println("1 - Cadastrar médico");
            System.out.println("2 - Cadastrar paciente");
            System.out.println("3 - Agendar consulta");
            System.out.println("4 - Listar consultas");
            System.out.println("5 - Alterar status de consulta");
            System.out.println("0 - Sair");
            System.out.print("Opção: ");

            try {
                switch (scanner.nextLine().trim()) {
                    case "1" -> cadastrarMedico();
                    case "2" -> cadastrarPaciente();
                    case "3" -> agendarConsulta();
                    case "4" -> listarConsultas();
                    case "5" -> alterarStatusConsulta();
                    case "0" -> { encerrar(); return; }
                    default -> System.out.println("Opção inválida.");
                }
            } catch (RegraNegocioException e) {
                // Erro de negócio: mensagem compreensível (RN09)
                System.out.println("Erro: " + e.getMessage());
            } catch (DateTimeParseException | IllegalArgumentException e) {
                // Data, número ou especialidade digitados em formato inválido
                System.out.println("Dado em formato inválido. Tente novamente.");
            }
        }
    }

    /**
     * Cadastra um médico: lê os dados, cria o objeto, valida RN01 (CRM e CPF únicos)
     * e salva no banco. O construtor de Medico já exige CRM não vazio.
     */
    public void cadastrarMedico() {
        Medico medico = new Medico(
                ler("Nome"),
                ler("CPF"),
                LocalDate.parse(ler("Nascimento (dd/MM/yyyy)"), FORMATO_DATA),
                ler("Email"),
                ler("Telefone"),
                ler("Cidade"),
                ler("Estado"),
                ler("CRM"),
                Especialidade.valueOf(
                        ler("Especialidade " + Arrays.toString(Especialidade.values())).toUpperCase()),
                new BigDecimal(ler("Valor da consulta")));

        if (medicoRepository.existePorCrm(medico.getCrm())) {
            throw new RegraNegocioException("Já existe um médico com este CRM");
        }
        validarCpfUnico(medico.getCpf());

        Medico salvo = medicoRepository.salvar(medico);
        System.out.println("Médico cadastrado com id " + salvo.getId());
    }

    /**
     * Cadastra um paciente: lê os dados, cria o objeto, valida o CPF único (RN01)
     * e salva no banco. O paciente nasce ATIVO (definido na classe Pessoa).
     */
    public void cadastrarPaciente() {
        Paciente paciente = new Paciente(
                ler("Nome"),
                ler("CPF"),
                LocalDate.parse(ler("Nascimento (dd/MM/yyyy)"), FORMATO_DATA),
                ler("Email"),
                ler("Telefone"),
                ler("Cidade"),
                ler("Estado"),
                ler("Nº do prontuário"),
                ler("Convênio"),
                ler("Contato de emergência"),
                ler("Telefone de emergência"));

        validarCpfUnico(paciente.getCpf());

        Paciente salvo = pacienteRepository.salvar(paciente);
        System.out.println("Paciente cadastrado com id " + salvo.getId());
    }

    /**
     * Agenda uma consulta: lê ids, data/hora e motivo e delega tudo ao ConsultaService,
     * que aplica as regras RN02 a RN05. A console só mostra o resultado.
     */
    public void agendarConsulta() {
        Long medicoId = Long.parseLong(ler("Id do médico"));
        Long pacienteId = Long.parseLong(ler("Id do paciente"));
        LocalDateTime dataHora = LocalDateTime.parse(
                ler("Data e hora (dd/MM/yyyy HH:mm)"), FORMATO_DATA_HORA);
        String motivo = ler("Motivo");

        Consulta consulta = consultaService.agendarConsulta(medicoId, pacienteId, dataHora, motivo);
        System.out.println("Consulta agendada: " + consulta.getResumo());
    }

    /** Lista as consultas de um médico ou de um paciente, imprimindo o resumo de cada uma. */
    public void listarConsultas() {
        boolean porMedico = ler("Listar por 1 - médico ou 2 - paciente").equals("1");
        Long id = Long.parseLong(ler("Id"));

        List<Consulta> consultas = porMedico
                ? consultaService.listarConsultasPorMedico(id)
                : consultaService.listarConsultasPorPaciente(id);

        if (consultas.isEmpty()) {
            System.out.println("Nenhuma consulta encontrada.");
        }
        consultas.forEach(c -> System.out.println(c.getResumo()));
    }

    /**
     * Confirma, cancela ou finaliza uma consulta, chamando o método correspondente
     * do ConsultaService. As regras de transição de status (RN06) ficam na classe Consulta.
     */
    public void alterarStatusConsulta() {
        Long id = Long.parseLong(ler("Id da consulta"));
        String acao = ler("1 - Confirmar | 2 - Cancelar | 3 - Finalizar");

        switch (acao) {
            case "1" -> consultaService.confirmarConsulta(id);
            case "2" -> consultaService.cancelarConsulta(id);
            case "3" -> consultaService.finalizarConsulta(id);
            default -> {
                System.out.println("Opção inválida.");
                return;
            }
        }
        System.out.println("Status atualizado.");
    }

    /** Encerra o sistema: avisa o usuário e fecha o Scanner. O menu termina logo em seguida. */
    public void encerrar() {
        System.out.println("Sistema encerrado.");
        scanner.close();
    }

    // ---------- Métodos auxiliares privados (não aparecem no UML) ----------

    /** Mostra o rótulo, lê uma linha digitada e remove espaços das pontas. Evita repetir println + nextLine. */
    private String ler(String rotulo) {
        System.out.print(rotulo + ": ");
        return scanner.nextLine().trim();
    }

    /** RN01: o CPF não pode se repetir, nem entre médicos e pacientes. */
    private void validarCpfUnico(String cpf) {
        if (medicoRepository.existePorCpf(cpf) || pacienteRepository.existePorCpf(cpf)) {
            throw new RegraNegocioException("Já existe uma pessoa cadastrada com este CPF");
        }
    }
}
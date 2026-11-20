package br.edu.avaliacao.sacm.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import br.edu.avaliacao.sacm.exception.ConflitoHorarioException;
import br.edu.avaliacao.sacm.exception.RegraNegocioException;
import br.edu.avaliacao.sacm.interfaces.Agendavel;
import br.edu.avaliacao.sacm.model.Consulta;
import br.edu.avaliacao.sacm.model.Medico;
import br.edu.avaliacao.sacm.model.Paciente;
import br.edu.avaliacao.sacm.repository.ConsultaRepository;
import br.edu.avaliacao.sacm.repository.MedicoRepository;
import br.edu.avaliacao.sacm.repository.PacienteRepository;

/**
 * Concentra as regras de negócio das consultas.
 * Implementa Agendavel (agendarConsulta e cancelarConsulta).
 * Não conhece o console: só recebe dados, aplica regras e usa os repositórios.
 */
@Service
public class ConsultaService implements Agendavel {

    // Atributos de instância (UML): os três repositórios usados pelo serviço
    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;
    private final ConsultaRepository consultaRepository;

    // Construtor: o Spring entrega os repositórios prontos (injeção de dependência)
    public ConsultaService(MedicoRepository medicoRepository,
                           PacienteRepository pacienteRepository,
                           ConsultaRepository consultaRepository) {
        this.medicoRepository = medicoRepository;
        this.pacienteRepository = pacienteRepository;
        this.consultaRepository = consultaRepository;
    }

    /**
     * Agenda uma consulta nova.
     * Valida na ordem: RN05 (data futura e motivo), RN02 (existem e estão ativos),
     * RN03 (médico disponível) e RN04 (sem conflito de horário).
     * Só então cria a Consulta (que nasce AGENDADA, RN06) e salva no banco.
     */
    @Override
    public Consulta agendarConsulta(Long medicoId, Long pacienteId,
                                    LocalDateTime dataHora, String motivo) {
        // RN05: validações baratas primeiro, antes de consultar o banco
        if (dataHora == null || !dataHora.isAfter(LocalDateTime.now())) {
            throw new RegraNegocioException("A data/hora da consulta deve ser futura");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new RegraNegocioException("O motivo da consulta é obrigatório");
        }

        Medico medico = medicoRepository.buscarPorId(medicoId);
        Paciente paciente = pacienteRepository.buscarPorId(pacienteId);

        // RN02: precisam existir e estar ATIVOS
        if (medico == null) {
            throw new RegraNegocioException("Médico não encontrado");
        }
        if (paciente == null) {
            throw new RegraNegocioException("Paciente não encontrado");
        }
        if (!medico.isAtivo()) {
            throw new RegraNegocioException("Médico inativo");
        }
        if (!paciente.podeAgendar()) {
            throw new RegraNegocioException("Paciente inativo");
        }

        // RN03: médico precisa estar DISPONIVEL
        if (!medico.isDisponivel()) {
            throw new RegraNegocioException("Médico indisponível para novos agendamentos");
        }

        // RN04: lança ConflitoHorarioException se já houver consulta ativa no horário
        verificarConflitoHorario(medico, paciente, dataHora);

        return consultaRepository.salvar(new Consulta(medico, paciente, dataHora, motivo));
    }

    /**
     * Cancela uma consulta existente.
     * A regra de transição (AGENDADA/CONFIRMADA -> CANCELADA) fica dentro de Consulta.cancelar().
     * Depois do cancelamento, salvar() faz o UPDATE no banco (RN07).
     */
    @Override
    public void cancelarConsulta(Long consultaId) {
        Consulta consulta = buscar(consultaId);
        consulta.cancelar();
        consultaRepository.salvar(consulta);
    }

    /** Confirma a consulta (AGENDADA -> CONFIRMADA) e persiste a mudança (RN07). */
    public void confirmarConsulta(Long consultaId) {
        Consulta consulta = buscar(consultaId);
        consulta.confirmar();
        consultaRepository.salvar(consulta);
    }

    /** Finaliza a consulta (CONFIRMADA -> FINALIZADA) e persiste a mudança. */
    public void finalizarConsulta(Long consultaId) {
        Consulta consulta = buscar(consultaId);
        consulta.finalizar();
        consultaRepository.salvar(consulta);
    }

    /** Devolve todas as consultas de um médico, filtrando pelo id. */
    public List<Consulta> listarConsultasPorMedico(Long medicoId) {
        return consultaRepository.listarTodos().stream()
                .filter(c -> c.getMedico().getId().equals(medicoId))
                .toList();
    }

    /** Devolve todas as consultas de um paciente, filtrando pelo id. */
    public List<Consulta> listarConsultasPorPaciente(Long pacienteId) {
        return consultaRepository.listarTodos().stream()
                .filter(c -> c.getPaciente().getId().equals(pacienteId))
                .toList();
    }

    /**
     * RN04: impede duas consultas no mesmo data/hora para o mesmo médico OU o mesmo paciente.
     * Só conta consulta ativa (AGENDADA ou CONFIRMADA): cancelada e finalizada
     * não bloqueiam o horário (RN07).
     * É void, como no UML: se houver conflito, lança exceção; se não, apenas retorna.
     */
    public void verificarConflitoHorario(Medico medico, Paciente paciente, LocalDateTime dataHora) {
        for (Consulta c : consultaRepository.listarTodos()) {
            boolean mesmoHorario = c.getDataHora().equals(dataHora);
            boolean mesmaPessoa = c.getMedico().getId().equals(medico.getId())
                               || c.getPaciente().getId().equals(paciente.getId());
            if (mesmoHorario && mesmaPessoa && c.isAtiva()) {
                throw new ConflitoHorarioException(
                        "Médico ou paciente já possui consulta neste horário");
            }
        }
    }

    /** Método auxiliar privado: busca a consulta ou lança erro se não existir (evita repetição). */
    private Consulta buscar(Long id) {
        Consulta consulta = consultaRepository.buscarPorId(id);
        if (consulta == null) {
            throw new RegraNegocioException("Consulta não encontrada");
        }
        return consulta;
    }
}
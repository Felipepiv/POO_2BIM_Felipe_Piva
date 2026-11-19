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

@Service
public class ConsultaService implements Agendavel {

    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;
    private final ConsultaRepository consultaRepository;

    public ConsultaService(MedicoRepository medicoRepository, PacienteRepository pacienteRepository,
                           ConsultaRepository consultaRepository) {
        this.medicoRepository = medicoRepository;
        this.pacienteRepository = pacienteRepository;
        this.consultaRepository = consultaRepository;
    }

    @Override
    public Consulta agendarConsulta(Long medicoId, Long pacienteId, LocalDateTime dataHora, String motivo) {
        if (dataHora == null || !dataHora.isAfter(LocalDateTime.now()))        // RN05
            throw new RegraNegocioException("A data/hora da consulta deve ser futura");
        if (motivo == null || motivo.isBlank())                                // RN05
            throw new RegraNegocioException("O motivo da consulta é obrigatório");

        Medico medico = medicoRepository.buscarPorId(medicoId);
        Paciente paciente = pacienteRepository.buscarPorId(pacienteId);
        if (medico == null)   throw new RegraNegocioException("Médico não encontrado");     // RN02
        if (paciente == null) throw new RegraNegocioException("Paciente não encontrado");   // RN02
        if (!medico.isAtivo())      throw new RegraNegocioException("Médico inativo");      // RN02
        if (!paciente.podeAgendar()) throw new RegraNegocioException("Paciente inativo");   // RN02
        if (!medico.isDisponivel()) throw new RegraNegocioException("Médico indisponível"); // RN03

        verificarConflitoHorario(medico, paciente, dataHora);                  // RN04
        return consultaRepository.salvar(new Consulta(medico, paciente, dataHora, motivo));
    }

    public void verificarConflitoHorario(Medico m, Paciente p, LocalDateTime data) {
        for (Consulta c : consultaRepository.listarTodos()) {
            boolean mesmoHorario = c.getDataHora().equals(data);
            boolean mesmaPessoa = c.getMedico().getId().equals(m.getId())
                               || c.getPaciente().getId().equals(p.getId());
            if (mesmoHorario && mesmaPessoa && c.isAtiva())   // cancelada/finalizada não bloqueia (RN07)
                throw new ConflitoHorarioException("Médico ou paciente já possui consulta neste horário");
        }
    }

    @Override public void cancelarConsulta(Long consultaId)  { Consulta c = buscar(consultaId); c.cancelar();  consultaRepository.salvar(c); }
    public void confirmarConsulta(Long consultaId)           { Consulta c = buscar(consultaId); c.confirmar(); consultaRepository.salvar(c); }
    public void finalizarConsulta(Long consultaId)           { Consulta c = buscar(consultaId); c.finalizar(); consultaRepository.salvar(c); }

    public List<Consulta> listarConsultasPorMedico(Long id) {
        return consultaRepository.listarTodos().stream()
                .filter(c -> c.getMedico().getId().equals(id)).toList();
    }
    public List<Consulta> listarConsultasPorPaciente(Long id) {
        return consultaRepository.listarTodos().stream()
                .filter(c -> c.getPaciente().getId().equals(id)).toList();
    }

    private Consulta buscar(Long id) {
        Consulta c = consultaRepository.buscarPorId(id);
        if (c == null) throw new RegraNegocioException("Consulta não encontrada");
        return c;
    }
}
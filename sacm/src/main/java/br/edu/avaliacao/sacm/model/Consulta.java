package br.edu.avaliacao.sacm.model;

import java.time.LocalDateTime;

import br.edu.avaliacao.sacm.enums.StatusConsulta;
import br.edu.avaliacao.sacm.exception.RegraNegocioException;

public class Consulta {

    private final Long id;
    private final Medico medico;
    private final Paciente paciente;
    private final LocalDateTime dataHora;
    private final String motivo;
    private StatusConsulta status;
    private final LocalDateTime dataAgendamento;
    private String observacao;

    // Nova: começa AGENDADA (RN06)
    public Consulta(Medico medico, Paciente paciente, LocalDateTime dataHora, String motivo) {
        this(null, medico, paciente, dataHora, motivo,
             StatusConsulta.AGENDADA, LocalDateTime.now(), null);
    }

    // Reconstrução
    public Consulta(Long id, Medico medico, Paciente paciente, LocalDateTime dataHora,
                    String motivo, StatusConsulta status,
                    LocalDateTime dataAgendamento, String observacao) {
        if (medico == null || paciente == null) {
            throw new RegraNegocioException("A consulta precisa de um médico e um paciente");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new RegraNegocioException("O motivo da consulta é obrigatório");
        }
        this.id = id;
        this.medico = medico;
        this.paciente = paciente;
        this.dataHora = dataHora;
        this.motivo = motivo;
        this.status = status;
        this.dataAgendamento = dataAgendamento;
        this.observacao = observacao;
    }

    public void confirmar() {
        if (status != StatusConsulta.AGENDADA) {
            throw new RegraNegocioException("Só é possível confirmar uma consulta AGENDADA");
        }
        this.status = StatusConsulta.CONFIRMADA;
    }

    public void cancelar() {
        if (status != StatusConsulta.AGENDADA && status != StatusConsulta.CONFIRMADA) {
            throw new RegraNegocioException("Uma consulta " + status + " não pode ser cancelada");
        }
        this.status = StatusConsulta.CANCELADA;
    }

    public void finalizar() {
        if (status != StatusConsulta.CONFIRMADA) {
            throw new RegraNegocioException("Só é possível finalizar uma consulta CONFIRMADA");
        }
        this.status = StatusConsulta.FINALIZADA;
    }

    public boolean isAtiva() {
        return status == StatusConsulta.AGENDADA || status == StatusConsulta.CONFIRMADA;
    }

    public String getResumo() {
        return "Consulta #" + id + " | " + medico.getNome() + " x " + paciente.getNome()
             + " | " + dataHora + " | " + status;
    }

    public void setObservacao(String observacao) { this.observacao = observacao; }

    public Long getId()                        { return id; }
    public Medico getMedico()                  { return medico; }
    public Paciente getPaciente()              { return paciente; }
    public LocalDateTime getDataHora()         { return dataHora; }
    public String getMotivo()                  { return motivo; }
    public StatusConsulta getStatus()          { return status; }
    public LocalDateTime getDataAgendamento()  { return dataAgendamento; }
    public String getObservacao()              { return observacao; }
}
package br.edu.avaliacao.sacm.model;

import java.time.LocalDateTime;
import br.edu.avaliacao.sacm.enums.StatusConsulta;

public class Consulta {

    private Long id;
    private Medico medico;
    private Paciente paciente;
    private LocalDateTime dataHora;
    private String motivo;
    private StatusConsulta status;
    private LocalDateTime dataAgendamento;
    private String observacao;

    public void confirmar() {  //Ira mudar o status para confirmado, mas só se estiver agendada
    if (this.status != StatusConsulta.AGENDADA) {
        throw new RegraNegocioException("Só é possível confirmar consulta agendada");
    }
    this.status = StatusConsulta.CONFIRMADA;
    }

    public void cancelar() { // Não pode cancelar o que já foi realizado ou já está cancelado
    if (this.status == StatusConsulta.REALIZADA
            || this.status == StatusConsulta.CANCELADA) {
        throw new RegraNegocioException("Consulta não pode ser cancelada");
    }
    this.status = StatusConsulta.CANCELADA;
    }

    public void finalizar() { // Só ira finalizar se estiver confirmada
    if (this.status != StatusConsulta.CONFIRMADA) {
        throw new RegraNegocioException("Só é possível finalizar consulta confirmada");
    }
    this.status = StatusConsulta.REALIZADA;
    }

    public boolean isAtiva() { // Devolve boolean.Ativa é a consulta que ainda conta na agenda, ou seja, não está cancelada nem realizada
    return this.status == StatusConsulta.AGENDADA
        || this.status == StatusConsulta.CONFIRMADA;
    }

}
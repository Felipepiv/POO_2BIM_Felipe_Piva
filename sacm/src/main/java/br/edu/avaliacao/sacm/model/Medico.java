package model;

import java.math.BigDecimal;

public class Medico {

    private String crm;
    private Especialidade especialidade;
    private DisponibilidadeMedico disponibilidade;
    private BigDecimal valorConsulta;


    public void alterarDisponibilidade(DisponibilidadeMedico disponibilidade) {
        this.disponibilidade = disponibilidade;
    }

    public boolean isDisponivel() {
        return this.disponibilidade == DisponibilidadeMedico.DISPONIVEL;
    }

    public boolean podeAtender() {
    return isAtivo() && isDisponivel();
}

    public abstract String getDescricao();
}

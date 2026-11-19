package br.edu.avaliacao.sacm.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import br.edu.avaliacao.sacm.enums.DisponibilidadeMedico;
import br.edu.avaliacao.sacm.enums.Especialidade;
import br.edu.avaliacao.sacm.enums.StatusPessoa;
import br.edu.avaliacao.sacm.exception.RegraNegocioException;

public class Medico extends Pessoa {

    private final String crm;
    private Especialidade especialidade;
    private DisponibilidadeMedico disponibilidade;
    private BigDecimal valorConsulta;

    // Cadastro novo: começa DISPONIVEL (RN01: CRM obrigatório)
    public Medico(String nome, String cpf, LocalDate dataNascimento, String email,
                  String telefone, String cidade, String estado,
                  String crm, Especialidade especialidade, BigDecimal valorConsulta) {
        super(nome, cpf, dataNascimento, email, telefone, cidade, estado);
        if (crm == null || crm.isBlank()) {
            throw new RegraNegocioException("O CRM é obrigatório");
        }
        this.crm = crm;
        this.especialidade = especialidade;
        this.valorConsulta = valorConsulta;
        this.disponibilidade = DisponibilidadeMedico.DISPONIVEL;
    }

    // Reconstrução
    public Medico(Long id, String nome, String cpf, LocalDate dataNascimento, String email,
                  String telefone, String cidade, String estado,
                  LocalDateTime dataCadastro, StatusPessoa status,
                  String crm, Especialidade especialidade,
                  DisponibilidadeMedico disponibilidade, BigDecimal valorConsulta) {
        super(id, nome, cpf, dataNascimento, email, telefone, cidade, estado, dataCadastro, status);
        this.crm = crm;
        this.especialidade = especialidade;
        this.disponibilidade = disponibilidade;
        this.valorConsulta = valorConsulta;
    }

    public void alterarDisponibilidade(DisponibilidadeMedico valor) {
        this.disponibilidade = valor;
    }

    public boolean isDisponivel() {
        return this.disponibilidade == DisponibilidadeMedico.DISPONIVEL;
    }

    public boolean podeAtender() {
        return isAtivo() && isDisponivel();
    }

    @Override
    public String getDescricao() {
        return "Dr(a). " + getNome() + " | CRM " + crm + " | " + especialidade;
    }

    public String getCrm()                          { return crm; }
    public Especialidade getEspecialidade()         { return especialidade; }
    public DisponibilidadeMedico getDisponibilidade(){ return disponibilidade; }
    public BigDecimal getValorConsulta()            { return valorConsulta; }
}
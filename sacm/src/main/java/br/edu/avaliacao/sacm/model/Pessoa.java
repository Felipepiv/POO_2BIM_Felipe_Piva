package br.edu.avaliacao.sacm.model;

import java.time.LocalDate;
import java.time.Period;

public abstract class Pessoa {

    private long id;
    private String nome;
    private String cpf;
    private LocalDate dataNascimento;
    private String email;
    private String telefone;
    private String cidade;
    private String estado;
    private LocalDate dataCadastro;
    private StatusPessoa status;

        public int getIdade() { // usei IA para me ajudar a descobrir como calcular a idade pela data de nascimento
            return Period.between(this.dataNascimento, LocalDate.now()).getYears();
    }

    public void atualizarContato(String email, String telefone) { // Troca os dados de contato das pessoas
        this.email = email;
        this.telefone = telefone;
    }

    public void ativar() {
        this.status = StatusPessoa.ATIVO;
    }

    public void desativar() {
        this.status = StatusPessoa.INATIVO;
    }

    public boolean isAtivo() {
        return this.status == StatusPessoa.ATIVO;
    }

    public abstract String getDescricao();
}

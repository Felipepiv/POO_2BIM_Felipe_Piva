package br.edu.avaliacao.sacm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

import br.edu.avaliacao.sacm.enums.StatusPessoa;
import br.edu.avaliacao.sacm.exception.RegraNegocioException;

public abstract class Pessoa {

    private final Long id;
    private String nome;
    private final String cpf;
    private final LocalDate dataNascimento;
    private String email;
    private String telefone;
    private String cidade;
    private String estado;
    private final LocalDateTime dataCadastro;
    private StatusPessoa status;

    // Cadastro novo: sem id, cadastrada agora, começa ATIVA
    protected Pessoa(String nome, String cpf, LocalDate dataNascimento, String email,
                     String telefone, String cidade, String estado) {
        this(null, nome, cpf, dataNascimento, email, telefone, cidade, estado,
             LocalDateTime.now(), StatusPessoa.ATIVO);
    }

    // Reconstrução (usado depois pelo repositório)
    protected Pessoa(Long id, String nome, String cpf, LocalDate dataNascimento, String email,
                     String telefone, String cidade, String estado,
                     LocalDateTime dataCadastro, StatusPessoa status) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("O nome é obrigatório");
        }
        if (cpf == null || cpf.isBlank()) {
            throw new RegraNegocioException("O CPF é obrigatório");
        }
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.email = email;
        this.telefone = telefone;
        this.cidade = cidade;
        this.estado = estado;
        this.dataCadastro = dataCadastro;
        this.status = status;
    }

    public int getIdade() {
        return Period.between(dataNascimento, LocalDate.now()).getYears();
    }

    public void atualizarContato(String email, String telefone) {
        this.email = email;
        this.telefone = telefone;
    }

    public void ativar()    { this.status = StatusPessoa.ATIVO; }
    public void desativar() { this.status = StatusPessoa.INATIVO; }
    public boolean isAtivo() { return this.status == StatusPessoa.ATIVO; }

    public abstract String getDescricao();

    public Long getId()                   { return id; }
    public String getNome()               { return nome; }
    public String getCpf()                { return cpf; }
    public LocalDate getDataNascimento()  { return dataNascimento; }
    public String getEmail()              { return email; }
    public String getTelefone()           { return telefone; }
    public String getCidade()             { return cidade; }
    public String getEstado()             { return estado; }
    public LocalDateTime getDataCadastro(){ return dataCadastro; }
    public StatusPessoa getStatus()       { return status; }
}
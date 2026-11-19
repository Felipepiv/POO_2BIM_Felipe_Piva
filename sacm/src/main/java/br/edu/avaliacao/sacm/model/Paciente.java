package br.edu.avaliacao.sacm.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import br.edu.avaliacao.sacm.enums.StatusPessoa;

public class Paciente extends Pessoa {

    private String numeroProntuario;
    private String convenio;
    private String contatoEmergencia;
    private String telefoneEmergencia;

    // Cadastro novo
    public Paciente(String nome, String cpf, LocalDate dataNascimento, String email,
                    String telefone, String cidade, String estado,
                    String numeroProntuario, String convenio,
                    String contatoEmergencia, String telefoneEmergencia) {
        super(nome, cpf, dataNascimento, email, telefone, cidade, estado);
        this.numeroProntuario = numeroProntuario;
        this.convenio = convenio;
        this.contatoEmergencia = contatoEmergencia;
        this.telefoneEmergencia = telefoneEmergencia;
    }

    // Reconstrução
    public Paciente(Long id, String nome, String cpf, LocalDate dataNascimento, String email,
                    String telefone, String cidade, String estado,
                    LocalDateTime dataCadastro, StatusPessoa status,
                    String numeroProntuario, String convenio,
                    String contatoEmergencia, String telefoneEmergencia) {
        super(id, nome, cpf, dataNascimento, email, telefone, cidade, estado, dataCadastro, status);
        this.numeroProntuario = numeroProntuario;
        this.convenio = convenio;
        this.contatoEmergencia = contatoEmergencia;
        this.telefoneEmergencia = telefoneEmergencia;
    }

    public void atualizarConvenio(String valor) {
        this.convenio = valor;
    }

    public void atualizarContatoEmergencia(String nome, String telefone) {
        this.contatoEmergencia = nome;
        this.telefoneEmergencia = telefone;
    }

    public boolean podeAgendar() {
        return isAtivo();
    }

    @Override
    public String getDescricao() {
        return "Paciente " + getNome() + " | Prontuário " + numeroProntuario
             + " | Convênio " + convenio;
    }

    public String getNumeroProntuario()  { return numeroProntuario; }
    public String getConvenio()          { return convenio; }
    public String getContatoEmergencia() { return contatoEmergencia; }
    public String getTelefoneEmergencia(){ return telefoneEmergencia; }
}
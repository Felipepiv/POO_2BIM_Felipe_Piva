package br.edu.avaliacao.sacm.model;

public class Paciente {
    
    private String numeroProntuario;
    private String convenio;
    private String contatoEmergencia;
    private String telefoneEmergencia;

    public void atualizarConvenio(String convenio) {
        this.convenio = convenio;
    }

    public void atualizarContatoEmergencia(String contatoEmergencia, String telefoneEmergencia) {
        this.contatoEmergencia = contatoEmergencia;
        this.telefoneEmergencia = telefoneEmergencia;
    }

    public boolean podeAgendar() {
        return isAtivo();
    }

    public abstract String getDescricao();
}

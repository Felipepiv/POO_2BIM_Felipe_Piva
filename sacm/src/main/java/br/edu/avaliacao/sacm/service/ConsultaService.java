package br.edu.avaliacao.sacm.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.edu.avaliacao.sacm.enums.StatusConsulta;
import br.edu.avaliacao.sacm.model.Consulta;
import br.edu.avaliacao.sacm.model.Medico;
import br.edu.avaliacao.sacm.model.Paciente;

public class ConsultaService implements Agendavel{ 
    
    private MedicoRepository medicoRepository;
    private PacienteRepository pacienteRepository;
    private ConsultaRepository consultaRepository;

    public ConsultaService(MedicoRepository medicoRepositiry, PacienteRepository pacienteRepository, ConsultaRepository consultaRepository) {
        this.medicoRepository = medicoRepository;
        this.pacienteRepository = pacienteRepository;
        this.consultaRepository = consultaRepository;
    }

    private boolean verificarConflitoHorario(Medico medico, LocalDateTime datahora) { // Ele vai olhar as consukltas do médico e vê se ja existe uma ativa naquele horário
        for (Consulta c : consultaRepository.listarTodos()){
            boolean mesmoMedico = c.getMedico().getID().equals(medico.getID);
            boolean mesmoHorario = c.getDataHora().equals(dataHora);
            boolean ativa = c.getStatus() != StatusConsulta.CANCELADA;
            if (mesmoMedico && mesmoHorario && ativa) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Consulta agendarConsulta(Long idMedico, Long idPaciente,
                                LocalDateTime dataHora, String motivo) { //Funçao que busca médico e paciente, valida as regras, chega conflitos e salva
    Medico medico = medicoRepository.buscarPorId(idMedico);
    Paciente paciente = pacienteRepository.buscarPorId(idPaciente);

    if (medico == null || paciente == null) {
        throw new RegraNegocioException("Médico ou paciente não encontrado");
    }
    if (!medico.podeAtender()) {
        throw new RegraNegocioException("Médico não pode atender");
    }
    if (!paciente.podeAgendar()) {
        throw new RegraNegocioException("Paciente não pode agendar");
    }
    if (verificarConflitoHorario(medico, dataHora)) {
        throw new ConflitoHorarioException("Médico já tem consulta nesse horário");
    }

    Consulta consulta = new Consulta();
    consulta.setMedico(medico);
    consulta.setPaciente(paciente);
    consulta.setDataHora(dataHora);
    consulta.setMotivo(motivo);
    consulta.setStatus(StatusConsulta.AGENDADA);
    consulta.setDataAgendamento(LocalDateTime.now());

    return consultaRepository.salvar(consulta);
    }

    public void confirmarConsulta(Long idConsulta) { //Função que ira confirmar se estivar AGENDA
    Consulta c = buscarConsulta(idConsulta);
    if (c.getStatus() != StatusConsulta.AGENDADA) {
        throw new RegraNegocioException("Só é possível confirmar consulta agendada");
    }
    c.setStatus(StatusConsulta.CONFIRMADA);
    consultaRepository.salvar(c);
    }
    
    @Override // Ira garantir que as assinaturas batem com a interface
    public void cancelarConsulta(Long idConsulta) { //Não pode cancelar uma consulta já realizada ou já cancelada
    Consulta c = buscarConsulta(idConsulta);
    if (c.getStatus() == StatusConsulta.REALIZADA
            || c.getStatus() == StatusConsulta.CANCELADA) {
        throw new RegraNegocioException("Consulta não pode ser cancelada");
    }
    c.setStatus(StatusConsulta.CANCELADA);
    consultaRepository.salvar(c);
    }

    public void finalizarConsulta(Long idConsulta) { // Finaliza se estiver CONFIRMADA
    Consulta c = buscarConsulta(idConsulta);
    if (c.getStatus() != StatusConsulta.CONFIRMADA) {
        throw new RegraNegocioException("Só é possível finalizar consulta confirmada");
    }
    c.setStatus(StatusConsulta.REALIZADA);
    consultaRepository.salvar(c);
    }

    private Consulta buscarConsulta(Long id) { //Usado como método auxiliar para não repetir a busca
    Consulta c = consultaRepository.buscarPorId(id);
    if (c == null) {
        throw new RegraNegocioException("Consulta não encontrada");
    }
    return c;
    }   

    public List<Consulta> listarConsultasPorMedico(Long idMedico) { //Vai percorrer as consultas e guardar só as do médico pedido
    List<Consulta> resultado = new ArrayList<>();
    for (Consulta c : consultaRepository.listarTodos()) {
        if (c.getMedico().getId().equals(idMedico)) {
            resultado.add(c);
        }
    }
    return resultado;
    }

}


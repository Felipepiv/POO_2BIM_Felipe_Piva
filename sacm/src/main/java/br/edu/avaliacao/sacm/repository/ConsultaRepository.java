package br.edu.avaliacao.sacm.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import br.edu.avaliacao.sacm.enums.StatusConsulta;
import br.edu.avaliacao.sacm.exception.RegraNegocioException;
import br.edu.avaliacao.sacm.model.Consulta;

@Repository
public class ConsultaRepository {

    private final JdbcTemplate jdbc;
    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;

    public ConsultaRepository(JdbcTemplate jdbc, MedicoRepository m, PacienteRepository p) {
        this.jdbc = jdbc; this.medicoRepository = m; this.pacienteRepository = p;
    }

    private Consulta montar(ResultSet rs, int n) throws SQLException {
        return new Consulta(rs.getLong("id"),
            medicoRepository.buscarPorId(rs.getLong("id_medico")),
            pacienteRepository.buscarPorId(rs.getLong("id_paciente")),
            rs.getObject("data_hora", LocalDateTime.class), rs.getString("motivo"),
            StatusConsulta.valueOf(rs.getString("status")),
            rs.getObject("data_agendamento", LocalDateTime.class), rs.getString("observacao"));
    }

    public Consulta salvar(Consulta c) {
        try {
            if (c.getId() == null) {                       // nova: INSERT
                String sql = "INSERT INTO consulta (id_medico, id_paciente, data_hora, motivo, "
                           + "status, data_agendamento, observacao) VALUES (?,?,?,?,?,?,?)";
                KeyHolder keys = new GeneratedKeyHolder();
                jdbc.update(con -> {
                    PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    ps.setLong(1, c.getMedico().getId());
                    ps.setLong(2, c.getPaciente().getId());
                    ps.setObject(3, c.getDataHora());
                    ps.setString(4, c.getMotivo());
                    ps.setString(5, c.getStatus().name());
                    ps.setObject(6, c.getDataAgendamento());
                    ps.setString(7, c.getObservacao());
                    return ps;
                }, keys);
                return buscarPorId(keys.getKey().longValue());
            }
            jdbc.update("UPDATE consulta SET status = ?, observacao = ? WHERE id = ?",   // existente: UPDATE (RN07)
                        c.getStatus().name(), c.getObservacao(), c.getId());
            return c;
        } catch (DataAccessException e) {
            throw new RegraNegocioException("Erro ao salvar a consulta", e);
        }
    }

    public Consulta buscarPorId(Long id) {
        try {
            List<Consulta> r = jdbc.query("SELECT * FROM consulta WHERE id = ?", this::montar, id);
            return r.isEmpty() ? null : r.get(0);
        } catch (DataAccessException e) {
            throw new RegraNegocioException("Erro ao buscar a consulta", e);
        }
    }

    public List<Consulta> listarTodos() {
        try {
            return jdbc.query("SELECT * FROM consulta ORDER BY data_hora", this::montar);
        } catch (DataAccessException e) {
            throw new RegraNegocioException("Erro ao listar consultas", e);
        }
    }
}
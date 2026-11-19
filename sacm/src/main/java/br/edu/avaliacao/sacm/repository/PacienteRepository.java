package br.edu.avaliacao.sacm.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import br.edu.avaliacao.sacm.enums.StatusPessoa;
import br.edu.avaliacao.sacm.exception.RegraNegocioException;
import br.edu.avaliacao.sacm.model.Paciente;

@Repository
public class PacienteRepository {

    private final JdbcTemplate jdbc;

    public PacienteRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Paciente> MAPPER = (rs, n) -> new Paciente(
        rs.getLong("id"),
        rs.getString("nome"),
        rs.getString("cpf"),
        rs.getObject("data_nascimento", LocalDate.class),
        rs.getString("email"),
        rs.getString("telefone"),
        rs.getString("cidade"),
        rs.getString("estado"),
        rs.getObject("data_cadastro", LocalDateTime.class),
        StatusPessoa.valueOf(rs.getString("status")),
        rs.getString("numero_prontuario"),
        rs.getString("convenio"),
        rs.getString("contato_emergencia"),
        rs.getString("telefone_emergencia"));

    public Paciente salvar(Paciente p) {
        String sql = "INSERT INTO paciente (nome, cpf, data_nascimento, email, telefone, cidade, "
                   + "estado, data_cadastro, status, numero_prontuario, convenio, "
                   + "contato_emergencia, telefone_emergencia) "
                   + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            KeyHolder keys = new GeneratedKeyHolder();
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, p.getNome());
                ps.setString(2, p.getCpf());
                ps.setObject(3, p.getDataNascimento());
                ps.setString(4, p.getEmail());
                ps.setString(5, p.getTelefone());
                ps.setString(6, p.getCidade());
                ps.setString(7, p.getEstado());
                ps.setObject(8, p.getDataCadastro());
                ps.setString(9, p.getStatus().name());
                ps.setString(10, p.getNumeroProntuario());
                ps.setString(11, p.getConvenio());
                ps.setString(12, p.getContatoEmergencia());
                ps.setString(13, p.getTelefoneEmergencia());
                return ps;
            }, keys);
            return buscarPorId(keys.getKey().longValue());
        } catch (DataAccessException e) {
            throw new RegraNegocioException("Erro ao salvar o paciente", e);
        }
    }

    public Paciente buscarPorId(Long id) {
        try {
            List<Paciente> r = jdbc.query("SELECT * FROM paciente WHERE id = ?", MAPPER, id);
            return r.isEmpty() ? null : r.get(0);
        } catch (DataAccessException e) {
            throw new RegraNegocioException("Erro ao buscar o paciente", e);
        }
    }

    public boolean existePorCpf(String cpf) {
        try {
            Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM paciente WHERE cpf = ?", Integer.class, cpf);
            return n != null && n > 0;
        } catch (DataAccessException e) {
            throw new RegraNegocioException("Erro ao verificar duplicidade de CPF", e);
        }
    }
}

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

import br.edu.avaliacao.sacm.enums.DisponibilidadeMedico;
import br.edu.avaliacao.sacm.enums.Especialidade;
import br.edu.avaliacao.sacm.enums.StatusPessoa;
import br.edu.avaliacao.sacm.exception.RegraNegocioException;
import br.edu.avaliacao.sacm.model.Medico;

@Repository
public class MedicoRepository {

    private final JdbcTemplate jdbc;

    public MedicoRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    private static final RowMapper<Medico> MAPPER = (rs, n) -> new Medico(
        rs.getLong("id"), rs.getString("nome"), rs.getString("cpf"),
        rs.getObject("data_nascimento", LocalDate.class),
        rs.getString("email"), rs.getString("telefone"),
        rs.getString("cidade"), rs.getString("estado"),
        rs.getObject("data_cadastro", LocalDateTime.class),
        StatusPessoa.valueOf(rs.getString("status")),
        rs.getString("crm"), Especialidade.valueOf(rs.getString("especialidade")),
        DisponibilidadeMedico.valueOf(rs.getString("disponibilidade")),
        rs.getBigDecimal("valor_consulta"));

    public Medico salvar(Medico m) {
        String sql = "INSERT INTO medico (nome, cpf, data_nascimento, email, telefone, cidade, estado, "
                   + "data_cadastro, status, crm, especialidade, disponibilidade, valor_consulta) "
                   + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            KeyHolder keys = new GeneratedKeyHolder();
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, m.getNome());
                ps.setString(2, m.getCpf());
                ps.setObject(3, m.getDataNascimento());
                ps.setString(4, m.getEmail());
                ps.setString(5, m.getTelefone());
                ps.setString(6, m.getCidade());
                ps.setString(7, m.getEstado());
                ps.setObject(8, m.getDataCadastro());
                ps.setString(9, m.getStatus().name());
                ps.setString(10, m.getCrm());
                ps.setString(11, m.getEspecialidade().name());
                ps.setString(12, m.getDisponibilidade().name());
                ps.setBigDecimal(13, m.getValorConsulta());
                return ps;
            }, keys);
            return buscarPorId(keys.getKey().longValue());   // devolve já com o id gerado
        } catch (DataAccessException e) {
            throw new RegraNegocioException("Erro ao salvar o médico", e);
        }
    }

    public Medico buscarPorId(Long id) {
        try {
            List<Medico> r = jdbc.query("SELECT * FROM medico WHERE id = ?", MAPPER, id);
            return r.isEmpty() ? null : r.get(0);
        } catch (DataAccessException e) {
            throw new RegraNegocioException("Erro ao buscar o médico", e);
        }
    }

    public boolean existePorCrm(String crm) { return contar("crm", crm) > 0; }
    public boolean existePorCpf(String cpf) { return contar("cpf", cpf) > 0; }

    private int contar(String coluna, String valor) {
        try {
            Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM medico WHERE " + coluna + " = ?", Integer.class, valor);
            return n == null ? 0 : n;
        } catch (DataAccessException e) {
            throw new RegraNegocioException("Erro ao verificar duplicidade", e);
        }
    }
}

// Eles são @Repository e recebem o JdbcTemplate pelo construtor, que o Spring injeta sozinho. Não há conexão manual: o JdbcTemplate abre e fecha tudo. Ele lança DataAccessException, que eu capturo e embrulho em RegraNegocioException guardando a causa (RN09).

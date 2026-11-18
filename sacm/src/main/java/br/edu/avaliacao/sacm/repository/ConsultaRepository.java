package br.edu.avaliacao.sacm.repository;

import java.util.ArrayList;
import java.util.List;
import br.edu.avaliacao.sacm.model.Consulta;

public class ConsultaRepository {

    private List<Consulta> consultas = new ArrayList<>();
    private long proximoId = 1;

    public Consulta salvar(Consulta consulta) {
        if (consulta.getId() == null) {
            consulta.setId(proximoId++);
            consultas.add(consulta);
        }
        return consulta;
    }

    public Consulta buscarPorId(Long id) {
        for (Consulta c : consultas) {
            if (c.getId().equals(id)) {
                return c;
            }
        }
        return null;
    }

    public List<Consulta> listarTodos() {
        return new ArrayList<>(consultas);
    }
}
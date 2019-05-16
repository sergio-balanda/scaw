package ar.edu.unlam.scaw.daos;

import java.util.List;

import ar.edu.unlam.scaw.entities.Auditoria;

public interface AuditoriaDao {

	List<Auditoria> todasLasAuditorias();

	void registrarAuditoria(Auditoria auditoria);

}

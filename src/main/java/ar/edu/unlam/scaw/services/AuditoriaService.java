package ar.edu.unlam.scaw.services;

import java.util.List;

import ar.edu.unlam.scaw.entities.Auditoria;
import ar.edu.unlam.scaw.entities.Usuario;

public interface AuditoriaService {

	String devolverPaginaDeAcuerdoAlRolDelUsuario(Integer intRol);

	List<Auditoria> todasLasAuditorias(Integer idRol);

	List<Auditoria> auditoriasDeUnUsuario(Integer id);

	void registrarAuditoria(Usuario usuario, String accion);

}

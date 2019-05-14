package ar.edu.unlam.scaw.services;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ar.edu.unlam.scaw.daos.AuditoriaDao;
import ar.edu.unlam.scaw.daos.UsuarioDao;
import ar.edu.unlam.scaw.entities.Auditoria;
import ar.edu.unlam.scaw.entities.Usuario;

public class AuditoriaServiceImpl implements AuditoriaService {

	@Autowired
	AuditoriaDao auditoriaDao;

	@Override
	public String devolverPaginaDeAcuerdoAlRolDelUsuario(Integer intRol) {
		switch (intRol) {
		case 1:
			return "historial";
		case 2:
			return "historialUsuario";
		default:
			return "index";
		}
	}

	@Override
	public List<Auditoria> todasLasAuditorias(Integer idRol) {
		if (idRol == 1) {
			return auditoriaDao.todasLasAuditorias();
		} else {
			return (List<Auditoria>) new Auditoria();
		}

	}

	@Override
	public List<Auditoria> auditoriasDeUnUsuario(Integer id) {
		List<Auditoria> auditorias = auditoriaDao.todasLasAuditorias();
		List<Auditoria> listaDeAuditorias = new LinkedList<Auditoria>();

		for (Auditoria auditoria : auditorias) {
			if (auditoria.getIdUsuario().equals(id)) {
				listaDeAuditorias.add(auditoria);
			}
		}
		return listaDeAuditorias;
	}

	@Override
	public void registrarAuditoria(Usuario usuario, String accion) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();

		Auditoria auditoria = new Auditoria();
		auditoria.setAccion(accion.toString());
		auditoria.setCreado(dateFormat.format(date));
		auditoria.setActualizado(dateFormat.format(date));
		auditoria.setIdUsuario(usuario.getId());

		auditoriaDao.registrarAuditoria(auditoria);
	}

}

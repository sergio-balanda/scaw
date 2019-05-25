package ar.edu.unlam.scaw.services;

import java.util.List;

import ar.edu.unlam.scaw.entities.Intento;
import ar.edu.unlam.scaw.entities.Usuario;

public interface IntentoService {
	
	void guardarIntentoDeLogin(String email);

	List<Intento> getIntentosPorEmail(String email);

	String deshabilitarUsuarioPorIntentosFallidos(String email);
}

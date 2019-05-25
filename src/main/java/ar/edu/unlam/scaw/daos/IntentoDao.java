package ar.edu.unlam.scaw.daos;

import java.util.List;

import ar.edu.unlam.scaw.entities.Intento;

public interface IntentoDao {
	
	void guardarIntentoDeLogin(Intento intento);

	List<Intento> getIntentosPorEmail(String email);
}

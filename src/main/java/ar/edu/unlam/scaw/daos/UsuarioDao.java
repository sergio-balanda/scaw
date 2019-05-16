package ar.edu.unlam.scaw.daos;

import java.sql.SQLException;
import java.util.List;

import ar.edu.unlam.scaw.entities.Usuario;

public interface UsuarioDao {

	public List<Usuario> getUsuarios();

	void guardarUsuario(Usuario usuario);

	Usuario buscarUsuarioPorEmailyContraseña(String email, String password);

	void usuarioModificacion(Integer id, String email, String texto, String estado, String password, Integer rol);

}

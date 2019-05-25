package ar.edu.unlam.scaw.services;

import java.util.List;

import ar.edu.unlam.scaw.entities.Salt;
import ar.edu.unlam.scaw.entities.Usuario;

public interface UsuarioService {

	List<Usuario> getUsuarios();

	void guardarUsuario(Usuario usuario);

	Usuario buscarUsuarioPorEmailyContraseña(String email, String password);

	Usuario buscarUsuarioPorId(Integer id);

	Usuario buscarUsuarioPorEmail(String email);

	void usuarioModificacion(Integer id, String email, String texto, String estado, String password, Integer rol);

	Usuario cambiarEstado(Usuario usuario);

	String usuarioModificaPasswordyTexto(String texto, String password, String passwordNuevo, Integer id);

	String enviarEmail(String email, String msj);

	Usuario buscarUsuarioDeshabilitadoParaLasAuditorias();

	boolean validaUsuarioEmail(Usuario usuario);

	boolean validaUsuarioPassword(Usuario usuario);

	boolean validarNoCaracteresEspeciales(String texto);

	boolean contraseñasComunes(Usuario usuario);

	String md5(String password, String salt);

	String habilitarUsuarioPorToken(String token, String email);

	void modificarTokenYFechaDeUnUsuarioANull(Integer id);

	void eliminarUsuario(Integer id);

	void guardarUsuarioConNuevoTokenYFecha(Integer id, String token);

	String recuperarPassword(String token, Usuario usuario);

	String getUrl();

	Salt buscarSaltDeUsuario(Integer id);

	String saltRandom();
}

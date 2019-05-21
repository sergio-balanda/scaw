package ar.edu.unlam.scaw.services;

import java.util.List;

import ar.edu.unlam.scaw.entities.Usuario;

public interface UsuarioService {

	public List<Usuario> getUsuarios();

	void guardarUsuario(Usuario usuario);

	Usuario buscarUsuarioPorEmailyContraseña(String email, String password);

	Usuario buscarUsuarioPorId(Integer id);
	
	Usuario buscarUsuarioPorEmail(String email);
	 
	void usuarioModificacion(Integer id, String email, String texto, String estado, String password, Integer rol);

	Usuario cambiarEstado(Usuario usuario);

	String usuarioModificaPasswordyTexto(String texto, String password, String passwordNuevo,Integer id);
	
	String enviarEmail(String email);
	
	Usuario buscarUsuarioDeshabilitadoParaLasAuditorias();
	
	public boolean validaUsuarioEmail(Usuario usuario);
	
	public boolean validaUsuarioPassword(Usuario usuario);
	
	boolean validarNoCaracteresEspeciales(String texto);
	
	String md5(String password);	
}

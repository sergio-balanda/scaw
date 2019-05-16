package ar.edu.unlam.scaw.services;

import java.util.List;
import java.util.Properties;

import javax.enterprise.inject.New;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.springframework.beans.factory.annotation.Autowired;

import ar.edu.unlam.scaw.daos.UsuarioDao;
import ar.edu.unlam.scaw.daos.UsuarioDaoImpl;
import ar.edu.unlam.scaw.entities.Usuario;
import ar.edu.unlam.scaw.services.UsuarioService;
import junit.framework.Assert;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UsuarioServiceImpl implements UsuarioService {

	@Autowired
	UsuarioDao usuarioDao;
	
	@Override
	public List<Usuario> getUsuarios() {
		return usuarioDao.getUsuarios();
	}

	@Override
	public Usuario buscarUsuarioPorId(Integer id) {
		// TODO Auto-generated method stub
		List<Usuario> usuarios = usuarioDao.getUsuarios();
		for (Usuario usuario : usuarios) {
			if (usuario.getId().equals(id)) {
				return usuario;
			}
		}
		return null;
	}

	@Override
	public void guardarUsuario(Usuario usuario) {
		usuarioDao.guardarUsuario(usuario);
	}

	@Override
	public void usuarioModificacion(Integer id, String email, String texto, String estado, String password,
			Integer rol) {
		//estado = "habilitado";
		usuarioDao.usuarioModificacion(id, email, texto, estado, password, rol);

	}

	public Usuario cambiarEstado(Usuario usuario) {
		Usuario nuevoUsuario = new Usuario();
		nuevoUsuario.setEstado("habilitado");
		if (usuario.getEstado().equals(nuevoUsuario.getEstado())) {
			nuevoUsuario.setEstado("deshabilitado");
		} else {
			nuevoUsuario.setEstado("habilitado");
		}
		return nuevoUsuario;
	}

	@Override
	public Usuario buscarUsuarioPorEmailyContraseña(String email, String password) {
		//System.out.println("email y pass del servicio "+email + " "+password);
		try {
			Usuario usuario = usuarioDao.buscarUsuarioPorEmailyContraseña(email, password);
			Usuario usuarioEquals = new Usuario();
			usuarioEquals.setEstado("habilitado");
			if (usuario.getId() !=null && usuarioEquals.getEstado().equals(usuario.getEstado())) {
				return usuario;
			} else {
				return null;//para el error en el bean
			}
		} catch (Exception e) {
			//System.out.println("error: "+ e.getMessage()+" "+e.getCause());
			return null;
		}

	}

	@Override
	public String usuarioModificaPasswordyTexto(String texto, String passwordViejo,String passwordNuevo, Integer id) {
		// TODO Auto-generated method stub
		Usuario usuario = buscarUsuarioPorId(id);

		if (passwordNuevo != "") {//si el password no es nulo
			Usuario usuarioValidaPass = new Usuario();
			usuarioValidaPass.setPassword(passwordNuevo);
			if(this.validaUsuarioPassword(usuarioValidaPass)==true) {//se valida pass nuevo si es true
				Usuario usuarioDePassActual = new Usuario();
				usuarioDePassActual.setPassword(passwordViejo);
				if(usuario.getPassword().equals(usuarioDePassActual.getPassword())) {//si el pass actual es igual al pass de db
					usuarioModificacion(id, usuario.getEmail(), texto, usuario.getEstado(), passwordNuevo, usuario.getRol());
					return "Campos modificados correctamente.";
				}else {//si los pass actual y db no son iguales
					return "Campo password actual no coincide.";
				}
			}else {//pass validacion false
				return "Campo password nuevo no valido.";
			}
		} 
		usuarioModificacion(id, usuario.getEmail(), texto, usuario.getEstado(), usuario.getPassword(), usuario.getRol());
		return "Campo texto modificado con exito.";
	}
	
	public Usuario buscarUsuarioPorEmail(String email) {
		// TODO Auto-generated method stub
		List<Usuario> usuarios = usuarioDao.getUsuarios();
		for (Usuario usuario : usuarios) {
			if (usuario.getEmail().equals(email)) {
				return usuario;
			}
		}
		return null;
	}
	
	public String enviarEmail(String email) {
		try {
			Usuario usuario = buscarUsuarioPorEmail(email);
			if(usuario==null) {
				return "Usuario no encontrado";
			}
			else {
				// El correo gmail de envío
				String correoEnvia = "***************";
				String claveCorreo = "***************";
				// La configuración para enviar correo
				Properties properties = new Properties();
				properties.put("mail.smtp.host", "smtp.gmail.com");
				properties.put("mail.smtp.starttls.enable", "true");
				properties.put("mail.smtp.port", "587");
				properties.put("mail.smtp.auth", "true");
				properties.put("mail.user", correoEnvia);
				properties.put("mail.password", claveCorreo);

				// Obtener la sesion
				Session session = Session.getInstance(properties, null);

				try {
					// Crear el cuerpo del mensaje
					MimeMessage mimeMessage = new MimeMessage(session);

					// Agregar quien envía el correo
					mimeMessage.setFrom(new InternetAddress(correoEnvia, "SCAW"));

					// Los destinatarios
					InternetAddress[] internetAddresses = { new InternetAddress(email) };

					// Agregar los destinatarios al mensaje
					mimeMessage.setRecipients(Message.RecipientType.TO, internetAddresses);

					// Agregar el asunto al correo
					mimeMessage.setSubject("Scaw a Enviando Correo.");

					// Creo la parte del mensaje
					MimeBodyPart mimeBodyPart = new MimeBodyPart();
					mimeBodyPart.setText("Pedido de recuperacion de contraseña. Su contraseña es: "+usuario.getPassword());

					// Crear el multipart para agregar la parte del mensaje anterior
					Multipart multipart = new MimeMultipart();
					multipart.addBodyPart(mimeBodyPart);

					// Agregar el multipart al cuerpo del mensaje
					mimeMessage.setContent(multipart);

					// Enviar el mensaje
					Transport transport = session.getTransport("smtp");
					transport.connect(correoEnvia, claveCorreo);
					transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
					transport.close();
					return "Se ha enviado un correo a su cuenta.";

				} catch (Exception ex) {
					//System.out.println("/////////"+ex.getMessage());
					ex.printStackTrace();
					return "Error!!!";
				}
			}
		
		} catch (Exception e) {
			return "Error!!!";
		}		
		
	}
	
	
	public String send() {
		return "Error!!!";
	}
	
	@Override
	public boolean validaUsuarioEmail(Usuario usuario) {
		Pattern EMAIL_PATTERN = Pattern.compile("[A-Za-z]+@[a-z]+\\.[a-z]+");
		String email = (String) usuario.getEmail();
		Matcher mather = EMAIL_PATTERN.matcher(email);
		if(!mather.find()){
			return false;
		}
		return true;
	}

	@Override
	public boolean validaUsuarioPassword(Usuario usuario) {
		if(usuario.getPassword().length()<12 || usuario.getPassword().length()>80) {
			return false;
		}else {
			return true;
		}
	}

}
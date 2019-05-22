package ar.edu.unlam.scaw.services;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Properties;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

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

import ar.edu.unlam.scaw.daos.AuditoriaDao;
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
	
	@Autowired
	AuditoriaService auditoriaService;
	
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
		String password = usuario.getPassword();
		usuario.setPassword(this.md5(password));
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
			Usuario usuario = usuarioDao.buscarUsuarioPorEmailyContraseña(email, this.md5(password));
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
				usuarioDePassActual.setPassword(this.md5(passwordViejo));
				if(usuario.getPassword().equals(usuarioDePassActual.getPassword())) {//si el pass actual es igual al pass de db
					usuarioModificacion(id, usuario.getEmail(), texto, usuario.getEstado(), this.md5(passwordNuevo), usuario.getRol());
					String accion = "Campos modificados correctamente usuario "+usuario.getEmail();
					auditoriaService.registrarAuditoria(usuario, accion);
					return accion;
				}else {//si los pass actual y db no son iguales
					String accion = "Error al modicar password actual no coincide usuario "+usuario.getEmail();
					auditoriaService.registrarAuditoria(usuario, accion);
					return "Error al modicar password actual no coincide.";
				}
			}else {//pass validacion false
				String accion = "Error al modicar password nuevo no valido "+usuario.getEmail();
				auditoriaService.registrarAuditoria(usuario, accion);
				return "Campo password nuevo no valido.";
			}
		} 
		usuarioModificacion(id, usuario.getEmail(), texto, usuario.getEstado(), usuario.getPassword(), usuario.getRol());
		String accion = "Campos texto modificado usario "+usuario.getEmail();
		auditoriaService.registrarAuditoria(usuario, accion);
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
				String accion = "Error al enviar email usuario no encontrado";
				auditoriaService.registrarAuditoria(usuario, accion);
				return "Usuario no encontrado";
			}
			else {
				// El correo gmail de envío
				String correoEnvia = "**************";
				String claveCorreo = "**************";
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
					String accion = "Se envio un email al usuario "+usuario.getEmail();
					auditoriaService.registrarAuditoria(usuario, accion);
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
	
	public boolean validarNoCaracteresEspeciales(String texto) {
		Pattern TEXT_PATTERN = Pattern.compile("[$&+,:;=?@#|'<>.^*()%!-]");
		Matcher mather = TEXT_PATTERN.matcher(texto);
		if(!mather.find()){
			return false;
		}
		return true;
	}
	
	public String md5(String password) {

		String secretKey = "scaw";//llave para desenciptar datos
		String passBase64 = "";
		
        try {
 
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digestOfPassword = md.digest(secretKey.getBytes("utf-8"));
            byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
            SecretKey key = new SecretKeySpec(keyBytes, "DESede");
            
            Cipher cipher = Cipher.getInstance("DESede");
            cipher.init(Cipher.ENCRYPT_MODE, key);
 
            byte[] plainTextBytes = password.getBytes("utf-8");
            byte[] buf = cipher.doFinal(plainTextBytes);
            byte[] base64Bytes = Base64.encodeBase64(buf);
            passBase64 = new String(base64Bytes);
 
        } catch (Exception ex) {
        	
        	
        	
        }
        return passBase64;

	}

	@Override
	public Usuario buscarUsuarioDeshabilitadoParaLasAuditorias() {
		// TODO Auto-generated method stub
		return usuarioDao.buscarUsuarioDeshabilitadoParaLasAuditorias();
	}
}

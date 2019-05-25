package ar.edu.unlam.scaw.services;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

import javax.enterprise.inject.New;
import javax.faces.context.FacesContext;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;

import ar.edu.unlam.scaw.daos.AuditoriaDao;
import ar.edu.unlam.scaw.daos.UsuarioDao;
import ar.edu.unlam.scaw.daos.UsuarioDaoImpl;
import ar.edu.unlam.scaw.entities.Intento;
import ar.edu.unlam.scaw.entities.Salt;
import ar.edu.unlam.scaw.entities.TokenGenerator;
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
		Salt salt = new Salt();
		salt.setSalt(saltRandom());
		usuario.setPassword(this.md5(password,salt.getSalt()));
		usuarioDao.guardarUsuario(usuario);
		Usuario usuarioDb = buscarUsuarioPorEmail(usuario.getEmail());
		if (usuarioDb!=null) {
			salt.setUsuario(usuarioDb.getId());
			usuarioDao.guardarSaltDeUsuario(salt);
		}
		this.enviarEmail(usuario.getEmail(), "Ingrese a la siguiente direccion para habilitar usuario: "+getUrl()+"/habilitar-usuario.xhtml?token="+usuario.getToken());//se envia email

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
		try {
			Usuario usuario = buscarUsuarioPorEmail(email);
			//Usuario usuario = usuarioDao.buscarUsuarioPorEmailyContraseña(email, this.md5(password));
			Usuario usuarioEquals = new Usuario();
			usuarioEquals.setEstado("habilitado");
			if (usuario.getId() !=null && usuarioEquals.getEstado().equals(usuario.getEstado())) {
				return usuario;
			} else {
				return null;//para el error en el bean
			}
		} catch (Exception e) {
			return null;
		}

	}

	@Override
	public String usuarioModificaPasswordyTexto(String texto, String passwordViejo,String passwordNuevo, Integer id) {
		// TODO Auto-generated method stub
		Usuario usuario = buscarUsuarioPorId(id);
		Salt salt = buscarSaltDeUsuario(usuario.getId());
		if(usuario==null || salt==null) {
			return "Usuario no encontrado.";
		}
		
		if (passwordNuevo != "") {//si el password no es nulo
			Usuario usuarioValidaPass = new Usuario();
			usuarioValidaPass.setPassword(passwordNuevo);
			if(this.validaUsuarioPassword(usuarioValidaPass)==true && contraseñasComunes(usuarioValidaPass)==true) {//se valida pass nuevo si es true
				Usuario usuarioDePassActual = new Usuario();
				usuarioDePassActual.setPassword(this.md5(passwordViejo,salt.getSalt()));
				if(usuario.getPassword().equals(usuarioDePassActual.getPassword())) {//si el pass actual es igual al pass de db
					usuarioModificacion(id, usuario.getEmail(), texto, usuario.getEstado(), this.md5(passwordNuevo,salt.getSalt()), usuario.getRol());
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
	
	public String enviarEmail(String email, String msj) {
		try {
			Usuario usuario = buscarUsuarioPorEmail(email);
			if(usuario==null) {
				String accion = "Error al enviar email usuario no encontrado";
				auditoriaService.registrarAuditoria(usuario, accion);
				return "Usuario no encontrado";
			}
			else {
				// El correo gmail de envío
				String correoEnvia = "****************";
				String claveCorreo = "****************";
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
					//mimeBodyPart.setText("Pedido de recuperacion de contraseña. Su contraseña es: "+usuario.getPassword());
					mimeBodyPart.setText(msj);
					
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
		if( usuario.getEmail().length()>=30 ) {
			return false;
		}
		
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
		if(texto.length()>=150) {
			return false;
		}
		Pattern TEXT_PATTERN = Pattern.compile("[$&+,:;=?@#|'<>.^*()%!-]");
		Matcher mather = TEXT_PATTERN.matcher(texto);
		if(!mather.find()){
			return false;
		}
		return true;
	}
	
	public String md5(String password,String salt) {

		String secretKey = "scaw"+salt;//llave para desenciptar datos
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
	
	public boolean contraseñasComunes(Usuario usuario) {
		Usuario usuarioUno = new Usuario();
		usuarioUno.setPassword("123456");
		Usuario usuarioDos = new Usuario();
		usuarioDos.setPassword("admin");
		Usuario usuarioTres = new Usuario();
		usuarioTres.setPassword("password");
		Usuario usuarioCuatro = new Usuario();
		usuarioCuatro.setPassword("password1");
		Usuario usuarioCinco = new Usuario();
		usuarioCinco.setPassword("password1234");
		Usuario usuarioSeis = new Usuario();
		usuarioSeis.setPassword("111111111111");
		Usuario usuarioSiete = new Usuario();
		usuarioSiete.setPassword("ADMIN");
		Usuario usuarioOcho = new Usuario();
		usuarioOcho.setPassword("ads3cret");
		Usuario usuarioNueve = new Usuario();
		usuarioNueve.setPassword("adroot");
		Usuario usuarioDiez = new Usuario();
		usuarioDiez.setPassword("admanager");
		
		List<Usuario> listaDeUsuarios = new LinkedList<Usuario>();
		listaDeUsuarios.add(usuarioUno);listaDeUsuarios.add(usuarioDos);listaDeUsuarios.add(usuarioTres);
		listaDeUsuarios.add(usuarioCuatro);listaDeUsuarios.add(usuarioCinco);listaDeUsuarios.add(usuarioSeis);
		listaDeUsuarios.add(usuarioSiete);listaDeUsuarios.add(usuarioOcho);listaDeUsuarios.add(usuarioNueve);
		listaDeUsuarios.add(usuarioDiez);
		
		for (Usuario u : listaDeUsuarios) {
			if ( u.getPassword().equals(usuario.getPassword()) ) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Usuario buscarUsuarioDeshabilitadoParaLasAuditorias() {
		// TODO Auto-generated method stub
		return usuarioDao.buscarUsuarioDeshabilitadoParaLasAuditorias();
	}
	
	//url del sitio
	public String getUrl() {
		Integer port =  ((HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest()).getServerPort();
		String serverName = ((HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest()).getServerName();
		String url= serverName+":"+port.toString()+"/scaw";
		return url;
	}

	@Override
	public String habilitarUsuarioPorToken(String token, String email) {
		
		Usuario usuario = buscarUsuarioPorEmail(email);
		if(usuario==null) {
			return "Usuario no encontrado.";
		}
		TokenGenerator miToken =  new TokenGenerator();

		DateFormat soloHora = new SimpleDateFormat("HH");
		DateFormat soloFecha = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();		
		String fechaActual = soloFecha.format(date);
		String horaActual = soloHora.format(date);
		String fechaDb = soloFecha.format(usuario.getFecha_token());
        String horaDb= soloHora.format(usuario.getFecha_token());

        Integer horaActualInt = Integer.parseInt(horaActual);
        Integer horaDbInt = Integer.parseInt(horaDb);
        Integer resultado = horaActualInt-horaDbInt;
        
		Usuario usuarioEquals = new Usuario();
		usuarioEquals.setToken(token);
		if(usuario.getToken().equals(usuarioEquals.getToken())) {
			if(fechaActual.equals(fechaDb) && resultado <=1) {
				//aca se habilita el usuario y se borran token y fecha
				Usuario usuarioDeshabilitado = new Usuario();
				usuarioDeshabilitado.setEstado("deshabilitado");
				if(usuario.getEstado().equals(usuarioDeshabilitado.getEstado())) {
					modificarTokenYFechaDeUnUsuarioANull(usuario.getId());
					String accion = "Usuario habilitado "+usuario.getEmail();
					auditoriaService.registrarAuditoria(usuario, accion);
					String msj = "Usuario habilitado con exito.";
					enviarEmail(usuario.getEmail(),msj);
					return "Usuario habilitado";
				}
			}
			usuario.setToken(miToken.generateToken());
			guardarUsuarioConNuevoTokenYFecha(usuario.getId(), usuario.getToken());
			this.enviarEmail(usuario.getEmail(), "Ingrese a la siguiente direccion para habilitar usuario: "+getUrl()+"/habilitar-usuario.xhtml?token="+usuario.getToken());
			return "Usuario no habilitado, se ha enviado un correo a su cuenta.";
		}else {
			usuario.setToken(miToken.generateToken());
			guardarUsuarioConNuevoTokenYFecha(usuario.getId(), usuario.getToken());
			this.enviarEmail(usuario.getEmail(), "Ingrese a la siguiente direccion para habilitar usuario: "+getUrl()+"/habilitar-usuario.xhtml?token="+usuario.getToken());
			return "Usuario no habilitado, se ha enviado un correo a su cuenta.";
		}

	}
	
	@Override
	public void modificarTokenYFechaDeUnUsuarioANull(Integer id) {
		usuarioDao.modificarTokenYFechaDeUnUsuarioANull(id);
	}

	@Override
	public void eliminarUsuario(Integer id) {
		usuarioDao.eliminarUsuario(id);
	}

	@Override
	public void guardarUsuarioConNuevoTokenYFecha(Integer id, String token) {
		usuarioDao.guardarUsuarioConNuevoTokenYFecha(id, token);	
	}

	@Override
	public String recuperarPassword(String token,Usuario usuario) {
		Usuario usuarioDb = buscarUsuarioPorEmail(usuario.getEmail());
		if(usuarioDb==null) {
			return "Usuario no encontrado.";
		}
		
		if(usuarioDb.getFecha_token()==null) {
			return "Error por fecha";
		}
		

		TokenGenerator miToken =  new TokenGenerator();
		DateFormat soloHora = new SimpleDateFormat("HH");
		DateFormat soloFecha = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();		
		String fechaActual = soloFecha.format(date);
		String horaActual = soloHora.format(date);
		String fechaDb = soloFecha.format(usuarioDb.getFecha_token());
        String horaDb= soloHora.format(usuarioDb.getFecha_token());
		
        Integer horaActualInt = Integer.parseInt(horaActual);
        Integer horaDbInt = Integer.parseInt(horaDb);
        Integer resultado = horaActualInt-horaDbInt;
        Salt salt = buscarSaltDeUsuario(usuarioDb.getId());
		if(validaUsuarioEmail(usuario)==true && validaUsuarioPassword(usuario)==true && contraseñasComunes(usuario)==true) {
			if (usuario.getToken().equals(usuarioDb.getToken()) ) {
				if(fechaActual.equals(fechaDb) && resultado <=1) {
					
					//Salt salt = buscarSaltDeUsuario(usuarioDb.getId());
					usuarioModificacion(usuarioDb.getId(), usuarioDb.getEmail(), usuarioDb.getTexto(), usuarioDb.getEstado(), md5(usuario.getPassword(),salt.getSalt()), usuarioDb.getRol());
					modificarTokenYFechaDeUnUsuarioANull(usuarioDb.getId());
					String accion = "Usuario modifico su passsword "+usuario.getEmail();
					auditoriaService.registrarAuditoria(usuarioDb, accion);
					String msj = "Contraseña recupera con exito.";
					enviarEmail(usuarioDb.getEmail(),msj);
					return "Recuperacion de contraseña exitosa";
					
				}else {
					usuarioDb.setToken(miToken.generateToken());
					guardarUsuarioConNuevoTokenYFecha(usuarioDb.getId(), usuarioDb.getToken());
					this.enviarEmail(usuarioDb.getEmail(), "Ingrese a la siguiente direccion para habilitar usuario: "+getUrl()+"/recuperar.xhtml?token="+usuarioDb.getToken());
					return "Se ha enviado  un nuevo correo a su cuenta.";
				}
			}else {
				usuarioDb.setToken(miToken.generateToken());
				guardarUsuarioConNuevoTokenYFecha(usuarioDb.getId(), usuarioDb.getToken());
				this.enviarEmail(usuarioDb.getEmail(), "Ingrese a la siguiente direccion para habilitar usuario: "+getUrl()+"/recuperar.xhtml?token="+usuarioDb.getToken());
				return "Se ha enviado  un nuevo correo a su cuenta.";
			}
		}else {
			return "Email o contraseña no validos";
		}

	}
	
	public String saltRandom(){
	    byte[] array = new byte[8]; 
	    new Random().nextBytes(array);
	    String stringRandom = new String(array, Charset.forName("UTF-8"));
	    return stringRandom;
	}

	@Override
	public Salt buscarSaltDeUsuario(Integer id) {
		return usuarioDao.buscarSaltDeUsuario(id);
	}
}

package ar.edu.unlam.scaw.beans;

import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.SeekableByteChannel;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.BusyConversationException;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ar.edu.unlam.scaw.entities.Auditoria;
import ar.edu.unlam.scaw.entities.Intento;
import ar.edu.unlam.scaw.entities.Salt;
import ar.edu.unlam.scaw.entities.TokenGenerator;
import ar.edu.unlam.scaw.entities.Usuario;
import ar.edu.unlam.scaw.entities.VerifyRecaptcha;
import ar.edu.unlam.scaw.services.AuditoriaService;
import ar.edu.unlam.scaw.services.IntentoService;
import ar.edu.unlam.scaw.services.UsuarioService;

@ManagedBean(name = "usuarioBean", eager = true)
@RequestScoped
@SessionScoped
public class UsuarioBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer id = null;
	private String email = null;
	private String password = null;
	private String passwordNuevo = null;
	private String texto = null;
	private String estado = null;
	private Integer rol = null;
	private String token = null;
	private Date fecha_token = null;
	private String error = null;
	private String invalidar = null;
	private List<Auditoria> auditorias;

	// Spring Inject
	ApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "beans.xml" });
	UsuarioService usuarioService = (UsuarioService) context.getBean("usuarioService");
	AuditoriaService auditoriaService = (AuditoriaService) context.getBean("auditoriaService");
	IntentoService intentoService = (IntentoService) context.getBean("intentoService");


	private FacesContext contextSession = FacesContext.getCurrentInstance();
	HttpSession session = (HttpSession) contextSession.getExternalContext().getSession(true);

	// LOGIN
	public String login() {
		boolean captcha = recaptcha();
		Usuario usuario = usuarioService.buscarUsuarioPorEmailyContraseña(this.email, this.password);
		if (usuario == null ||  usuarioService.validaUsuarioPassword(usuario)==false  || captcha==false) {
			String accion = "Usuario "+ email +" no encontrado al loguearse.";
			auditoriaService.registrarAuditoria(usuario, accion);
			error = "Usuario no encontrado";
			
			intentoService.guardarIntentoDeLogin(email);
			invalidar = intentoService.deshabilitarUsuarioPorIntentosFallidos(email);
			return "index";
		} else {
				error = null;
				invalidar= null;
				session.setAttribute("email", usuario.getEmail());
				session.setAttribute("rol", usuario.getRol());
				session.setAttribute("id", usuario.getId());
				String accion = "Usuario "+usuario.getEmail()+" Logueado.";
				auditoriaService.registrarAuditoria(usuario, accion);
				return "home";
		}
	}
	

	//recaptcha
    public Boolean recaptcha(){
    	boolean recaptcha=false;
        try {
       String gRecaptchaResponse = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
    		   .get("g-recaptcha-response");
       boolean verify = VerifyRecaptcha.verify(gRecaptchaResponse);
       if(verify){
    	   return verify;
       }else{
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage( null, new FacesMessage( "Seleccione una opcion") );
            return verify;
         }
        } catch (Exception e) {
        	return  recaptcha;
        }
    }  

	// LOGOUT
	public void logout() {
		ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
		String ctxPath = ((ServletContext) ctx.getContext()).getContextPath();
		try {
			((HttpSession) ctx.getSession(false)).invalidate();
			ctx.redirect(ctxPath + "/faces/index.xhtml");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	// Muestra usuario por id
	public Usuario verUsuario() {
		String id = session.getAttribute("id").toString();
		Integer idUsuario = Integer.parseInt(id);
		Usuario usuario = usuarioService.buscarUsuarioPorId(idUsuario);
		if (usuario != null) {
			return usuario;
		}
		return new Usuario();
	}

	// lista todos los usuarios
	public List<Usuario> getListaDeUsuarios() {
		List<Usuario> listaDeUsuarios = usuarioService.getUsuarios();
		return listaDeUsuarios;
	}

	// Admin ve todos los usuarios listados
	public String verUsuarios() {
		String stringIdRol = session.getAttribute("rol").toString();
		Integer idRol = Integer.parseInt(stringIdRol);

		if (idRol == 1) {
			return "TodosLosUsuarios";
		}

		return "home";
	}

	// guarda usuarios con rol usuario
	public String guardarUsuario() {
		error="";
		Usuario nuevoUsuario = new Usuario();
		nuevoUsuario.setPassword(password);
		nuevoUsuario.setEmail(email);
		TokenGenerator miToken =  new TokenGenerator();
		nuevoUsuario.setToken(miToken.generateToken());
		
		if(usuarioService.validaUsuarioEmail(nuevoUsuario)==true && usuarioService.validaUsuarioPassword(nuevoUsuario)==true && usuarioService.contraseñasComunes(nuevoUsuario)==true) {
			error=null;
			usuarioService.guardarUsuario(nuevoUsuario);//se guarda usuairo
			Usuario usuarioDb = usuarioService.buscarUsuarioPorEmailyContraseña(nuevoUsuario.getEmail(), nuevoUsuario.getPassword());
			if (usuarioDb != null) {
				String accion = "Usuario "+ usuarioDb.getEmail() + " registrado.";
				auditoriaService.registrarAuditoria(usuarioDb,accion);
			}
			error="Se envio un correo a su cuenta.";
			return "index";
		}
		Usuario usuarioDb = usuarioService.buscarUsuarioPorEmailyContraseña(nuevoUsuario.getEmail(), nuevoUsuario.getPassword());
		if(usuarioDb==null) {
			String accion = "Error al registrar usuario "+email;
			auditoriaService.registrarAuditoria(usuarioDb,accion);
		}
		error="Email o Contraseña no validos";
		return "registro";
	}
	
	//Modifica texto o pass
	public String modificarUsuario() {
		String stringIdUsuario = session.getAttribute("id").toString();
		Integer intIdUsuario = Integer.parseInt(stringIdUsuario);
		
		FacesContext con = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) con.getExternalContext().getRequest();
		String cambioDeTexto = request.getParameter("myForm:texto");
		String passwordViejo = request.getParameter("myForm:password");
		String passwordNuevo = request.getParameter("myForm:passwordNuevo");
		Usuario usuarioDb = usuarioService.buscarUsuarioPorId(intIdUsuario);
		if (usuarioDb != null) {
			if(usuarioService.validarNoCaracteresEspeciales(cambioDeTexto)==true) {
				String accion = "Usuario "+ usuarioDb.getEmail() + " error al modificar texto.";
				auditoriaService.registrarAuditoria(usuarioDb,accion);
				error ="Campo texto no permitido";
			}
			else {
				error = usuarioService.usuarioModificaPasswordyTexto(cambioDeTexto, passwordViejo, passwordNuevo, intIdUsuario);
			}
		}
		return "home";
		
	}
	
	// Habilitar Deshabilitar
	public void habilitarDeshabilitar() {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String idString = params.get("habilitarDeshabilitar");
		Integer idInteger = Integer.parseInt(idString);

		String stringIdRol = session.getAttribute("rol").toString();
		Integer intIdRol= Integer.parseInt(stringIdRol);
		if (intIdRol == 1) {
			Usuario usuario = usuarioService.buscarUsuarioPorId(idInteger);
			Usuario nuevoEstado = usuarioService.cambiarEstado(usuario);
			usuarioService.usuarioModificacion(idInteger, usuario.getEmail(), usuario.getTexto(),
					nuevoEstado.getEstado(), usuario.getPassword(), intIdRol);
			String accion = "Se desactivo usuario "+ usuario.getEmail();
			auditoriaService.registrarAuditoria(usuario,accion);
		}

	}
	
	public String habilitarRegistro() {
		error="";
		boolean captcha = recaptcha();
		if(captcha==false) {
			error="Error Captcha incorrecto.";
			return "habilitar-usuario";
		}
		
		FacesContext fc = FacesContext.getCurrentInstance();
		Map<String,String> params = fc.getExternalContext().getRequestParameterMap();
		String data =  params.get("token"); 
	      
		if(data=="" || data ==null) {
			return "index";
		}

	    error = usuarioService.habilitarUsuarioPorToken(data, email);
		return "index";
	}
	
	public String recuperar() {
		error="";
		boolean captcha = recaptcha();
		if(captcha==false) {
			error="Error Captcha incorrecto.";
			return "recuperar";
		}
		FacesContext fc = FacesContext.getCurrentInstance();
		Map<String,String> params = fc.getExternalContext().getRequestParameterMap();
		String data =  params.get("token"); 
		Usuario usuario = new Usuario();
		usuario.setEmail(email);
		usuario.setPassword(password);
		usuario.setToken(data);
		error = usuarioService.recuperarPassword(data, usuario);
		
		return "recuperar";		
	}
	
	//Sessions
	public void verificarSesion() throws IOException {
		if (FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("email") == null) {
			FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
		}
	}
	
	// si no tiene rol 1 (admmin), redirige
	public void verificarRol() throws IOException {
		if (FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("email") != null) {
			Object rol = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("rol");

			if ((Integer) rol != 1) {
				FacesContext.getCurrentInstance().getExternalContext().redirect("home.xhtml");
			} else {
				// System.out.println("sin permisos");
			}
		}
	}
	
	/****************************************************************************************************/
	public UsuarioBean() {
		super();
		this.email = null;
		this.password = null;
		this.passwordNuevo = null;
		this.texto = null;
		this.estado = null;
		this.rol = null;
		this.token = null;
		this.fecha_token = null;
	}

	public Usuario UsuarioBean() {
		// getter setter
		Usuario usuario = new Usuario();
		usuario.setEmail(this.email);
		usuario.setPassword(this.password);
		usuario.setPassword(this.passwordNuevo);
		usuario.setTexto(this.texto);
		usuario.setEstado(this.estado);
		usuario.setRol(this.rol);
		usuario.setToken(this.token);
		usuario.setFecha_token(this.fecha_token);
		return usuario;
	}

	// get y set
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public Integer getRol() {
		return rol;
	}

	public void setRol(Integer rol) {
		this.rol = rol;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public List<Auditoria> getAuditorias() {
		return auditorias;
	}

	public void setAuditorias(List<Auditoria> auditorias) {
		this.auditorias = auditorias;
	}

	public String getPasswordNuevo() {
		return passwordNuevo;
	}

	public void setPasswordNuevo(String passwordNuevo) {
		this.passwordNuevo = passwordNuevo;
	}

	public String getInvalidar() {
		return invalidar;
	}

	public void setInvalidar(String invalidar) {
		this.invalidar = invalidar;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Date getFecha_token() {
		return fecha_token;
	}

	public void setFecha_token(Date fecha_token) {
		this.fecha_token = fecha_token;
	}
	
}

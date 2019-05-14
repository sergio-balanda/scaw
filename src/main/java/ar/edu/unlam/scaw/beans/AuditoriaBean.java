package ar.edu.unlam.scaw.beans;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ar.edu.unlam.scaw.entities.Auditoria;
import ar.edu.unlam.scaw.entities.Usuario;
import ar.edu.unlam.scaw.services.AuditoriaService;
import ar.edu.unlam.scaw.services.UsuarioService;

@ManagedBean(name = "auditoriaBean", eager = true)
@RequestScoped
@SessionScoped
public class AuditoriaBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private Integer id = null;
	private Integer idUsuario = null;
	private String accion = null;
	private String creado = null;
	private String actualizado = null;
	private String error = null;
	private Usuario usuario;

	public AuditoriaBean() {
		super();
		this.accion = null;
		this.creado = null;
		this.actualizado = null;
		this.error = null;
	}

	public Auditoria AuditoriaBean() {
		// getter setter
		Auditoria auditoria = new Auditoria();
		auditoria.setAccion(this.accion);
		auditoria.setCreado(this.creado);
		auditoria.setActualizado(this.actualizado);
		return auditoria;
	}

	// Spring Inject
	ApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "beans.xml" });
	AuditoriaService auditoriaService = (AuditoriaService) context.getBean("auditoriaService");
	
	private FacesContext contextSession = FacesContext.getCurrentInstance();
	HttpSession session = (HttpSession) contextSession.getExternalContext().getSession(true);
	
	
	public String historial() {
		String idRol = session.getAttribute("rol").toString();
		Integer intRol = Integer.parseInt(idRol);
		String url = auditoriaService.devolverPaginaDeAcuerdoAlRolDelUsuario(intRol); 
		return url;
	}
	
	public List<Auditoria> todasLasAuditorias(){
		String idRol = session.getAttribute("rol").toString();
		Integer intRol = Integer.parseInt(idRol);
		return auditoriaService.todasLasAuditorias(intRol);
	}
	
	public List<Auditoria> auditoriasDeUnUsuario(){
		String idUsuario = session.getAttribute("id").toString();
		Integer intIdUsuario = Integer.parseInt(idUsuario);
		return auditoriaService.auditoriasDeUnUsuario(intIdUsuario);
	}
	

	/**************************************************************/

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getAccion() {
		return accion;
	}

	public void setAccion(String accion) {
		this.accion = accion;
	}

	public String getCreado() {
		return creado;
	}

	public void setCreado(String creado) {
		this.creado = creado;
	}

	public String getActualizado() {
		return actualizado;
	}

	public void setActualizado(String actualizado) {
		this.actualizado = actualizado;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}
	
}

package ar.edu.unlam.scaw.beans;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ar.edu.unlam.scaw.services.UsuarioService;

@ManagedBean(name = "enviarEmailBean")
@RequestScoped
@SessionScoped
public class EnviarEmailBean implements Serializable {
	private String email;
	private String error;

	// Spring Inject
	ApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "beans.xml" });
	UsuarioService usuarioService = (UsuarioService) context.getBean("usuarioService");

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public void send() {
		if(email!="") {
			error = usuarioService.enviarEmail(email);
		}else {
			error="El campo email es obligatorio";
		}
	
	}

}

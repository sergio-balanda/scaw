package ar.edu.unlam.scaw.beans;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ar.edu.unlam.scaw.entities.TokenGenerator;
import ar.edu.unlam.scaw.entities.Usuario;
import ar.edu.unlam.scaw.entities.VerifyRecaptcha;
import ar.edu.unlam.scaw.services.AuditoriaService;
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
	AuditoriaService auditoriaService = (AuditoriaService) context.getBean("auditoriaService");

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
		Boolean b =  recaptcha();
		if(b==false) {
			error="Error Captcha incorrecto.";
		}
		
		if(email!="") {
			TokenGenerator token = new TokenGenerator();
			Usuario usuarioToken = new Usuario();
			usuarioToken.setToken(token.generateToken());
			String url = usuarioService.getUrl();
			String msj = "Pedido de recuperacion de contraseña. Para recuperar su contrase ingrese: "+url+"/recuperar.xhtml?token="+usuarioToken.getToken();
			Usuario usuario = usuarioService.buscarUsuarioPorEmail(email);
			if(usuario==null) {
				error="Usuario no encontrado";
			}
			usuarioService.guardarUsuarioConNuevoTokenYFecha(usuario.getId(), usuarioToken.getToken());
			error = usuarioService.enviarEmail(email,msj);
		}else {
			Usuario usuario = new Usuario();
			String accion = "Error al enviar email de recuperacion para email"+email;
			auditoriaService.registrarAuditoria(usuario, accion);
			error="El campo email es obligatorio";
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
}

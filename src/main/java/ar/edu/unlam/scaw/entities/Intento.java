package ar.edu.unlam.scaw.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Intento implements Serializable{
	private static final long serialVersionUID = 1L;
	private Integer id;
	private String email;
	private Integer usuario;
	private Integer intento;
	private Date fecha_intento;

	public Intento() {

	}

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

	public Integer getUsuario() {
		return usuario;
	}

	public void setUsuario(Integer usuario) {
		this.usuario = usuario;
	}

	public Integer getIntento() {
		return intento;
	}

	public void setIntento(Integer intento) {
		this.intento = intento;
	}

	public Date getFecha_intento() {
		return fecha_intento;
	}

	public void setFecha_intento(Date fecha_intento) {
		this.fecha_intento = fecha_intento;
	}

	
}

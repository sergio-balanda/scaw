package ar.edu.unlam.scaw.entities;

import java.io.Serializable;

public class Salt implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer id;
	private String salt;
	private Integer usuario;

	public Salt() {

	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public Integer getUsuario() {
		return usuario;
	}

	public void setUsuario(Integer usuario) {
		this.usuario = usuario;
	}
}

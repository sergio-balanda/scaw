package ar.edu.unlam.scaw.entities;

import java.io.Serializable;

public class Auditoria {
	private static final long serialVersionUID = 1L;
	private Integer id;
	private String accion;
	private String creado;
	private String actualizado;
	private Integer idUsuario;
	private Usuario usuario;

	public Auditoria() {

	}

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

	public Integer getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(Integer idUsuario) {
		this.idUsuario = idUsuario;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

}

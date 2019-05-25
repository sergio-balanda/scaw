package ar.edu.unlam.scaw.daos;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.server.Server;
import org.hsqldb.server.ServerAcl.AclFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import ar.edu.unlam.scaw.entities.Auditoria;
import ar.edu.unlam.scaw.entities.Usuario;

public class AuditoriaDaoImpl implements AuditoriaDao {

	Connection connection = null;
	java.sql.Statement statement = null;
	ResultSet resultSet = null;

	// CONEXION A HSQLDB
	public boolean HSQLDBMain() {
		try {
			HsqlProperties hsqlProperties = new HsqlProperties();
			hsqlProperties.setProperty("server.database.0", "file:/Java/projects/seguridad/db/scaw");
			hsqlProperties.setProperty("server.dbname.0", "mdb");

			Server server = new Server();
			server.setProperties(hsqlProperties);
			server.setTrace(false);
			server.start();
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
			connection = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/mdb", "SA", "");
			statement = connection.createStatement();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return true;
	}

	public AuditoriaDaoImpl() {
		super();
	}

	@Override
	public List<Auditoria> todasLasAuditorias() {
		List<Auditoria> listaDeAuditorias = new LinkedList<Auditoria>();
		try {
			this.HSQLDBMain();
			ResultSet rs = statement.executeQuery(
					"SELECT AUDITORIA.ID as idAuditoria, AUDITORIA.ACCION as auditoriaAccion, AUDITORIA.ACTUALIZADO as auditoriaActualizado, AUDITORIA.CREADO as auditoriaCreado, AUDITORIA.USUARIO as auditoriaUsuario, USUARIO.EMAIL as usuarioEmail FROM AUDITORIA INNER JOIN USUARIO ON USUARIO.ID = AUDITORIA.USUARIO ORDER BY ID");

			while (rs.next()) {

				Integer id = rs.getInt("idAuditoria");
				String accion = rs.getString("auditoriaAccion");
				String actualizado = rs.getString("auditoriaActualizado");
				String creado = rs.getString("auditoriaCreado");
				Integer idUsuario = rs.getInt("auditoriaUsuario");
				String email = rs.getString("usuarioEmail");

				Auditoria auditoria = new Auditoria();
				auditoria.setId(id);
				auditoria.setAccion(accion);
				auditoria.setActualizado(actualizado);
				auditoria.setCreado(creado);
				auditoria.setIdUsuario(idUsuario);
				Usuario usuario = new Usuario();
				usuario.setEmail(email);
				auditoria.setUsuario(usuario);
				listaDeAuditorias.add(auditoria);
			}
			rs.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return listaDeAuditorias;
	}

	@Override
	public void registrarAuditoria(Auditoria auditoria) {
		try {
			this.HSQLDBMain();
		        PreparedStatement ps = statement.getConnection()
		        		.prepareStatement("INSERT INTO AUDITORIA (ACCION,CREADO,ACTUALIZADO,USUARIO) VALUES  (?,?,?,?)");
		        ps.setString(1, auditoria.getAccion());
		        ps.setString(2, auditoria.getCreado());
		        ps.setString(3, auditoria.getActualizado());
		        ps.setInt(4, auditoria.getIdUsuario());
		        ps.executeUpdate();
		        ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void registrarAuditoriaDeIntentosFallidos(Auditoria auditoria) {
		try {
			this.HSQLDBMain();
		        PreparedStatement ps = statement.getConnection()
		        		.prepareStatement("INSERT INTO AUDITORIA (ACCION,CREADO,ACTUALIZADO,USUARIO) VALUES  (?,?,?,NULL)");
		        ps.setString(1, auditoria.getAccion());
		        ps.setString(2, auditoria.getCreado());
		        ps.setString(3, auditoria.getActualizado());
		        ps.executeUpdate();
		        ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}

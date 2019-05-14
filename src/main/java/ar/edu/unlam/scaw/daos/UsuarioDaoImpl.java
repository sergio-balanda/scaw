package ar.edu.unlam.scaw.daos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.server.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.sun.mail.imap.ResyncData;

import ar.edu.unlam.scaw.entities.Usuario;

public class UsuarioDaoImpl implements UsuarioDao {

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

	public UsuarioDaoImpl() {
		super();
	}

	@Override
	public List<Usuario> getUsuarios() {
		List<Usuario> listaDeUsuarios = new LinkedList<Usuario>();

		try {
			this.HSQLDBMain();
			ResultSet rs = statement.executeQuery("SELECT * FROM USUARIO");
			while (rs.next()) {
				String email = rs.getString("email");
				String password = rs.getString("password");
				String estado = rs.getString("estado");
				Integer id = rs.getInt("id");
				String texto = rs.getString("texto");
				Integer rol = rs.getInt("rol");

				Usuario usuario = new Usuario();
				usuario.setEmail(email);
				usuario.setPassword(password);
				usuario.setEstado(estado);
				usuario.setId(id);
				usuario.setTexto(texto);
				usuario.setRol(rol);

				listaDeUsuarios.add(usuario);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return listaDeUsuarios;
	}

	public void guardarUsuario(Usuario usuario) {
		try {
			this.HSQLDBMain();
			usuario.setEstado("habilitado");
			usuario.setTexto("");
			statement.executeUpdate("INSERT INTO USUARIO (EMAIL,PASSWORD,TEXTO,ESTADO,ROL) VALUES ('"+usuario.getEmail()+"', '"+usuario.getPassword()+"','','habilitado',2)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Usuario buscarUsuarioPorEmailyContraseña(String email, String password) {
		Usuario usuario = new Usuario();

		try {
			this.HSQLDBMain();
			String sql = "SELECT * FROM USUARIO WHERE EMAIL = ? AND PASSWORD = ?";
			PreparedStatement ps = statement.getConnection().prepareStatement(sql);
			ps.setString(1, email);
			ps.setString(2, password);
			resultSet = ps.executeQuery();
			while (resultSet.next()) {
				String rsEmail = resultSet.getString("email");
				String rsPassword = resultSet.getString("password");
				Integer id = resultSet.getInt("id");
				String texto = resultSet.getString("texto");
				String estado = resultSet.getString("estado");
				Integer rol = resultSet.getInt("rol");

				usuario.setEmail(rsEmail);
				usuario.setPassword(rsPassword);
				usuario.setId(id);
				usuario.setTexto(texto);
				usuario.setEstado(estado);
				usuario.setRol(rol);
			}
			resultSet.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return usuario;
	}

	@Override
	public void usuarioModificacion(Integer id, String email, String texto, String estado, String password,
			Integer rol) {
		try {
			this.HSQLDBMain();
	        PreparedStatement ps = statement.getConnection().prepareStatement("UPDATE USUARIO SET TEXTO = ?, PASSWORD = ?, ESTADO = ?  WHERE ID = ?");
	        ps.setString(1, texto);
	        ps.setString(2, password);
	        ps.setString(3, estado);
	        ps.setInt(4, id);
	        ps.executeUpdate();
	        ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}

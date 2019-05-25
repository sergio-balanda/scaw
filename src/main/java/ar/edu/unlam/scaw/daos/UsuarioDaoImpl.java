package ar.edu.unlam.scaw.daos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.server.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.sun.mail.imap.ResyncData;

import ar.edu.unlam.scaw.entities.Salt;
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
			ResultSet rs = statement.executeQuery("SELECT * FROM USUARIO WHERE ID <> 1");
			while (rs.next()) {
				String email = rs.getString("email");
				String password = rs.getString("password");
				String estado = rs.getString("estado");
				Integer id = rs.getInt("id");
				String texto = rs.getString("texto");
				Integer rol = rs.getInt("rol");
				String token = rs.getString("token");
				Timestamp fecha_token = rs.getTimestamp("fecha_token");

				Usuario usuario = new Usuario();
				usuario.setEmail(email);
				usuario.setPassword(password);
				usuario.setEstado(estado);
				usuario.setId(id);
				usuario.setTexto(texto);
				usuario.setRol(rol);
				usuario.setToken(token);
				usuario.setFecha_token(fecha_token);

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
			//statement.executeUpdate("INSERT INTO USUARIO (EMAIL,PASSWORD,TEXTO,ESTADO,ROL,TOKEN,FECHA_TOKEN) VALUES ('"+usuario.getEmail()+"', '"+usuario.getPassword()+"','','deshabilitado', 2, '"+usuario.getToken()+"', NOW())");
			PreparedStatement ps = statement.getConnection().prepareStatement("INSERT INTO USUARIO (EMAIL,PASSWORD,TEXTO,ESTADO,ROL,TOKEN,FECHA_TOKEN) VALUES (?, ?,'','deshabilitado', 2, ?, NOW())");
	        ps.setString(1, usuario.getEmail());
	        ps.setString(2, usuario.getPassword());
	        ps.setString(3, usuario.getToken());
	        ps.executeUpdate();
	        ps.close();
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
				String token = resultSet.getString("token");
				Timestamp fecha_token = resultSet.getTimestamp("fecha_token");

				usuario.setEmail(rsEmail);
				usuario.setPassword(rsPassword);
				usuario.setId(id);
				usuario.setTexto(texto);
				usuario.setEstado(estado);
				usuario.setRol(rol);
				usuario.setToken(token);
				usuario.setFecha_token(fecha_token);
			}
			resultSet.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return usuario;
	}
	
	@Override
	public Usuario buscarUsuarioDeshabilitadoParaLasAuditorias() {
		Usuario usuario = new Usuario();

		try {
			this.HSQLDBMain();
			String sql = "SELECT * FROM USUARIO WHERE ID = 1";
			PreparedStatement ps = statement.getConnection().prepareStatement(sql);
			resultSet = ps.executeQuery();
			while (resultSet.next()) {
				String rsEmail = resultSet.getString("email");
				String rsPassword = resultSet.getString("password");
				Integer id = resultSet.getInt("id");
				String texto = resultSet.getString("texto");
				String estado = resultSet.getString("estado");
				Integer rol = resultSet.getInt("rol");
				String token = resultSet.getString("token");
				Timestamp fecha_token = resultSet.getTimestamp("fecha_token");

				usuario.setEmail(rsEmail);
				usuario.setPassword(rsPassword);
				usuario.setId(id);
				usuario.setTexto(texto);
				usuario.setEstado(estado);
				usuario.setRol(rol);
				usuario.setToken(token);
				usuario.setFecha_token(fecha_token);
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
	
	@Override
	public void modificarTokenYFechaDeUnUsuarioANull(Integer id) {
		try {
			this.HSQLDBMain();
	        PreparedStatement ps = statement.getConnection().prepareStatement("UPDATE USUARIO SET TOKEN = '', FECHA_TOKEN = NULL, ESTADO = 'habilitado' WHERE ID = ?");
	        ps.setInt(1, id);
	        ps.executeUpdate();
	        ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void guardarUsuarioConNuevoTokenYFecha(Integer id, String token) {
		try {
			this.HSQLDBMain();
	        PreparedStatement ps = statement.getConnection().prepareStatement("UPDATE USUARIO SET TOKEN = ?, FECHA_TOKEN = NOW() WHERE ID = ?");
	        ps.setString(1, token);
	        ps.setInt(2, id);
	        ps.executeUpdate();
	        ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void eliminarUsuario(Integer id) {
		try {
			this.HSQLDBMain();
	        PreparedStatement ps = statement.getConnection().prepareStatement("DELETE FROM USUARIO WHERE WHERE ID = ?");
	        ps.setInt(1, id);
	        ps.executeUpdate();
	        ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void guardarSaltDeUsuario(Salt salt) {
		System.out.println("dao "+salt.getUsuario());
		try {
			this.HSQLDBMain();
			PreparedStatement ps = statement.getConnection().prepareStatement("INSERT INTO SALT (SALT,USUARIO) VALUES (?, ?)");
	        ps.setString(1, salt.getSalt());
	        ps.setInt(2, salt.getUsuario());
	        ps.executeUpdate();
	        ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Salt buscarSaltDeUsuario(Integer id) {
		Salt salt = new Salt();

		try {
			this.HSQLDBMain();
			String sql = "SELECT * FROM SALT WHERE USUARIO = ?";
			PreparedStatement ps = statement.getConnection().prepareStatement(sql);
			ps.setInt(1, id);
			resultSet = ps.executeQuery();
			while (resultSet.next()) {
				String saltDb = resultSet.getString("salt");
				Integer usuario = resultSet.getInt("usuario");
				Integer idDb = resultSet.getInt("id");

				salt.setSalt(saltDb);
				salt.setUsuario(usuario);
				salt.setId(idDb);
			}
			resultSet.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return salt;
	}

}

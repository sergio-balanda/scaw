package ar.edu.unlam.scaw.daos;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.server.Server;

import ar.edu.unlam.scaw.entities.Intento;
import ar.edu.unlam.scaw.entities.Usuario;

public class IntentoDaoImpl implements IntentoDao {
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

	public IntentoDaoImpl() {
		super();
	}

	@Override
	public void guardarIntentoDeLogin(Intento intento) {
		try {
			this.HSQLDBMain();
		        PreparedStatement ps = statement.getConnection()
		        		.prepareStatement("INSERT INTO INTENTO (EMAIL,INTENTO,USUARIO,FECHA_INTENTO) VALUES  (?,?,?,NOW())");
		        ps.setString(1, intento.getEmail());
		        ps.setInt(2, intento.getIntento());
		        ps.setInt(3, intento.getUsuario());
		        ps.executeUpdate();
		        ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public List<Intento> getIntentosPorEmail(String email) {
		List<Intento> listaDeIntentos = new LinkedList<Intento>();

		try {
			this.HSQLDBMain();
			String sql = "SELECT * FROM INTENTO WHERE EMAIL = ?";
			PreparedStatement ps = statement.getConnection().prepareStatement(sql);
			ps.setString(1, email);
			resultSet = ps.executeQuery();
			while (resultSet.next()) {
				String emailDb = resultSet.getString("email");
				Integer usuario = resultSet.getInt("usuario");
				Integer intento = resultSet.getInt("intento");
				Timestamp fecha_intento = resultSet.getTimestamp("fecha_intento");
				
				Intento nuevoIntento = new Intento();
				nuevoIntento.setEmail(emailDb);
				nuevoIntento.setUsuario(usuario);
				nuevoIntento.setIntento(intento);
				nuevoIntento.setFecha_intento(fecha_intento);
				listaDeIntentos.add(nuevoIntento);
			}
			resultSet.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return listaDeIntentos;
	}
}

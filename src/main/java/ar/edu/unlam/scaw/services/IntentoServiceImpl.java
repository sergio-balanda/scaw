package ar.edu.unlam.scaw.services;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ar.edu.unlam.scaw.daos.IntentoDao;
import ar.edu.unlam.scaw.entities.Auditoria;
import ar.edu.unlam.scaw.entities.Intento;
import ar.edu.unlam.scaw.entities.Usuario;


public class IntentoServiceImpl implements IntentoService{
	@Autowired
	IntentoDao intentoDao;
	
	@Autowired
	UsuarioService usuarioService;

	@Override
	public void guardarIntentoDeLogin(String email) {
		Usuario usuarioDb = usuarioService.buscarUsuarioPorEmail(email);
		if(usuarioDb!=null) {
			Intento nuevoIntento = new Intento();
			Integer nro = 1;
			nuevoIntento.setEmail(email);
			nuevoIntento.setUsuario(usuarioDb.getId());
			nuevoIntento.setIntento(nro);
			intentoDao.guardarIntentoDeLogin(nuevoIntento);
		}else {
			//System.out.println("usuario null");
		}
	}

	@Override
	public List<Intento> getIntentosPorEmail(String email) {
		// TODO Auto-generated method stub
		return intentoDao.getIntentosPorEmail(email);
	}
	
	public String deshabilitarUsuarioPorIntentosFallidos(String email) {
		List<Intento> listaDeIntentosDb = getIntentosPorEmail(email);
		DateFormat dateFormat = new SimpleDateFormat("HH");
		DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();		
		String actual = dateFormat2.format(date);
		String fechaHoraActual = dateFormat.format(date);
		
        Integer horaActualInt = Integer.parseInt(fechaHoraActual);

		
		List<Intento> listaB = new LinkedList<Intento>();
		for(Intento i :listaDeIntentosDb)
		{
			String fechaDb = dateFormat2.format(i.getFecha_intento());
			if(   actual.equals(fechaDb) ) {//si las fechas son igual es decir ejemplo 22-02-02 = 22-02-02
				
	            String fechaHoraDb= dateFormat.format(i.getFecha_intento());
	            Integer horaDbInt = Integer.parseInt(fechaHoraDb);
	            Integer resultado = horaActualInt-horaDbInt;
            	if( resultado <=1 ){//compara solo la hora
            		listaB.add(i);
            	}
            	
			}
		}
		if(listaB.size()>3) {
			Usuario usuario = usuarioService.buscarUsuarioPorEmail(email);
			if(usuario==null) {
				return "Usuario no encontrado";
			}
			else {
				Usuario usuarioEquals = new Usuario();
				usuarioEquals.setEstado("habilitado");
				if(usuarioEquals.getEstado().equals(usuario.getEstado())) {
					Usuario usuarioConCambioDeEstado = usuarioService.cambiarEstado(usuario);
					usuarioService.usuarioModificacion(usuario.getId(), usuario.getEmail(), usuario.getTexto(),
							usuarioConCambioDeEstado.getEstado(), usuario.getPassword(), usuario.getRol());
					return "Usuario deshabilitado, intente recuperar su contraseña.";
				}
			}
		}
		return "";
	}
}

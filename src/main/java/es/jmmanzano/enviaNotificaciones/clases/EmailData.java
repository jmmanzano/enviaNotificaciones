package es.jmmanzano.enviaNotificaciones.clases;

public class EmailData {
	private String numero;
	private String nombre;
	private String email;
	
	
	
	public EmailData(String numero, String nombre, String email) {
		this.numero = numero;
		this.nombre = nombre;
		this.email = email;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getNumero() {
		return numero;
	}
	public void setNumero(String numero) {
		this.numero = numero;
	}

}

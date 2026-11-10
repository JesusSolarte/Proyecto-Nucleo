package co.edu.unbosque.proyectonucleo1.dto;

/**
 * Datos necesarios para iniciar sesión.
 */
public class LoginDTO {

	private String nombreUsuario;
	private String contrasena;

	public LoginDTO() {
	}

	public LoginDTO(String nombreUsuario, String contrasena) {
		this.nombreUsuario = nombreUsuario;
		this.contrasena = contrasena;
	}

	public String getNombreUsuario() {
		return nombreUsuario;
	}

	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}

	public String getContrasena() {
		return contrasena;
	}

	public void setContrasena(String contrasena) {
		this.contrasena = contrasena;
	}

}

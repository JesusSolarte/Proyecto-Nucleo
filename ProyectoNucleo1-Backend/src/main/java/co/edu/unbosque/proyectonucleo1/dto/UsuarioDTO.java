package co.edu.unbosque.proyectonucleo1.dto;

import java.util.Objects;

import co.edu.unbosque.proyectonucleo1.util.enums.RolUsuario;

/**
 * Objeto de transferencia de datos para {@link co.edu.unbosque.proyectonucleo1.entity.Usuario}.
 */
public class UsuarioDTO {

	private Long id;
	private String nombre;
	private String apellido;
	private String correo;
	private String nombreUsuario;
	private String contrasena;
	private String telefono;
	private RolUsuario rol;

	public UsuarioDTO() {
	}

	public UsuarioDTO(Long id, String nombre, String apellido, String correo, String nombreUsuario, String contrasena,
			String telefono, RolUsuario rol) {
		this.id = id;
		this.nombre = nombre;
		this.apellido = apellido;
		this.correo = correo;
		this.nombreUsuario = nombreUsuario;
		this.contrasena = contrasena;
		this.telefono = telefono;
		this.rol = rol;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getApellido() {
		return apellido;
	}

	public void setApellido(String apellido) {
		this.apellido = apellido;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
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

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public RolUsuario getRol() {
		return rol;
	}

	public void setRol(RolUsuario rol) {
		this.rol = rol;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		UsuarioDTO other = (UsuarioDTO) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return "UsuarioDTO [id=" + id + ", nombre=" + nombre + ", apellido=" + apellido + ", correo=" + correo
				+ ", nombreUsuario=" + nombreUsuario + ", telefono=" + telefono + ", rol=" + rol + "]";
	}

}

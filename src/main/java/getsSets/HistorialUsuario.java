
package getsSets;

public class HistorialUsuario {
    private int id;
    private int usuarioId;
    private String accion;
    private String tablaAfectada;
    private int registroAfectadoId;
    private String descripcion;
    private String fecha; // Formato: YYYY-MM-DD para el JS
    private String hora;  // Formato: HH:MM:SS para el JS

    // Constructor vacío
    public HistorialUsuario() {}

    // Constructor completo para lectura
    public HistorialUsuario(int id, int usuarioId, String accion, String descripcion, String fecha, String hora) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.accion = accion;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.hora = hora;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getTablaAfectada() { return tablaAfectada; }
    public void setTablaAfectada(String tablaAfectada) { this.tablaAfectada = tablaAfectada; }

    public int getRegistroAfectadoId() { return registroAfectadoId; }
    public void setRegistroAfectadoId(int registroAfectadoId) { this.registroAfectadoId = registroAfectadoId; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }


}

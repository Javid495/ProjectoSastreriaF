
package modelo;


public class DetallesPedidoMedida {
    
    private int idPedidoMedida;
    private int idUsuario;
    private String medidas;
    private String tipoPrenda;
    private String tela;
    private String descripcion;
    private String imagenReferencia;
    

    public int getIdPedidoMedida() {
        return idPedidoMedida;
    }

    public void setIdPedidoMedida(int idPedidoMedida) {
        this.idPedidoMedida = idPedidoMedida;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getMedidas() {
        return medidas;
    }

    public void setMedidas(String medidas) {
        this.medidas = medidas;
    }

    public String getTipoPrenda() {
        return tipoPrenda;
    }

    public void setTipoPrenda(String tipoPrenda) {
        this.tipoPrenda = tipoPrenda;
    }

    public String getTela() {
        return tela;
    }

    public void setTela(String tela) {
        this.tela = tela;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getImagenReferencia() {
        return imagenReferencia;
    }

    public void setImagenReferencia(String imagenReferencia) {
        this.imagenReferencia = imagenReferencia;
    }
}

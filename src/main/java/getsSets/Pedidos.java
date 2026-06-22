
package getsSets;

// Getters y setter que hacen referencia a la tabla de pedidos
public class Pedidos {
    
    private int idPedido;
    private int NuevoPedidoId;
    private String FechaInicio;
    private String EstadoPedido;
    private String Direccion;
    private String TipoCompra; 

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public int getNuevoPedidoId() {
        return NuevoPedidoId;
    }

    public void setNuevoPedidoId(int NuevoPedidoId) {
        this.NuevoPedidoId = NuevoPedidoId;
    }

    public String getFechaInicio() {
        return FechaInicio;
    }

    public void setFechaInicio(String FechaInicio) {
        this.FechaInicio = FechaInicio;
    }

    public String getEstadoPedido() {
        return EstadoPedido;
    }

    public void setEstadoPedido(String EstadoPedido) {
        this.EstadoPedido = EstadoPedido;
    }

    public String getDireccion() {
        return Direccion;
    }

    public void setDireccion(String Direccion) {
        this.Direccion = Direccion;
    }

    public String getTipoCompra() {
        return TipoCompra;
    }

    public void setTipoCompra(String TipoCompra) {
        this.TipoCompra = TipoCompra;
    }   
}

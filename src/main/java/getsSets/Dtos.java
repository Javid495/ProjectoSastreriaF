package getsSets;

import java.util.List;

public class Dtos {
    
    // ==========================================
    // 1. Contenedor global de respuesta
    // ==========================================
    public static class ReporteCajaDTO {
        private MetricasDTO metricas;
        private List<PagoDTO> listaPagos;

        public ReporteCajaDTO(MetricasDTO metricas, List<PagoDTO> listaPagos) {
            this.metricas = metricas;
            this.listaPagos = listaPagos;
        }

        // Getters y Setters
        public MetricasDTO getMetricas() { return metricas; }
        public void setMetricas(MetricasDTO metricas) { this.metricas = metricas; }

        public List<PagoDTO> getListaPagos() { return listaPagos; }
        public void setListaPagos(List<PagoDTO> listaPagos) { this.listaPagos = listaPagos; }
    }

    // ==========================================
    // 2. Datos de cada tarjeta de pago
    // ==========================================
    public static class PagoDTO {
        private int idPago;
        private String usuario;
        private double total;
        private String metodoPago;
        private String fecha;
        private String tipoCompra;

        public PagoDTO(int idPago, String usuario, double total, String metodoPago, String fecha, String tipoCompra) {
            this.idPago = idPago;
            this.usuario = usuario;
            this.total = total;
            this.metodoPago = metodoPago;
            this.fecha = fecha;
            this.tipoCompra = tipoCompra;
        }

        // Getters y Setters
        public int getIdPago() { return idPago; }
        public void setIdPago(int idPago) { this.idPago = idPago; }

        public String getUsuario() { return usuario; }
        public void setUsuario(String usuario) { this.usuario = usuario; }

        public double getTotal() { return total; }
        public void setTotal(double total) { this.total = total; }

        public String getMetodoPago() { return metodoPago; }
        public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

        public String getFecha() { return fecha; }
        public void setFecha(String fecha) { this.fecha = fecha; }

        public String getTipoCompra() { return tipoCompra; }
        public void setTipoCompra(String tipoCompra) { this.tipoCompra = tipoCompra; }
    }

    // ==========================================
    // 3. Datos de los KPIs de ganancias
    // ==========================================
    public static class MetricasDTO {
        private double diario;
        private double semanal;
        private double mensual;

        public MetricasDTO(double diario, double semanal, double mensual) {
            this.diario = diario;
            this.semanal = semanal;
            this.mensual = mensual;
        }

        // Getters y Setters
        public double getDiario() { return diario; }
        public void setDiario(double diario) { this.diario = diario; }

        public double getSemanal() { return semanal; }
        public void setSemanal(double semanal) { this.semanal = semanal; }

        public double getMensual() { return mensual; }
        public void setMensual(double mensual) { this.mensual = mensual; }
    }
}

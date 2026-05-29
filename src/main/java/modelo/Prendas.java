
package modelo;

import java.util.ArrayList;
import java.util.List;


public class Prendas {
    
    //Declaramos los atributos que usaremos o llamaremos de la tabla prendas
    private int id;
    private String nombre;
    private String tipoPrenda;
    private double valor;
    private String talla;
    private String descripcion;
    private int stock;
    private String estado;
    
    private String categoria;
    private int categoriaId;
    private String imagen;
    private List<String> listaImagenes = new ArrayList();
    private int visitas;

    //Se declaran los getters y setters de los datos anteriores
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipoPrenda() {
        return tipoPrenda;
    }

    public void setTipoPrenda(String tipoPrenda) {
        this.tipoPrenda = tipoPrenda;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getTalla() {
        return talla;
    }

    public void setTalla(String talla) {
        this.talla = talla;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }
    
    public int getVisitas() {
        return visitas;
    }

    public void setVisitas(int visitas) {
        this.visitas = visitas;
    }   

    public List<String> getListaImagenes() {
        return listaImagenes;
    }

    public void setListaImagenes(List<String> listaImagenes) {
        this.listaImagenes = listaImagenes;
    }

    public int getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(int categoriaId) {
        this.categoriaId = categoriaId;
    }
    
}

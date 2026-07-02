package Dtos;

public class PrendasRecientes {
    private int id;
    private String nombre;
    private double valor;
    private String img;

    public PrendasRecientes(int id, String nombre, double valor, String img) {
        this.id = id;
        this.nombre = nombre;
        this.valor = valor;
        this.img = img;
    }
    // (Generar Getters y Setters aquí...)

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

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
    
    
}
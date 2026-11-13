package ni.edu.uam.laboratoriocontrol.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "equipo", schema = "catalogo")
public class Equipo {
    public static final List<String> ESTADOS =
            List.of("Disponible", "En mantenimiento", "Fuera de servicio");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_equipo")
    private Long idEquipo;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "marca", nullable = false, length = 50)
    private String marca;

    @Column(name = "estado", nullable = false, length = 50)
    private String estado;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    // Lado propietario de la relación: aquí está la llave foránea id_tipo
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo", nullable = false)
    private TipoEquipo tipoEquipo;

    public Equipo() {
    }

    public Equipo(String codigo, String nombre, String marca, String estado,
                  Integer cantidad, TipoEquipo tipoEquipo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.marca = marca;
        this.estado = estado;
        this.cantidad = cantidad;
        this.tipoEquipo = tipoEquipo;
    }

    public Long getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(Long idEquipo) {
        this.idEquipo = idEquipo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public TipoEquipo getTipoEquipo() {
        return tipoEquipo;
    }

    public void setTipoEquipo(TipoEquipo tipoEquipo) {
        this.tipoEquipo = tipoEquipo;
    }

    public Long getIdTipo() {
        return tipoEquipo != null ? tipoEquipo.getIdTipo() : null;
    }

    public String getNombreTipo() {
        return tipoEquipo != null ? tipoEquipo.getNombre() : "";
    }

    public String getDescripcionTipo() {
        return tipoEquipo != null ? tipoEquipo.getDescripcion() : "";
    }
}
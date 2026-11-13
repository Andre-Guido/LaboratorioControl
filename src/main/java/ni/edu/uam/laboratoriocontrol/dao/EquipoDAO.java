package ni.edu.uam.laboratoriocontrol.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import ni.edu.uam.laboratoriocontrol.database.JPAUtil;
import ni.edu.uam.laboratoriocontrol.model.Equipo;
import ni.edu.uam.laboratoriocontrol.model.TipoEquipo;

import java.util.ArrayList;
import java.util.List;

public class EquipoDAO {

    private TipoEquipo obtenerTipo(EntityManager em, Equipo equipo) {
        if (equipo.getTipoEquipo() == null || equipo.getTipoEquipo().getIdTipo() == null) {
            throw new IllegalArgumentException("Debe seleccionar un tipo de equipo.");
        }
        TipoEquipo tipo = em.find(TipoEquipo.class, equipo.getTipoEquipo().getIdTipo());
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de equipo seleccionado ya no existe.");
        }
        return tipo;
    }

    public void guardar(Equipo equipo) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            equipo.setTipoEquipo(obtenerTipo(em, equipo));
            em.persist(equipo);
            em.flush();
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw traducirError(ex);
        } finally {
            em.close();
        }
    }

    public boolean actualizar(Equipo equipo) {
        if (equipo.getIdEquipo() == null) {
            throw new IllegalArgumentException("Debe seleccionar un equipo para actualizar.");
        }
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (em.find(Equipo.class, equipo.getIdEquipo()) == null) {
                tx.rollback();
                return false;
            }
            equipo.setTipoEquipo(obtenerTipo(em, equipo));
            em.merge(equipo);
            em.flush();
            tx.commit();
            return true;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw traducirError(ex);
        } finally {
            em.close();
        }
    }

    public boolean eliminar(Long idEquipo) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Equipo equipo = em.find(Equipo.class, idEquipo);
            if (equipo == null) {
                tx.rollback();
                return false;
            }
            em.remove(equipo);
            tx.commit();
            return true;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw traducirError(ex);
        } finally {
            em.close();
        }
    }

    public List<Equipo> listar() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT e FROM Equipo e JOIN FETCH e.tipoEquipo ORDER BY e.idEquipo",
                    Equipo.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Equipo> buscar(String nombre, TipoEquipo tipo) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            boolean filtrarNombre = nombre != null && !nombre.isBlank();
            boolean filtrarTipo = tipo != null && tipo.getIdTipo() != null;

            List<String> condiciones = new ArrayList<>();
            if (filtrarNombre) {
                condiciones.add("LOWER(e.nombre) LIKE :nombre");
            }
            if (filtrarTipo) {
                condiciones.add("e.tipoEquipo.idTipo = :idTipo");
            }

            String jpql = "SELECT e FROM Equipo e JOIN FETCH e.tipoEquipo";
            if (!condiciones.isEmpty()) {
                jpql += " WHERE " + String.join(" AND ", condiciones);
            }
            jpql += " ORDER BY e.idEquipo";

            TypedQuery<Equipo> query = em.createQuery(jpql, Equipo.class);
            if (filtrarNombre) {
                query.setParameter("nombre", "%" + nombre.trim().toLowerCase() + "%");
            }
            if (filtrarTipo) {
                query.setParameter("idTipo", tipo.getIdTipo());
            }
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    private RuntimeException traducirError(RuntimeException ex) {
        Throwable causa = ex;
        while (causa != null) {
            String mensaje = causa.getMessage();
            if (mensaje != null) {
                if (mensaje.contains("equipo_codigo_key")) {
                    return new IllegalStateException("Ya existe un equipo con ese código.", ex);
                }
                if (mensaje.contains("chk_equipo_cantidad")) {
                    return new IllegalStateException("La cantidad no puede ser negativa.", ex);
                }
                if (mensaje.contains("chk_equipo_estado")) {
                    return new IllegalStateException("El estado no es un valor permitido.", ex);
                }
                if (mensaje.contains("fk_equipo_tipo")) {
                    return new IllegalStateException("El tipo de equipo seleccionado no existe.", ex);
                }
            }
            causa = causa.getCause();
        }
        return ex;
    }
}
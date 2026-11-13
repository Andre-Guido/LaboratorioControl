package ni.edu.uam.laboratoriocontrol.dao;

import jakarta.persistence.EntityManager;
import ni.edu.uam.laboratoriocontrol.database.JPAUtil;
import ni.edu.uam.laboratoriocontrol.model.TipoEquipo;

import java.util.List;

public class TipoEquipoDAO {

    // Lista los tipos para llenar los ComboBox del formulario y del filtro
    public List<TipoEquipo> listar() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT t FROM TipoEquipo t ORDER BY t.nombre", TipoEquipo.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
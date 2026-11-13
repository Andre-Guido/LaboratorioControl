package ni.edu.uam.laboratoriocontrol.database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

public class JPAUtil {
    private static EntityManagerFactory entityManagerFactory;

    public static synchronized void iniciar() {
        if (entityManagerFactory == null) {
            // La contraseña se lee de una variable de entorno para no subirla a GitHub
            String password = System.getenv("DB_PASSWORD");
            if (password == null || password.isBlank()) {
                throw new RuntimeException("La variable de entorno DB_PASSWORD está vacía.");
            }
            Map<String, Object> propiedades = new HashMap<>();
            propiedades.put("jakarta.persistence.jdbc.password", password);
            entityManagerFactory = Persistence.createEntityManagerFactory("laboratorioPU", propiedades);
        }
    }

    public static EntityManager getEntityManager() {
        iniciar();
        return entityManagerFactory.createEntityManager();
    }

    public static synchronized void cerrar() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
            entityManagerFactory = null;
        }
    }
}
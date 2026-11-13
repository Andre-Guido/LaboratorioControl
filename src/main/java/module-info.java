module ni.edu.uam.laboratorioequipos {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;

    opens ni.edu.uam.laboratoriocontrol to javafx.fxml;
    exports ni.edu.uam.laboratoriocontrol;
    opens ni.edu.uam.laboratoriocontrol.controller to javafx.fxml;
    exports ni.edu.uam.laboratoriocontrol.controller;
    opens ni.edu.uam.laboratoriocontrol.model to org.hibernate.orm.core, javafx.base;
}
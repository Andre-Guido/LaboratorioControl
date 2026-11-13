package ni.edu.uam.laboratoriocontrol.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import ni.edu.uam.laboratoriocontrol.dao.EquipoDAO;
import ni.edu.uam.laboratoriocontrol.dao.TipoEquipoDAO;
import ni.edu.uam.laboratoriocontrol.model.Equipo;
import ni.edu.uam.laboratoriocontrol.model.TipoEquipo;

import java.net.URL;
import java.util.List;
import java.util.Optional;

public class LaboratorioController {

    // Clase CSS que activa el modo oscuro en estilos.css
    private static final String CLASE_OSCURO = "oscuro";

    @FXML
    private ToggleButton tglEstilo;

    // Formulario
    @FXML
    private TextField txtCodigo;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtMarca;
    @FXML
    private TextField txtCantidad;
    @FXML
    private ComboBox<String> cmbEstado;
    @FXML
    private ComboBox<TipoEquipo> cmbTipoEquipo;
    @FXML
    private TextArea txtAreaDescripcion;

    // Botones que se habilitan solo cuando hay un equipo seleccionado
    @FXML
    private Button btnActualizar;
    @FXML
    private Button btnEliminar;

    // Búsqueda y filtro
    @FXML
    private TextField txtBusquedaNombre;
    @FXML
    private ComboBox<TipoEquipo> cmbBusquedaTipo;

    // Tabla
    @FXML
    private TableView<Equipo> tblEquipos;
    @FXML
    private TableColumn<Equipo, Long> colIdEquipo;
    @FXML
    private TableColumn<Equipo, String> colCodigo;
    @FXML
    private TableColumn<Equipo, String> colNombre;
    @FXML
    private TableColumn<Equipo, String> colMarca;
    @FXML
    private TableColumn<Equipo, String> colEstado;
    @FXML
    private TableColumn<Equipo, Integer> colCantidad;
    @FXML
    private TableColumn<Equipo, String> colTipo;
    @FXML
    private TableColumn<Equipo, String> colDescripcion;
    @FXML
    private TableColumn<Equipo, Long> colIdTipo;

    private final EquipoDAO equipoDAO = new EquipoDAO();
    private final TipoEquipoDAO tipoEquipoDAO = new TipoEquipoDAO();

    // Id del equipo seleccionado en la tabla; null si no hay ninguno
    private Long idSeleccionado = null;

    @FXML
    public void initialize() {
        // Cada columna lee el getter con ese nombre en Equipo (getCodigo, getNombreTipo, etc.)
        colIdEquipo.setCellValueFactory(new PropertyValueFactory<>("idEquipo"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("nombreTipo"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcionTipo"));
        colIdTipo.setCellValueFactory(new PropertyValueFactory<>("idTipo"));
        tblEquipos.setPlaceholder(new Label("No hay equipos para mostrar."));

        // Solo los estados permitidos por la restricción CHECK de la tabla
        cmbEstado.setItems(FXCollections.observableArrayList(Equipo.ESTADOS));

        // La tabla equipo no tiene descripción propia: el TextArea muestra la del tipo elegido
        cmbTipoEquipo.valueProperty().addListener((obs, anterior, nuevo) -> mostrarDescripcionTipo(nuevo));

        // Al seleccionar una fila, sus datos se cargan en el formulario
        tblEquipos.getSelectionModel().selectedItemProperty().addListener((obs, anterior, nuevo) -> {
            if (nuevo != null) {
                seleccionarEquipo(nuevo);
            }
        });

        actualizarBotones();

        try {
            cargarTipos();
            cargarEquipos();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudieron cargar los datos. " + mensajeError(ex));
        }
    }

    private void cargarTipos() {
        List<TipoEquipo> tipos = tipoEquipoDAO.listar();
        cmbTipoEquipo.setItems(FXCollections.observableArrayList(tipos));
        cmbBusquedaTipo.setItems(FXCollections.observableArrayList(tipos));
    }

    private void cargarEquipos() {
        tblEquipos.setItems(FXCollections.observableArrayList(equipoDAO.listar()));
    }

    @FXML
    private void guardarRegistro() {
        Equipo equipo = obtenerFormulario();
        if (equipo == null) {
            return;
        }
        try {
            equipoDAO.guardar(equipo);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo guardar el equipo. " + mensajeError(ex));
            return;
        }
        // Solo se llega aquí si el commit se completó
        mostrarAlerta(Alert.AlertType.INFORMATION, "Equipo guardado correctamente.");
        limpiarCampos();
        limpiarFiltros();
    }

    @FXML
    private void actualizarRegistro() {
        if (idSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Seleccione en la tabla el equipo que desea actualizar.");
            return;
        }
        Equipo equipo = obtenerFormulario();
        if (equipo == null) {
            return;
        }
        equipo.setIdEquipo(idSeleccionado);

        boolean actualizado;
        try {
            actualizado = equipoDAO.actualizar(equipo);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo actualizar el equipo. " + mensajeError(ex));
            return;
        }
        if (actualizado) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Equipo actualizado correctamente.");
        } else {
            mostrarAlerta(Alert.AlertType.WARNING, "El equipo ya no existe en la base de datos.");
        }
        limpiarCampos();
        limpiarFiltros();
    }

    @FXML
    private void eliminarRegistro() {
        Equipo seleccionado = tblEquipos.getSelectionModel().getSelectedItem();
        if (idSeleccionado == null || seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Seleccione en la tabla el equipo que desea eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("Eliminar equipo");
        confirmacion.setContentText("¿Está seguro de que desea eliminar el equipo "
                + seleccionado.getCodigo() + " - " + seleccionado.getNombre() + "?");
        aplicarEstilo(confirmacion);
        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) {
            return;
        }

        boolean eliminado;
        try {
            eliminado = equipoDAO.eliminar(idSeleccionado);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo eliminar el equipo. " + mensajeError(ex));
            return;
        }
        if (eliminado) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Equipo eliminado correctamente.");
        } else {
            mostrarAlerta(Alert.AlertType.WARNING, "El equipo ya no existía en la base de datos.");
        }
        limpiarCampos();
        limpiarFiltros();
    }

    @FXML
    private void limpiarCampos() {
        txtCodigo.clear();
        txtNombre.clear();
        txtMarca.clear();
        txtCantidad.clear();
        cmbEstado.setValue(null);
        cmbTipoEquipo.setValue(null);
        txtAreaDescripcion.clear();
        idSeleccionado = null;
        tblEquipos.getSelectionModel().clearSelection();
        actualizarBotones();
    }

    // Búsqueda por nombre parcial y filtro por tipo; se pueden usar por separado o juntos
    @FXML
    private void buscarEquipo() {
        try {
            List<Equipo> resultado = equipoDAO.buscar(txtBusquedaNombre.getText(), cmbBusquedaTipo.getValue());
            limpiarCampos();
            tblEquipos.setItems(FXCollections.observableArrayList(resultado));
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo realizar la búsqueda. " + mensajeError(ex));
        }
    }

    @FXML
    private void limpiarFiltros() {
        txtBusquedaNombre.clear();
        cmbBusquedaTipo.setValue(null);
        try {
            cargarEquipos();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudieron cargar los equipos. " + mensajeError(ex));
        }
    }

    // El botón seleccionado significa modo oscuro (así arranca la vista)
    @FXML
    private void cambiarEstilo() {
        Parent raiz = tglEstilo.getScene().getRoot();
        if (tglEstilo.isSelected()) {
            if (!raiz.getStyleClass().contains(CLASE_OSCURO)) {
                raiz.getStyleClass().add(CLASE_OSCURO);
            }
            tglEstilo.setText("Modo Claro");
        } else {
            raiz.getStyleClass().remove(CLASE_OSCURO);
            tglEstilo.setText("Modo Oscuro");
        }
    }

    // Las alertas se abren en su propia ventana; se les aplica la misma hoja de estilos
    // y el modo que esté activo para que no aparezcan en blanco sobre la vista oscura
    private void aplicarEstilo(Alert alerta) {
        DialogPane panel = alerta.getDialogPane();
        URL hojaEstilos = getClass().getResource("/ni/edu/uam/laboratoriocontrol/estilos.css");
        if (hojaEstilos != null) {
            panel.getStylesheets().add(hojaEstilos.toExternalForm());
        }
        if (tglEstilo.isSelected()) {
            panel.getStyleClass().add(CLASE_OSCURO);
        }
    }

    private void seleccionarEquipo(Equipo equipo) {
        idSeleccionado = equipo.getIdEquipo();
        txtCodigo.setText(equipo.getCodigo());
        txtNombre.setText(equipo.getNombre());
        txtMarca.setText(equipo.getMarca());
        txtCantidad.setText(String.valueOf(equipo.getCantidad()));
        cmbEstado.setValue(equipo.getEstado());
        // Al cambiar el tipo, el listener actualiza la descripción
        cmbTipoEquipo.setValue(buscarTipo(equipo.getTipoEquipo()));
        actualizarBotones();
    }

    // Busca en el ComboBox el tipo con el mismo id; se compara por id porque el tipo
    // del equipo y los del ComboBox son objetos de consultas distintas
    private TipoEquipo buscarTipo(TipoEquipo tipo) {
        if (tipo == null) {
            return null;
        }
        for (TipoEquipo t : cmbTipoEquipo.getItems()) {
            if (t.getIdTipo().equals(tipo.getIdTipo())) {
                return t;
            }
        }
        return null;
    }

    private void mostrarDescripcionTipo(TipoEquipo tipo) {
        if (tipo == null || tipo.getDescripcion() == null) {
            txtAreaDescripcion.clear();
        } else {
            txtAreaDescripcion.setText(tipo.getDescripcion());
        }
    }

    private void actualizarBotones() {
        boolean sinSeleccion = idSeleccionado == null;
        btnActualizar.setDisable(sinSeleccion);
        btnEliminar.setDisable(sinSeleccion);
    }

    // Lee y valida el formulario; si algún dato es inválido muestra un aviso y devuelve null
    private Equipo obtenerFormulario() {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();
        String marca = txtMarca.getText().trim();

        if (codigo.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "El código es obligatorio.");
            return null;
        }
        if (nombre.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "El nombre es obligatorio.");
            return null;
        }
        if (marca.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "La marca es obligatoria.");
            return null;
        }
        // Mismas longitudes que las columnas VARCHAR de la tabla equipo
        if (codigo.length() > 50 || marca.length() > 50 || nombre.length() > 100) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "El código y la marca admiten hasta 50 caracteres, y el nombre hasta 100.");
            return null;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(txtCantidad.getText().trim());
        } catch (NumberFormatException ex) {
            mostrarAlerta(Alert.AlertType.WARNING, "La cantidad debe ser un número entero.");
            return null;
        }
        // Mismo límite que la restricción chk_equipo_cantidad
        if (cantidad < 0) {
            mostrarAlerta(Alert.AlertType.WARNING, "La cantidad no puede ser negativa.");
            return null;
        }

        String estado = cmbEstado.getValue();
        if (estado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Seleccione el estado del equipo.");
            return null;
        }
        // Mismos valores que la restricción chk_equipo_estado
        if (!Equipo.ESTADOS.contains(estado)) {
            mostrarAlerta(Alert.AlertType.WARNING, "El estado seleccionado no está permitido.");
            return null;
        }

        // La columna id_tipo es NOT NULL, así que el tipo es obligatorio
        TipoEquipo tipo = cmbTipoEquipo.getValue();
        if (tipo == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Seleccione el tipo de equipo.");
            return null;
        }

        return new Equipo(codigo, nombre, marca, estado, cantidad, tipo);
    }

    // Los errores del DAO ya traen un mensaje entendible; para los demás
    // se muestra la causa original (por ejemplo, un fallo de conexión)
    private String mensajeError(Throwable ex) {
        if (ex instanceof IllegalArgumentException || ex instanceof IllegalStateException) {
            return ex.getMessage();
        }
        Throwable causa = ex;
        while (causa.getCause() != null) {
            causa = causa.getCause();
        }
        return causa.getMessage();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        alerta.setHeaderText(null);
        aplicarEstilo(alerta);
        alerta.showAndWait();
    }
}
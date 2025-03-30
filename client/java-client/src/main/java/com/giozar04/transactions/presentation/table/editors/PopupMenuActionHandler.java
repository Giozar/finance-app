package com.giozar04.transactions.presentation.table.editors;


/**
 * Interfaz para manejar las acciones del menú emergente en la columna de opciones.
 */
public interface PopupMenuActionHandler {
    /**
     * Se invoca cuando se selecciona la opción de editar.
     * @param rowIndex Índice de la fila seleccionada.
     */
    void onEditTransaction(int rowIndex);

    /**
     * Se invoca cuando se selecciona la opción de eliminar.
     * @param rowIndex Índice de la fila seleccionada.
     */
    void onDeleteTransaction(int rowIndex);

    /**
     * Se invoca cuando se selecciona la opción de ver detalles.
     * @param rowIndex Índice de la fila seleccionada.
     */
    void onViewDetails(int rowIndex);
}

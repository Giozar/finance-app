package com.giozar04.transactions.presentation.form;

import java.util.List;

import com.giozar04.transactions.domain.entities.Transaction;

/**
 * Contrato de cada sección del formulario de transacciones.
 *
 * <p>Es la convención de los subpaneles de {@code accounts} ({@code validate/applyTo/loadFrom/clear}),
 * ahora como interfaz real para que {@code TransactionFormPanel} las trate de forma uniforme.</p>
 *
 * <ul>
 *   <li>{@link #onContextChanged}: se muestra, oculta, recarga o limpia según el contexto.</li>
 *   <li>{@link #validate}: añade los errores de lo que aplica en el contexto actual.</li>
 *   <li>{@link #applyTo}: escribe en el agregado solo lo que le corresponde.</li>
 *   <li>{@link #loadFrom}: rellena sus campos para editar (se llama en el orden de las secciones).</li>
 *   <li>{@link #clear}: restablece sus campos.</li>
 * </ul>
 */
public interface TransactionFormSection {

    void onContextChanged(TransactionFormContext ctx);

    void validate(List<String> errors);

    void applyTo(Transaction tx);

    void loadFrom(Transaction tx);

    void clear();
}

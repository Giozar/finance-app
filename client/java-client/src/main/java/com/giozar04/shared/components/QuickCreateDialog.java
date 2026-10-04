package com.giozar04.shared.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * Alta rápida: abre un {@link JDialog} modal que incrusta un formulario existente
 * (p. ej. {@code CategoryFormPanel}) y se cierra en cuanto el formulario notifica que guardó.
 *
 * <p>El formulario debe exponer un callback de guardado del tipo {@code setOnSaved(Consumer<T>)}
 * que reciba la entidad devuelta por el servicio. Así se reutiliza el mismo formulario del módulo,
 * sin duplicarlo.</p>
 *
 * Uso:
 * <pre>
 *   CategoryFormPanel form = new CategoryFormPanel();
 *   form.presetUser(user, true);
 *   QuickCreateDialog.show(this, "Nueva categoría", form, form::setOnSaved)
 *           .ifPresent(category -&gt; { reloadCategories(); categoryCombo.setSelectedItem(category); });
 * </pre>
 *
 * <p>Cerrar la ventana cancela el alta (devuelve {@link Optional#empty()}).</p>
 */
public final class QuickCreateDialog {

    private QuickCreateDialog() {
    }

    /**
     * Abre el diálogo modal y espera a que se cierre.
     *
     * @param parent          componente desde el que se abre (su ventana es la propietaria del diálogo)
     * @param title           título del diálogo
     * @param form            formulario a incrustar
     * @param registerOnSaved registra el callback de guardado en el formulario (p. ej. {@code form::setOnSaved})
     * @return la entidad guardada, o vacío si el usuario cerró el diálogo sin guardar
     */
    public static <T> Optional<T> show(Component parent, String title, JPanel form,
                                       Consumer<Consumer<T>> registerOnSaved) {
        Window owner = parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
        JDialog dialog = new JDialog(owner, title, JDialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        AtomicReference<T> result = new AtomicReference<>();
        registerOnSaved.accept(saved -> {
            result.set(saved);
            dialog.dispose();
        });

        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(form, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true); // bloquea hasta que se cierre (modal)

        registerOnSaved.accept(null); // el formulario deja de referenciar al diálogo
        return Optional.ofNullable(result.get());
    }

    /**
     * Variante con callback: abre el diálogo y, si se guardó, entrega la entidad a {@code onCreated}.
     */
    public static <T> void open(Component parent, String title, JPanel form,
                                Consumer<Consumer<T>> registerOnSaved, Consumer<T> onCreated) {
        show(parent, title, form, registerOnSaved).ifPresent(onCreated);
    }
}



import java.util.List;

import javax.swing.JFrame;

import com.giozar04.shared.components.table.ColumnDefinition;
import com.giozar04.shared.components.table.GenericTablePanel;
import com.giozar04.shared.components.table.OptionsCellEditor;
import com.giozar04.shared.components.table.OptionsCellRenderer;
import com.giozar04.shared.components.table.PopupMenuActionHandler;

class User {

    private final String name;
    private final int age;
    private final String email;

    public User(String name, int age, String email) {
        this.name = name;
        this.age = age;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getEmail() {
        return email;
    }
}

public class TestTable {

    private final List<ColumnDefinition<User>> columns = List.of(
        new ColumnDefinition<>("Nombre", user -> user.getName()),
        new ColumnDefinition<>("Edad", user -> user.getAge()),
        new ColumnDefinition<>("Email", user -> user.getEmail()),
        optionsColumn() // columna con opciones como editar, eliminar, etc.
    );

    // Método auxiliar para columna de opciones
    private static ColumnDefinition<User> optionsColumn() {
        ColumnDefinition<User> colOptions = new ColumnDefinition<>("Opciones", u -> "···");
        colOptions.setRenderer(new OptionsCellRenderer());
        colOptions.setEditor(new OptionsCellEditor(new PopupMenuActionHandler() {
            @Override
            public void onEdit(int rowIndex) {
                // Aquí iría la lógica para editar un usuario
                System.out.println("Editando usuario en la fila: " + rowIndex);
            }

            @Override
            public void onDelete(int rowIndex) {
                // Aquí iría la lógica para eliminar un usuario
                System.out.println("Eliminando usuario en la fila: " + rowIndex);
            }

            @Override
            public void onViewDetails(int rowIndex) {
                // Aquí iría la lógica para ver los detalles de un usuario
                System.out.println("Viendo detalles del usuario en la fila: " + rowIndex);
            }
        }));
        return colOptions;
    }

    public void showView() {
        List<User> users = List.of(
            new User("Juan", 25, "juan@gmail.com"),
            new User("Ana", 30, "ana@gmail.com"),
            new User("Luis", 22, "luis@gmail.com")
        );

        GenericTablePanel<User> userTablePanel = new GenericTablePanel<>(columns, users);

        JFrame frame = new JFrame("Usuarios");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.add(userTablePanel);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new TestTable().showView();
    }
}

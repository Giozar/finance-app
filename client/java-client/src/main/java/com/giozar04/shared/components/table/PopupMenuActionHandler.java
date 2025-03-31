package com.giozar04.shared.components.table;

public interface PopupMenuActionHandler {
    void onEdit(int rowIndex);
    void onDelete(int rowIndex);
    void onViewDetails(int rowIndex);
}

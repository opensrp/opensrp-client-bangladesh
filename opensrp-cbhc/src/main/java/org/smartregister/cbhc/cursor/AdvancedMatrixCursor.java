package org.smartregister.cbhc.cursor;

import org.smartregister.cbhc.util.Utils;

import java.util.Date;

public class AdvancedMatrixCursor extends net.sqlcipher.MatrixCursor {
    public AdvancedMatrixCursor(String[] columnNames) {
        super(columnNames);
    }

    @Override
    public long getLong(int column) {
        try {
            return super.getLong(column);
        } catch (NumberFormatException e) {
            Utils.appendLog(getClass().getName(), e);
            return (new Date()).getTime();
        }
    }

}
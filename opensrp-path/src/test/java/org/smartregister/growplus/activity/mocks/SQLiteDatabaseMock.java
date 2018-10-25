package org.smartregister.path.activity.mocks;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabaseHook;

/**
 * Created by kaderchowdhury on 06/12/17.
 */

public class SQLiteDatabaseMock extends SQLiteDatabase {
    public SQLiteDatabaseMock(String path, char[] password, CursorFactory factory, int flags) {
        super(path, password, factory, flags);
    }

    public SQLiteDatabaseMock(String path, char[] password, CursorFactory factory, int flags, SQLiteDatabaseHook databaseHook) {
        super(path, password, factory, flags, databaseHook);
    }
}

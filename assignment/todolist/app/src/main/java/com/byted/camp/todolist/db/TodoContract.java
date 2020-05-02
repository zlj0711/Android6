package com.byted.camp.todolist.db;

import android.provider.BaseColumns;
/**
 * Created on 2019/1/22.
 *
 * @author xuyingyi@bytedance.com (Yingyi Xu)
 */
public final class TodoContract {

    // TODO 定义表结构和 SQL 语句常量
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + Todo.TABLE_NAME + " (" +
                    Todo._ID + " INTEGER PRIMARY KEY," +
                    Todo.COLUMN_NAME_CONTENT + " TEXT," +
                    Todo.COLUMN_NAME_DATE + " TEXT," +
                    Todo.COLUMN_NAME_LEVEL + " TEXT," +
                    Todo.COLUMN_NAME_STATE + " TEXT)";

    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + Todo.TABLE_NAME;
    private TodoContract() {}
    public static class Todo implements BaseColumns{
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_STATE = "state";
        public static final String COLUMN_NAME_CONTENT = "content";
        public static final String COLUMN_NAME_LEVEL = "level";
    }
}


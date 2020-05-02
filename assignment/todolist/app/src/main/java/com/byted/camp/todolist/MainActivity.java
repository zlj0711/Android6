package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.byted.camp.todolist.beans.Note;
import com.byted.camp.todolist.beans.State;
import com.byted.camp.todolist.db.TodoContract;
import com.byted.camp.todolist.db.TodoDbHelper;
import com.byted.camp.todolist.operation.activity.DatabaseActivity;
import com.byted.camp.todolist.operation.activity.DebugActivity;
import com.byted.camp.todolist.operation.activity.SettingActivity;
import com.byted.camp.todolist.ui.NoteListAdapter;
import com.byted.camp.todolist.ui.NoteViewHolder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD = 1002;
    private static final String TAG = "zlj";
    private RecyclerView recyclerView;
    private NoteListAdapter notesAdapter;
    private TodoDbHelper myDBHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myDBHelper = new TodoDbHelper(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(MainActivity.this, NoteActivity.class),
                        REQUEST_CODE_ADD);
            }
        });
        recyclerView = findViewById(R.id.list_todo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        notesAdapter = new NoteListAdapter(new NoteOperator() {
            @Override
            public void deleteNote(Note note) {
                MainActivity.this.deleteNote(note);
            }

            @Override
            public void updateNote(Note note) {
                MainActivity.this.updateNode(note);
            }
        });
        recyclerView.setAdapter(notesAdapter);
        try {
            notesAdapter.refresh(loadNotesFromDatabase());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingActivity.class));
                return true;
            case R.id.action_debug:
                startActivity(new Intent(this, DebugActivity.class));
                return true;
            case R.id.action_database:
                startActivity(new Intent(this, DatabaseActivity.class));
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD
                && resultCode == Activity.RESULT_OK) {
            try {
                notesAdapter.refresh(loadNotesFromDatabase());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private List<Note> loadNotesFromDatabase() throws ParseException {
        // TODO 从数据库中查询数据，并转换成 JavaBeans
        SQLiteDatabase db = myDBHelper.getReadableDatabase();
        List<Note> infoList;
        infoList= new ArrayList<Note>();
        String[] projection = {
                BaseColumns._ID,
                TodoContract.Todo.COLUMN_NAME_DATE,
                TodoContract.Todo.COLUMN_NAME_STATE,
                TodoContract.Todo.COLUMN_NAME_CONTENT,
                TodoContract.Todo.COLUMN_NAME_LEVEL
        };
        Cursor cursor = db.query(
                TodoContract.Todo.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                TodoContract.Todo.COLUMN_NAME_LEVEL// The sort order
        );
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(TodoContract.Todo._ID));
            String dateT = cursor.getString(cursor.getColumnIndex(TodoContract.Todo.COLUMN_NAME_DATE));
            String stateT = cursor.getString(cursor.getColumnIndex(TodoContract.Todo.COLUMN_NAME_STATE));
            String content = cursor.getString(cursor.getColumnIndex(TodoContract.Todo.COLUMN_NAME_CONTENT));
            String levelT = cursor.getString(cursor.getColumnIndex(TodoContract.Todo.COLUMN_NAME_LEVEL));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.ENGLISH);
            Date date = simpleDateFormat.parse(dateT);
            State state;
            int level = Integer.parseInt(levelT);
            state = State.from(Integer.parseInt(stateT));
            Note tmp = new Note(id);
            tmp.setContent(content);
            tmp.setDate(date);
            tmp.setState(state);
            tmp.setLevel(level);
            infoList.add(tmp);
        }
        cursor.close();
        return infoList;
    }

    private void deleteNote(Note note) {
        // TODO 删除数据
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        String contentStr = note.getContent();
        String delete = "DELETE FROM " + TodoContract.Todo.TABLE_NAME + " WHERE " + TodoContract.Todo.COLUMN_NAME_CONTENT
                + " = '" + contentStr + "'" + ";";
        db.execSQL(delete);
        try {
            notesAdapter.refresh(loadNotesFromDatabase());
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void updateNode(Note note) {
        // 更新数据
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        ContentValues updatedValues = new ContentValues();
        State newState = note.getState();
        String stateStr = Integer.toString(newState.intValue);
        updatedValues.put(TodoContract.Todo.COLUMN_NAME_STATE, stateStr);
        String where = TodoContract.Todo._ID + "=" + note.id;
        db.update(TodoContract.Todo.TABLE_NAME, updatedValues, where, null);
        try {
            notesAdapter.refresh(loadNotesFromDatabase());
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}

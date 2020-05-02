package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.byted.camp.todolist.db.TodoContract;
import com.byted.camp.todolist.db.TodoDbHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NoteActivity extends AppCompatActivity {

    private EditText editText;
    private RadioGroup myGroup;
    private Button addBtn;
    private TodoDbHelper myDBHelper;
    private static final String TAG = "zlj";
    private int level;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        setTitle(R.string.take_a_note);

        editText = findViewById(R.id.edit_text);
        editText.setFocusable(true);
        editText.requestFocus();
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.showSoftInput(editText, 0);
        }

        addBtn = findViewById(R.id.btn_add);
        myGroup = findViewById(R.id.choice);
        myGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.highBtn:
                        level = 0;
                        break;
                    case R.id.normalBtn:
                        level = 1;
                        break;
                    case R.id.lowBtn:
                        level = 2;
                        break;
                    default:
                        Log.d(TAG,"error");
                        break;
                }
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence content = editText.getText();
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(NoteActivity.this,
                            "No content to add", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean succeed = saveNote2Database(content.toString().trim(), level);
                if (succeed) {
                    Toast.makeText(NoteActivity.this,
                            "Note added", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                } else {
                    Toast.makeText(NoteActivity.this,
                            "Error", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
        myDBHelper = new TodoDbHelper(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private boolean saveNote2Database(String content, int level) {
        // TODO 插入一条新数据，返回是否插入成功
        SQLiteDatabase db = myDBHelper.getWritableDatabase();
        ContentValues newValues = new ContentValues();
        String newContent = content;
        int newLevel = level;
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.ENGLISH);
        String dateStr = simpleDateFormat.format(date);
        newValues.put(TodoContract.Todo.COLUMN_NAME_CONTENT, newContent);
        newValues.put(TodoContract.Todo.COLUMN_NAME_DATE, dateStr);
        String stateStr = "0";
        newValues.put(TodoContract.Todo.COLUMN_NAME_STATE, stateStr);
        newValues.put(TodoContract.Todo.COLUMN_NAME_LEVEL, Integer.toString(newLevel));
        long result = db.insert(TodoContract.Todo.TABLE_NAME, null, newValues);
        if(result == -1){
            return false;
        }else{
            return true;
        }

    }
}

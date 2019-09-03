package com.sunil.myquiz;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.sunil.myquiz.QuizContract.*;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class QuizDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MyQuiz.db";
    private static final int DATABASE_VERSION = 1;

    private static QuizDBHelper instance;
    private SQLiteDatabase sqLiteDatabase;

    private QuizDBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized QuizDBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new QuizDBHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        this.sqLiteDatabase = sqLiteDatabase;

        final String SQL_CREATE_CATEGORY_TABLE = "CREATE TABLE " + CategoriesTable.TABLE_NAME + " ( " +
                CategoriesTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CategoriesTable.COLUMN_NAME + " TEXT" +
                ")";

        final String SQL_CREATE_QUESTIONS_TABLE = "CREATE TABLE " + QuestionsTable.TABLE_NAME + " ( " +
                QuestionsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT , " +
                QuestionsTable.COLUMN_QUESTION + " TEXT, " +
                QuestionsTable.COLUMN_OPTION1 + " TEXT, " +
                QuestionsTable.COLUMN_OPTION2 + " TEXT, " +
                QuestionsTable.COLUMN_OPTION3 + " TEXT, " +
                QuestionsTable.COLUMN_ANSWER_NUMBER + " INTEGER , " +
                QuestionsTable.COLUMN_DIFFICULTY + " TEXT, " +
                QuestionsTable.COLUMN_CATEGORY_ID + " INTEGER, " +
                "FOREIGN KEY(" + QuestionsTable.COLUMN_CATEGORY_ID + ") REFERENCES " +
                CategoriesTable.TABLE_NAME + "(" + CategoriesTable._ID + ")" + "ON DELETE CASCADE" +
                ")";

        sqLiteDatabase.execSQL(SQL_CREATE_CATEGORY_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_QUESTIONS_TABLE);

        fillCategoriesTable();
        fillQuestionsTable();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion)
    {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + QuestionsTable.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CategoriesTable.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    private void fillCategoriesTable()
    {
        Category c1 = new Category("Programming");
        addCategory(c1);
        Category c2 = new Category("Geography");
        addCategory(c2);
        Category c3 = new Category("Math");
        addCategory(c3);
    }

    private void addCategory(Category category) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CategoriesTable.COLUMN_NAME, category.getName());
        sqLiteDatabase.insert(CategoriesTable.TABLE_NAME, null, contentValues);
    }

    private void fillQuestionsTable()
    {
        Question q1 = new Question("Programming, Easy: A is correct",
                "A", "B", "C", 1,
                Question.DIFFICULTY_EASY, Category.PROGRAMMING);
        addQuestion(q1);
        Question q2 = new Question("Geography, Medium: B is correct",
                "A", "B", "C", 2,
                Question.DIFFICULTY_MEDIUM, Category.GEOGRAPHY);
        addQuestion(q2);
        Question q3 = new Question("Math, Hard: C is correct",
                "A", "B", "C", 3,
                Question.DIFFICULTY_HARD, Category.MATH);
        addQuestion(q3);
        Question q4 = new Question("Math, Easy: A is correct",
                "A", "B", "C", 1,
                Question.DIFFICULTY_EASY, Category.MATH);
        addQuestion(q4);
        Question q5 = new Question("Non existing, Easy: A is correct",
                "A", "B", "C", 1,
                Question.DIFFICULTY_EASY, 4);
        addQuestion(q5);
        Question q6 = new Question("Non existing, Medium: B is correct",
                "A", "B", "C", 2,
                Question.DIFFICULTY_MEDIUM, 5);
        addQuestion(q6);
    }

    private void addQuestion(Question question)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(QuestionsTable.COLUMN_QUESTION , question.getQuestion());
        contentValues.put(QuestionsTable.COLUMN_OPTION1 , question.getOption1());
        contentValues.put(QuestionsTable.COLUMN_OPTION2 , question.getOption2());
        contentValues.put(QuestionsTable.COLUMN_OPTION3 , question.getOption3());
        contentValues.put(QuestionsTable.COLUMN_ANSWER_NUMBER , question.getAnswerNumber());
        contentValues.put(QuestionsTable.COLUMN_DIFFICULTY , question.getDifficulty());
        contentValues.put(QuestionsTable.COLUMN_CATEGORY_ID , question.getCategoryID());

        sqLiteDatabase.insert(QuestionsTable.TABLE_NAME , null , contentValues);
    }

    public List<Category> getAllCategories() {
        List<Category> categoryList = new ArrayList<>();
        sqLiteDatabase = getReadableDatabase();
        Cursor c = sqLiteDatabase.rawQuery("SELECT * FROM " + CategoriesTable.TABLE_NAME, null);

        if (c.moveToFirst()) {
            do {
                Category category = new Category();
                category.setId(c.getInt(c.getColumnIndex(CategoriesTable._ID)));
                category.setName(c.getString(c.getColumnIndex(CategoriesTable.COLUMN_NAME)));
                categoryList.add(category);
            } while (c.moveToNext());
        }

        c.close();
        return categoryList;
    }

    public ArrayList<Question> getAllQuestions()
    {
        ArrayList<Question> questionList = new ArrayList<>();

        sqLiteDatabase = getReadableDatabase();

        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + QuestionsTable.TABLE_NAME , null);

        if (cursor.moveToFirst())
        {
            do
            {
                Question question = new Question();

                question.setId(cursor.getInt(cursor.getColumnIndex(QuestionsTable._ID)));
                question.setQuestion(cursor.getString(cursor.getColumnIndex(QuestionsTable.COLUMN_QUESTION)));
                question.setOption1(cursor.getString(cursor.getColumnIndex(QuestionsTable.COLUMN_OPTION1)));
                question.setOption2(cursor.getString(cursor.getColumnIndex(QuestionsTable.COLUMN_OPTION2)));
                question.setOption3(cursor.getString(cursor.getColumnIndex(QuestionsTable.COLUMN_OPTION3)));
                question.setAnswerNumber(cursor.getInt(cursor.getColumnIndex(QuestionsTable.COLUMN_ANSWER_NUMBER)));
                question.setDifficulty(cursor.getString(cursor.getColumnIndex(QuestionsTable.COLUMN_DIFFICULTY)));
                question.setCategoryID(cursor.getInt(cursor.getColumnIndex(QuestionsTable.COLUMN_CATEGORY_ID)));

                questionList.add(question);
            }while (cursor.moveToNext());
        }
        cursor.close();

        return questionList;
    }


    public ArrayList<Question> getQuestions(int categoryID , String difficulty)
    {
        ArrayList<Question> questionList = new ArrayList<>();

        String selection = QuestionsTable.COLUMN_CATEGORY_ID + " = ? " +
                " AND " + QuestionsTable.COLUMN_DIFFICULTY + " = ? ";
        String[] selectionArgs = new String[]{String.valueOf(categoryID), difficulty};

        Cursor cursor = sqLiteDatabase.query(
                QuestionsTable.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.moveToFirst())
        {
            do
            {
                Question question = new Question();

                question.setId(cursor.getInt(cursor.getColumnIndex(QuestionsTable._ID)));
                question.setQuestion(cursor.getString(cursor.getColumnIndex(QuestionsTable.COLUMN_QUESTION)));
                question.setOption1(cursor.getString(cursor.getColumnIndex(QuestionsTable.COLUMN_OPTION1)));
                question.setOption2(cursor.getString(cursor.getColumnIndex(QuestionsTable.COLUMN_OPTION2)));
                question.setOption3(cursor.getString(cursor.getColumnIndex(QuestionsTable.COLUMN_OPTION3)));
                question.setAnswerNumber(cursor.getInt(cursor.getColumnIndex(QuestionsTable.COLUMN_ANSWER_NUMBER)));
                question.setDifficulty(cursor.getString(cursor.getColumnIndex(QuestionsTable.COLUMN_DIFFICULTY)));
                question.setCategoryID(cursor.getInt(cursor.getColumnIndex(QuestionsTable.COLUMN_CATEGORY_ID)));

                questionList.add(question);
            }while (cursor.moveToNext());
        }
        cursor.close();

        return questionList;
    }

}

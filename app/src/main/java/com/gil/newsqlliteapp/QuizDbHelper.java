package com.gil.newsqlliteapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import com.gil.newsqlliteapp.QuizContract.QuestionTable;
import com.gil.newsqlliteapp.QuizContract.CategoriesTable;

import java.util.ArrayList;
import java.util.List;

public class QuizDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MyQuiz.db";
    private static final int DATABASE_VERSION = 1;

    private static QuizDbHelper instance;

    private SQLiteDatabase db;

    private QuizDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized QuizDbHelper getInstance (Context context){
        if (instance == null){
            instance = new QuizDbHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        this.db = db;

        final String SQL_CREATE_CATEGORY_TABLE = "CREATE TABLE " +
                CategoriesTable.TABLE_NAME + "(" +
                CategoriesTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CategoriesTable.COLUMN_NAME + " TEXT " +
                ")";

        final String SQL_CREATE_QUESTIONS_TABLE = "CREATE TABLE " +
                QuestionTable.TABLE_NAME + " ( " +
                QuestionTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                QuestionTable.COLUMN_QUESTION + " TEXT, " +
                QuestionTable.COLUMN_OPTION1 + " TEXT, " +
                QuestionTable.COLUMN_OPTION2 + " TEXT, " +
                QuestionTable.COLUMN_OPTION3 + " TEXT, " +
                QuestionTable.COLUMN_ANSWER_NR + " INTEGER, " +
                QuestionTable.COLUMN_DIFFICULT + " TEXT, " +
                QuestionTable.COLUMN_CATEGORY_ID + " INTEGER, " +
                "FOREIGN KEY(" +QuestionTable.COLUMN_CATEGORY_ID + ") REFERENCES " +
                CategoriesTable.TABLE_NAME + "(" +CategoriesTable._ID +")" + "ON DELETE CASCADE" +
                ")";

        db.execSQL(SQL_CREATE_CATEGORY_TABLE);
        db.execSQL(SQL_CREATE_QUESTIONS_TABLE);
        fillCategoriesTable();
        fillQuestionTable();
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CategoriesTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + QuestionTable.TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            db.setForeignKeyConstraintsEnabled(true);
        }
    }

    private void fillCategoriesTable(){
        Category c1 = new Category("Programming");
        insertCategory(c1);
        Category c2 = new Category("Geography");
        insertCategory(c2);
        Category c3 = new Category("Math");
        insertCategory(c3);
    }

    public void addCategory(Category category){
        db = getWritableDatabase();
        insertCategory(category);
    }

    public void addCategories(List<Category> categories){
        db = getWritableDatabase();

        for (Category category : categories){
            insertCategory(category);
        }
    }

    private void insertCategory(Category category){
        ContentValues cv = new ContentValues();
        cv.put(CategoriesTable.COLUMN_NAME , category.getName());
        db.insert(CategoriesTable.TABLE_NAME , null , cv);
    }

    private void fillQuestionTable() {
        Question q1 = new Question("Programming , Easy: A is correct", "A", "B", "C", 1 , Question.DIFFICULT_EASY , Category.PROGRAMMING);
        insertQuestions(q1);
        Question q2 = new Question("Geography , Medium: B is correct", "A", "B", "C", 2 ,Question.DIFFICULT_MEDIUM , Category.GEOGRAPHY);
        insertQuestions(q2);
        Question q3 = new Question("Math , Hard: C is correct", "A", "B", "C", 31 , Question.DIFFICULT_HARD , Category.MATH);
        insertQuestions(q3);
        Question q4 = new Question("Not Exisiting Question , Easy: A is correct", "A", "B", "C", 1 , Question.DIFFICULT_EASY , 4);
        insertQuestions(q4);
        Question q5 = new Question("Math , Medium: C is correct", "A", "B", "C", 3 , Question.DIFFICULT_MEDIUM ,3);
        insertQuestions(q5);
    }

    public void addQuestion(Question question){
        db = getWritableDatabase();
        insertQuestions(question);
    }
    public void addQuestions(List<Question> questions){
        db = getWritableDatabase();

        for (Question question : questions){
            insertQuestions(question);
        }
    }

    private void insertQuestions(Question question) {
        ContentValues cv = new ContentValues();
        cv.put(QuestionTable.COLUMN_QUESTION, question.getQuestion());
        cv.put(QuestionTable.COLUMN_OPTION1, question.getOption1());
        cv.put(QuestionTable.COLUMN_OPTION2, question.getOption2());
        cv.put(QuestionTable.COLUMN_OPTION3, question.getOption3());
        cv.put(QuestionTable.COLUMN_ANSWER_NR, question.getAnswerNr());
        cv.put(QuestionTable.COLUMN_DIFFICULT , question.getDifficult());
        cv.put(QuestionTable.COLUMN_CATEGORY_ID , question.getCategoryId());
        db.insert(QuestionTable.TABLE_NAME, null, cv);
    }

    public List<Category> getAllCategories(){
        List<Category> categoryList = new ArrayList<>();
        db = getReadableDatabase();
        Cursor c = db.rawQuery(" select * from " + CategoriesTable.TABLE_NAME , null);

        if(c.moveToFirst()){
            do {
                Category category = new Category();
                category.setId(c.getInt(c.getColumnIndex(CategoriesTable._ID)));
                category.setName(c.getString(c.getColumnIndex(CategoriesTable.COLUMN_NAME)));
                categoryList.add(category);
            }while (c.moveToNext());
        }
        c.close();
        return categoryList;
    }

    public ArrayList<Question> getAllQuestions() {
        ArrayList<Question> questionList = new ArrayList<>();
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + QuestionTable.TABLE_NAME, null);
        if (c.moveToFirst()) {
            do {
                Question question = new Question();
                question.setId(c.getInt(c.getColumnIndex(QuestionTable._ID)));
                question.setQuestion(c.getString(c.getColumnIndex(QuestionTable.COLUMN_QUESTION)));
                question.setOption1(c.getString(c.getColumnIndex(QuestionTable.COLUMN_OPTION1)));
                question.setOption2(c.getString(c.getColumnIndex(QuestionTable.COLUMN_OPTION2)));
                question.setOption3(c.getString(c.getColumnIndex(QuestionTable.COLUMN_OPTION3)));
                question.setAnswerNr(c.getInt(c.getColumnIndex(QuestionTable.COLUMN_ANSWER_NR)));
                question.setDifficult(c.getString(c.getColumnIndex(QuestionTable.COLUMN_DIFFICULT)));
                question.setCategoryId(c.getInt(c.getColumnIndex(QuestionTable.COLUMN_CATEGORY_ID)));
                questionList.add(question);
            } while (c.moveToNext());
        }
        c.close();
        return questionList;
    }

    public ArrayList<Question> getQuestionsByDifficult(String difficult , int categoryID) {
        ArrayList<Question> questionList = new ArrayList<>();
        db = getReadableDatabase();

        String selection = QuestionTable.COLUMN_CATEGORY_ID +" = ? " +
                " AND " +QuestionTable.COLUMN_DIFFICULT + " = ? ";
        String[] selectionArgs = new String[]{String.valueOf(categoryID) , difficult};
        Cursor c = db.query(QuestionTable.TABLE_NAME , null , selection , selectionArgs , null , null , null);

        if (c.moveToFirst()) {
            do {
                Question question = new Question();
                question.setId(c.getInt(c.getColumnIndex(QuestionTable._ID)));
                question.setQuestion(c.getString(c.getColumnIndex(QuestionTable.COLUMN_QUESTION)));
                question.setOption1(c.getString(c.getColumnIndex(QuestionTable.COLUMN_OPTION1)));
                question.setOption2(c.getString(c.getColumnIndex(QuestionTable.COLUMN_OPTION2)));
                question.setOption3(c.getString(c.getColumnIndex(QuestionTable.COLUMN_OPTION3)));
                question.setAnswerNr(c.getInt(c.getColumnIndex(QuestionTable.COLUMN_ANSWER_NR)));
                question.setDifficult(c.getString(c.getColumnIndex(QuestionTable.COLUMN_DIFFICULT)));
                question.setCategoryId(c.getInt(c.getColumnIndex(QuestionTable.COLUMN_CATEGORY_ID)));
                questionList.add(question);
            } while (c.moveToNext());
        }
        c.close();
        return questionList;
    }
}

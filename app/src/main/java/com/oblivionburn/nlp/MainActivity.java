package com.oblivionburn.nlp;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemSelectedListener
{
    //Variables
    private int int_Time = 7000;
    private int int_Delay = 0;
    private int wordfix_selection = 0;
    private EditText Output = null;
    private EditText Input = null;
    private EditText txt_WordFix = null;
    private Spinner sp_WordFix = null;
    private Button btn_WordFix = null;
    private Button btn_Enter = null;
    private Button btn_Menu = null;
    private Boolean bl_Typing = false;
    private Boolean bl_Ready = false;
    private Boolean bl_History = false;
    private Boolean bl_Thought = false;
    public static Boolean bl_Feedback = false;
    public static final File Brain_dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Brain/" );
    public static final File History_dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Brain/History/" );
    public static final File Thought_dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Brain/Thoughts/" );
    private Handler handler;
    private boolean KeyboardOpen;
    private static Context context;
    private View rootView;
    private final int PERMISSION_REQUEST = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        MainActivity.context = getApplicationContext();

        Input = (EditText)findViewById(R.id.txt_Input);

        Output = (EditText)findViewById(R.id.txt_Output);
        Output.setMaxLines(Integer.MAX_VALUE);

        btn_Enter = (Button)findViewById(R.id.btn_Enter);
        btn_Menu = (Button)findViewById(R.id.btn_Menu);
        sp_WordFix = (Spinner)findViewById(R.id.sp_WordFix);
        sp_WordFix.setOnItemSelectedListener(this);
        txt_WordFix = (EditText)findViewById(R.id.txt_WordFix);
        btn_WordFix = (Button)findViewById(R.id.btn_WordFix);

        handler = new Handler();

        if (!Brain_dir.exists())
        {
            Brain_dir.mkdirs();
        }

        File file = new File(Brain_dir, "Words.txt");
        if (!file.exists())
        {
            try
            {
                file.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error: " + e.toString(), Toast.LENGTH_LONG).show();
            }
        }

        file = new File(Brain_dir, "InputList.txt");
        if (!file.exists())
        {
            try
            {
                file.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error: " + e.toString(), Toast.LENGTH_LONG).show();
            }
        }

        if (!History_dir.exists())
        {
            History_dir.mkdirs();
        }

        if (!Thought_dir.exists())
        {
            Thought_dir.mkdirs();
        }

        Input.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void afterTextChanged(Editable s)
            {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (Output.getText().toString().equals("(thinking...)"))
                {
                    Output.setText("");
                }
                bl_Typing = true;
                Logic.bl_Initiation = false;
                stopTimer();

                if (Input.getText().toString().equals(""))
                {
                    bl_Typing = false;
                    startTimer();
                }
            }
        });

        rootView = findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                int heightDiff = rootView.getRootView().getHeight() - rootView.getHeight();
                if (heightDiff > dpToPx(MainActivity.context, 200))
                {
                    KeyboardOpen = true;
                }
                else
                {
                    KeyboardOpen = false;
                }
            }
        });

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion > Build.VERSION_CODES.LOLLIPOP)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
            }
            else
            {
                bl_Ready = true;
                startTimer();
            }
        }
        else
        {
            bl_Ready = true;
            startTimer();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case PERMISSION_REQUEST:
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // permission granted
                    bl_Ready = true;
                    startTimer();
                }
                else
                {
                    // permission denied
                    onDestroy();
                }
            }
        }
    }

    @Override
    public void onDestroy()
    {
        stopTimer();
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
    }

    //Back Button
    @Override
    public void onBackPressed()
    {
        if (bl_History)
        {
            Output.setText("");
            startTimer();
            bl_History = false;
        }
        else if (bl_Thought)
        {
            Output.setText("");
            startTimer();
            bl_Thought = false;
        }
        else
        {
            Acknowledge_Exit();
        }
    }

    //Timer
    private final Runnable StatusChecker = new Runnable()
    {
        @Override
        public void run()
        {
            if (int_Delay == 0)
            {
                int_Delay++;
            }
            else if (int_Delay == 1)
            {
                AttentionSpan();
                int_Delay = 0;
            }
            handler.postDelayed(StatusChecker, int_Time);
        }
    };

    private void startTimer()
    {
        int_Delay = 0;
        StatusChecker.run();
    }

    private void stopTimer()
    {
        handler.removeCallbacks(StatusChecker);
    }

    private void AttentionSpan()
    {
        if (!bl_Typing)
        {
            Random random = new Random();
            int int_choice = random.nextInt(100);
            if (int_choice > 25)
            {
                if (Logic.bl_NewInput)
                {
                    CleanMemory();
                    Discourage();
                }

                Output.setText(R.string.thinking);
                Output.setSelection(0);
                Logic.FeedbackLoop();

                int_Time = 1000;
            }
            else if (int_choice > 0 && int_choice <= 25)
            {
                Logic.bl_Initiation = true;
                bl_Feedback = false;
                Data.getHistory();

                Logic.Respond();

                Data.HistoryList.add("AI: " + Logic.str_Output);
                Data.saveHistory();

                Data.getHistory();

                Output.setText("");
                Output.setMovementMethod(new ScrollingMovementMethod());

                if (Data.HistoryList.size() > 40)
                {
                    for (int i = Data.HistoryList.size() - 40; i < Data.HistoryList.size(); i++)
                    {
                        Output.append(Data.HistoryList.get(i) + "\n");
                    }
                }
                else
                {
                    for (int i = 0; i < Data.HistoryList.size(); i++)
                    {
                        Output.append(Data.HistoryList.get(i) + "\n");
                    }
                }

                Output.setSelection(Output.getText().length());

                if (Logic.bl_NewInput)
                {
                    CleanMemory();
                    Discourage();
                }

                int_Time = 10000;
            }

            Logic.bl_NewInput = false;
        }
    }

    //After Enter
    public void onSend(View view)
    {
        if (bl_Ready)
        {
            Logic.str_Input = Input.getText().toString();
            if (Logic.str_Input.length() > 0)
            {
                bl_Feedback = false;
                Logic.prepInput();

                Data.getHistory();
                Logic.str_History = Logic.str_Input;
                Logic.HistoryRules();
                Data.HistoryList.add("User: " + Logic.str_History);

                Logic.Respond();

                Data.HistoryList.add("AI: " + Logic.str_Output);
                Data.saveHistory();

                Data.getHistory();

                Output.setText("");
                Output.setMovementMethod(new ScrollingMovementMethod());

                if (Data.HistoryList.size() > 40)
                {
                    for (int i = Data.HistoryList.size() - 40; i < Data.HistoryList.size(); i++)
                    {
                        Output.append(Data.HistoryList.get(i) + "\n");
                    }
                }
                else
                {
                    for (int i = 0; i < Data.HistoryList.size(); i++)
                    {
                        Output.append(Data.HistoryList.get(i) + "\n");
                    }
                }

                Output.setSelection(Output.getText().length());

                Logic.ClearLeftovers();
                Input.setText("");
                int_Time = 10000;
            }
        }
    }

    //MessageBox
    private void PopUp()
    {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage("Memory has been erased.");
        dlgAlert.setTitle("System Message");
        dlgAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {

            }
        });
        dlgAlert.setCancelable(false);
        dlgAlert.create().show();
    }

    //Yes/No Box for Exit
    private void Acknowledge_Exit()
    {
        stopTimer();
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        android.os.Process.killProcess(android.os.Process.myPid());
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        startTimer();
                        break;
                }
            }
        };
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage("Exit the NLP Program?");
        dlgAlert.setTitle("System Message");
        dlgAlert.setNegativeButton("No", dialogClickListener);
        dlgAlert.setPositiveButton("Yes", dialogClickListener);
        dlgAlert.setCancelable(false);
        dlgAlert.create().show();
    }

    //Yes/No Box for Erase
    private void Acknowledge_Erase()
    {
        stopTimer();
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        EraseMemory(Brain_dir);

                        Output.setText("");
                        Input.setText("");

                        if (!Brain_dir.exists())
                        {
                            Brain_dir.mkdirs();
                        }

                        File file = new File(Brain_dir, "Words.txt");
                        if (!file.exists())
                        {
                            try
                            {
                                file.createNewFile();
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        }

                        file = new File(Brain_dir, "InputList.txt");
                        if (!file.exists())
                        {
                            try
                            {
                                file.createNewFile();
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        }

                        DateFormat f = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
                        String currentDate = f.format(new Date());

                        file = new File(History_dir, currentDate + ".txt");
                        if (!History_dir.exists())
                        {
                            History_dir.mkdirs();
                            if (!file.exists())
                            {
                                try
                                {
                                    file.createNewFile();
                                }
                                catch (IOException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }

                        file = new File(Thought_dir, currentDate + ".txt");
                        if (!Thought_dir.exists())
                        {
                            Thought_dir.mkdirs();
                            if (!file.exists())
                            {
                                try
                                {
                                    file.createNewFile();
                                }
                                catch (IOException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }

                        PopUp();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        startTimer();
                        break;
                }
            }
        };

        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage("Erase all memory?");
        dlgAlert.setTitle("System Message");
        dlgAlert.setNegativeButton("No", dialogClickListener);
        dlgAlert.setPositiveButton("Yes", dialogClickListener);
        dlgAlert.setCancelable(false);
        dlgAlert.create().show();
    }

    //Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    public void onMenu(View view)
    {
        this.openOptionsMenu();
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu)
    {
        Output.setVisibility(View.INVISIBLE);
        Input.setVisibility(View.INVISIBLE);
        btn_Enter.setVisibility(View.INVISIBLE);
        btn_Menu.setVisibility(View.INVISIBLE);

        if (KeyboardOpen)
        {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(Input.getWindowToken(), 0);
        }

        stopTimer();

        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public void onPanelClosed(int featureId, Menu menu)
    {
        Output.setVisibility(View.VISIBLE);
        Input.setVisibility(View.VISIBLE);
        btn_Enter.setVisibility(View.VISIBLE);
        btn_Menu.setVisibility(View.VISIBLE);

        startTimer();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.exit_app:
                Acknowledge_Exit();
                return true;
            case R.id.erase_memory:
                Acknowledge_Erase();
                return true;
            case R.id.thought_log:
                Data.getThoughts();
                Output.setText("");
                for (int i = 0; i < Data.ThoughtList.size(); i++)
                {
                    Output.append(Data.ThoughtList.get(i) + "\n");
                }
                stopTimer();
                bl_Thought = true;
                return true;
            case R.id.word_fix:
                //Set Spinner
                Data.getWords();

                if (Data.Words.size() > 0)
                {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Data.Words);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_WordFix.setAdapter(adapter);
                    sp_WordFix.setSelection(0);
                    sp_WordFix.setVisibility(View.VISIBLE);
                    sp_WordFix.setClickable(true);
                    sp_WordFix.setFocusable(true);

                    //Set Input
                    txt_WordFix.setText(Data.Words.get(wordfix_selection));
                    txt_WordFix.setVisibility(View.VISIBLE);
                    txt_WordFix.setClickable(true);
                    txt_WordFix.setFocusableInTouchMode(true);
                    txt_WordFix.setFocusable(true);
                    txt_WordFix.requestFocus();

                    //Set Button
                    btn_WordFix.setVisibility(View.VISIBLE);
                    btn_WordFix.setClickable(true);
                    btn_WordFix.setFocusable(true);

                    btn_Enter.setClickable(false);
                    stopTimer();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void EraseMemory(File fileOrDirectory)
    {
        if (fileOrDirectory.isDirectory())
        {
            for (File child : fileOrDirectory.listFiles())
            {
                EraseMemory(child);
            }
        }
        fileOrDirectory.delete();
    }

    private void CleanMemory()
    {
        Data.getInputList();

        if (Data.InputList.size() > 0)
        {
            for (int i = 0; i < Data.InputList.size(); i++)
            {
                String MemoryCheck = Data.InputList.get(i);
                File file = new File(Brain_dir, MemoryCheck + ".txt");

                if (file.exists())
                {
                    Data.getOutputList(MemoryCheck);
                    if (Data.OutputList.size() == 0)
                    {
                        file.delete();
                        Data.InputList.remove(i);
                        if (i > 0)
                        {
                            i--;
                        }
                    }
                }
                else
                {
                    Data.InputList.remove(i);
                    if (i > 0)
                    {
                        i--;
                    }
                }
            }

            Data.saveInputList();
        }
    }

    private void Discourage()
    {
        if (!Logic.str_last_response.equals(""))
        {
            if (Logic.str_last_response.contains("."))
            {
                String str = Logic.str_last_response;
                StringBuilder sb = new StringBuilder(str).replace(Logic.str_last_response.indexOf("."), Logic.str_last_response.indexOf(".") + 1, " .");
                Logic.str_last_response = sb.toString();
            }
            else if (Logic.str_last_response.contains("?"))
            {
                String str = Logic.str_last_response;
                StringBuilder sb = new StringBuilder(str).replace(Logic.str_last_response.indexOf("?"), Logic.str_last_response.indexOf("?") + 1, " $");
                Logic.str_last_response = sb.toString();
            }
            else if (Logic.str_last_response.contains("!"))
            {
                String str = Logic.str_last_response;
                StringBuilder sb = new StringBuilder(str).replace(Logic.str_last_response.indexOf("!"), Logic.str_last_response.indexOf("!") + 1, " !");
                Logic.str_last_response = sb.toString();
            }

            String[] WordArray = Logic.str_last_response.split(" ");
            for (int i = 0; i < WordArray.length; i++)
            {
                switch (WordArray[i])
                {
                    case ",":
                        WordArray[i] = " ,";
                        break;
                    case ";":
                        WordArray[i] = " ;";
                        break;
                    case ":":
                        WordArray[i] = null;
                        break;
                    case "?":
                        WordArray[i] = " $";
                        break;
                    case "$":
                        WordArray[i] = " $";
                        break;
                    case "!":
                        WordArray[i] = " !";
                        break;
                    case ".":
                        WordArray[i] = " .";
                        break;
                }
            }


            for (int pro = 0; pro < WordArray.length - 1; pro++)
            {
                Data.getProWords(WordArray[pro]);
                if (Data.Words.contains(WordArray[pro + 1]))
                {
                    int index = Data.Words.indexOf(WordArray[pro + 1]);

                    if (Data.Frequencies.get(index) > 0)
                    {
                        Data.Frequencies.set(index, Data.Frequencies.get(index) - 1);
                    }
                    else if (Data.Frequencies.get(index) < 0)
                    {
                        Data.Frequencies.set(index, 0);
                    }
                }
            }

            for (int pre = 1; pre < WordArray.length; pre++)
            {
                Data.getPreWords(WordArray[pre]);
                if (Data.Words.contains(WordArray[pre - 1]))
                {
                    int index = Data.Words.indexOf(WordArray[pre - 1]);

                    if (Data.Frequencies.get(index) > 0)
                    {
                        Data.Frequencies.set(index, Data.Frequencies.get(index) - 1);
                    }
                    else if (Data.Frequencies.get(index) < 0)
                    {
                        Data.Frequencies.set(index, 0);
                    }
                }
            }
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        wordfix_selection = parent.getSelectedItemPosition();
        txt_WordFix.setText(Data.Words.get(wordfix_selection));
    }

    public void onNothingSelected(AdapterView<?> parent)
    {

    }

    public void WordFix(View view)
    {
        String oldWord = Data.WordDataSet.get(wordfix_selection).getWord();
        String newWord = txt_WordFix.getText().toString();

        Data.getInputList();
        for (int i = 0; i < Data.InputList.size(); i++)
        {
            Data.getOutputList(Data.InputList.get(i));
            for (int j = 0; j < Data.OutputList.size(); j++)
            {
                if (Data.OutputList.get(j).contains(oldWord))
                {
                    String newOutput = Data.OutputList.get(j).replace(oldWord, newWord);
                    Data.OutputList.set(j, newOutput);
                }
            }
            Data.saveOutput(Data.InputList.get(i));

            if (Data.InputList.get(i).contains(oldWord))
            {
                String oldPath = Data.InputList.get(i) + ".txt";
                String newInput = Data.InputList.get(i).replace(oldWord, newWord);
                Data.InputList.set(i, newInput);
                File oldFile = new File(MainActivity.Brain_dir, oldPath);
                File newFile = new File(MainActivity.Brain_dir, Data.InputList.get(i) + ".txt");
                oldFile.renameTo(newFile);
            }
        }
        Data.saveInputList();

        Data.getWords();
        List<String> words = new ArrayList<>();
        for (String word : Data.Words)
        {
            words.add(word);
        }

        for (int i = 0; i < words.size(); i++)
        {
            Data.getPreWords(words.get(i));
            for (int j = 0; j < Data.WordDataSet.size(); j++)
            {
                if (Data.WordDataSet.get(j).getWord().equals(oldWord))
                {
                    String oldPath = "Pre-" + Data.WordDataSet.get(j).getWord() + ".txt";
                    Data.WordDataSet.get(j).setWord(newWord);
                    String newPath = "Pre-" + Data.WordDataSet.get(j).getWord() + ".txt";

                    File oldFile = new File(MainActivity.Brain_dir, oldPath);
                    File newFile = new File(MainActivity.Brain_dir, newPath);
                    oldFile.renameTo(newFile);
                }
            }
            Data.savePreWords(words.get(i));

            Data.getProWords(words.get(i));
            for (int j = 0; j < Data.WordDataSet.size(); j++)
            {
                if (Data.WordDataSet.get(j).getWord().equals(oldWord))
                {
                    String oldPath = "Pro-" + Data.WordDataSet.get(j).getWord() + ".txt";
                    Data.WordDataSet.get(j).setWord(newWord);
                    String newPath = "Pro-" + Data.WordDataSet.get(j).getWord() + ".txt";

                    File oldFile = new File(MainActivity.Brain_dir, oldPath);
                    File newFile = new File(MainActivity.Brain_dir, newPath);
                    oldFile.renameTo(newFile);
                }
            }
            Data.saveProWords(words.get(i));
        }

        Data.getWords();
        Data.WordDataSet.get(wordfix_selection).setWord(newWord);
        Data.saveWords();

        //Set Spinner
        sp_WordFix.setVisibility(View.INVISIBLE);
        sp_WordFix.setClickable(false);
        sp_WordFix.setFocusable(false);

        //Set Input
        txt_WordFix.setVisibility(View.INVISIBLE);
        txt_WordFix.setClickable(false);
        txt_WordFix.setFocusable(false);
        txt_WordFix.setFocusableInTouchMode(false);

        //Set Button
        btn_WordFix.setVisibility(View.INVISIBLE);
        btn_WordFix.setClickable(false);
        btn_WordFix.setFocusable(false);

        Input.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        btn_Enter.setClickable(true);
        startTimer();
    }

    public static float dpToPx(Context context, float valueInDp)
    {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }
}

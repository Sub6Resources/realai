package com.oblivionburn.nlp;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    private int int_Time = 10000;
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
    private Boolean bl_Thought = false;
    private Boolean bl_WordFix = false;
    public static final File Brain_dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Brain/" );
    public static final File History_dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Brain/History/" );
    public static final File Thought_dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Brain/Thoughts/" );
    private Handler handler;
    private boolean KeyboardOpen;
    private View rootView;
    private final int PERMISSION_REQUEST = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

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
                Logic.Initiation = false;
                stopTimer();
                stopThinking();

                if (Input.getText().toString().equals(""))
                {
                    bl_Typing = false;
                    startTimer();
                    startThinking();
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
                KeyboardOpen = heightDiff > dpToPx(getApplicationContext());
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
                startThinking();
            }
        }
        else
        {
            bl_Ready = true;
            startTimer();
            startThinking();
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
                    startThinking();
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
        stopThinking();
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
    }

    //Back Button
    @Override
    public void onBackPressed()
    {
        if (bl_Thought)
        {
            CloseThought();
        }
        else if (bl_WordFix)
        {
            CloseWordFix();
        }
        else
        {
            Acknowledge_Exit();
        }
    }

    //Timer
    private final Runnable Timer = new Runnable()
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
            handler.postDelayed(Timer, int_Time);
        }
    };

    private void startTimer()
    {
        int_Delay = 0;
        Timer.run();
    }

    private void stopTimer()
    {
        handler.removeCallbacks(Timer);
    }

    //Thinking
    private final Runnable Thought = new Runnable()
    {
        @Override
        public void run()
        {
            Logic.NewInput_ForThinking = Logic.last_response_thinking.equals("");

            List<String> thoughts = Data.getThoughts();
            String[] wordArray = Logic.prepInput(Logic.last_response_thinking);

            Logic.last_response_thinking = Logic.Think(wordArray);
            Logic.last_response_thinking = Logic.HistoryRules(Logic.last_response_thinking);

            if (!Logic.last_response_thinking.equals(""))
            {
                thoughts.add("NLP: " + Logic.last_response_thinking);
                Data.saveThoughts(thoughts);
            }

            Logic.ClearLeftovers();

            if (bl_Thought)
            {
                ScrollThoughts();
            }

            handler.postDelayed(Thought, 1000);
        }
    };

    private void startThinking()
    {
        Thought.run();
    }

    private void stopThinking()
    {
        handler.removeCallbacks(Thought);
    }

    //Try to initiate conversation
    private void AttentionSpan()
    {
        if (!bl_Typing)
        {
            if (Logic.NewInput)
            {
                CleanMemory();
                Discourage();
            }

            Logic.NewInput = false;
            Logic.Initiation = true;
            List<String> history = Data.getHistory();
            String[] wordArray = new String[0];

            String output = Logic.Respond(wordArray, "", Logic.Initiation);

            if (!output.equals(""))
            {
                history.add("AI: " + output);
                Data.saveHistory(history);
            }

            ScrollHistory();

            int_Time = 10000;
        }
    }

    //After Enter
    public void onSend(View view)
    {
        if (bl_WordFix)
        {
            CloseWordFix();
        }
        else if (bl_Thought)
        {
            CloseThought();
        }
        else if (bl_Ready)
        {
            String input = Input.getText().toString();
            if (input.length() > 0)
            {
                Logic.Initiation = false;
                Logic.UserInput = true;
                String[] wordArray = Logic.prepInput(input);

                List<String> history = Data.getHistory();
                input = Logic.HistoryRules(input);
                history.add("User: " + input);

                String output = Logic.Respond(wordArray, input, Logic.Initiation);

                if (!output.equals(""))
                {
                    history.add("AI: " + output);
                }

                Data.saveHistory(history);

                ScrollHistory();

                Logic.ClearLeftovers();
                Input.setText("");
                int_Time = 10000;
            }
        }
    }

    private void ScrollHistory()
    {
        Output.setText("");
        Output.setMovementMethod(new ScrollingMovementMethod());

        List<String> history = Data.getHistory();
        if (history.size() > 40)
        {
            for (int i = history.size() - 40; i < history.size(); i++)
            {
                Output.append(history.get(i) + "\n");
            }
        }
        else
        {
            for (int i = 0; i < history.size(); i++)
            {
                Output.append(history.get(i) + "\n");
            }
        }

        Output.setSelection(Output.getText().length());
    }

    private void ScrollThoughts()
    {
        Output.setText("");
        Output.setMovementMethod(new ScrollingMovementMethod());

        List<String> thoughts = Data.getThoughts();
        if (thoughts.size() > 40)
        {
            for (int i = thoughts.size() - 40; i < thoughts.size(); i++)
            {
                Output.append(thoughts.get(i) + "\n");
            }
        }
        else
        {
            for (int i = 0; i < thoughts.size(); i++)
            {
                Output.append(thoughts.get(i) + "\n");
            }
        }

        Output.setSelection(Output.getText().length());
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
        if (!bl_Thought)
        {
            if (!bl_WordFix)
            {
                Output.setVisibility(View.VISIBLE);
                Input.setVisibility(View.VISIBLE);
                btn_Menu.setVisibility(View.VISIBLE);

                btn_Enter.setText("Enter");
                btn_Enter.setVisibility(View.VISIBLE);
                startTimer();
            }
            else
            {
                btn_Enter.setText("Back");
                btn_Enter.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            btn_Enter.setText("Back");
            btn_Enter.setVisibility(View.VISIBLE);
            Output.setVisibility(View.VISIBLE);
        }
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
                stopTimer();
                bl_Thought = true;
                return true;
            case R.id.word_fix:
                //Set Spinner
                List<WordData> data = Data.getWords();
                List<String> words = new ArrayList<>();
                for (int i = 0; i < data.size(); i++)
                {
                    words.add(data.get(i).getWord());
                }

                if (words.size() > 0)
                {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, words);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_WordFix.setAdapter(adapter);
                    sp_WordFix.setSelection(0);
                    sp_WordFix.setVisibility(View.VISIBLE);
                    sp_WordFix.setClickable(true);
                    sp_WordFix.setFocusable(true);

                    //Set Input
                    txt_WordFix.setText(words.get(wordfix_selection));
                    txt_WordFix.setVisibility(View.VISIBLE);
                    txt_WordFix.setClickable(true);
                    txt_WordFix.setFocusableInTouchMode(true);
                    txt_WordFix.setFocusable(true);
                    txt_WordFix.requestFocus();

                    //Set Button
                    btn_WordFix.setVisibility(View.VISIBLE);
                    btn_WordFix.setClickable(true);
                    btn_WordFix.setFocusable(true);

                    stopTimer();
                    bl_WordFix = true;
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

    private void ReleaseThoughts(File directory)
    {
        for (File child : directory.listFiles())
        {
            EraseMemory(child);
        }
    }

    private void CleanMemory()
    {
        List<String> input = Data.getInputList();

        if (input.size() > 0)
        {
            for (int i = 0; i < input.size(); i++)
            {
                String MemoryCheck = input.get(i);
                File file = new File(Brain_dir, MemoryCheck + ".txt");

                if (file.exists())
                {
                    List<String> output = Data.getOutputList(MemoryCheck);
                    if (output.size() == 0)
                    {
                        file.delete();
                        input.remove(i);
                        if (i > 0)
                        {
                            i--;
                        }
                    }
                }
                else
                {
                    input.remove(i);
                    if (i > 0)
                    {
                        i--;
                    }
                }
            }

            Data.saveInputList(input);
        }
    }

    private void Discourage()
    {
        if (!Logic.last_response.equals(""))
        {
            if (Logic.last_response.contains("."))
            {
                String str = Logic.last_response;
                StringBuilder sb = new StringBuilder(str).replace(Logic.last_response.indexOf("."), Logic.last_response.indexOf(".") + 1, " .");
                Logic.last_response = sb.toString();
            }
            else if (Logic.last_response.contains("?"))
            {
                String str = Logic.last_response;
                StringBuilder sb = new StringBuilder(str).replace(Logic.last_response.indexOf("?"), Logic.last_response.indexOf("?") + 1, " $");
                Logic.last_response = sb.toString();
            }
            else if (Logic.last_response.contains("!"))
            {
                String str = Logic.last_response;
                StringBuilder sb = new StringBuilder(str).replace(Logic.last_response.indexOf("!"), Logic.last_response.indexOf("!") + 1, " !");
                Logic.last_response = sb.toString();
            }

            String[] WordArray = Logic.last_response.split(" ");
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
                        WordArray[i] = "";
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

            List<WordData> data;
            List<String> words = new ArrayList<>();
            List<Integer> frequencies = new ArrayList<>();

            for (int pro = 0; pro < WordArray.length - 1; pro++)
            {
                data = Data.getProWords(WordArray[pro]);
                words.clear();
                frequencies.clear();

                for (int i = 0; i < data.size(); i++)
                {
                    words.add(data.get(i).getWord());
                    frequencies.add(data.get(i).getFrequency());
                }

                if (words.contains(WordArray[pro + 1]))
                {
                    int index = words.indexOf(WordArray[pro + 1]);

                    if (frequencies.get(index) > 0)
                    {
                        frequencies.set(index, frequencies.get(index) - 1);
                    }
                    else if (frequencies.get(index) < 0)
                    {
                        frequencies.set(index, 0);
                    }
                }

                data.clear();
                for (int i = 0; i < data.size(); i++)
                {
                    WordData new_data = new WordData();
                    new_data.setWord(words.get(i));
                    new_data.setFrequency(frequencies.get(i));
                    data.add(new_data);
                }

                Data.saveProWords(data, WordArray[pro]);
            }

            for (int pre = 1; pre < WordArray.length; pre++)
            {
                data = Data.getPreWords(WordArray[pre]);
                words.clear();
                frequencies.clear();

                for (int i = 0; i < data.size(); i++)
                {
                    words.add(data.get(i).getWord());
                    frequencies.add(data.get(i).getFrequency());
                }

                if (words.contains(WordArray[pre - 1]))
                {
                    int index = words.indexOf(WordArray[pre - 1]);

                    if (frequencies.get(index) > 0)
                    {
                        frequencies.set(index, frequencies.get(index) - 1);
                    }
                    else if (frequencies.get(index) < 0)
                    {
                        frequencies.set(index, 0);
                    }
                }

                data.clear();
                for (int i = 0; i < data.size(); i++)
                {
                    WordData new_data = new WordData();
                    new_data.setWord(words.get(i));
                    new_data.setFrequency(frequencies.get(i));
                    data.add(new_data);
                }

                Data.savePreWords(data, WordArray[pre]);
            }
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        List<WordData> data = Data.getWords();
        List<String> words = new ArrayList<>();
        for (int i = 0; i < data.size(); i++)
        {
            words.add(data.get(i).getWord());
        }

        wordfix_selection = parent.getSelectedItemPosition();
        txt_WordFix.setText(words.get(wordfix_selection));
    }

    public void onNothingSelected(AdapterView<?> parent)
    {

    }

    public void WordFix(View view)
    {
        List<WordData> data = Data.getWords();

        String oldWord = data.get(wordfix_selection).getWord();
        String newWord = txt_WordFix.getText().toString();

        List<String> input = Data.getInputList();
        for (int i = 0; i < input.size(); i++)
        {
            List<String> output = Data.getOutputList(input.get(i));
            for (int j = 0; j < output.size(); j++)
            {
                if (output.get(j).contains(oldWord))
                {
                    String newOutput = output.get(j).replace(oldWord, newWord);
                    output.set(j, newOutput);
                }
            }
            Data.saveOutput(output, input.get(i));

            if (input.get(i).contains(oldWord))
            {
                String oldPath = input.get(i) + ".txt";
                String newInput = input.get(i).replace(oldWord, newWord);
                input.set(i, newInput);
                File oldFile = new File(MainActivity.Brain_dir, oldPath);
                File newFile = new File(MainActivity.Brain_dir, input.get(i) + ".txt");
                oldFile.renameTo(newFile);
            }
        }
        Data.saveInputList(input);

        List<String> words = new ArrayList<>();
        for (int i = 0; i < data.size(); i++)
        {
            words.add(data.get(i).getWord());
        }

        for (int i = 0; i < words.size(); i++)
        {
            data = Data.getPreWords(words.get(i));
            for (int j = 0; j < data.size(); j++)
            {
                if (data.get(j).getWord().equals(oldWord))
                {
                    String oldPath = "Pre-" + data.get(j).getWord() + ".txt";
                    data.get(j).setWord(newWord);
                    String newPath = "Pre-" + data.get(j).getWord() + ".txt";

                    File oldFile = new File(MainActivity.Brain_dir, oldPath);
                    File newFile = new File(MainActivity.Brain_dir, newPath);
                    oldFile.renameTo(newFile);
                }
            }
            Data.savePreWords(data, words.get(i));

            data = Data.getProWords(words.get(i));
            for (int j = 0; j < data.size(); j++)
            {
                if (data.get(j).getWord().equals(oldWord))
                {
                    String oldPath = "Pro-" + data.get(j).getWord() + ".txt";
                    data.get(j).setWord(newWord);
                    String newPath = "Pro-" + data.get(j).getWord() + ".txt";

                    File oldFile = new File(MainActivity.Brain_dir, oldPath);
                    File newFile = new File(MainActivity.Brain_dir, newPath);
                    oldFile.renameTo(newFile);
                }
            }
            Data.saveProWords(data, words.get(i));
        }

        data = Data.getWords();
        data.get(wordfix_selection).setWord(newWord);
        Data.saveWords(data);

        CloseWordFix();
    }

    public void CloseWordFix()
    {
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

        Output.setVisibility(View.VISIBLE);
        Input.setVisibility(View.VISIBLE);
        btn_Menu.setVisibility(View.VISIBLE);
        btn_Enter.setText("Enter");
        btn_Enter.setVisibility(View.VISIBLE);

        ScrollHistory();

        Input.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        bl_WordFix = false;

        startTimer();
    }

    public void CloseThought()
    {
        Input.setVisibility(View.VISIBLE);
        btn_Menu.setVisibility(View.VISIBLE);
        btn_Enter.setText("Enter");
        btn_Enter.setVisibility(View.VISIBLE);

        ScrollHistory();

        Input.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        bl_Thought = false;

        startTimer();
    }

    private static float dpToPx(Context context)
    {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 200, metrics);
    }
}

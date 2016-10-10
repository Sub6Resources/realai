package com.oblivionburn.nlp;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener
{		
	//Variables
	private TextToSpeech speech;
	private int int_Time = 7000;
	private int int_Delay = 0;
	private EditText Output = null;
	private EditText Input = null;
	private Boolean bl_Typing = false;
	private Boolean bl_Ready = false;
	private Boolean bl_History = false;
	private Boolean bl_Thought = false;
	public static Boolean bl_Feedback = false;
	public static File Brain_dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Brain/" );
	public static File History_dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Brain/History/" );
	public static File Thought_dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Brain/Thoughts/" );
	private Handler handler;
	private static final int MY_DATA_CHECK_CODE = 1234;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();
        
        Output = (EditText)findViewById(R.id.txt_Output);
    	Input = (EditText)findViewById(R.id.txt_Input);
    	
    	Intent checkIntent = new Intent(); 
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA); 
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
        
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
			public void afterTextChanged(Editable s){}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after){}
			
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
        
        bl_Ready = true;
        startTimer();
    }

    @Override
    public void onDestroy() 
    {
	    if (speech != null) 
	    {
		    speech.stop();
		    speech.shutdown();
	    }
	    stopTimer();
	    android.os.Process.killProcess(android.os.Process.myPid());
	    super.onDestroy();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == MY_DATA_CHECK_CODE)
        {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
            {
                speech = new TextToSpeech(this, this);
            }
            else
            {
            	Toast.makeText(getApplicationContext(), "Text-To-Speech has encountered an error.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
	public void onInit(int status)
    {
    	if (status == TextToSpeech.SUCCESS)
    	{
    		speech.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener()
    		{
                @Override
                public void onUtteranceCompleted(String utId) 
                {
                    if (utId.indexOf("ok")!=-1)
                    {
                    	startTimer();
                    }
                }
            });
    	}
    	else if (status == TextToSpeech.ERROR)
    	{
    		Toast.makeText(getApplicationContext(), "Text-To-Speech has encountered an error.", Toast.LENGTH_SHORT).show();
        }
    }
    
    //Back Button
    @Override
    public void onBackPressed()
    {
    	if (bl_History == true)
    	{
    		Output.setText("");
    		startTimer();
    		bl_History = false;
    	}
    	else if (bl_Thought == true)
    	{
    		Output.setText("");
    		startTimer();
    		bl_Thought = false;
    	}
    	else
    	{
    		Acknowledge();
    	}
    }

    //Timer
    Runnable StatusChecker = new Runnable() 
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
        		try
        		{
					try 
					{
						AttentionSpan();
					} 
					catch (InterruptedException e) 
					{
						e.printStackTrace();
					}
				}
        		catch (IOException e)
        		{
					e.printStackTrace();
					Toast.makeText(getApplicationContext(), "Error: " + e.toString(), Toast.LENGTH_LONG).show();
				}
        		int_Delay = 0;
        	}
        	handler.postDelayed(StatusChecker, int_Time);
        }
    };
        
    void startTimer() 
    {
    	int_Delay = 0;
    	StatusChecker.run(); 
    }
      
    void stopTimer() 
    {
        handler.removeCallbacks(StatusChecker);
    }
      
    public void AttentionSpan() throws IOException, InterruptedException
    {    	
    	if (bl_Typing == false)
    	{
    		Random random = new Random();
            int int_choice = random.nextInt(100);
            if (int_choice > 50)
            {
            	if (Logic.bl_NewInput == true)
        		{
        			CleanMemory();
        			Discourage();
        		}
            	
            	bl_Feedback = true;
                Output.setText("(thinking...)");
        		Logic.FeedbackLoop();
        		bl_Feedback = false;
        		
        		int_Time = 1000;
            }
            else if (int_choice > 0 && int_choice <= 50)
            {
                Logic.bl_Initiation = true;
                try
            	{
                	bl_Feedback = false;
                	Data.getHistory();
                	
                	Logic.Respond();
                	
					Data.HistoryList.add("NLP: " + Logic.str_Output);
					Data.saveHistory();
					
					Output.setText(Logic.str_Output);
					
					if (speech != null)
					{
						speech.speak(Logic.str_Output, TextToSpeech.QUEUE_FLUSH, null);
					}
					
					if (Logic.bl_NewInput == true)
					{
						CleanMemory();
						Discourage();
					}
					int_Time = 10000;
				}
            	catch (IOException e)
            	{
					e.printStackTrace();
					Toast.makeText(getApplicationContext(), "Error: " + e.toString(), Toast.LENGTH_LONG).show();
				}
            }
            Logic.bl_NewInput = false;
    	}
    }
    
    //After Enter
    public void onSend(View view)
    {
    	if (bl_Ready == true)
    	{
    		Logic.str_Input = Input.getText().toString();
        	bl_Feedback = false;
            try
            {
            	Logic.prepInput();
            	
    			Data.getHistory();
    			Logic.str_History = Logic.str_Input;
    			Logic.HistoryRules();
    			Data.HistoryList.add("User: " + Logic.str_History);
    			
    			Logic.Respond();
    			
    			Data.HistoryList.add("NLP: " + Logic.str_Output);
    			Data.saveHistory();
    			
    			Output.setText(String.format(Logic.str_Output,Output));
    			
    			Logic.ClearLeftovers();
    			Input.setText(String.format("", Input));
    			speech.speak(Logic.str_Output, TextToSpeech.QUEUE_FLUSH, null);
    			int_Time = 10000;
    		}
            catch (IOException e)
            {
            	e.printStackTrace();
            	Toast.makeText(this, "Error: " + e.toString(), Toast.LENGTH_LONG).show();
    		}
    	}
    }

    //MessageBox
    public void PopUp(String message)
    {
    	AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage(message);
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
    
    //Yes/No Box
    public void Acknowledge()
    {
    	stopTimer();
    	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
    	    @Override
    	    public void onClick(DialogInterface dialog, int which) {
    	        switch (which){
    	        case DialogInterface.BUTTON_POSITIVE:
    	        	if (speech != null)
    	    	    {
    	    		    speech.stop();
    	    		    speech.shutdown();
    	    	    }
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
      
    //Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        switch (item.getItemId()) 
        {
            case R.id.exit_app:
            	Acknowledge();
            	return true;
            case R.id.erase_memory:
                EraseMemory(Brain_dir);
                
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
                
                PopUp("Memory has been erased.");
                onInit(0);
                return true;
            case R.id.history_log:
                Data.getHistory();
                Output.setText("");
                for (int i = 0; i < Data.HistoryList.size(); i++)
                {
                	Output.append(Data.HistoryList.get(i) + "\n");
                }
                stopTimer();
                bl_History = true;
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
        
    	Output.setText(String.format("",Output));
    	Input.setText(String.format("",Input));
    }
    
    private void CleanMemory()
    {
    	Data.getInputList();
    	
    	if (Data.InputList.size() > 0)
    	{
    		for (int i = 0; i < Data.InputList.size(); i++)
        	{
        		String MemoryCheck = Data.InputList.get(i).toString();
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
        	
        	try 
        	{
    			Data.saveInputList();
    		}
        	catch (IOException e)
        	{
    			e.printStackTrace();
    		}
    	}
    }
    
    private void Discourage()
    {
    	if (!Logic.str_last_response.equals("") && !Logic.str_last_response.equals(null))
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
                if (WordArray[i].equals(","))
                {
                    WordArray[i] = " ,";
                }
                else if (WordArray[i].equals(";"))
                {
                	WordArray[i] = " ;";
                }
                else if (WordArray[i].equals(":"))
                {
                	WordArray[i] = null;
                }
                else if (WordArray[i].equals("?"))
                {
                	WordArray[i] = " $";
                }
                else if (WordArray[i].equals("$"))
                {
                	WordArray[i] = " $";
                }
                else if (WordArray[i].equals("!"))
                {
                	WordArray[i] = " !";
                }
                else if (WordArray[i].equals("."))
                {
                	WordArray[i] = " .";
                }
            }
    		
    		
    		for (int pro = 0; pro < WordArray.length - 1; pro++)
    		{
    			Data.getProWords(WordArray[pro]);
    			if (Data.Words.contains(WordArray[pro + 1]))
    			{
    				int index = Data.Words.indexOf(WordArray[pro + 1]);
        			Data.Frequencies.set(index, Data.Frequencies.get(index) - 1);
    			}
    		}
    		
    		for (int pre = 1; pre < WordArray.length; pre++)
    		{
    			Data.getPreWords(WordArray[pre]);
    			if (Data.Words.contains(WordArray[pre - 1]))
    			{
    				int index = Data.Words.indexOf(WordArray[pre - 1]);
        			Data.Frequencies.set(index, Data.Frequencies.get(index) - 1);
    			}
    		}
    	}
    }
   
}

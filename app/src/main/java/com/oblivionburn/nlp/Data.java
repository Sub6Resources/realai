package com.oblivionburn.nlp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

class Data
{
    //Variables
    public static final List<WordData> WordDataSet = new ArrayList<>();
    public static final List<String> Words = new ArrayList<>();
    public static final List<Integer> Frequencies = new ArrayList<>();
    public static final List<String> InputList = new ArrayList<>();
    public static final List<String> OutputList = new ArrayList<>();
    public static final List<String> HistoryList = new ArrayList<>();
    public static final List<String> ThoughtList = new ArrayList<>();
    public static final List<String> InformationBank = new ArrayList<>();

    //Words Data
    public static void saveWords()
    {
        BufferedWriter writer;
        try
        {
            File file = new File(MainActivity.Brain_dir, "Words.txt");
            if (!file.exists())
            {
                file.createNewFile();
            }
            writer = new BufferedWriter(new FileWriter(file));
            String WordsLine;

            for (int i = 0; i < WordDataSet.size(); i++)
            {
                WordsLine = WordDataSet.get(i).getWord() + "~" + WordDataSet.get(i).getFrequency().toString() + "\n";
                writer.write(WordsLine);
                writer.newLine();
            }
            writer.close();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public static void getWords()
    {
        WordDataSet.clear();
        Words.clear();
        Frequencies.clear();
        String WordSet[];

        File file = new File(MainActivity.Brain_dir, "Words.txt");

        try
        {
            BufferedReader br = new BufferedReader(new FileReader(file));

            String line;
            while ((line = br.readLine()) != null)
            {
                if (line.contains("~"))
                {
                    WordSet = line.split("~");
                    Words.add(WordSet[0]);
                    Frequencies.add(Integer.parseInt(WordSet[1]));
                }
            }
            for (int i = 0; i < Words.size(); i++)
            {
                WordData newset = new WordData();
                newset.setWord(Words.get(i));
                newset.setFrequency(Frequencies.get(i));
                WordDataSet.add(newset);
            }
            br.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    //PreWords Data
    public static void savePreWords(String word)
    {
        BufferedWriter writer;
        try
        {
            File file = new File(MainActivity.Brain_dir, "Pre-" + word + ".txt");
            if (!file.exists())
            {
                file.createNewFile();
            }
            writer = new BufferedWriter(new FileWriter(file));
            String WordsLine;

            for (int i = 0; i < WordDataSet.size(); i++)
            {
                WordsLine = WordDataSet.get(i).getWord() + "~" + WordDataSet.get(i).getFrequency().toString();
                writer.write(WordsLine);
                writer.newLine();
            }
            writer.close();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public static void getPreWords(String word)
    {
        WordDataSet.clear();
        Words.clear();
        Frequencies.clear();
        String WordSet[];
        File file = new File(MainActivity.Brain_dir, "Pre-" + word + ".txt");

        if (file.isFile())
        {
            try
            {
                BufferedReader br = new BufferedReader(new FileReader(file));

                String line;
                while ((line = br.readLine()) != null)
                {
                    if (line.contains("~"))
                    {
                        WordSet = line.split("~");
                        Words.add(WordSet[0]);
                        Frequencies.add(Integer.parseInt(WordSet[1]));
                    }
                }
                for (int i = 0; i < Words.size(); i++)
                {
                    WordData newset = new WordData();
                    newset.setWord(Words.get(i));
                    newset.setFrequency(Frequencies.get(i));
                    WordDataSet.add(newset);
                }

                br.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            BufferedWriter writer;
            try
            {
                writer = new BufferedWriter(new FileWriter(file));
                String WordsLine = "";
                writer.write(WordsLine);
                writer.newLine();
                writer.close();
            }
            catch(IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    //ProWords Data
    public static void saveProWords(String word)
    {
        BufferedWriter writer;
        try
        {
            File file = new File(MainActivity.Brain_dir, "Pro-" + word + ".txt");
            if (!file.exists())
            {
                file.createNewFile();
            }
            writer = new BufferedWriter(new FileWriter(file));
            String WordsLine;

            for (int i = 0; i < WordDataSet.size(); i++)
            {
                WordsLine = WordDataSet.get(i).getWord() + "~" + WordDataSet.get(i).getFrequency().toString();
                writer.write(WordsLine);
                writer.newLine();
            }
            writer.close();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public static void getProWords(String word)
    {
        WordDataSet.clear();
        Words.clear();
        Frequencies.clear();
        String WordSet[];
        File file = new File(MainActivity.Brain_dir, "Pro-" + word + ".txt");

        if (file.isFile())
        {
            try
            {
                BufferedReader br = new BufferedReader(new FileReader(file));

                String line;
                while ((line = br.readLine()) != null)
                {
                    if (line.contains("~"))
                    {
                        WordSet = line.split("~");
                        Words.add(WordSet[0]);
                        Frequencies.add(Integer.parseInt(WordSet[1]));
                    }
                }
                for (int i = 0; i < Words.size(); i++)
                {
                    WordData newset = new WordData();
                    newset.setWord(Words.get(i));
                    newset.setFrequency(Frequencies.get(i));
                    WordDataSet.add(newset);
                }
                br.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            BufferedWriter writer;
            try
            {
                writer = new BufferedWriter(new FileWriter(file));
                String WordsLine = "";
                writer.write(WordsLine);
                writer.newLine();
                writer.close();
            }
            catch(IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    //Input Data
    public static void saveInputList() {
        BufferedWriter writer;
        try
        {
            File file = new File(MainActivity.Brain_dir, "InputList.txt");
            if (!file.exists())
            {
                file.createNewFile();
            }
            writer = new BufferedWriter(new FileWriter(file));

            for (int i = 0; i < InputList.size(); i++)
            {
                writer.write(InputList.get(i));
                writer.newLine();
            }
            writer.close();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public static void getInputList()
    {
        InputList.clear();

        try
        {
            File file = new File(MainActivity.Brain_dir, "InputList.txt");
            BufferedReader br = new BufferedReader(new FileReader(file));

            String line;
            while ((line = br.readLine()) != null)
            {
                if (!line.equals(""))
                {
                    InputList.add(line);
                }
            }
            br.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    //Output Data
    public static void saveOutput(String input)
    {
        BufferedWriter writer;
        try
        {
            File file = new File(MainActivity.Brain_dir, input + ".txt");
            if (!file.exists())
            {
                file.createNewFile();
            }
            writer = new BufferedWriter(new FileWriter(file));

            for (int i = 0; i < OutputList.size(); i++)
            {
                writer.write(OutputList.get(i));
                writer.newLine();
            }
            writer.close();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public static void getOutputList(String input)
    {
        OutputList.clear();

        File file = new File(MainActivity.Brain_dir, input + ".txt");

        if (file.isFile())
        {
            try
            {
                BufferedReader br = new BufferedReader(new FileReader(file));

                String line;
                while ((line = br.readLine()) != null)
                {
                    if (!line.equals("") && !line.contains("~"))
                    {
                        OutputList.add(line);
                    }
                }
                br.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            BufferedWriter writer;
            try
            {
                writer = new BufferedWriter(new FileWriter(file));
                String WordsLine = "";
                writer.write(WordsLine);
                writer.newLine();
                writer.close();
            }
            catch(IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    public static String getTopic(String input)
    {
        String result = "";

        File file = new File(MainActivity.Brain_dir, input + ".txt");

        if (file.isFile())
        {
            try
            {
                BufferedReader br = new BufferedReader(new FileReader(file));

                String line;
                while ((line = br.readLine()) != null)
                {
                    if (line.contains("~"))
                    {
                        result = line.substring(1, line.length());
                    }
                }
                br.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return result;
    }

    //History Data
    public static void saveHistory() {
        BufferedWriter writer;
        DateFormat f = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        String currentDate = f.format(new Date());

        try
        {
            File file = new File(MainActivity.History_dir, currentDate + ".txt");
            if (!file.exists())
            {
                file.createNewFile();
            }
            writer = new BufferedWriter(new FileWriter(file));

            for (int i = 0; i < HistoryList.size(); i++)
            {
                writer.write(HistoryList.get(i));
                writer.newLine();
            }
            writer.close();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public static void getHistory()
    {
        HistoryList.clear();

        DateFormat f = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        String currentDate = f.format(new Date());
        File file = new File(MainActivity.History_dir, currentDate + ".txt");

        if (!file.exists())
        {
            saveHistory();
        }

        try
        {
            BufferedReader br = new BufferedReader(new FileReader(file));

            String line;
            while ((line = br.readLine()) != null)
            {
                if (!line.equals(""))
                {
                    HistoryList.add(line);
                }
            }
            br.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    //Thought Data
    public static void saveThoughts() {
        BufferedWriter writer;
        DateFormat f = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        String currentDate = f.format(new Date());

        try
        {
            File file = new File(MainActivity.Thought_dir, currentDate + ".txt");
            if (!file.exists())
            {
                file.createNewFile();
            }
            writer = new BufferedWriter(new FileWriter(file));

            for (int i = 0; i < ThoughtList.size(); i++)
            {
                writer.write(ThoughtList.get(i));
                writer.newLine();
            }
            writer.close();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public static void getThoughts()
    {
        ThoughtList.clear();

        DateFormat f = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        String currentDate = f.format(new Date());
        File file = new File(MainActivity.Thought_dir, currentDate + ".txt");

        if (!file.exists())
        {
            saveHistory();
        }

        try
        {
            BufferedReader br = new BufferedReader(new FileReader(file));

            String line;
            while ((line = br.readLine()) != null)
            {
                if (!line.equals(""))
                {
                    ThoughtList.add(line);
                }
            }
            br.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void pullInfo(String topic)
    {
        getInputList();
        InformationBank.clear();

        if (InputList.size() > 0)
        {
            for (int a = 0; a < InputList.size(); a++)
            {
                String result = getTopic(InputList.get(a));
                if (result.equals(topic))
                {
                    getOutputList(InputList.get(a));
                    for (int b = 0; b < OutputList.size(); b++)
                    {
                        InformationBank.add(OutputList.get(b));
                    }
                }
            }
        }
    }

}

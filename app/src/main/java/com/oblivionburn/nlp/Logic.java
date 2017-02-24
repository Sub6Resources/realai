package com.oblivionburn.nlp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Logic
{
    //Variables
    static String last_response = "";
    static Boolean Initiation = false;
    static Boolean NewInput = false;
    static Boolean UserInput = false;

    static String last_response_thinking = "";

    static String topic = "";

    static String[] prepInput(String input)
    {
        String[] wordArray = prepInput_CreateWordArray(input);

        if (wordArray != null)
        {
            if (UserInput)
            {
                UpdateInputList(input);
                prepInput_UpdateExistingFrequencies(wordArray);
                prepInput_AddNewWords(wordArray);
                prepInput_UpdatePreWords(wordArray);
                prepInput_UpdateProWords(wordArray);
            }
        }

        return wordArray;
    }

    private static String[] prepInput_CreateWordArray(String input)
    {
        String[] wordArray;
        String[] reserved = {"|", "\\", "*", "<", "\"", ":", ">"};
        String result = input;

        List<String> doc_chars = new ArrayList<>();
        for (char c : result.toCharArray())
        {
            String str = Character.toString(c);
            doc_chars.add(str);
        }

        result = "";

        for (int i = 0; i < doc_chars.size(); i++)
        {
            boolean okay = true;
            for(String s : reserved)
            {
                if (doc_chars.get(i).equals(s))
                {
                    okay = false;
                    doc_chars.set(i, null);
                    break;
                }
            }

            if (okay)
            {
                if (doc_chars.get(i).equals(","))
                {
                    doc_chars.set(i, " ,");
                }
                else if (doc_chars.get(i).equals(";"))
                {
                    doc_chars.set(i, " ;");
                }
                else if (doc_chars.get(i).equals("?"))
                {
                    doc_chars.set(i, " $");
                }
                else if (doc_chars.get(i).equals("$"))
                {
                    doc_chars.set(i, " $");
                }
                else if (doc_chars.get(i).equals("!"))
                {
                    doc_chars.set(i, " !");
                }
                else if (doc_chars.get(i).equals("."))
                {
                    if (doc_chars.size() >= i + 2)
                    {
                        if (doc_chars.get(i + 1).equals("."))
                        {
                            doc_chars.set(i, " .");
                            i = i + 2;
                        }
                        else
                        {
                            doc_chars.set(i, " .");
                        }
                    }
                    else
                    {
                        doc_chars.set(i, " .");
                    }
                }
            }
        }

        for (int i = 0; i < doc_chars.size(); i++)
        {
            result += doc_chars.get(i);
        }

        if (!result.equals(""))
        {
            wordArray = result.split(" ");

            for (int i = 0; i < wordArray.length; i++)
            {
                wordArray[i] = PunctuationFix_ForInput(wordArray[i]);
            }

            return wordArray;
        }

        return null;
    }

    private static void prepInput_UpdateExistingFrequencies(String[] wordArray)
    {
        List<WordData> data = Data.getWords();

        for (int a = 0; a < data.size(); a++)
        {
            for (String word : wordArray)
            {
                if (data.get(a).getWord().equals(word))
                {
                    data.get(a).setFrequency(data.get(a).getFrequency() + 1);
                }
            }
        }

        Data.saveWords(data);
    }

    private static void prepInput_AddNewWords(String[] wordArray)
    {
        if (wordArray.length > 0)
        {
            List<WordData> data = Data.getWords();

            for (String word : wordArray)
            {
                //noinspection SuspiciousMethodCalls
                if (data.size() > 0)
                {
                    Boolean found = false;
                    for (int i = 0; i < data.size(); i++)
                    {
                        if (data.get(i).getWord().equals(word) && !word.equals(""))
                        {
                            found = true;
                            break;
                        }
                    }

                    if (!found)
                    {
                        WordData new_wordset = new WordData();
                        new_wordset.setWord(word);
                        new_wordset.setFrequency(1);
                        data.add(new_wordset);
                    }
                }
                else
                {
                    WordData new_wordset = new WordData();
                    new_wordset.setWord(word);
                    new_wordset.setFrequency(1);
                    data.add(new_wordset);
                }
            }

            Data.saveWords(data);
        }
    }

    private static void prepInput_UpdatePreWords(String[] wordArray)
    {
        for (int i = 0; i < wordArray.length - 1; i++)
        {
            //Get current pre_words from the database
            List<WordData> data = Data.getPreWords(wordArray[i + 1]);

            List<String> words = new ArrayList<>();
            for (int a = 0; a < data.size(); a++)
            {
                words.add(data.get(a).getWord());
            }

            //Update the frequency of existing words
            if (words.contains(wordArray[i]))
            {
                int index = words.indexOf(wordArray[i]);
                data.get(index).setFrequency(data.get(index).getFrequency() + 1);
                Data.savePreWords(data, wordArray[i + 1]);
            }
            else
            {
                //Or add the word
                if (!wordArray[i].equals(""))
                {
                    WordData new_wordset = new WordData();
                    new_wordset.setWord(wordArray[i]);
                    new_wordset.setFrequency(1);
                    data.add(new_wordset);
                    Data.savePreWords(data, wordArray[i + 1]);
                }
            }
        }
    }

    private static void prepInput_UpdateProWords(String[] wordArray)
    {
        for (int i = 0; i < wordArray.length - 1; i++)
        {
            if (i != wordArray.length)
            {
                //Get current pro_words from the database
                List<WordData> data = Data.getProWords(wordArray[i]);

                List<String> words = new ArrayList<>();
                for (int b = 0; b < data.size(); b++)
                {
                    words.add(data.get(b).getWord());
                }

                //Update the frequency of existing words
                if (words.contains(wordArray[i + 1]))
                {
                    int index = words.indexOf(wordArray[i + 1]);
                    data.get(index).setFrequency(data.get(index).getFrequency() + 1);
                    Data.saveProWords(data, wordArray[i]);
                }
                else
                {
                    //Or add the word
                    if (!wordArray[i + 1].equals(""))
                    {
                        WordData new_wordset = new WordData();
                        new_wordset.setWord(wordArray[i + 1]);
                        new_wordset.setFrequency(1);
                        data.add(new_wordset);
                        Data.saveProWords(data, wordArray[i]);
                    }
                }
            }
        }
    }

    static String Respond(String[] wordArray, String input, Boolean initiation)
    {
        String output;
        String response = "";

        String lowest_word = Get_LowestFrequency(wordArray, initiation);

        if (UserInput)
        {
            AddTopic(input, lowest_word);
            topic = lowest_word;
        }

        if (NewInput)
        {
            UpdateOutputList(input);
        }

        if (lowest_word.length() > 0)
        {
            Boolean bl_MatchFound = false;

            //Check for existing responses to phrases using the topic
            List<String> info = Data.pullInfo(lowest_word);
            if (info.size() > 0)
            {
                //If some found, pick one at random
                Random rand = new Random();
                int int_random_choice = rand.nextInt(info.size());
                response = info.get(int_random_choice);
                bl_MatchFound = true;
            }

            //If none found, check for conditioned responses
            if (!bl_MatchFound)
            {
                String temp_input = PunctuationFix_ForInput(input);
                List<String> outputList = Data.getOutputList_NoTopics(temp_input);
                if (outputList.size() > 0)
                {
                    //If some found, pick one at random
                    Random rand = new Random();
                    int int_random_choice = rand.nextInt(outputList.size());
                    response = outputList.get(int_random_choice);
                    bl_MatchFound = true;
                }
            }

            //If none found, procedurally generate a response using the topic
            if (!bl_MatchFound)
            {
                response = GenerateResponse(lowest_word);
            }

            response = RulesCheck(response);
            output = response;
            last_response = response;

            NewInput = true;
        }
        else
        {
            output = "";
        }

        return output;
    }

    private static String GenerateResponse(String lowest_word)
    {
        int int_highest_f;
        String current_pre_word = lowest_word;
        String current_pro_word = lowest_word;
        String response = current_pre_word;
        Boolean words_found = true;
        String[] checker;
        String[] checker2;
        String repeater_check = "";
        Random random;

        List<WordData> data;
        List<String> words;
        List<Integer> frequencies;

        while (words_found)
        {
            data = Data.getPreWords(current_pre_word);
            if (data.size() > 0)
            {
                words = new ArrayList<>();
                frequencies = new ArrayList<>();

                for (int c = 0; c < data.size(); c++)
                {
                    words.add(data.get(c).getWord());
                    frequencies.add(data.get(c).getFrequency());
                }

                int_highest_f = GetMax(frequencies);
                List<Integer> RandomOnes = new ArrayList<>();
                for (int b = 0; b < frequencies.size(); b++)
                {
                    if (frequencies.get(b) == int_highest_f)
                    {
                        RandomOnes.add(b);
                    }
                }
                random = new Random();
                int int_choice2 = random.nextInt(RandomOnes.size());
                current_pre_word = words.get(RandomOnes.get(int_choice2));

                if (current_pre_word.length() > 1)
                {
                    StringBuilder sb = new StringBuilder(current_pre_word).delete(1, current_pre_word.length() - 1);
                    char first_letter = sb.charAt(0);
                    if (Character.isUpperCase(first_letter))
                    {
                        String str2 = response;
                        StringBuilder sb2 = new StringBuilder(str2).insert(0, current_pre_word + " ");
                        response = sb2.toString();
                        break;
                    }
                }

                checker2 = response.split(" ");
                for (String check2 : checker2)
                {
                    String check = check2;
                    check = PunctuationFix_ForInput(check);
                    if (check.equals(current_pre_word))
                    {
                        words_found = false;
                        break;
                    }
                }

                if (words_found)
                {
                    String str = response;
                    StringBuilder sb = new StringBuilder(str).insert(0, current_pre_word + " ");
                    response = sb.toString();
                }
            }
            else
            {
                words_found = false;
            }
        }
        words_found = true;

        while (words_found)
        {
            data = Data.getProWords(current_pro_word);
            if (data.size() > 0)
            {
                words = new ArrayList<>();
                frequencies = new ArrayList<>();

                for (int e = 0; e < data.size(); e++)
                {
                    words.add(data.get(e).getWord());
                    frequencies.add(data.get(e).getFrequency());
                }

                int_highest_f = GetMax(frequencies);
                List<Integer> RandomOnes = new ArrayList<>();
                for (int b = 0; b < frequencies.size(); b++)
                {
                    if (frequencies.get(b) == int_highest_f)
                    {
                        RandomOnes.add(b);
                    }
                }
                random = new Random();
                int int_choice2 = random.nextInt(RandomOnes.size());
                current_pro_word = words.get(RandomOnes.get(int_choice2));

                if (repeater_check.length() > 0)
                {
                    checker = repeater_check.split(" ");
                    for (String check1 : checker)
                    {
                        String check = check1;
                        check = PunctuationFix_ForInput(check);
                        if (check.equals(current_pro_word))
                        {
                            words_found = false;
                            break;
                        }
                    }
                }

                if (words_found)
                {
                    String str = response;
                    StringBuilder sb = new StringBuilder(str).insert(response.length(), " " + current_pro_word);
                    response = sb.toString();

                    String str2 = repeater_check;
                    StringBuilder sb2 = new StringBuilder(str2).insert(repeater_check.length(), current_pro_word + " ");
                    repeater_check = sb2.toString();

                    if (current_pro_word.equals(".") || current_pro_word.equals("$") || current_pro_word.equals("!"))
                    {
                        break;
                    }
                }
            }
            else
            {
                words_found = false;
            }
        }

        return response;
    }

    private static void UpdateInputList(String input)
    {
        String new_input = input;
        List<String> inputList = Data.getInputList();

        if (input.length() > 1)
        {
            new_input = PunctuationFix_ForInput(new_input);
        }

        if (!inputList.contains(new_input))
        {
            inputList.add(new_input);
            Data.saveInputList(inputList);
        }
    }

    private static void UpdateOutputList(String input)
    {
        String temp_input = input;
        String temp_last_response = last_response;

        if (temp_input.length() > 1)
        {
            temp_input = PunctuationFix_ForInput(temp_input);
        }

        if (temp_last_response.length() > 1)
        {
            temp_last_response = PunctuationFix_ForInput(temp_last_response);
        }

        //Add new input to previous response output list
        List<String> output = Data.getOutputList(temp_last_response);

        if (!output.contains(temp_input) && !temp_last_response.equals(temp_input))
        {
            output.add(temp_input);
            Data.saveOutput(output, temp_last_response);
        }
    }

    private static void AddTopic(String input, String lowest_word)
    {
        String temp_input = input;

        if (temp_input.length() > 1)
        {
            temp_input = PunctuationFix_ForInput(temp_input);
        }

        //Add lowest frequency word to current input's output list
        List<String> output = Data.getOutputList(temp_input);
        if (output.size() > 0)
        {
            if (output.get(0).contains("~"))
            {
                output.remove(0);
            }
        }

        if (!(lowest_word.equals(" .") || lowest_word.equals(" $") || lowest_word.equals(" !") || lowest_word.equals(" ,") || lowest_word.equals("")))
        {
            output.add(0, "~" + lowest_word);
            Data.saveOutput(output, temp_input);
        }
    }

    private static String Get_LowestFrequency(String[] wordArray, Boolean initiation)
    {
        List<String> words = new ArrayList<>();
        List<Integer> frequencies = new ArrayList<>();
        int int_lowest_f;
        String lowest_word = "";

        List<WordData> data = Data.getWords();

        if (initiation)
        {
            if (topic.length() > 0)
            {
                lowest_word = topic;
            }
            else
            {
                lowest_word = Get_RandomWord();
                topic = lowest_word;
            }
        }
        else
        {
            if (wordArray != null)
            {
                for (String word : wordArray)
                {
                    for (int a2 = 0; a2 < data.size(); a2++)
                    {
                        if (data.get(a2).getWord().equals(word))
                        {
                            words.add(data.get(a2).getWord());
                            frequencies.add(data.get(a2).getFrequency());
                        }
                    }
                }
            }

            if (frequencies.size() > 0)
            {
                int_lowest_f = GetMin(frequencies);
                List<Integer> RandomOnes = new ArrayList<>();
                for (int b = 0; b < frequencies.size(); b++)
                {
                    if (frequencies.get(b) == int_lowest_f)
                    {
                        RandomOnes.add(b);
                    }
                }

                Boolean bl_accepted;
                for (int i = 0; i < RandomOnes.size(); i++)
                {
                    Random random = new Random();
                    int int_choice = random.nextInt(RandomOnes.size());
                    lowest_word = words.get(RandomOnes.get(int_choice));

                    bl_accepted = !(lowest_word.equals(" .") || lowest_word.equals(" $") || lowest_word.equals(" !") || lowest_word.equals(" ,"));

                    if (bl_accepted)
                    {
                        break;
                    }
                    else if (i == RandomOnes.size() - 1)
                    {
                        lowest_word = Get_RandomWord();
                    }
                }
            }
        }

        return lowest_word;
    }

    private static String Get_RandomWord()
    {
        List<String> words = new ArrayList<>();
        String lowest_word = "";

        List<WordData> data = Data.getWords();

        for (int a = 0; a < data.size(); a++)
        {
            words.add(data.get(a).getWord());
        }

        if (words.size() > 0)
        {
            Boolean bl_accepted;
            for (int i = 0; i < words.size(); i++)
            {
                Random random = new Random();
                int int_choice = random.nextInt(words.size());
                lowest_word = words.get(int_choice);

                bl_accepted = !(lowest_word.equals(" .") || lowest_word.equals(" $") || lowest_word.equals(" !") || lowest_word.equals(" ,"));

                if (bl_accepted)
                {
                    break;
                }
            }
        }

        return lowest_word;
    }

    private static int GetMin(List<Integer> Integer_List)
    {
        int lowest_number = 0;
        int current_number = Integer_List.get(0);
        for (int b = 0; b < Integer_List.size(); b++)
        {
            if (current_number <= Integer_List.get(b))
            {
                lowest_number = current_number;
            }
            else
            {
                current_number = Integer_List.get(b);
            }
        }

        return lowest_number;
    }

    private static int GetMax(List<Integer> Integer_List)
    {
        int highest_number = 0;
        int current_number = Integer_List.get(0);
        for (int b = 0; b < Integer_List.size(); b++)
        {
            if (current_number >= Integer_List.get(b))
            {
                highest_number = current_number;
            }
            else
            {
                current_number = Integer_List.get(b);
            }
        }

        return highest_number;
    }

    private static String PunctuationFix_ForInput(String old_word)
    {
        String word = old_word;

        if (word.contains("$") && word.indexOf('$') > 0)
        {
            if (word.charAt(word.indexOf("$") - 1) != ' ')
            {
                String str = word;
                StringBuilder sb = new StringBuilder(str);
                sb.replace(str.indexOf('$'), str.indexOf('$') + 1, " $");
                word = sb.toString();
            }
        }
        else if (word.contains("?") && word.indexOf('?') > 0)
        {
            if (word.charAt(word.indexOf("?") - 1) != ' ')
            {
                String str = word;
                StringBuilder sb = new StringBuilder(str);
                sb.replace(str.indexOf('?'), str.indexOf('?') + 1, " $");
                word = sb.toString();
            }
        }
        else if (word.contains(".") && word.indexOf('.') > 0)
        {
            if (word.charAt(word.indexOf(".") - 1) != ' ')
            {
                String str = word;
                StringBuilder sb = new StringBuilder(str);
                sb.replace(str.indexOf('.'), str.indexOf('.') + 1, " .");
                word = sb.toString();
            }
        }
        else if (word.contains("!") && word.indexOf('!') > 0)
        {
            if (word.charAt(word.indexOf("!") - 1) != ' ')
            {
                String str = word;
                StringBuilder sb = new StringBuilder(str);
                sb.replace(str.indexOf('!'), str.indexOf('!') + 1, " !");
                word = sb.toString();
            }
        }
        else if (word.contains(",") && word.indexOf(',') > 0)
        {
            if (word.charAt(word.indexOf(",") - 1) != ' ')
            {
                String str = word;
                StringBuilder sb = new StringBuilder(str);
                sb.replace(str.indexOf(','), str.indexOf(',') + 1, " ,");
                word = sb.toString();
            }
        }
        else if (word.contains(";") && word.indexOf(';') > 0)
        {
            if (word.charAt(word.indexOf(";") - 1) != ' ')
            {
                String str = word;
                StringBuilder sb = new StringBuilder(str);
                sb.replace(str.indexOf(';'), str.indexOf(';') + 1, " ;");
                word = sb.toString();
            }
        }
        else if (word.equals("$"))
        {
            word = " $";
        }
        else if (word.equals("?"))
        {
            word = " $";
        }
        else if (word.equals("."))
        {
            word = " .";
        }
        else if (word.equals("!"))
        {
            word = " !";
        }
        else if (word.equals(","))
        {
            word = " ,";
        }
        else if (word.equals(";"))
        {
            word = " ;";
        }

        return word;
    }

    static String Think(String[] wordArray)
    {
        String output;
        String response = "";

        String lowest_word = Get_LowestFrequency(wordArray, true);

        //Get topic
        if (lowest_word.length() > 0)
        {
            Boolean bl_MatchFound = false;

            //Check for existing responses to phrases using the topic
            List<String> info = Data.pullInfo(lowest_word);
            if (info.size() > 0)
            {
                //If some found, pick one at random
                Random rand = new Random();
                int int_random_choice = rand.nextInt(info.size());
                response = info.get(int_random_choice);
                bl_MatchFound = true;
            }

            //If none found, check for conditioned responses
            if (!bl_MatchFound)
            {
                String temp_input = PunctuationFix_ForInput(last_response_thinking);
                List<String> outputList = Data.getOutputList_NoTopics(temp_input);
                if (outputList.size() > 0)
                {
                    //If some found, pick one at random
                    Random rand = new Random();
                    int int_random_choice = rand.nextInt(outputList.size());
                    response = outputList.get(int_random_choice);
                    bl_MatchFound = true;
                }
            }

            //If none found, procedurally generate a response using the topic
            if (!bl_MatchFound)
            {
                response = GenerateResponse(lowest_word);
            }

            response = RulesCheck(response);

            if (response.equals(last_response_thinking))
            {
                lowest_word = Get_LowestFrequency(wordArray, true);
                response = GenerateResponse(lowest_word);
            }

            output = response;
        }
        else
        {
            output = "";
        }

        return output;
    }

    static void ClearLeftovers()
    {
        File file = new File(MainActivity.Brain_dir, ".txt");
        if (file.exists())
        {
            file.delete();
        }

        file = new File(MainActivity.Brain_dir, ",.txt");
        if (file.exists())
        {
            file.delete();
        }

        file = new File(MainActivity.Brain_dir, "..txt");
        if (file.exists())
        {
            file.delete();
        }
    }

    private static String RulesCheck(String input)
    {
        String response = input;

        if (response.length() > 1)
        {
            //Learn which words should be capitalized by example
            String[] str_response_check = response.split(" ");
            for (int i = 1; i < str_response_check.length; i++)
            {
                String str_checked_word = str_response_check[i];
                if (!str_checked_word.equals(""))
                {
                    char capital_letter = str_checked_word.charAt(0);
                    if (Character.isUpperCase(capital_letter) && !str_checked_word.equals("I"))
                    {
                        List<String> words = new ArrayList<>();
                        List<Integer> frequencies = new ArrayList<>();

                        List<WordData> data = Data.getWords();
                        for (int a = 0; a < data.size(); a++)
                        {
                            words.add(data.get(a).getWord());
                            frequencies.add(data.get(a).getFrequency());
                        }

                        String str_lower_word = str_checked_word;
                        String str_capital_letter = Character.toString(capital_letter);
                        int int_high_frequency = 0;
                        int int_low_frequency = 0;
                        List<Integer> Frequency_List = new ArrayList<>();
                        str_capital_letter = str_capital_letter.toLowerCase();
                        String str = str_lower_word;
                        StringBuilder sb = new StringBuilder(str).replace(0, 0, "");
                        sb.insert(0, str_capital_letter);
                        str_lower_word = sb.toString();

                        for (int b = 0; b < words.size(); b++)
                        {
                            if (words.get(b).equals(str_lower_word))
                            {
                                int_low_frequency = frequencies.get(b);
                                Frequency_List.add(int_low_frequency);
                            }
                            else if (words.get(b).equals(str_checked_word))
                            {
                                int_high_frequency = frequencies.get(b);
                                Frequency_List.add(int_high_frequency);
                            }
                        }

                        if (Frequency_List.size() > 0)
                        {
                            if (GetMax(Frequency_List) == int_low_frequency)
                            {
                                str_response_check[i] = str_lower_word;
                            }
                            else if (GetMax(Frequency_List) == int_high_frequency)
                            {
                                str_response_check[i] = str_checked_word;
                            }
                        }
                    }
                }
            }
            String str_new_response = "";

            for (String word : str_response_check)
            {
                str_new_response += word + " ";
            }
            response = str_new_response;

            //Remove any spaces before commas
            while (response.contains(" ,"))
            {
                String str3 = response;
                StringBuilder sb3 = new StringBuilder(str3).replace(response.indexOf(" ,"), response.indexOf(" ,") + 2, ",");
                response = sb3.toString();
            }

            //Remove any spaces before colons
            while (response.contains(" :"))
            {
                String str3 = response;
                StringBuilder sb3 = new StringBuilder(str3).replace(response.indexOf(" :"), response.indexOf(" :") + 2, ":");
                response = sb3.toString();
            }

            //Remove any spaces before semicolons
            while (response.contains(" ;"))
            {
                String str3 = response;
                StringBuilder sb3 = new StringBuilder(str3).replace(response.indexOf(" ;"), response.indexOf(" ;") + 2, ";");
                response = sb3.toString();
            }

            //Make sure the first word is capitalized
            char first_letter = response.charAt(0);
            if (!Character.isUpperCase(first_letter))
            {
                String str_capital_letter = Character.toString(first_letter);
                str_capital_letter = str_capital_letter.toUpperCase();
                String str2 = response;
                StringBuilder sb2 = new StringBuilder(str2).delete(0, 1);
                sb2.insert(0, str_capital_letter);
                response = sb2.toString();
            }

            //Remove any empty spaces at the end
            String str2 = response;
            StringBuilder sb2 = new StringBuilder(str2).delete(0, response.length() - 1);
            char last_letter = sb2.charAt(0);
            String str_last_letter = Character.toString(last_letter);

            while (str_last_letter.equals(" "))
            {
                String str3 = response;
                StringBuilder sb3 = new StringBuilder(str3).delete(response.length() - 1, response.length());
                response = sb3.toString();

                String str4 = response;
                StringBuilder sb4 = new StringBuilder(str4).delete(0, response.length() - 1);
                last_letter = sb4.charAt(0);

                str_last_letter = Character.toString(last_letter);
            }

            //Set an ending punctuation if one does not exist
            if (!str_last_letter.equals(".") && !str_last_letter.equals("$") && !str_last_letter.equals("!"))
            {
                String str3 = response;
                StringBuilder sb3 = new StringBuilder(str3).insert(response.length(), ".");
                response = sb3.toString();
            }

            //Learn the best ending punctuation from example
            if (response.endsWith("$") || response.endsWith(".") || response.endsWith("!"))
            {
                List<String> inputList = Data.getInputList();
                if (inputList.size() > 0)
                {
                    int q_count = 0;
                    int p_count = 0;
                    int e_count = 0;

                    for (int i = 0; i < inputList.size(); i++)
                    {
                        String CurrentSentence = inputList.get(i);
                        String[] str_currentwords_check = CurrentSentence.split(" ");
                        String[] str_response_check2 = response.split(" ");

                        if (str_currentwords_check.length > 0 && str_response_check2.length > 0)
                        {
                            if (str_currentwords_check[0].equals(str_response_check2[0]))
                            {
                                switch (str_currentwords_check[str_currentwords_check.length - 1])
                                {
                                    case "$":
                                        q_count++;
                                        break;
                                    case ".":
                                        p_count++;
                                        break;
                                    case "!":
                                        e_count++;
                                        break;
                                }
                            }
                        }
                    }

                    if (q_count > p_count && q_count > e_count)
                    {
                        String str3 = response;
                        StringBuilder sb3 = new StringBuilder(str3).replace(response.length() - 1, response.length(), "$");
                        response = sb3.toString();
                    }
                    else if (p_count > q_count && p_count > e_count)
                    {
                        String str3 = response;
                        StringBuilder sb3 = new StringBuilder(str3).replace(response.length() - 1, response.length(), ".");
                        response = sb3.toString();
                    }
                    else if (e_count > q_count && e_count > p_count)
                    {
                        String str3 = response;
                        StringBuilder sb3 = new StringBuilder(str3).replace(response.length() - 1, response.length(), "!");
                        response = sb3.toString();
                    }
                }
            }

            //Replace any dollar signs with question marks
            while (response.contains("$"))
            {
                String str3 = response;
                StringBuilder sb3 = new StringBuilder(str3).replace(response.indexOf("$"), response.indexOf("$") + 1, "?");
                response = sb3.toString();
            }

            //Remove any spaces before ending punctuation
            while (response.contains(" ."))
            {
                String str3 = response;
                StringBuilder sb3 = new StringBuilder(str3).replace(response.indexOf(" ."), response.indexOf(" .") + 2, ".");
                response = sb3.toString();
            }
            while (response.contains(" ?"))
            {
                String str3 = response;
                StringBuilder sb3 = new StringBuilder(str3).replace(response.indexOf(" ?"), response.indexOf(" ?") + 2, "?");
                response = sb3.toString();
            }
            while (response.contains(" !"))
            {
                String str3 = response;
                StringBuilder sb3 = new StringBuilder(str3).replace(response.indexOf(" !"), response.indexOf(" !") + 2, "!");
                response = sb3.toString();
            }
        }

        return response;
    }

    static String HistoryRules(String old_string)
    {
        String new_string = old_string;

        if (new_string.length() > 1 && !new_string.equals(""))
        {
            //Remove any spaces before commas
            while (new_string.contains(" ,"))
            {
                String str3 = new_string;
                StringBuilder sb3 = new StringBuilder(str3).replace(new_string.indexOf(" ,"), new_string.indexOf(" ,") + 2, ",");
                new_string = sb3.toString();
            }

            //Remove any spaces before colons
            while (new_string.contains(" :"))
            {
                String str3 = new_string;
                StringBuilder sb3 = new StringBuilder(str3).replace(new_string.indexOf(" :"), new_string.indexOf(" :") + 2, ":");
                new_string = sb3.toString();
            }

            //Remove any spaces before semicolons
            while (new_string.contains(" ;"))
            {
                String str3 = new_string;
                StringBuilder sb3 = new StringBuilder(str3).replace(new_string.indexOf(" ;"), new_string.indexOf(" ;") + 2, ";");
                new_string = sb3.toString();
            }

            //Make sure the first word is capitalized
            char first_letter = new_string.charAt(0);
            if (!Character.isUpperCase(first_letter))
            {
                String str_capital_letter = Character.toString(first_letter);
                str_capital_letter = str_capital_letter.toUpperCase();
                String str2 = new_string;
                StringBuilder sb2 = new StringBuilder(str2).delete(0, 1);
                sb2.insert(0, str_capital_letter);
                new_string = sb2.toString();
            }

            //Remove any empty spaces at the end
            String str2 = new_string;
            StringBuilder sb2 = new StringBuilder(str2).delete(0, new_string.length() - 1);
            char last_letter = sb2.charAt(0);
            String str_last_letter = Character.toString(last_letter);

            while (str_last_letter.equals(" "))
            {
                String str3 = new_string;
                StringBuilder sb3 = new StringBuilder(str3).delete(new_string.length() - 1, new_string.length());
                new_string = sb3.toString();

                String str4 = new_string;
                StringBuilder sb4 = new StringBuilder(str4).delete(0, new_string.length() - 1);
                last_letter = sb4.charAt(0);

                str_last_letter = Character.toString(last_letter);
            }

            //Set an ending punctuation if one does not exist
            if (!str_last_letter.equals(".") && !str_last_letter.equals("$") && !str_last_letter.equals("!") && !str_last_letter.equals("?"))
            {
                String str3 = new_string;
                StringBuilder sb3 = new StringBuilder(str3).insert(new_string.length(), ".");
                new_string = sb3.toString();
            }

            //Replace any dollar signs with question marks
            while (new_string.contains("$"))
            {
                String str3 = new_string;
                StringBuilder sb3 = new StringBuilder(str3).replace(new_string.indexOf("$"), new_string.indexOf("$") + 1, "?");
                new_string = sb3.toString();
            }

            //Remove any spaces before ending punctuation
            while (new_string.contains(" ."))
            {
                String str3 = new_string;
                StringBuilder sb3 = new StringBuilder(str3).replace(new_string.indexOf(" ."), new_string.indexOf(" .") + 2, ".");
                new_string = sb3.toString();
            }
            while (new_string.contains(" ?"))
            {
                String str3 = new_string;
                StringBuilder sb3 = new StringBuilder(str3).replace(new_string.indexOf(" ?"), new_string.indexOf(" ?") + 2, "?");
                new_string = sb3.toString();
            }
            while (new_string.contains(" !"))
            {
                String str3 = new_string;
                StringBuilder sb3 = new StringBuilder(str3).replace(new_string.indexOf(" !"), new_string.indexOf(" !") + 2, "!");
                new_string = sb3.toString();
            }
        }

        return new_string;
    }
}

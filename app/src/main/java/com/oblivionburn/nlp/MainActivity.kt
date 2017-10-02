package com.oblivionburn.nlp

import java.io.File
import java.io.IOException
import java.text.DateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Environment
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.method.ScrollingMovementMethod
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import com.sub6resources.utilities.fromApi

class MainActivity : com.sub6resources.utilities.BaseActivity(R.layout.activity_main), OnItemSelectedListener {

    override val menu = R.menu.main

    private var int_Time = 10000
    private var int_Delay = 0
    private var wordfix_selection = 0
    private var delay_selection = 0
    private val ready = 0

    private var Output: EditText? = null
    private var Input: EditText? = null
    private var txt_WordFix: EditText? = null
    private var sp_WordFix: Spinner? = null
    private var btn_WordFix: Button? = null
    private var btn_Enter: Button? = null
    private var btn_Encourage: Button? = null
    private var btn_Discourage: Button? = null
    private var img_Face: ImageView? = null

    private var bl_Typing = false
    private var bl_Ready = false
    private var bl_Thought = false
    private var bl_WordFix = false
    private var bl_Delay = false
    private var bl_DelayForever = false
    private var bl_Tips = false
    private var bl_PermitsMissing = false
    private var bl_Encourage_Pressed = false
    private var bl_Discourage_Pressed = false

    private var handler: Handler? = null
    private var KeyboardOpen: Boolean = false
    private var rootView: View? = null

    private fun createBrain() {
        if (!Brain_dir.exists()) {
            Brain_dir.mkdirs()
        }

        var file = File(Brain_dir, "Words.txt")
        if (!file.exists()) {
            try {
                file.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(applicationContext, "Error: " + e.toString(), Toast.LENGTH_LONG).show()
            }

        }

        file = File(Brain_dir, "InputList.txt")
        if (!file.exists()) {
            try {
                file.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(applicationContext, "Error: " + e.toString(), Toast.LENGTH_LONG).show()
            }

        }

        file = File(Brain_dir, "Config.ini")
        if (!file.exists()) {
            try {
                file.createNewFile()
                Data.initConfig()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(applicationContext, "Error: " + e.toString(), Toast.LENGTH_LONG).show()
            }

        } else {
            val config = Data.getDelay()
            when (config) {
                "10 seconds" -> {
                    int_Time = 10000
                    delay_selection = 0
                }

                "20 seconds" -> {
                    int_Time = 20000
                    delay_selection = 1
                }

                "30 seconds" -> {
                    int_Time = 30000
                    delay_selection = 2
                }

                "Infinite" -> {
                    delay_selection = 3
                    bl_DelayForever = true
                }
            }

            val advanced = Data.getAdvanced()
            when (advanced) {
                "Off" -> Logic.Advanced = false

                "On" -> Logic.Advanced = true
            }
        }

        if (!History_dir.exists()) {
            History_dir.mkdirs()
        }

        if (!Thought_dir.exists()) {
            Thought_dir.mkdirs()
        }
    }

    lateinit var tts: TextToSpeech

    override fun setUp() {
        Input = findViewById<View>(R.id.txt_Input) as EditText

        Output = findViewById<View>(R.id.txt_Output) as EditText
        Output!!.maxLines = Integer.MAX_VALUE

        btn_Enter = findViewById<View>(R.id.btn_Enter) as Button

        sp_WordFix = findViewById<View>(R.id.sp_WordFix) as Spinner
        sp_WordFix!!.onItemSelectedListener = this
        txt_WordFix = findViewById<View>(R.id.txt_WordFix) as EditText
        btn_WordFix = findViewById<View>(R.id.btn_WordFix) as Button

        btn_Encourage = findViewById<View>(R.id.btn_Encourage) as Button
        btn_Discourage = findViewById<View>(R.id.btn_Discourage) as Button

        img_Face = findViewById<View>(R.id.img_Face) as ImageView

        handler = Handler()

        createBrain()
        createListeners()

        rootView = findViewById(android.R.id.content)
        rootView!!.viewTreeObserver.addOnGlobalLayoutListener {
            val heightDiff = rootView!!.rootView.height - rootView!!.height
            KeyboardOpen = heightDiff > dpToPx(applicationContext)
        }

        fromApi(23, {
            checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    onGranted = {
                        bl_Ready = true
                    },
                    onDenied = {},
                    showExplanation = {id ->
                        recheckPermission(id)
                    })
        })
        tts = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = Locale.getDefault()
            }
        })

    }


    private fun createListeners() {
        Input!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {

            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                img_Face!!.setImageResource(R.drawable.face_neutral)

                if (Input!!.text.toString() == "") {
                    bl_Typing = false
                    startTimer()
                    startThinking()
                } else {
                    bl_Typing = true
                    Logic.Initiation = false
                    stopTimer()
                    stopThinking()
                }
            }
        })

        Input!!.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onSend(v)
            }
            true
        }

        btn_Encourage!!.setOnTouchListener(object : OnTouchListener {
            private val handler = Handler()
            private val runnable = object : Runnable {
                override fun run() {
                    if (!bl_Encourage_Pressed) {
                        handler.removeCallbacks(this)
                        img_Face!!.setImageResource(R.drawable.face_neutral)
                    } else {
                        bl_Encourage_Pressed = false
                        handler.postDelayed(this, 250)
                    }
                }
            }

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    img_Face!!.setImageResource(R.drawable.face_encourage)
                    bl_Encourage_Pressed = true
                }

                if (event.action == MotionEvent.ACTION_UP) {
                    handler.postDelayed(runnable, 250)
                }
                return false
            }
        })

        btn_Discourage!!.setOnTouchListener(object : OnTouchListener {
            private val handler = Handler()
            private val runnable = object : Runnable {
                override fun run() {
                    if (!bl_Discourage_Pressed) {
                        handler.removeCallbacks(this)
                        img_Face!!.setImageResource(R.drawable.face_neutral)
                    } else {
                        bl_Discourage_Pressed = false
                        handler.postDelayed(this, 250)
                    }
                }
            }

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    img_Face!!.setImageResource(R.drawable.face_discourage)
                    bl_Discourage_Pressed = true
                }

                if (event.action == MotionEvent.ACTION_UP) {
                    handler.postDelayed(runnable, 250)
                }
                return false
            }
        })
    }

    public override fun onDestroy() {
        stopTimer()
        stopThinking()
        android.os.Process.killProcess(android.os.Process.myPid())
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }

    //Back Button
    override fun onBackPressed() {
        if (bl_Thought) {
            CloseThought()
        } else if (bl_WordFix || bl_Delay) {
            CloseWordFix()
        } else if (bl_Tips) {
            CloseTips()
        } else if (bl_PermitsMissing) {
            onDestroy()
        } else {
            Acknowledge_Exit()
        }
    }

    //Timer
    private val Timer = object : Runnable {
        override fun run() {
            if (int_Delay == 0) {
                int_Delay++
            } else if (int_Delay == 1 && !bl_DelayForever) {
                AttentionSpan()
                int_Delay = 0
            }
            handler!!.postDelayed(this, int_Time.toLong())
        }
    }

    private fun startTimer() {
        int_Delay = 0
        Timer.run()
    }

    private fun stopTimer() {
        handler!!.removeCallbacks(Timer)
    }

    //Thinking
    private val Thought = object : Runnable {
        override fun run() {
            Logic.UserInput = false

            val thoughts = Data.getThoughts()
            val wordArray = Logic.prepInput(Logic.last_response_thinking)

            Logic.last_response_thinking = Logic.Think(wordArray)
            Logic.last_response_thinking = Logic.HistoryRules(Logic.last_response_thinking)

            if (Logic.last_response_thinking != "") {
                thoughts.add("Thought: " + Logic.last_response_thinking)
                Data.saveThoughts(thoughts)
            }

            Logic.ClearLeftovers()

            if (bl_Thought) {
                ScrollThoughts()
            }

            handler!!.postDelayed(this, 500)
        }
    }

    private fun startThinking() {
        Thought.run()
    }

    private fun stopThinking() {
        handler!!.removeCallbacks(Thought)
    }

    //Try to initiate conversation
    private fun AttentionSpan() {
        if (!bl_Typing) {
            if (Logic.NewInput) {
                CleanMemory()
            }

            Logic.NewInput = false
            Logic.Initiation = true
            Logic.UserInput = false

            val history = Data.getHistory()
            val wordArray = arrayOfNulls<String>(0)

            val output = Logic.Respond(wordArray, "")

            if (output != "") {
                history.add("AI: " + output)
                Data.saveHistory(history)
                tts.speak(output, TextToSpeech.QUEUE_ADD, null, null)
            }

            ScrollHistory()
            img_Face!!.setImageResource(R.drawable.face_neutral)
        }
    }

    //After Enter
    fun onSend(view: View) {
        if (bl_WordFix || bl_Delay) {
            CloseWordFix()
        } else if (bl_Thought) {
            CloseThought()
        } else if (bl_Tips) {
            CloseTips()
        } else if (bl_PermitsMissing) {
            onDestroy()
        } else if (bl_Ready) {
            var input = Input!!.text.toString()
            if (input.length > 0) {
                Logic.Initiation = false
                Logic.UserInput = true
                val wordArray = Logic.prepInput(input)

                val history = Data.getHistory()
                input = Logic.HistoryRules(input)
                history.add("User: " + input)

                val output = Logic.Respond(wordArray, input)

                if (output != "") {
                    history.add("AI: " + output)
                }

                Data.saveHistory(history)

                ScrollHistory()

                Logic.ClearLeftovers()
                Input!!.setText("")
                img_Face!!.setImageResource(R.drawable.face_neutral)
            }
        }
    }

    private fun ScrollHistory() {
        Output!!.setText("")

        val history = Data.getHistory()
        if (history.size > 40) {
            for (i in history.size - 40..history.size - 1) {
                Output!!.append(history[i] + "\n")
            }
        } else {
            for (i in history.indices) {
                Output!!.append(history[i] + "\n")
            }
        }

        Output!!.setSelection(Output!!.text.length)
    }

    private fun ScrollThoughts() {
        Output!!.setText("")
        Output!!.movementMethod = ScrollingMovementMethod()

        val thoughts = Data.getThoughts()
        if (thoughts.size > 40) {
            for (i in thoughts.size - 40..thoughts.size - 1) {
                Output!!.append(thoughts[i] + "\n")
            }
        } else {
            for (i in thoughts.indices) {
                Output!!.append(thoughts[i] + "\n")
            }
        }

        Output!!.setSelection(Output!!.text.length)
    }

    //MessageBox
    private fun PopUp() {
        val dlgAlert = AlertDialog.Builder(this)
        dlgAlert.setMessage("Memory has been erased.")
        dlgAlert.setTitle("System Message")
        dlgAlert.setPositiveButton("Ok") { dialog, which -> }
        dlgAlert.setCancelable(false)
        dlgAlert.create().show()
    }

    //Yes/No Box for Exit
    private fun Acknowledge_Exit() {
        stopTimer()
        val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> android.os.Process.killProcess(android.os.Process.myPid())
                DialogInterface.BUTTON_NEGATIVE -> startTimer()
            }
        }
        val dlgAlert = AlertDialog.Builder(this)
        dlgAlert.setMessage("Exit the NLP Program?")
        dlgAlert.setTitle("System Message")
        dlgAlert.setNegativeButton("No", dialogClickListener)
        dlgAlert.setPositiveButton("Yes", dialogClickListener)
        dlgAlert.setCancelable(false)
        dlgAlert.create().show()
    }

    //Yes/No Box for Erase
    private fun Acknowledge_Erase() {
        stopTimer()
        val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    EraseMemory(Brain_dir)

                    Output!!.setText("")
                    Input!!.setText("")

                    Logic.last_response = ""
                    Logic.last_response_thinking = ""
                    Logic.topics.clear()

                    if (!Brain_dir.exists()) {
                        Brain_dir.mkdirs()
                    }

                    var file = File(Brain_dir, "Words.txt")
                    if (!file.exists()) {
                        try {
                            file.createNewFile()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                    }

                    file = File(Brain_dir, "InputList.txt")
                    if (!file.exists()) {
                        try {
                            file.createNewFile()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                    }

                    val f = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault())
                    val currentDate = f.format(Date())

                    file = File(History_dir, currentDate + ".txt")
                    if (!History_dir.exists()) {
                        History_dir.mkdirs()
                        if (!file.exists()) {
                            try {
                                file.createNewFile()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }

                        }
                    }

                    file = File(Thought_dir, currentDate + ".txt")
                    if (!Thought_dir.exists()) {
                        Thought_dir.mkdirs()
                        if (!file.exists()) {
                            try {
                                file.createNewFile()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }

                        }
                    }

                    PopUp()
                }
                DialogInterface.BUTTON_NEGATIVE -> startTimer()
            }
        }

        val dlgAlert = AlertDialog.Builder(this)
        dlgAlert.setMessage("Erase all memory?")
        dlgAlert.setTitle("System Message")
        dlgAlert.setNegativeButton("No", dialogClickListener)
        dlgAlert.setPositiveButton("Yes", dialogClickListener)
        dlgAlert.setCancelable(false)
        dlgAlert.create().show()
    }

    override fun onMenuOpened(featureId: Int, menu: Menu?): Boolean {
        if (KeyboardOpen) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(Input!!.windowToken, 0)
        }

        stopTimer()
        stopThinking()

        return super.onMenuOpened(featureId, menu)
    }

    override fun onPanelClosed(featureId: Int, menu: Menu) {
        if (!bl_Thought && !bl_Tips) {
            if (!bl_WordFix && !bl_Delay) {
                Output!!.visibility = View.VISIBLE
                Input!!.visibility = View.VISIBLE

                btn_Enter!!.setText(R.string.enter_button)
                btn_Enter!!.visibility = View.VISIBLE

                btn_Encourage!!.visibility = View.VISIBLE
                btn_Discourage!!.visibility = View.VISIBLE
                img_Face!!.visibility = View.VISIBLE
                img_Face!!.setImageResource(R.drawable.face_neutral)

                startTimer()
                startThinking()
            } else {
                btn_Enter!!.setText(R.string.back_button)
                btn_Enter!!.visibility = View.VISIBLE
            }
        } else {
            btn_Enter!!.setText(R.string.back_button)
            btn_Enter!!.visibility = View.VISIBLE
            Output!!.visibility = View.VISIBLE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.tips -> {
                DisplayTips()
                return true
            }

            R.id.thought_log -> {
                stopTimer()
                startThinking()
                bl_Thought = true
                return true
            }

            R.id.word_fix -> {
                DisplayWordFix()
                return true
            }

            R.id.setdelay -> {
                DisplayDelay()
                return true
            }

            R.id.erase_memory -> {
                Acknowledge_Erase()
                return true
            }

            R.id.advanced -> {
                ToggleAdvanced(item)
                return true
            }

            R.id.exit_app -> {
                Acknowledge_Exit()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun ToggleAdvanced(item: MenuItem) {
        if (Logic.Advanced) {
            Logic.Advanced = false
            item.title = "Advanced Mode: Off"

            if (bl_DelayForever) {
                Data.setConfig("Infinite", "Off")
            } else {
                Data.setConfig((int_Time / 1000).toString() + " seconds", "Off")
            }
        } else {
            Logic.Advanced = true
            item.title = "Advanced Mode: On"

            if (bl_DelayForever) {
                Data.setConfig("Infinite", "On")
            } else {
                Data.setConfig((int_Time / 1000).toString() + " seconds", "On")
            }
        }
    }

    private fun EraseMemory(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) {
            for (child in fileOrDirectory.listFiles()) {
                EraseMemory(child)
            }
        }
        fileOrDirectory.delete()
    }

    private fun CleanMemory() {
        val input = Data.getInputList()
        if (input.size > 0) {
            var i = 0
            while (i < input.size) {
                val MemoryCheck = input[i]
                val file = File(Brain_dir, MemoryCheck + ".txt")

                if (file.exists()) {
                    val output = Data.getOutputList(MemoryCheck)
                    if (output.size == 0) {
                        file.delete()
                        input.removeAt(i)
                        if (i > 0) {
                            i--
                        }
                    } else if (output.size == 1) {
                        if (output[0].contains("~")) {
                            file.delete()
                            input.removeAt(i)
                            if (i > 0) {
                                i--
                            }
                        }
                    }
                } else {
                    input.removeAt(i)
                    if (i > 0) {
                        i--
                    }
                }
                i++
            }

            Data.saveInputList(input)
        }

        val files = Brain_dir.listFiles()
        if (files != null) {
            for (file in files) {
                var MemoryCheck = file.name
                val index = MemoryCheck.lastIndexOf('.')
                if (index > 0) {
                    MemoryCheck = MemoryCheck.substring(0, index)

                    val output = Data.getOutputList(MemoryCheck)
                    if (output.size == 0) {
                        file.delete()
                    }
                }
            }
        }
    }

    private fun Encourage() {
        if (Logic.last_response != "") {
            if (Logic.last_response.contains(".")) {
                val str = Logic.last_response
                val sb = StringBuilder(str).replace(Logic.last_response.indexOf("."), Logic.last_response.indexOf(".") + 1, " .")
                Logic.last_response = sb.toString()
            } else if (Logic.last_response.contains("?")) {
                val str = Logic.last_response
                val sb = StringBuilder(str).replace(Logic.last_response.indexOf("?"), Logic.last_response.indexOf("?") + 1, " $")
                Logic.last_response = sb.toString()
            } else if (Logic.last_response.contains("!")) {
                val str = Logic.last_response
                val sb = StringBuilder(str).replace(Logic.last_response.indexOf("!"), Logic.last_response.indexOf("!") + 1, " !")
                Logic.last_response = sb.toString()
            }

            val WordArray = Logic.last_response.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (i in WordArray.indices) {
                when (WordArray[i]) {
                    "," -> WordArray[i] = " ,"
                    ";" -> WordArray[i] = " ;"
                    ":" -> WordArray[i] = ""
                    "?" -> WordArray[i] = " $"
                    "$" -> WordArray[i] = " $"
                    "!" -> WordArray[i] = " !"
                    "." -> WordArray[i] = " ."
                }
            }

            var data: MutableList<WordData>
            val words = ArrayList<String>()
            val frequencies = ArrayList<Int>()

            for (pro in 0..WordArray.size - 1 - 1) {
                data = Data.getProWords(WordArray[pro])
                words.clear()
                frequencies.clear()

                for (i in data.indices) {
                    words.add(data[i].word)
                    frequencies.add(data[i].frequency)
                }

                if (words.contains(WordArray[pro + 1])) {
                    val index = words.indexOf(WordArray[pro + 1])
                    frequencies[index] = frequencies[index] + 1
                }

                data.clear()
                for (i in words.indices) {
                    val new_data = WordData()
                    new_data.word = words[i]
                    new_data.frequency = frequencies[i]
                    data.add(new_data)
                }

                Data.saveProWords(data, WordArray[pro])
            }

            for (pre in 1..WordArray.size - 1) {
                data = Data.getPreWords(WordArray[pre])
                words.clear()
                frequencies.clear()

                for (i in data.indices) {
                    words.add(data[i].word)
                    frequencies.add(data[i].frequency)
                }

                if (words.contains(WordArray[pre - 1])) {
                    val index = words.indexOf(WordArray[pre - 1])
                    frequencies[index] = frequencies[index] + 1
                }

                data.clear()
                for (i in words.indices) {
                    val new_data = WordData()
                    new_data.word = words[i]
                    new_data.frequency = frequencies[i]
                    data.add(new_data)
                }

                Data.savePreWords(data, WordArray[pre])
            }
        }
    }

    private fun Discourage() {
        if (Logic.last_response != "") {
            if (Logic.last_response.contains(".")) {
                val str = Logic.last_response
                val sb = StringBuilder(str).replace(Logic.last_response.indexOf("."), Logic.last_response.indexOf(".") + 1, " .")
                Logic.last_response = sb.toString()
            } else if (Logic.last_response.contains("?")) {
                val str = Logic.last_response
                val sb = StringBuilder(str).replace(Logic.last_response.indexOf("?"), Logic.last_response.indexOf("?") + 1, " $")
                Logic.last_response = sb.toString()
            } else if (Logic.last_response.contains("!")) {
                val str = Logic.last_response
                val sb = StringBuilder(str).replace(Logic.last_response.indexOf("!"), Logic.last_response.indexOf("!") + 1, " !")
                Logic.last_response = sb.toString()
            }

            val WordArray = Logic.last_response.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (i in WordArray.indices) {
                when (WordArray[i]) {
                    "," -> WordArray[i] = " ,"
                    ";" -> WordArray[i] = " ;"
                    ":" -> WordArray[i] = ""
                    "?" -> WordArray[i] = " $"
                    "$" -> WordArray[i] = " $"
                    "!" -> WordArray[i] = " !"
                    "." -> WordArray[i] = " ."
                }
            }

            var data: MutableList<WordData>
            val words = ArrayList<String>()
            val frequencies = ArrayList<Int>()

            for (pro in 0..WordArray.size - 1 - 1) {
                data = Data.getProWords(WordArray[pro])
                words.clear()
                frequencies.clear()

                for (i in data.indices) {
                    words.add(data[i].word)
                    frequencies.add(data[i].frequency)
                }

                if (words.contains(WordArray[pro + 1])) {
                    val index = words.indexOf(WordArray[pro + 1])
                    if (frequencies[index] > 0) {
                        frequencies[index] = frequencies[index] - 1
                    }
                }

                data.clear()
                for (i in words.indices) {
                    val new_data = WordData()
                    new_data.word = words[i]
                    new_data.frequency = frequencies[i]
                    data.add(new_data)
                }

                Data.saveProWords(data, WordArray[pro])
            }

            for (pre in 1..WordArray.size - 1) {
                data = Data.getPreWords(WordArray[pre])
                words.clear()
                frequencies.clear()

                for (i in data.indices) {
                    words.add(data[i].word)
                    frequencies.add(data[i].frequency)
                }

                if (words.contains(WordArray[pre - 1])) {
                    val index = words.indexOf(WordArray[pre - 1])
                    if (frequencies[index] > 0) {
                        frequencies[index] = frequencies[index] - 1
                    }
                }

                data.clear()
                for (i in words.indices) {
                    val new_data = WordData()
                    new_data.word = words[i]
                    new_data.frequency = frequencies[i]
                    data.add(new_data)
                }

                Data.savePreWords(data, WordArray[pre])
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
        if (bl_WordFix) {
            val data = Data.getWords()
            val words = ArrayList<String>()
            for (i in data.indices) {
                words.add(data[i].word)
            }

            wordfix_selection = parent.selectedItemPosition
            txt_WordFix!!.setText(words[wordfix_selection])
        } else if (bl_Delay) {
            delay_selection = parent.selectedItemPosition
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {

    }

    private fun DisplayWordFix() {
        //Set Spinner
        val data = Data.getWords()
        val words = ArrayList<String>()
        for (i in data.indices) {
            words.add(data[i].word)
        }

        if (words.size > 0) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, words)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            sp_WordFix!!.adapter = adapter
            sp_WordFix!!.setSelection(0)
            sp_WordFix!!.visibility = View.VISIBLE
            sp_WordFix!!.isClickable = true
            sp_WordFix!!.isFocusable = true

            //Set Input
            if (wordfix_selection > words.size - 1) {
                wordfix_selection = words.size - 1
            }

            txt_WordFix!!.setText(words[wordfix_selection])
            txt_WordFix!!.visibility = View.VISIBLE
            txt_WordFix!!.isClickable = true
            txt_WordFix!!.isFocusableInTouchMode = true
            txt_WordFix!!.isFocusable = true
            txt_WordFix!!.requestFocus()

            //Set Button
            btn_WordFix!!.visibility = View.VISIBLE
            btn_WordFix!!.isClickable = true
            btn_WordFix!!.isFocusable = true

            stopTimer()
            bl_WordFix = true
        }
    }

    fun WordFix(view: View) {
        if (bl_WordFix) {
            var data = Data.getWords()

            val oldWord = data[wordfix_selection].word
            val newWord = txt_WordFix!!.text.toString()

            val input = Data.getInputList()
            for (i in input.indices) {
                val output = Data.getOutputList(input[i])
                for (j in output.indices) {
                    if (output[j].contains(oldWord)) {
                        val newOutput = output[j].replace(oldWord, newWord)
                        output[j] = newOutput
                    }
                }
                Data.saveOutput(output, input[i])

                if (input[i].contains(oldWord)) {
                    val oldPath = input[i] + ".txt"
                    val newInput = input[i].replace(oldWord, newWord)
                    input[i] = newInput
                    val oldFile = File(MainActivity.Brain_dir, oldPath)
                    val newFile = File(MainActivity.Brain_dir, input[i] + ".txt")
                    oldFile.renameTo(newFile)
                }
            }
            Data.saveInputList(input)

            val words = ArrayList<String>()
            for (i in data.indices) {
                words.add(data[i].word)
            }

            for (i in words.indices) {
                data = Data.getPreWords(words[i])
                for (j in data.indices) {
                    if (data[j].word == oldWord) {
                        val oldPath = "Pre-" + data[j].word + ".txt"
                        data[j].word = newWord
                        val newPath = "Pre-" + data[j].word + ".txt"

                        val oldFile = File(MainActivity.Brain_dir, oldPath)
                        val newFile = File(MainActivity.Brain_dir, newPath)
                        oldFile.renameTo(newFile)
                    }
                }
                Data.savePreWords(data, words[i])

                data = Data.getProWords(words[i])
                for (j in data.indices) {
                    if (data[j].word == oldWord) {
                        val oldPath = "Pro-" + data[j].word + ".txt"
                        data[j].word = newWord
                        val newPath = "Pro-" + data[j].word + ".txt"

                        val oldFile = File(MainActivity.Brain_dir, oldPath)
                        val newFile = File(MainActivity.Brain_dir, newPath)
                        oldFile.renameTo(newFile)
                    }
                }
                Data.saveProWords(data, words[i])
            }

            data = Data.getWords()
            data[wordfix_selection].word = newWord
            Data.saveWords(data)
        } else if (bl_Delay) {
            if (delay_selection == 3) {
                if (Logic.Advanced) {
                    Data.setConfig("Infinite", "On")
                } else {
                    Data.setConfig("Infinite", "Off")
                }

                bl_DelayForever = true
            } else {
                if (Logic.Advanced) {
                    Data.setConfig((delay_selection * 10 + 10).toString() + " seconds", "On")
                } else {
                    Data.setConfig((delay_selection * 10 + 10).toString() + " seconds", "Off")
                }

                int_Time = (delay_selection * 10 + 10) * 1000
                bl_DelayForever = false
            }
        }

        CloseWordFix()
    }

    private fun DisplayDelay() {
        //Set Spinner
        val delays = ArrayList<String>()
        delays.add("10 seconds")
        delays.add("20 seconds")
        delays.add("30 seconds")
        delays.add("Infinite")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, delays)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sp_WordFix!!.adapter = adapter
        sp_WordFix!!.setSelection(delay_selection)
        sp_WordFix!!.visibility = View.VISIBLE
        sp_WordFix!!.isClickable = true
        sp_WordFix!!.isFocusable = true

        //Set Button
        btn_WordFix!!.visibility = View.VISIBLE
        btn_WordFix!!.isClickable = true
        btn_WordFix!!.isFocusable = true

        stopTimer()
        bl_Delay = true
    }

    private fun DisplayTips() {
        Input!!.visibility = View.INVISIBLE
        btn_Enter!!.setText(R.string.ok_button)
        btn_Enter!!.visibility = View.VISIBLE
        btn_Encourage!!.visibility = View.INVISIBLE
        btn_Discourage!!.visibility = View.INVISIBLE
        img_Face!!.visibility = View.INVISIBLE

        var tips = ""
        tips += "Here are some tips for teaching the AI: \n\n"

        tips += "1. The AI learns from observing how you respond to what it says... " +
                "so, if it says \"Hello.\" and you say \"How are you?\" it will learn that \"How are you?\" " +
                "is a possible response to \"Hello.\". If you say something it has never seen before, it will " +
                "repeat it to see how -you- would respond to it. Learning by imitation, like a young child, " +
                "is not the only way it learns as you will soon discover.\n\n"

        tips += "2. It will generate stuff that sounds nonsensical early on... this is part of the learning process, " + "similar to the way children phrase things in ways that don't quite make sense early on. \n\n"

        tips += "3. The AI runs in real-time and will try to initiate conversation on its own if idle for too long. " +
                "To adjust how long it waits before assuming you're idle, or to make it never check for idleness, " +
                "check out the Set Delay option in the Menu. \n\n"

        tips += "4. If it says something that doesn't make sense, you can discourage the AI by pressing the Discourage button. " +
                "This will also reset the session so that whatever you say next WILL NOT be considered a response to what was " +
                "last said. \n\n"

        tips += "5. In contrast to Discouraging the AI, there is a button to Encourage it... pressing said button will let it know " +
                "it has used words properly. If you would like a more technical breakdown of how exactly " +
                "this works, check here: http://realai.freeforums.net/thread/18/expect-ai?page=1&scrollTo=50 \n\n"

        tips += "6. The AI cannot see/hear/taste/smell/feel any 'things' you refer to, so it can never have any contextual " +
                "understanding of what exactly the 'thing' is (the way you understand it). This also means it'll " +
                "never understand you trying to reference it (or yourself) directly, as it can never have a concept of " +
                "anything external being something different from it without spatial recognition gained from sight/touch/sound. \n\n"

        tips += "7. Use complete sentences when responding. Start with a capital letter and end with a punctuation mark. \n\n"

        tips += "8. Limit your responses to single sentences/questions. \n\n"

        tips += "9. Avoid conjunctions (use \"it is\" instead of \"it's\"). \n\n"

        tips += "10. In general... keep it simple. The simpler you speak to it, the better it learns. \n\n"

        Output!!.movementMethod = LinkMovementMethod.getInstance()
        Output!!.setText(tips)

        stopTimer()
        bl_Tips = true
    }

    private fun DisplayPermissions() {
        Input!!.visibility = View.INVISIBLE
        btn_Enter!!.setText(R.string.exit_app)
        btn_Enter!!.visibility = View.VISIBLE
        btn_Encourage!!.visibility = View.INVISIBLE
        btn_Discourage!!.visibility = View.INVISIBLE
        img_Face!!.visibility = View.INVISIBLE

        var permissions = ""
        permissions += "This app requires the 'Storage' and 'Draw over other apps' permissions to function. \n\n"

        permissions += "To enable the 'Storage' permission: \n"
        permissions += "1. Exit the app \n"
        permissions += "2. Go to Settings \n"
        permissions += "3. Go to Apps \n"
        permissions += "4. Find 'Real AI Text' in the list and tap it. \n"
        permissions += "5. Tap 'Permissions'. \n"
        permissions += "6. Toggle 'Storage' to ON. \n\n"

        permissions += "To enable the 'Draw over other apps' permission: \n"
        permissions += "1. Exit the app \n"
        permissions += "2. Go to Settings \n"
        permissions += "3. Go to Apps \n"
        permissions += "4. On the top right, tap the gear icon. \n"
        permissions += "5. Under Advanced, chose 'Draw over other apps' \n"
        permissions += "6. Find 'Real AI Text' in the list and tap it. \n"
        permissions += "7. Toggle 'Permit drawing over other apps' to ON. \n"

        Output!!.movementMethod = LinkMovementMethod.getInstance()
        Output!!.setText(permissions)

        stopTimer()
        bl_PermitsMissing = true
    }

    private fun CloseWordFix() {
        //Set Spinner
        sp_WordFix!!.visibility = View.INVISIBLE
        sp_WordFix!!.isClickable = false
        sp_WordFix!!.isFocusable = false

        //Set Input
        txt_WordFix!!.visibility = View.INVISIBLE
        txt_WordFix!!.isClickable = false
        txt_WordFix!!.isFocusable = false
        txt_WordFix!!.isFocusableInTouchMode = false

        //Set Button
        btn_WordFix!!.visibility = View.INVISIBLE
        btn_WordFix!!.isClickable = false
        btn_WordFix!!.isFocusable = false

        Output!!.visibility = View.VISIBLE
        Input!!.visibility = View.VISIBLE
        btn_Enter!!.setText(R.string.enter_button)
        btn_Enter!!.visibility = View.VISIBLE
        btn_Encourage!!.visibility = View.VISIBLE
        btn_Discourage!!.visibility = View.VISIBLE
        img_Face!!.visibility = View.VISIBLE
        img_Face!!.setImageResource(R.drawable.face_neutral)

        ScrollHistory()

        Input!!.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)

        bl_WordFix = false
        bl_Delay = false

        startTimer()
    }

    private fun CloseThought() {
        Input!!.visibility = View.VISIBLE
        btn_Enter!!.setText(R.string.enter_button)
        btn_Enter!!.visibility = View.VISIBLE
        btn_Encourage!!.visibility = View.VISIBLE
        btn_Discourage!!.visibility = View.VISIBLE
        img_Face!!.visibility = View.VISIBLE
        img_Face!!.setImageResource(R.drawable.face_neutral)

        ScrollHistory()

        Input!!.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)

        bl_Thought = false

        startTimer()
    }

    private fun CloseTips() {
        Input!!.visibility = View.VISIBLE
        btn_Enter!!.setText(R.string.enter_button)
        btn_Enter!!.visibility = View.VISIBLE
        btn_Encourage!!.visibility = View.VISIBLE
        btn_Discourage!!.visibility = View.VISIBLE
        img_Face!!.visibility = View.VISIBLE
        img_Face!!.setImageResource(R.drawable.face_neutral)

        ScrollHistory()

        Input!!.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)

        bl_Tips = false

        startTimer()
        startThinking()
    }

    fun Encourage(view: View) {
        Encourage()
    }

    fun Discourage(view: View) {
        CleanMemory()
        Discourage()

        val history = Data.getHistory()
        history.add("---New Session---")
        Data.saveHistory(history)
        ScrollHistory()

        Logic.NewInput = false
    }

    companion object {

        val Brain_dir = File(Environment.getExternalStorageDirectory().absolutePath + "/Brain/")
        val History_dir = File(Environment.getExternalStorageDirectory().absolutePath + "/Brain/History/")
        val Thought_dir = File(Environment.getExternalStorageDirectory().absolutePath + "/Brain/Thoughts/")

        private fun dpToPx(context: Context): Float {
            val metrics = context.resources.displayMetrics
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200.toFloat(), metrics)
        }
    }
}

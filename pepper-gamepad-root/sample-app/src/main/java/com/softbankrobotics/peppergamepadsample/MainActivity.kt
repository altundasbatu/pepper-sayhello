package com.softbankrobotics.peppergamepadsample

import android.app.Activity
import android.content.Context
import android.hardware.input.InputManager
import android.hardware.input.InputManager.InputDeviceListener
import android.os.Bundle
import android.util.Log
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.`object`.holder.AutonomousAbilitiesType
import com.aldebaran.qi.sdk.`object`.holder.Holder
import com.aldebaran.qi.sdk.builder.HolderBuilder
import com.aldebaran.qi.sdk.builder.SayBuilder
import com.softbankrobotics.peppergamepad.RemoteRobotController
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.random.Random
import com.aldebaran.qi.sdk.`object`.conversation.Phrase
import com.aldebaran.qi.sdk.`object`.conversation.Say
import com.aldebaran.qi.sdk.`object`.conversation.BodyLanguageOption
import com.aldebaran.qi.sdk.`object`.conversation.PhraseSet
import com.aldebaran.qi.sdk.`object`.conversation.Listen
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder
import com.aldebaran.qi.sdk.builder.ListenBuilder
import com.aldebaran.qi.sdk.`object`.conversation.ListenResult
import com.aldebaran.qi.sdk.util.PhraseSetUtil

private const val TAG = "ListenRobotActivity"

//private const val
class MainActivity : Activity(), RobotLifecycleCallbacks {
    private var qiContext: QiContext? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        QiSDK.register(this, this)


    }

    override fun onResume() {
        super.onResume()
    }

    override fun onRobotFocusGained(qiContext: QiContext) {

        this.qiContext = qiContext
        runCode()
    }

    override fun onRobotFocusLost() {

        this.qiContext = null
    }

    override fun onRobotFocusRefused(reason: String?) {

    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        QiSDK.unregister(this, this)
    }

    fun say(text: String) {
        val phrase: Phrase = Phrase("$text")

        val say : Say = SayBuilder.with(qiContext)
            .withPhrase(phrase)
            .build()

        say.run()
    }

    fun acknowledge(humanText: String) {
        say("You said $humanText")
    }


//    fun <T> asArray(vararg input: T): List<T> {
//        val result = arrayOf<T>(input)
////        for (item in input) // input is an Array
////            result.add(item)
//        return result
//    }

    fun listen(vararg phrases: PhraseSet): ListenResult {
        // Build the action.

        var phraseSet = arrayOf<PhraseSet>(*phrases)
        val listen: Listen = ListenBuilder.with(this.qiContext)
            .withPhraseSets(*phraseSet)
            .build()

        // Run the action synchronously.
        val listenResult: ListenResult = listen.run()
        val humanText = listenResult.heardPhrase.text
        Log.i(TAG, "Heard phrase: $humanText")
        val matchedPhraseSet: PhraseSet = listenResult.matchedPhraseSet;

        if(true) {
            acknowledge(humanText)
        }
        return listenResult
    }


    fun intro() {
        say("Hello, I am Pepper, Human Cyborg Relations. How may I help you?")
    }

    fun runCode() {
        intro()
        // Questions
        val questionFlags = listOf<String>(
            "ten",
            "ten",
            "zero",
            "zero",
            "ten",
            "zero",
            "zero",
            "zero"
        )
        val questions = listOf<String>(
            "On a scale of one to ten, how would you rate your pain",
            "During the past 24 hours, how would you rate any pain you feel during your general activity?",
            "How would you rate your mood?",
            "How would you rate your walking ability?",
            "How busy is your normal work activities, this could be your regular day job or housework?",
            "How would you rate your relationship with other people?",
            "How would you rate your sleep quality?",
            "How would you rate your general enjoyment of life?"
        )
        // Answers
        val reset = PhraseSetBuilder.with(this.qiContext) // Create the builder using the QiContext.
            .withTexts("reset", "restart") // Add the phrases Pepper will listen to.
            .build() // Build the PhraseSet.
        val end = PhraseSetBuilder.with(this.qiContext) // Create the builder using the QiContext.
            .withTexts("end", "finish") // Add the phrases Pepper will listen to.
            .build() // Build the PhraseSet.
        val shutdown = PhraseSetBuilder.with(this.qiContext)
            .withTexts("shutdown", "shut down", "shut off")
            .build()
        val binaryResponse = PhraseSetBuilder.with(this.qiContext)
            .withTexts("yes", "no")
            .build()
        val scale = PhraseSetBuilder.with(this.qiContext)
            .withTexts("one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "zero")
            .build()

        while (true) {
            var index = 0
            while(index != questions.size) {
                if (index == 0) {
                    say("I would like to ask you a few questions, please say yes when you are ready to continue")
                    var listenResult: ListenResult = listen(reset, end, binaryResponse)
                    var matchedPhraseSet: PhraseSet = listenResult.matchedPhraseSet
                    var humanText: String = listenResult.heardPhrase.text
                    if(humanText == "no") {
                        say("Alright, bye.")
                        continue
                    }
                    else{
                        say("Please rate the following questions from 0 to 10.")
                    }
                }
                var question = questions[index]
                say(question)
                var listenResult: ListenResult = listen(reset, end, scale)
                var matchedPhraseSet: PhraseSet = listenResult.matchedPhraseSet
                var humanText: String = listenResult.heardPhrase.text
                if(matchedPhraseSet == scale && questionFlags[index] == humanText){
                    say("Do you need immediate medical assistance?")
                    var listenResult: ListenResult = listen(reset, end, binaryResponse)
                    var matchedPhraseSet: PhraseSet = listenResult.matchedPhraseSet
                    var humanText: String = listenResult.heardPhrase.text
                    if(humanText == "yes") {
                        say("I shall contact medical professionals then.")
                        // TODO: Contact Medical Professionals
                        return
                    }

                }
                if(matchedPhraseSet == end) {
                    say("Alright bye.")
                    break
                } else if(matchedPhraseSet == reset) {
                    index = 0 // reset the counter
                    continue
                } else {
                    index += 1 // increment the question index
                }
            }

            say("This concludes the questions, please say end to let me go.")
            var listenResult: ListenResult = listen(reset, end, shutdown)
            var matchedPhraseSet: PhraseSet = listenResult.matchedPhraseSet
            var humanText: String = listenResult.heardPhrase.text
            if(matchedPhraseSet == end) {
                continue
            } else if (matchedPhraseSet == shutdown) {
                say("I am going to sleep now.")
            }
        }


    }
}

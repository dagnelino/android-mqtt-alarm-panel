/*
 * Copyright (c) 2018 ThanksMister LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thanksmister.iot.mqtt.alarmpanel.ui.fragments

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment
import com.thanksmister.iot.mqtt.alarmpanel.R
import com.thanksmister.iot.mqtt.alarmpanel.constants.CodeTypes
import com.thanksmister.iot.mqtt.alarmpanel.network.MQTTOptions
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Configuration
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.SettingsActivity
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.BaseAlarmView
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils
import com.thanksmister.iot.mqtt.alarmpanel.utils.MqttUtils
import com.thanksmister.iot.mqtt.alarmpanel.viewmodel.MainViewModel
import com.thanksmister.iot.mqtt.alarmpanel.viewmodel.TriggeredViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.dialog_alarm_code_set.view.*
import kotlinx.android.synthetic.main.dialog_alarm_triggered_code.*
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.view_keypad.view.*
import timber.log.Timber
import javax.inject.Inject

class TriggeredFragment : BaseFragment() {

    // TODO we need to get these values
    var codeType: CodeTypes = CodeTypes.DISARM
    var currentCode: String = ""

    var codeComplete = false
    var enteredCode = ""
    val handler: Handler = Handler()

    private val delayRunnable = object : Runnable {
        override fun run() {
            handler.removeCallbacks(this)
            if(codeComplete) {
                listener?.publishDisarm(enteredCode)
            }
            codeComplete = false
            enteredCode = ""
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: TriggeredViewModel

    @Inject
    lateinit var configuration: Configuration

    @Inject
    lateinit var dialogUtils: DialogUtils

    @Inject
    lateinit var mqttOptions: MQTTOptions

    private var listener: OnTriggeredFragmentListener? = null

    interface OnTriggeredFragmentListener {
        fun publishDisarm(code: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Timber.d("onAttach")
        if (context is OnTriggeredFragmentListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnTriggeredFragmentListener")
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Timber.d("onActivityCreated")
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(TriggeredViewModel::class.java)
        observeViewModel(viewModel)
        if(mqttOptions.useRemoteDisarm) {
            if(mqttOptions.requireCodeForDisarming) {
                codeType = CodeTypes.DISARM_REMOTE
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated")

        keypadLayout.button0.setOnClickListener {
            addPinCode("0")
        }
        keypadLayout.button1.setOnClickListener {
            addPinCode("1")
        }
        keypadLayout.button2.setOnClickListener {
            addPinCode("2")
        }
        keypadLayout.button3.setOnClickListener {
            addPinCode("3")
        }
        keypadLayout.button4.setOnClickListener {
            addPinCode("4")
        }
        keypadLayout.button5.setOnClickListener {
            addPinCode("5")
        }
        keypadLayout.button6.setOnClickListener {
            addPinCode("6")
        }
        keypadLayout.button7.setOnClickListener {
            addPinCode("7")
        }
        keypadLayout.button8.setOnClickListener {
            addPinCode("8")
        }
        keypadLayout.button9.setOnClickListener {
            addPinCode("9")
        }
        keypadLayout.buttonDel.setOnClickListener {
            removePinCode()
        }
        keypadLayout.buttonDel.setOnClickListener {
            removePinCode()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_alarm_triggered_code, container, false)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDetach() {
        super.onDetach()
        buttonSleep?.apply {
            setOnTouchListener(null)
        }
        listener = null
        handler.removeCallbacks(delayRunnable)
    }

    private fun observeViewModel(viewModel: TriggeredViewModel) {
        // nothing to observer
    }

    private fun addPinCode(code: String) {
        if (codeComplete) return
        enteredCode += code
        if(codeType == CodeTypes.DISARM_REMOTE) {
            if(enteredCode.length == MAX_CODE_LENGTH) {
                codeComplete = true
                handler.postDelayed(delayRunnable, 500)
            }
        } else if (enteredCode.length == MAX_CODE_LENGTH && enteredCode == currentCode) {
            codeComplete = true
            handler.postDelayed(delayRunnable, 500)
        } else if (enteredCode.length == MAX_CODE_LENGTH) {
            codeComplete = true
            handler.postDelayed(delayRunnable, 500)
        }
    }

    private fun removePinCode() {
        if (codeComplete) {
            return
        }
        if (enteredCode.isNotEmpty()) {
            enteredCode = enteredCode.substring(0, enteredCode.length - 1)
        }
    }

    companion object {
        val MAX_CODE_LENGTH = 4
        @JvmStatic
        fun newInstance(): TriggeredFragment {
            return TriggeredFragment()
        }
    }
}
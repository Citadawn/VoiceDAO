2. 添加一个切换语言和发音人的功能，用户可以选择不同的语言进行语音合成。可以使用TextToSpeech的setLanguage()方法来设置语言，可用的语言通过TextToSpeech的getAvailableLanguages()方法获取。选择语言后，使用setVoice()方法来设置发音人，可用的发音人通过TextToSpeech的getVoices()方法获取。可以使用Spinner控件来显示可用的语言和发音人列表，用户可以选择不同的语言和发音人进行语音合成。还可以将语言和发音人恢复到默认值，比如中文和默认发音人。并且为默认的语言和发音人添加一个提示，比如“默认”。
3. 使用TextToSpeech的setOnUtteranceProgressListener()方法来监听语音合成的进度，当语音合成开始、结束或者出错时，会触发相应的回调方法，可以在回调方法中更新 UI，显示语音合成的进度。
4. 列出所有已安装的 TTS 引擎。可以使用TextToSpeech的getEngines()方法来获取已安装的 TTS 引擎列表，该方法返回一个List<EngineInfo>对象，其中每个EngineInfo对象包含了引擎的名称、包名等信息。
5. 在 UI 上显示朗读状态，使用TextToSpeech的isSpeaking()方法来检查 TTS 是否正在朗读，如果正在朗读，则显示“正在朗读”，否则显示“停止朗读”。又比如可以添加一个进度条或者一个文本框来显示朗读的进度，用户可以直观地看到朗读的状态。
6. 添加一个继续朗读的功能。
7. 使用getLocale()，获取语言和地区（Locale对象），如zh_CN、en_US等。getAvailableLanguages、getDefaultEngine、getDefaultLanguage【getDefaultVoice().getLocale()】、getDefaultVoice、getEngines、getLanguage【getVoice().getLocale()】、getVoice、getVoices、isLanguageAvailable、isSpeaking、setLanguage、setVoice、locale.getCountry()
8. 检查输入是否为空的时候要去除空格。
9. “已开始保存音频，完成后请在文件管理器中查看。”、“音频已保存到自定义目录”统一一下。
10. 将Android Studio中划黄线的代码修改一下。
11. 中文与英文要加空格、数字与英文之间要加空格、数字与中文之间要加空格。
12. 使用public int speak (CharSequence text, 
                int queueMode, 
                Bundle params, 
                String utteranceId)方法的返回值，判断是否成功，显示在UI上。
13. 使用TextToSpeech的setOnUtteranceProgressListener()方法来监听语音合成的进度，当语音合成开始、结束或者出错时，会触发相应的回调方法，可以在回调方法中更新 UI，显示语音合成的进度。
14. 弄一个可用语言列表和一个声音（voice）列表。  

    可用语言列表包括语言的完整本地化名，如“英语（美国）”、语言标签字符串，如 "zh-CN"两列。  

    声音列表包括发音人的语言和地区（如 zh_CN、en_US）（getLocale方法）、发音人（Voice）的唯一名称（标识符）（如“en-us-x-sfg#male_1-local”）（getName方法）、是否需要联网（isNetworkConnectionRequired方法）、音质等级（如 Voice.QUALITY_HIGH）（getQuality方法）、发音人（Voice）合成语音时的“预期延迟”级别（getLatency方法）


TTS 初始化每次打开软件的时候会执行，如果在打开软件的情况下更改了系统TTS引擎的话，是不是软件的TTS引擎任然是更改之前到的，怎么做到实时同步系统TTS引擎的更改
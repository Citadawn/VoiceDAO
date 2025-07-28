package com.citadawn.speechapp.ui.test;

import java.util.ArrayList;
import java.util.List;

public class TestManager {
    private static TestManager instance;
    private final List<TestCase> testCases = new ArrayList<>();
    private boolean testMode = false;

    private TestManager() {
        // 添加测试用例 - 使用资源ID，在MainActivity中动态设置
        testCases.add(new TestCase("speed_pitch_failure", "", ""));
        testCases.add(new TestCase("log_tts_voices", "", ""));
        // 删除彩蛋测试项，无需保留
    }

    public static TestManager getInstance() {
        if (instance == null) {
            instance = new TestManager();
        }
        return instance;
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }

    public void setTestMode(boolean enabled) {
        this.testMode = enabled;
    }

    public boolean isTestMode() {
        return testMode;
    }

    public List<TestCase> getSelectedTestCases() {
        List<TestCase> selected = new ArrayList<>();
        for (TestCase tc : testCases) {
            if (tc.selected) selected.add(tc);
        }
        return selected;
    }

    public void resetAll() {
        for (TestCase tc : testCases) {
            tc.selected = false;
        }
        testMode = false;
    }
} 
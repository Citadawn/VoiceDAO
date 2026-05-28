package com.citadawn.speechapp.ui.test;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 调试项管理器单例类
 * 负责管理调试项的创建、选择和调试模式状态
 */
public class TestManager {

    // region 静态变量

    /**
     * 单例实例
     */
    private static TestManager instance;

    // endregion

    // region 成员变量

    /**
     * 所有调试项列表
     */
    private final List<TestCase> testCases = new ArrayList<>();

    /**
     * 调试模式状态
     */
    private boolean testMode = false;

    // endregion

    // region 构造方法

    /**
     * 私有构造函数，初始化调试项
     */
    private TestManager() {
        // 添加调试项 - 使用资源 ID，在 MainActivity 中动态设置名称与描述
        testCases.add(new TestCase("log_tts_voices", "", ""));
        testCases.add(new TestCase("fill_test_text", "", ""));
        testCases.add(new TestCase("show_current_voice", "", ""));
    }

    // endregion

    // region 静态工具方法

    /**
     * 获取TestManager单例实例
     *
     * @return TestManager实例
     */
    @NonNull
    public static TestManager getInstance() {
        if (instance == null) {
            instance = new TestManager();
        }
        return instance;
    }

    // endregion

    // region 公开方法

    /**
     * 获取所有调试项列表
     *
     * @return 调试项列表
     */
    @NonNull
    public List<TestCase> getTestCases() {
        return testCases;
    }

    /**
     * 检查是否处于调试模式
     *
     * @return 是否处于调试模式
     */
    public boolean isTestMode() {
        return testMode;
    }

    /**
     * 设置调试模式状态
     *
     * @param enabled 是否启用调试模式
     */
    public void setTestMode(boolean enabled) {
        this.testMode = enabled;
    }

    /**
     * 获取已选中的调试项列表
     *
     * @return 已选中的调试项列表
     */
    @NonNull
    public List<TestCase> getSelectedTestCases() {
        List<TestCase> selected = new ArrayList<>();
        for (TestCase tc : testCases) {
            if (tc.selected) {
                selected.add(tc);
            }
        }
        return selected;
    }

    /**
     * 重置所有调试项的选择状态和调试模式
     */
    public void resetAll() {
        for (TestCase tc : testCases) {
            tc.selected = false;
        }
        testMode = false;
    }

    // endregion
}
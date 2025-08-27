package com.citadawn.speechapp.ui.test;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试管理器单例类
 * 负责管理测试用例的创建、选择和状态控制
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
     * 所有测试用例列表
     */
    private final List<TestCase> testCases = new ArrayList<>();

    /**
     * 测试模式状态
     */
    private boolean testMode = false;

    // endregion

    // region 构造方法

    /**
     * 私有构造函数，初始化测试用例
     */
    private TestManager() {
        // 添加测试用例 - 使用资源ID，在MainActivity中动态设置
        testCases.add(new TestCase("speed_pitch_failure", "", ""));
        testCases.add(new TestCase("log_tts_voices", "", ""));
        // 删除彩蛋测试项，无需保留
    }

    // endregion

    // region 静态工具方法

    /**
     * 获取TestManager单例实例
     *
     * @return TestManager实例
     */
    public static TestManager getInstance() {
        if (instance == null) {
            instance = new TestManager();
        }
        return instance;
    }

    // endregion

    // region 公开方法

    /**
     * 获取所有测试用例列表
     *
     * @return 测试用例列表
     */
    public List<TestCase> getTestCases() {
        return testCases;
    }

    /**
     * 检查是否处于测试模式
     *
     * @return 是否处于测试模式
     */
    public boolean isTestMode() {
        return testMode;
    }

    /**
     * 设置测试模式状态
     *
     * @param enabled 是否启用测试模式
     */
    public void setTestMode(boolean enabled) {
        this.testMode = enabled;
    }

    /**
     * 获取已选中的测试用例列表
     *
     * @return 已选中的测试用例列表
     */
    public List<TestCase> getSelectedTestCases() {
        List<TestCase> selected = new ArrayList<>();
        for (TestCase tc : testCases) {
            if (tc.selected) selected.add(tc);
        }
        return selected;
    }

    /**
     * 重置所有测试用例的选择状态和测试模式
     */
    public void resetAll() {
        for (TestCase tc : testCases) {
            tc.selected = false;
        }
        testMode = false;
    }

    // endregion
}
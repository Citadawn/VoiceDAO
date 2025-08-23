package com.citadawn.speechapp.ui.test;

/**
 * 测试用例数据模型类
 * 用于存储测试项的基本信息和选择状态
 */
public class TestCase {
    
    // region 成员变量
    
    /** 测试用例的唯一标识符 */
    public final String id;
    
    /** 测试用例的显示名称 */
    public String name;
    
    /** 测试用例的详细描述 */
    public String description;
    
    /** 测试用例是否被选中 */
    public boolean selected;
    
    // endregion
    
    // region 构造方法
    
    /**
     * 创建测试用例实例
     * @param id 测试用例的唯一标识符
     * @param name 测试用例的显示名称
     * @param description 测试用例的详细描述
     */
    public TestCase(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.selected = false;
    }
    
    // endregion
} 
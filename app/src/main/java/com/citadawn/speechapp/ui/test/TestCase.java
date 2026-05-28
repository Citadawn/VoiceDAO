package com.citadawn.speechapp.ui.test;



/**

 * 调试项数据模型类

 * 用于存储调试项的基本信息和选择状态

 */

public class TestCase {



    // region 成员变量



    /**

     * 调试项的唯一标识符

     */

    public final String id;



    /**

     * 调试项的显示名称

     */

    public String name;



    /**

     * 调试项的详细描述

     */

    public String description;



    /**

     * 调试项是否被选中

     */

    public boolean selected;



    // endregion



    // region 构造方法



    /**

     * 创建调试项实例

     *

     * @param id          调试项的唯一标识符

     * @param name        调试项的显示名称

     * @param description 调试项的详细描述

     */

    public TestCase(String id, String name, String description) {

        this.id = id;

        this.name = name;

        this.description = description;

        this.selected = false;

    }



    // endregion

}


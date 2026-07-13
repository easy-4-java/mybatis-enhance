package org.apache.ibatis.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * {@code MyBatisSQL} 框架组件。
 *
 * <p>该类型是 mybatis-enhance 公共或受保护扩展面的一部分。</p>
 */
public class MyBatisSQL {

    /*预编译后的 sql 有 ? 号 */
    private String preSQL;
    /* 运行期 sql */
    private String runSQL;
    /* 参数 数组 */
    private Object[] parameters;

    /**
     * 完成 {@code main} 对应的框架处理。
     *
     * @param args 调用参数
     */
    public static void main(String[] args) {
        System.out.println("select t.xh,t.xsjbxxb_id,t.xqdmb_id,t.ssxy_id,t.zyfxdmb_id,".replaceAll("(\\r\\n(\\s*\\r\\n)+)", "\r\n").replaceAll(" +", ""));
    }

    /**
     * 获取 {@code preSQL}。
     *
     * @return 对应的属性值
     */
    public String getPreSQL() {
        return preSQL;
    }

    /**
     * 设置 {@code preSQL}。
     *
     * @param preSQL 调用参数 {@code preSQL}
     */
    public void setPreSQL(String preSQL) {
        this.preSQL = preSQL;
    }

    /**
     * 获取 {@code runSQL}。
     *
     * @return 对应的属性值
     */
    public String getRunSQL() {
        return runSQL;
        //return null!= sql? sql.replaceAll("\r|\n", "").replaceAll("\\s*","#").replaceAll("##"," ").replaceAll("#","") :"";
    }

    /**
     * 设置 {@code runSQL}。
     *
     * @param runSQL 调用参数 {@code runSQL}
     */
    public void setRunSQL(String runSQL) {
        this.runSQL = runSQL;
    }

    /**
     * 获取 {@code parameters}。
     *
     * @return 对应的属性值
     */
    public Object[] getParameters() {
        return parameters;
    }

    /**
     * 设置 {@code parameters}。
     *
     * @param parameters 调用参数 {@code parameters}
     */
    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    /**
     * 转换 {@code toString} 定义的框架操作。
     *
     * @return 处理结果
     */
    @Override
    public String toString() {
        if (parameters == null || runSQL == null) {
            return "";
        }
        List<Object> parametersArray = Arrays.asList(parameters);
        List<Object> list = new ArrayList<Object>(parametersArray);
        while (runSQL.indexOf(" ") != -1 && list.size() > 0 && parameters.length > 0) {
            runSQL = runSQL.replaceFirst("\\ ", list.get(0).toString());
            list.remove(0);
        }
        return runSQL.replaceAll("(\r \n(\\s*\r \n)+)", "\r\n");
    }
}

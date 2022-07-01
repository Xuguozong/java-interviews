[基础](Java/README.md)

[JDBC](JDBC/README.md)

[JVM](JVM/README.md)

[Servlet](Servlet/Servlet.md)

[多线程与并发](多线程与并发/README.md)

[新特性](新特性/README.md)

[实践](实践/README.md)

[网络编程](网络编程/README.md)

[集合](集合/README.md)

```
package com.sky.common.utils;

import com.sky.common.annotation.IndexExpression;
import com.sky.common.exception.CustomException;
import org.apache.commons.collections.CollectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 检索表达式的　Sql 解析器

 可检索字段:

 CO=运维单位,ST=变电站,GIS=是否GIS站,TP=设备类型,VL=电压等级,UT=间隔单元,NM=设备名称,MF=生产厂家,MD=型号,OD=投运日期,PD=出厂日期,OY=投运年份，OA=投运年限，EA=设备增加方式,

 示例：

 1）CO=平湖 and TP=主变压器 and (MF%TOSHIBA + 东芝) 可以检索到运维单位为“平湖”并且设备类型为“主变压器”并且生产厂家包含“TOSHIBA”或“东芝”的所有设备；

 2）ST=瓦山变 and VL>=35 可以检索到“瓦山变”35kV及以上的所有设备；

 3）MD%P00HXG - (MF=ABB) and OA>=5 可检索型号中包含“P00HXG”非ABB“生产的，投运年限不低于5年的相关设备。

 * --> and
 + --> or
 - --> not

 * @author xuguozong
 */
public final class ExpressionSqlParser {

    private IndexExpression i;

    public ExpressionSqlParser() {}

    /** 需要解析成参数占位符的形式，防止　sql 注入 -- #{} */
    public ParseResult parse(Class<?> model, String source) throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        return parse(model, source, ExpressionNode::parse);
    }

    /** 需要解析成参数占位符的形式，防止　sql 注入 -- ? Jdbc */
    public ParseResult parseJdbc(Class<?> model, String source) throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        return parse(model, source, ExpressionNode::parseJdbc);
    }

    /** 解析专业检索表达式　*/
    private ParseResult parse(Class<?> model, String source, Function<ExpressionNode, ParseResult> f) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // 1. 精确等值匹配
        // CO=XXX
        // 2. 数值范围
        // TP>=XXX
        // 3. 模糊匹配
        // MF%TOSHIBA
        Objects.requireNonNull(StringUtils.trim(source), "检索表达式不能为空");
        Method method = model.getDeclaredMethod("getExpressionCodeMap", null);
        if (Objects.isNull(method)) return null;
        Map<String, IndexExpression> map = (Map<String, IndexExpression>) method.invoke(null, null);
        if (map.isEmpty()) return null;
        // 解析原始检索表达式
        // 操作符类型： = > < >= <= % + -
        // code 后面需要约束的操作符类型
        String[] parts = source.split("and");
        NodeFactory factory = new NodeFactory(map);
        List<ExpressionNode> nodes = Arrays.stream(parts)
                .map(factory::create)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        // sql 解析结果拼接
        if (!nodes.isEmpty()) {
            ExpressionNode baseNode = nodes.get(0);
            ParseResult parseResult = f.apply(baseNode);
            nodes.remove(baseNode);
            if (!nodes.isEmpty()) {
                nodes.forEach(n -> parseResult.merge(f.apply(n)));
            }
            // 特殊字段 sql 处理
            replaceWithSpecialSql(map, parseResult);
            // 字段类型处理
            checkFieldTypeNumber(map, parseResult);
            return parseResult;
        }
        return null;
    }

    /**
     * 数值字段类型处理
     */
    private void checkFieldTypeNumber(Map<String, IndexExpression> map, ParseResult parseResult) {
        Map<String, Object> kvMap = parseResult.getKvMap();
        map.values().stream().filter(i -> i.type().equals(IndexExpression.Type.NUMBER)).forEach(i -> {
            String dbField = i.dbField();
            if (kvMap.containsKey(dbField)) {
                Object value = kvMap.get(dbField);
                try {
                    Double valueL = Double.parseDouble(value.toString());
                    kvMap.put(dbField, valueL);
                } catch (Exception e) {
                    throw new CustomException(i.code() + "只支持数值类型的参数");
                }
            }
        });
    }

    /** 替换需要修改的 sql 内容 */
    private void replaceWithSpecialSql(Map<String, IndexExpression> map, ParseResult parseResult) {
        Map<String, Object> kvMap = parseResult.getKvMap();
        map.values().stream().filter(i -> StringUtils.isNotEmpty(i.replaceSql())).forEach(i -> {
            String replaceSql = i.replaceSql();
            String dbField = i.dbField();
            if (kvMap.containsKey(dbField)) {
                parseResult.setRowSql(parseResult.getRowSql().replace(dbField + " ", replaceSql));
            }
        });
    }

    /**
     * 专业检索表达式的解析结果
     */
    public static class ParseResult {
        /** 存储　dbField 字段和其对应的数值,用于　mybatis 的　#{}参数替换　*/
        Map<String, Object> kvMap = new LinkedHashMap<>();

        /** 带　#{} 或者 ? 参数占位符的　sql 语句　*/
        private String rowSql;

        /** ? 对应的具体参数值 */
        private List<Object> values = new LinkedList<>();

        public ParseResult(String rowSql) {
            this.rowSql = rowSql;
        }

        public ParseResult addValues(Object value) {
            values.add(value);
            return this;
        }

        public List<Object> getValues() {
            return values;
        }

        /**
         * 合并其他　sql 解析结果
         * @param other　其他解析结果
         */
        public ParseResult merge(ParseResult other) {
            Objects.requireNonNull(other);
            String otherSql = other.getRowSql();
            if (otherSql.trim().startsWith("and") || otherSql.trim().startsWith("or")) {
                setRowSql(getRowSql() + otherSql);
            } else {
                setRowSql(getRowSql() + " and " + otherSql);
            }
            Map<String, Object> kvMap = other.getKvMap();
            if (!kvMap.isEmpty()) {
                Map<String, Object> resultKvMap = getKvMap();
                kvMap.forEach((k, v) -> {
                    // 先添加到　values 中
                    addValues(v);
                    if (resultKvMap.containsKey(k)
                            && !v.equals(resultKvMap.get(k))) {

                        // 如果已经有 key 且值不同
                        // 修改 sql 中相应字段
                        String newK = k + "_" + UUID.randomUUID().toString().replace("-", "");
                        String newSql = replaceSqlMultiK(getRowSql(), k, "#{" + newK +"}");
                        setRowSql(newSql);
                        resultKvMap.put(newK, v);
                    }
                    if (!resultKvMap.containsKey(k)) {
                        resultKvMap.put(k, v);
                    }
                });
            }
            return this;
        }

        /**
         * 替换制定字符串
         * @param rowSql place= #{place}  and equip_type_name= #{equip_type_name}  and manu_facturer like concat('%', #{manu_facturer}, '%') or manu_facturer = #{manu_facturer}
         * @param k manu_facturer
         * @param newK xxxxx
         * @return place= #{place}  and equip_type_name= #{equip_type_name}  and manu_facturer like concat('%', #{manu_facturer}, '%') or manu_facturer = xxxxx
         */
        private String replaceSqlMultiK(String rowSql, String k, String newK) {
            String replaced = "#{" + k +"}";
            String toBeReplaced = "_" + replaced;
            int index = rowSql.lastIndexOf(replaced);
            StringBuilder sb = new StringBuilder(rowSql);
            if (index > 0) sb.insert(index, "_");
            return sb.toString().replace(toBeReplaced, newK);
        }

        public ParseResult add(String dbField, Object value) {
            kvMap.putIfAbsent(dbField, value);
            return this;
        }

        public Map<String, Object> getKvMap() {
            return kvMap;
        }

        public String getRowSql() {
            return rowSql;
        }

        public ParseResult setRowSql(String rowSql) {
            this.rowSql = rowSql;
            return this;
        }

    }

    class NodeFactory {

        final Map<String, IndexExpression> expressionMap;

        /** 因为有 "=" 的存在,需要将　"<=", ">="　放在前面　*/
        final List<String> operators = Arrays.asList("<=", ">=", "=", "%", "<",  ">");

        public NodeFactory(Map<String, IndexExpression> expressionMap) {
            this.expressionMap = expressionMap;
        }

        /** 工厂模式创建操作节点　*/
        ExpressionNode create(String part) {
            String trim = StringUtils.trim(part);
            if (trim.contains("(")) {
                if (!trim.contains(")")) throw new CustomException("括号应当成对出现");
                if (trim.trim().startsWith("(")) {
                    trim = trim.replace("(", "").replace(")", "");
                }
                // 默认以空格间隔
                String[] split = trim.split(" ");
                if (split.length == 1) {
                    // 单个表达式
                    String node = split[0];
                    return node(node);
                } else if (split.length == 3) {
                    // 表达式嵌套
                    String node = split[0];
                    ExpressionNode expressionNode = node(node);
                    assert expressionNode != null;
                    String operator = split[1];
                    String nodeOrValue = split[2];
                    if (nodeOrValue.contains("(")) {
                        // sub node
                        NodeWithOperator nodeWithOperator = new NodeWithOperator(
                                node(nodeOrValue.replace("(", "").replace(")", "")), operator);
                        expressionNode.addNodeWithOp(nodeWithOperator);
                    } else {
                        // value
                        ValueWithOperator valueWithOperator = new ValueWithOperator(expressionNode.dbField,
                                nodeOrValue, expressionNode, operator);
                        expressionNode.addValueWithOp(valueWithOperator);
                    }
                    return expressionNode;
                }
            } else {
                // 不包含括号也是单个表达式的情况
                return node(trim);
            }
            return null;
        }

        private ExpressionNode node(String expression) {
            for (String operator: operators) {
                if (expression.contains(operator)) {
                    String[] split = expression.split(operator);
                    if (split.length == 2) {
                        String code = split[0];
                        String value = split[1];
                        if (!expressionMap.containsKey(code)) {
                            throw new CustomException("unsupported code: " + code);
                        }
                        String dbField = expressionMap.get(code).dbField();
                        switch (operator) {
                            case "=":
                                return new EqualsExpressionNode(code, operator, value, dbField);
                            case "%":
                                return new ContainsExpressionNode(code, operator, value, dbField);
                            case ">":
                                return new RangeExpressionNode(code, operator, value, dbField);
                            case ">=":
                                return new RangeExpressionNode(code, operator, value, dbField);
                            case "<":
                                return new RangeExpressionNode(code, operator, value, dbField);
                            case "<=":
                                return new RangeExpressionNode(code, operator, value, dbField);
                            default:
                                throw new CustomException("unsupported operator: " + operator);
                        }
                    } else {
                        throw new CustomException("unsupported expression: " + expression);
                    }
                }
            }
            return null;
        }
    }

    abstract class ExpressionNode {
        /** 检索字段代码　*/
        protected String code;
        /** 操作符　*/
        protected String operator;
        /** 操作数　*/
        protected String value;


        protected String dbField;

        public ExpressionNode(String code, String operator, String value,
                              String dbField) {
            this.code = code;
            this.operator = operator;
            this.value = value;
            this.dbField = dbField;
        }

        /** MD%P00HXG - (MF=ABB) 中的　- (MF=ABB)　部分　*/
        List<NodeWithOperator> nodeWithOperators = new LinkedList<>();

        /** (MF%TOSHIBA+东芝) 中的　+东芝　部分 */
        List<ValueWithOperator> valueWithOperators = new LinkedList<>();

        public void addNodeWithOp(NodeWithOperator nodeWithOperator) {
            nodeWithOperators.add(nodeWithOperator);
        }

        public void addValueWithOp(ValueWithOperator valueWithOperator) {
            valueWithOperators.add(valueWithOperator);
        }

        public ParseResult parse() {
            String rowSql = parseSql();
            // sub node parser
            ParseResult parseResult = new ParseResult(rowSql)
                    .add(dbField, value)
                    .addValues(value);
            if (CollectionUtils.isNotEmpty(nodeWithOperators)) {
                nodeWithOperators.forEach(n -> parseResult.merge(n.parse()));
            }
            if (CollectionUtils.isNotEmpty(valueWithOperators)) {
                valueWithOperators.forEach(n -> parseResult.merge(n.parse()));
            }
            return parseResult;
        }

        public ParseResult parseJdbc() {
            String rowSql = parseJdbcSql();
            // sub node parser
            ParseResult parseResult = new ParseResult(rowSql)
                    .add(dbField, value)
                    .addValues(value);
            if (CollectionUtils.isNotEmpty(nodeWithOperators)) {
                nodeWithOperators.forEach(n -> parseResult.merge(n.parseJdbc()));
            }
            if (CollectionUtils.isNotEmpty(valueWithOperators)) {
                valueWithOperators.forEach(n -> parseResult.merge(n.parseJdbc()));
            }
            return parseResult;
        }

        /** 使用　#{} 占位符　*/
        public abstract String parseSql();

        /** 使用 ? 占位符 */
        public abstract String parseJdbcSql();

        public abstract String parseValueNode(ValueWithOperator valueWithOperator);

    }

    class EqualsExpressionNode extends ExpressionNode {

        public EqualsExpressionNode(String code, String operator, String value, String dbField) {
            super(code, operator, value, dbField);
        }

        @Override
        public String parseSql() {
            return dbField + " = #{" + dbField + "} ";
        }

        @Override
        public String parseJdbcSql() {
            return dbField + " = ? ";
        }

        @Override
        public String parseValueNode(ValueWithOperator valueWithOperator) {
            return " = #{" + valueWithOperator.dbField + "} ";
        }
    }

    class ContainsExpressionNode extends ExpressionNode {

        public ContainsExpressionNode(String code, String operator, String value, String dbField) {
            super(code, operator, value, dbField);
        }

        @Override
        public String parseSql() {
            return dbField + " like concat('%', #{" + dbField + "}, '%') ";
        }

        @Override
        public String parseJdbcSql() {
            return dbField + " like concat('%', ?, '%') ";
        }

        @Override
        public String parseValueNode(ValueWithOperator valueWithOperator) {
            return " like CONCAT('%',#{" + valueWithOperator.dbField + "},'%')";
        }
    }

    class RangeExpressionNode extends ExpressionNode {

        public RangeExpressionNode(String code, String operator, String value, String dbField) {
            super(code, operator, value, dbField);
        }

        @Override
        public String parseSql() {
            switch (operator) {
                case ">":
                    return dbField + " >  #{" + dbField + "} ";
                case ">=":
                    return dbField + " >= #{" + dbField + "} ";
                case "<":
                    return dbField + " <  #{" + dbField + "} ";
                case "<=":
                    return dbField + " <= #{" + dbField + "} ";
                default:
                    throw new CustomException("unsupported operator: " + operator);
            }
        }

        @Override
        public String parseJdbcSql() {
            switch (operator) {
                case ">":
                    return dbField + " >  ? ";
                case ">=":
                    return dbField + " >= ? ";
                case "<":
                    return dbField + " <  ? ";
                case "<=":
                    return dbField + " <= ? ";
                default:
                    throw new CustomException("unsupported operator: " + operator);
            }
        }

        @Override
        public String parseValueNode(ValueWithOperator valueWithOperator) {
            switch (operator) {
                case ">":
                    return " >  #{" + valueWithOperator.dbField + "} ";
                case ">=":
                    return " >= #{" + valueWithOperator.dbField + "} ";
                case "<":
                    return " <  #{" + valueWithOperator.dbField + "} ";
                case "<=":
                    return " <= #{" + valueWithOperator.dbField + "} ";
                default:
                    throw new CustomException("unsupported operator: " + operator);
            }
        }
    }

    /** MD%P00HXG - (MF=ABB) 中的　- (MF=ABB)　部分　*/
    class NodeWithOperator {
        private ExpressionNode node;
        private String operator;

        public NodeWithOperator(ExpressionNode node, String operator) {
            this.node = node;
            this.operator = operator;
        }

        public ParseResult parse() {
            ParseResult parseResult = node.parse();
            String rowSql = parseResult.getRowSql();
            switch (operator) {
                case "*":
                    return parseResult.setRowSql("and " + rowSql);
                case "+":
                    return parseResult.setRowSql("or " + rowSql);
                case "-":
                    return parseResult.setRowSql("and " + convertToNotEquals(rowSql));
                default:
                    throw new CustomException("unsupported operator: " + operator);
            }
        }

        public ParseResult parseJdbc() {
            ParseResult parseResult = node.parseJdbc();
            String rowSql = parseResult.getRowSql();
            switch (operator) {
                case "*":
                    return parseResult.setRowSql("and " + rowSql);
                case "+":
                    return parseResult.setRowSql("or " + rowSql);
                case "-":
                    return parseResult.setRowSql("and " + convertToNotEquals(rowSql));
                default:
                    throw new CustomException("unsupported operator: " + operator);
            }
        }

        /** 转换成非 */
        private String convertToNotEquals(String sql) {
            return sql.replace("=", "!=");
        }
    }

    /** (MF%TOSHIBA + 东芝) 中的　+东芝　部分 */
    class ValueWithOperator {
        private String dbField;
        private String value;
        private ExpressionNode fatherNode;
        private String operator;

        public ValueWithOperator(String dbField, String value, ExpressionNode fatherNode, String operator) {
            this.dbField = dbField;
            this.value = value;
            this.fatherNode = fatherNode;
            this.operator = operator;
        }

        public ParseResult parse() {
            // 如　MF%TOSHIBA + 东芝 应解析为　
            // manu_facturer like CONCAT('%', TOSHIBA, '%') or manu_facturer like CONCAT('%', 东芝, '%')
            String subSql = dbField + fatherNode.parseValueNode(this);
            switch (operator) {
                case "*":
                    return new ParseResult("and " + subSql)
                            .add(dbField, value).addValues(value);
                case "+":
                    return new ParseResult("or " + subSql)
                            .add(dbField, value).addValues(value);
                case "-":
                    return new ParseResult("and NOT " + subSql)
                            .add(dbField, value).addValues(value);
                default:
                    throw new CustomException("unsupported operator: " + operator);
            }
        }

        public ParseResult parseJdbc() {
            switch (operator) {
                case "*":
                    return new ParseResult("and " + dbField + " = ? ")
                            .add(dbField, value).addValues(value);
                case "+":
                    return new ParseResult("or " + dbField + " = ? ")
                            .add(dbField, value).addValues(value);
                case "-":
                    return new ParseResult("and " + dbField + " != ? ")
                            .add(dbField, value).addValues(value);
                default:
                    throw new CustomException("unsupported operator: " + operator);
            }
        }

    }

}

```
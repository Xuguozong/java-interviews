## 字符串处理函数

| 函数名          | 用途                     | 参数                             | 参数说明                                                   | 示例                                                         | 参考                                                         |
| --------------- | ------------------------ | -------------------------------- | ---------------------------------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| SUBSTRING_INDEX | 截取分隔符分割的字符字串 | SUBSTRING_INDEX(str,delim,count) | str 源串 \|delim 分隔符 \|count 截取个数，负数表示从右开始 | SUBSTRING_INDEX('www.mysql.com', '.', 2);        -> 'www.mysql' SUBSTRING_INDEX('www.mysql.com', '.', -2);        -> 'mysql.com' | [function_substring-index](https://dev.mysql.com/doc/refman/5.7/en/string-functions.html#function_substring-index) |
| RAND            | 随机数函数               | RAND(), RAND(S)                  | S 种子                                                     | SELECT id,value,label FROM uc_dictionary where parent_id=13 ORDER BY RAND() LIMIT 10;  # 获取随机10条数据 | [function_rand](https://dev.mysql.com/doc/refman/5.7/en/mathematical-functions.html#function_rand) |
|                 |                          |                                  |                                                            |                                                              |                                                              |


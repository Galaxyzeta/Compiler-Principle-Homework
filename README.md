# HDU编译原理实验
Compiler Principle Homework in Hangzhou Dianzi University.

作者：@Galaxyzeta

## 基于java的词法分析器
A Java-based Lexual Analyzer.

特点：一遍扫描，准确分析。使用反射特性，轻松添加分析规则。

过程概述：使用 `FileReader` 类从文本文件逐字符读取程序，直到每读到`EOF`。每一个字符存储在`StringBuilder`缓冲区，每次读取从缓冲区取出字符串进行 **正则表达式** 匹配，并对 **超读** 现象进行暂停1轮文件读取的操作。每次检测完毕进行错误检测。结果保存为2元式，写入文本文件。

类概述：

1. `PatternFactory`：在其中添加匹配规则。包含一个用反射设计的匹配方法，参数1为匹配规则字符串，2为
2. `WordAnalyzer`：词法分析类。包含一个边读取边分析的方法，以及根据对应匹配规则名称确定编码类别的 `HashMap`。
3. `WordCodec`：编码类别枚举。
4. `ParseResult`：生成的二元式实体类。
5. `WordSpellException`：错误处理类。

## 基于java的递归下降子程序分析
A Java-based Top-Down Syntax Analyzer
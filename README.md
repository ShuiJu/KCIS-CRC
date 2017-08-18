# KCIS-CRC

# 你好(〃'▽'〃)!

# 关于

这是一个由本人自主开发的康桥车长点名客户端，意在支持康桥小车长使用饭卡来进行点名操作。

点名操作所要做的事情只需要是登录本地客户端，插上读卡器将每位车上人员的学生卡录入，并提交。

本系统是基于康桥[网页车长点名系统](http://portal.kcisec.com/rollcall)之上,
通过抓包分析主系统API实现的。
康桥原系统不支持卡号输入，至允许输入卡号，所以将卡号转为学号是本客户端的主要特点，
通过与[康桥点餐系统](http://ordering.kcisec.com/chaxun.asp)相连获取学生姓名后与数据库
比对获取学号，数据库为之前轮寻抓下来的，不与学校同步，存储于开发者的oss中，
预计数据库为每半年更新一次，但是内部实现了错误提交，所以可以在远端oss中查看缺省信息并对其进行更新

----------------------------------------------------------------------------------

# 目前功能

目前KCIS-CRC客户端还为早期版本，只为实现功能，同时由于开发者经验尚少UI部分并不好看，所以
还恳请见谅，预计会在2018年年初前完成第一个公共版本，并确定至少会拥有如下基础功能

* 免密码学生卡登录，φ(>ω<*)
* 基础卡号点名，(〃'▽'〃)
* 传统报站功能，φ(>ω<*)

以上功能已在目前beta 1.1中实现(报站功能会在Beta 1.2推出)

----------------------------------------------------------------------------------

# 在计划之中的功能有:

* 为逐个叫号优化过后的传统点名,
* 预约报站功能,
* 目的地天气预报,
* 彩蛋,
* 构建并发行手机版本.


# 在第一个正式版之前会做出的优化如下
* 更好看的UI ￣ω￣=
* UI事件与执行异步，联网操作不会在UI线程上 ╮(╯﹏╰）╭
* 优化UI部分源码 (；´д｀)ゞ
* 无需预置java环境

----------------------------------------------------------------------------------
# 谢谢支持(◕ᴗ◕✿)

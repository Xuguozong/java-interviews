- 导入数据只做单月多条数据(什么背景，出现频次，是否可替代，是多次分批导入还是一次性导入)的累计计算，然后存储，
	- ~~当月发薪次数改为查询时计算，就不用维护这个信息了~~
	- 首次发薪日如何处理？
- 列表查询时才做历史累计数据的计算
- 计税时生成新的个税记录，与薪资记录区分开
- 导入账单报税时如何处理？
- 薪资记录状态：计薪、已发放
- 个税记录状态：已计税、申报个税中、报税成功、报税失败
- 如果累积计算放到查询阶段做
	- 需要计算的项目
		- 实发、应发、累积应发、应税、累积应税、应缴、累积已缴、累积免税等等
	- 极端情况，查询 12 月记录，需要查询最少 12 条记录，分页 200 的话是 200 * 12 * 配置了薪税服务的员工数量(1600) 300-400多万条数据 ---> 这个肯定是行不通的
	- 导入薪资数据时计算部分字段，部分字段通过查询时计算
		- 查询时计算的字段：当月发薪次数(easy)[当前是作为查询条件的，不能轻易删除] -- 首次发薪日期(hard) -- 需要隐藏的属性(涉及各种计算 hard)
- 用研
  - 用户使用习惯记录，查询条件频次记录等 TODO
﻿1. 由哪些组件组成
	1）**`Propagation`** 定义传播策略
	2）**`Span`**
	3）**`Sampler`**
	4）**`TraceContext`**
	5）**`Reporter`**
	
2. 各个组件的组成及作用
	1）Propagation
		* Factory 用于创建 Propagation 的抽象工厂类
		* KeyFactory<K> 用于创建传播策略中的 key 的接口
		* Setter<C, K> 用于设置传播上下文中的 key 所对应的 value 的接口，其中 C 是 Carrier，表示 trace context 传播的载体（举例说明）
		* Getter<C, K> 用于获取传播上下文中的 key 所对应的 value 的接口，其中 C 是 Carrier，表示 trace context 传播的载体（举例说明）
	2）Span
	3）Sampler
	4）TraceContext
		* Injector<C>  用于定义如何将 trace context 设置到传播载体中的接口
		* Extractor<C> 用于定义如何从传播载体中获取 trace context 的接口
	5）Reporter
	6）Sender
	
3. 各个组件如何发挥作用

4. 如何扩展组件

5. 集成到日志服务需要做的事情
	1）寻址
	2）扩展信息
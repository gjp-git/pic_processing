# -*- coding: utf-8 -*- 

import producer
import consumer
import threading
import Queue


#初始化
q_data=Queue.Queue()
event=threading.Event()
lock=threading.Lock()
t_num = 10#待测试最优线程数
 
if event.isSet:
	event.clear()
	
p=producer.Producer('-Watchdog',q_data)
p.start()


for each in range(t_num):
	c=consumer.Consumer(each,q_data)
	c.start()

#q_data.join()

"""
若进程中断，则可比较源文件和已转换文件名，将未转换文件转换
"""


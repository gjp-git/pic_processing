# -*- coding: utf-8 -*- 

import producer
import consumer
import threading
import Queue

import os

def getAllFiles(path, list):
	if not os.path.isdir(path):
		return list
	parents = os.listdir(path)
	for parent in parents:
		child = os.path.join(path,parent)
		if os.path.isdir(child):
			getAllFiles(child, list)
		else:
			list.append(child)
	return list
	
if __name__ == "__main__":
	#初始化
	q_data=Queue.Queue()
	t_num = 10#待测试最优线程数
	
	for each in range(t_num):
		c=consumer.Consumer(each,q_data)
		c.start()
		
	rootPath = 'D:\\cicv\\AAAdata'
	fileList = []
	
	getAllFiles(rootPath, fileList)
	
	print "file num : "+str(len(fileList))
	
	for file in fileList:
		#if (file.endswith('.png') or file.endswith('.jpg')) and not "pcl1" in file:
		if file.endswith('.jpg'):
			q_data.put(file)

	'''
	p=producer.Producer('-Watchdog',q_data)
	p.start()
	'''

	
		

	"""
	若进程中断，则可比较源文件和已转换文件名，将未转换文件转换
	"""


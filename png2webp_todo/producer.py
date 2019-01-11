# -*- coding: utf-8 -*- 
import time
import os
import threading
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler, LoggingEventHandler
from watchdog.observers.api import ObservedWatch


class MyHandler(FileSystemEventHandler):
	def __init__(self,name,queue):
			FileSystemEventHandler.__init__(self)
			self.name=name
			self.queue=queue
			self.num=0
			
	def on_created(self, event):
		#print "log file %s created!" % event.src_path
		if event.src_path.endswith('.png') or event.src_path.endswith('.jpg'):
			t = threading.Thread(target=self.put, args=(event.src_path,))
			t.start()
			
			#self.put(event.src_path)
			#print "todo:"+str(self.queue._qsize())+"---created:"+str(self.num)
			#print self.name+" produced data "+ event.src_path
			
	def put(self, path):
		self.queue.put(path)
		"""
		try:
			self.queue.put_nowait(path)
		except Exception:
			print "fail"
			self.put(path)
		"""
	
	

class Producer(threading.Thread):
	def __init__(self,name,queue):
		threading.Thread.__init__(self)
		self.name="Producer"+str(name)
		self.queue=queue
 
		
	def getAllFiles(self, path, list):
		if not os.path.isdir(path):
			return list
		parents = os.listdir(path)
		for parent in parents:
			child = os.path.join(path,parent)
			if os.path.isdir(child):
				self.getAllFiles(child, list)
			else:
				list.append(child)

		return list
		
	def run(self):
		dir = "D:\\cicv\\AAAdata"
		list = []
		self.getAllFiles(dir,list)
		print len(list)
		for file in list:
			if file.endswith('.png') or file.endswith('.jpg'):
				self.queue.put(file)
		
		
		
		"""
		event_handler1 = MyHandler(self.name,self.queue)
		observer = Observer()
		observer.schedule(event_handler1, path='D:\\cicv\\AAAdata', recursive=True)
		observer.start()
		"""
		'''
		try:
			while True:
				time.sleep(1)
		except KeyboardInterrupt:
			observer.stop()
		'''
			
		
		

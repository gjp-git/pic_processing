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
			self.set = set()
			self.num=0
			self.no=0
			
	def on_created(self, event):
		if event.src_path.endswith('.png') or event.src_path.endswith('.jpg'):
			self.no += 1
			print "log file %s created! No %s" % (event.src_path, self.no)
			self.set.add(event.src_path)
			#self.put(event.src_path)
			self.num+=1
			print "todo:"+str(self.queue._qsize())+"---created:"+str(self.num)
			#print self.name+" produced data "+ event.src_path
			
	def put(self, path):
		self.queue.put_nowait(path)
		'''
		try:
			self.queue.put_nowait(path)
		except Exception:
			print "fail"
			self.put(path)
		'''
	

class Producer(threading.Thread):
	def __init__(self,path,name,queue):
		threading.Thread.__init__(self)
		self.path = path
		self.name="Producer"+str(name)
		self.queue=queue

		
	def run(self):

		event_handler1 = MyHandler(self.name,self.queue)
		observer = Observer()
		observer.schedule(event_handler1, path=self.path, recursive=True)
		observer.start()


		try:
			while True:
				time.sleep(1)
		except KeyboardInterrupt:
			observer.stop()

			
		
		

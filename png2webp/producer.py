# -*- coding: utf-8 -*- 
import time
import threading
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler, LoggingEventHandler
from watchdog.observers.api import ObservedWatch


class MyHandler(FileSystemEventHandler):
	def __init__(self,name,queue):
			FileSystemEventHandler.__init__(self)
			self.name=name
			self.queue=queue
			
	def on_created(self):
		#print "log file %s created!" % event.src_path
		if event.src_path.endswith('.png') or event.src_path.endswith('.jpg'):
			self.queue.put(event.src_path)
			#print self.name+" produced data "+ event.src_path
			

class Producer(threading.Thread):
	def __init__(self,name,queue):
		threading.Thread.__init__(self)
		self.name="Producer"+str(name)
		self.queue=queue
 
		
	def run(self):
		event_handler1 = MyHandler(self.name,self.queue)
		observer = Observer()
		observer.schedule(event_handler1, path='D:\\cicv\\test data', recursive=True)
		observer.start()
		try:
			while True:
				time.sleep(1)
		except KeyboardInterrupt:
			observer.stop()
		

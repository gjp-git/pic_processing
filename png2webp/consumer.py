# -*- coding: utf-8 -*- 

import threading
import time
from PIL import Image
import os

class Consumer(threading.Thread):
	def __init__(self,name,queue,event,lock):
		threading.Thread.__init__(self)
		self.name="Consumer"+str(name)
		self.queue=queue
		self.event=event
		self.lock=lock
	
	def create_image(self,infile):
		im = Image.open(infile)
		s = infile.split('\\')
		path = '\\'.join(s[:-1])+'_webp\\'
		isExists=os.path.exists(path)
		if not isExists:
			try:
				os.makedirs(path)
			except Exception:
				pass
		im.save(path+s[-1][:-3]+'webp', "WEBP")
	
	def run(self):
		while True:
			if self.queue.empty():
				self.event.wait()
				if self.event.isSet():
					self.event.clear()
			else:
				self.lock.acquire()
				data=self.queue.get()
				self.queue.task_done()
				self.lock.release()
				self.create_image(data)
				print self.name+" Spend data "+ str(data)

# -*- coding: utf-8 -*- 

import threading
import time
from PIL import Image
import os

class Consumer(threading.Thread):
	def __init__(self,name,queue):
		threading.Thread.__init__(self)
		self.name="Consumer"+str(name)
		self.queue=queue

	
	def create_image(self,infile):
		s = infile.split('\\')
		dirPath = '\\'.join(s[:-1])+'_webp\\'
		filePath = dirPath+s[-1][:-3]+'webp'
		
		if os.path.exists(filePath):
			return
		
		if not os.path.exists(dirPath):
			try:
				os.makedirs(dirPath)
			except Exception:
				if os.path.exists(dirPath):
					pass
				else:
					print self.name + " makedir failed : " + dirPath
		try:
			im = Image.open(infile)
			im.save(filePath, "WEBP")
		except Exception:
			print self.name + " change type failed : " + infile
	
	def run(self):
		while True:
			data=self.queue.get()
			self.create_image(data)
			print self.name+" Spend data "+ str(data)
			'''
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
			'''

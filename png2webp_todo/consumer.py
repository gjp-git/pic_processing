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
		storePath = dirPath+s[-1][:-3]+'webp'
		try:
			im = Image.open(infile)
			if not os.path.exists(dirPath):
				try:
					os.makedirs(dirPath)
				except Exception:
					if os.path.exists(dirPath):
						pass
					else:
						print self.name+" makedirs failed!"
			im.save(storePath, "WEBP")
		except Exception:
			print "Illegal file: "+infile
			"""
			if not os.path.exists(storePath):
				self.queue.put(infile)
			"""
	
	def run(self):
		while True:
			data=self.queue.get()
			self.create_image(data)
			print self.name+" Spend data "+ str(data)


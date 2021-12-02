import time
import os
from mss import screenshot
import numpy
import sys
from PIL import Image
import io

horizontalSize = 160  # 250
verticalSize = 33  # 88
frameSize = 16
frameRateCoefficient = 2 # 16 / coefficient fps
begin = time.time()


class Main:

	def loop(self):
		hCompress = 0
		vCompress = 0
		for j in range(1, 116886, frameSize):
			textfile = io.open('A:/SWANH160x33/{:05d}.txt'.format(int(j/frameSize-1)), 'w', encoding='utf-8')
			for i in range(j, j+frameSize, frameRateCoefficient):
				imgDir = "D:/Users/CanOf/Documents/MEGAsync Downloads/SW_DEED_v2.7_AVCHD/Star Wars Despecialized 2.7 AVCHD/BDMV/STREAM/out{:07d}.png".format(i)
				print(imgDir, (time.time()-begin))
				colorhalf = 0
				screenshot = Image.open(imgDir).crop(
					(0, 90, 1280, 630)).convert('L')
				#screenshot.save('test.png')
				hCompress = int(screenshot.width / horizontalSize)
				vCompress = int(screenshot.height / verticalSize)

				img = numpy.array(screenshot)
				screenshot.close()

				output = numpy.empty([verticalSize, horizontalSize], dtype=str)
				overlay = numpy.zeros(
					[verticalSize, horizontalSize], dtype=float)
				blackFilter = numpy.ones(
					[verticalSize, horizontalSize], dtype=bool)
				for y in range(0, overlay.shape[0]):
					for x in range(0, overlay.shape[1]):
						overlay[y][x] = numpy.average(
							img[y*vCompress:y*vCompress + vCompress, x*hCompress:x*hCompress+hCompress])
						if overlay[y][x] <= 2:
					   		blackFilter[y][x] = False

				blackFilter[0][0] = True
				coloravg = (numpy.average(overlay[blackFilter]))
				colorlow = (numpy.min(overlay))
				colormed = (numpy.percentile(
					overlay[blackFilter], 48) + numpy.percentile(overlay[blackFilter], 52)) / 2
				colorhigh = (numpy.max(overlay))

				color90 = (numpy.percentile(overlay, 90))
				color10 = (numpy.percentile(overlay[blackFilter], 10))
				# (colormed + colorhigh + colorlow) / 4
				colorhalf = max(colormed, 8)
				colorDownRange = max(colorhalf - color10, 4)
				colorUpRange = max(color90 - colorhalf, 4)
				#print('ColorAvg: {:3.3f} '.format(coloravg) + 'ColorMed: {:3.3f} '.format(colormed) + 'ColorHalf: {:3.3f}'.format(colorhalf))
				#print('10th Percentile: ', color10)
				#print('90th Percentile: ', color90)
				#print('ColorLowRange: ', colorDownRange)
				#print('ColorHighRange: ', colorUpRange)
				#print(' ','▫','□','+','┼','╬','╋','\u2591','\u2592','\u2593','\u2588')
				for y in range(0, output.shape[0]):
					for x in range(0, output.shape[1]):
						if (overlay[y][x] < color10 or overlay[y][x] <= 4):
							output[y][x] = ' '
						elif (overlay[y][x] < colorhalf - .8*colorDownRange):
							output[y][x] = '▫'
						elif (overlay[y][x] < colorhalf - .6*colorDownRange):
							output[y][x] = '□'
						elif (overlay[y][x] < colorhalf - .4*colorDownRange):
							output[y][x] = '+'
						elif (overlay[y][x] < colorhalf - .2*colorDownRange):
							output[y][x] = '┼'
						elif (overlay[y][x] < colorhalf + .2*colorUpRange):
							output[y][x] = '╬'
						elif (overlay[y][x] < colorhalf + .2*colorUpRange):
							output[y][x] = '╋'
						elif (overlay[y][x] < colorhalf + .6*colorUpRange):
							output[y][x] = '\u2591'
						elif (overlay[y][x] < colorhalf + .8*colorUpRange):
							output[y][x] = '\u2592'
						elif (overlay[y][x] < color90):
							output[y][x] = '\u2593'
						elif (overlay[y][x] >= color90):
							output[y][x] = '\u2588'
						else:
							output[y][x] = '?'
				for y in range(0, output.shape[0]):
					for x in range(0, output.shape[1]):
						textfile.write(str(output[y][x]))
					textfile.write('\n')
		time.sleep(5)


Object = Main()
Object.loop()

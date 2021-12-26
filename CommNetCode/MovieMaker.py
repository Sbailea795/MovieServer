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

#This prog reads through 116886 still frames of a movie, and takes the cropped area that is the movie, and does
#Fancy math to determine how to use the char set to best represent the frame of the movie. Every segment
# (the 8 frames composing a second of the movie) becomes its own file.
#
#
class Main:

    def loop(self):
        hCompress = 0
        vCompress = 0
        for j in range(1, 116886, frameSize):
            textfile = io.open('A:/SWANH160x33/{:05d}.txt'.format(int(j/frameSize-1)), 'w+', encoding='utf-8')
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

                # determines if the pixel is near pure black. This is for weighting averages and preventing dark frames
                # from overwhelming the char set and making the range cover what should objectively be dark
                for y in range(0, overlay.shape[0]):
                    for x in range(0, overlay.shape[1]):
                        overlay[y][x] = numpy.average(
                            img[y*vCompress:y*vCompress + vCompress, x*hCompress:x*hCompress+hCompress])
                        if overlay[y][x] <= 2:
                               blackFilter[y][x] = False
                # Maths to determines percentiles and such, mostly for the sake of finding ranges
                blackFilter[0][0] = True
                coloravg = (numpy.average(overlay[blackFilter]))
                colorlow = (numpy.min(overlay))
                colormed = (numpy.percentile(
                    overlay[blackFilter], 48) + numpy.percentile(overlay[blackFilter], 52)) / 2
                colorhigh = (numpy.max(overlay))

                color90 = (numpy.percentile(overlay, 90))
                color10 = (numpy.percentile(overlay[blackFilter], 10))
                colorhalf = max(colormed, 8)
                colorDownRange = max(colorhalf - color10, 4)
                colorUpRange = max(color90 - colorhalf, 4)

                # Should I use something besides a nested if? Yes. Will I? No.
                # Runs fast enough for the time being. This determines the UNICODE representation
                # Of the pixel in question
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
                # Print to files
                for y in range(0, output.shape[0]):
                    textfile.write('Line {:02d}|'.format(y))
                    for x in range(0, output.shape[1]):
                        textfile.write(str(output[y][x]))
                    textfile.write('\n')
        time.sleep(5)


Object = Main()
Object.loop()

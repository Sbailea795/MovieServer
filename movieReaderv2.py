import time
import os
textfile = open('D:/Users/CanOf/Documents/MEGAsync Downloads/SW_DEED_v2.7_AVCHD/Star Wars Despecialized 2.7 AVCHD/BDMV/STREAM/Star_Wars_A_New_Hope_240x50.txt', 'r', encoding='utf-8')
subfile = open('D:/Users/CanOf/Documents/MEGAsync Downloads/SW_DEED_v2.7_AVCHD/Star Wars Despecialized 2.7 AVCHD/BDMV/STREAM/Subtitles.txt', 'r', encoding='utf-8')
begin = time.time()

class Main:

	def loop(self):
            byte = ''
            for frame in range(1, 116886):
                last_time = time.time_ns()
                for line in range(0, 33):
                    byte += textfile.readline()
                #for line in range(0, 18):
                    #byte += subfile.readline()
                cls(self)
                print('\n\n\n\n\n\n\n\n\n')
                print('\n\n\n\n\n\n\n\n\n')
                print('\n\n\n\n\n\n\n\n\n')
                print(byte, end ='')
                #byte = textfile.readline()
                byte = subfile.readline()
                now_time = time.time_ns()
                print("Frame: {:9s}".format(str(frame)) + "Time Stamp: {:9s}".format(str(frame / 8)) + "Overhead Time: {:9s}".format(str((now_time-last_time)/100000000)) + "Time: {:6s}".format(str(time.time()-begin)) )
                if time.time()-begin < frame / 8:
                    time.sleep(1/8)
                elif (frame / 8) - (time.time() - begin) < 1:
                    time.sleep(1/40)
                else:
                    continue
                ##if time.time()-begin >= frame / 8 and 1/8 - (now_time-last_time)/100000000 > 0:
                ##    time.sleep(1/8 - (now_time-last_time)/100000000)
                ##elif time.time()-begin > frame / 8:
                ##    time.sleep(1/80)
                ##else:
                #3    time.sleep(1/8)

def cls(self):
    os.system('cls' if os.name == 'nt' else 'clear')

Object = Main()
Object.loop()

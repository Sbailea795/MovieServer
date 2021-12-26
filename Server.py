#!/usr/bin/python3           # This is server.py file
import os
import socket                                         
import time
cls = '\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n'
class Main:

    def loop(self):

        serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM) 
        host = socket.gethostname()                        
        port = 2115
        serversocket.bind(('', port))

        # queue up to 5 requests
        serversocket.listen(5)                                           

        while True:
            clientsocket,addr = serversocket.accept()  
            data = ''
            print("Got a connection from %s" % str(addr))

            clientsocket.send('This Is meant for a ratio of (COL:168, ROW:34) character displays.\r\n'.encode('utf-8'))
            clientsocket.send('This is designed for UTF-8 chacters, not all terminals/telnet applications support this. Please consider using PuTTY or another UTF-8 capaable telnet client.\r\n\r\nTo continue with ASCII encoding, press 1 (not recommended), otherwise press any key + enter (recommended)\r\n'.encode('utf-8'))
            #time.sleep(5)
            isASCII = False
            try:
                print('try')
                clientsocket.settimeout(None)
                msg = clientsocket.recv(1024)
                msg = clientsocket.recv(1024).strip()
                if msg.decode('utf-8') == '1': 
                    isASCII = True
                print('again?')
            except:
                continue
            clientsocket.send(data.encode('utf-8'))
            print('welcome')
            Main.welcome(clientsocket)
            print('movie starting')
            begin = time.time()
            for segment in range(0,7304):
                for frame in range(0,7,2):
                    last_time = time.time()
                    textfile = open('./MovieSegments/{:05d}.txt'.format(segment), 'r', encoding='utf-8')
                    #subfile = open('S{:05d}.txt'.format(segment), 'r', encoding='utf-8')

                    data = ''
                    for line in range(0, frame * 33):
                        textfile.readline()
                    for line in range(0, 33):
                        data += textfile.readline()
                    data = cls + data.replace('\n', '\r\n')
                    if isASCII:
                        data = data.replace('█', '#')
                        data = data.replace('▓', '%')
                        data = data.replace('▒', '|')
                        data = data.replace('░', '!')
                        data = data.replace('╬', '^')
                        data = data.replace('┼', '=')
                        data = data.replace('□', '-')
                        data = data.replace('▫', '.')
                    
                    #for line in range(0, frame * 18):
                        #subfile.readline()
                    #for line in range(0, 18):
                        #byte += subfile.readline()
                    clientsocket.send(data.encode('utf-8'))
                    clientsocket.send(("Frame: {:9s} ".format(str(8*segment + frame)) + "Time Stamp: {:9s} ".format(str(segment + frame / 8)) + "Overhead Time: {:01.8f} ".format(time.time()-last_time) + "Time: {:6s} ".format(str(time.time()-begin))).encode())
                    if time.time() - begin <= segment:
                        time.sleep(4/16)
                    elif time.time() - begin <= segment + 1/4:
                        time.sleep(3/16)
                    elif time.time() - begin <= segment + 1/2:
                        time.sleep(2/16)
                    else:
                        time.sleep(1/16)
            clientsocket.close()

    def welcome(clientSocket):
        text = ''
        textfile = open('./MovieSegments/FrameOutline.txt', 'r', encoding='utf-8')
        for line in range(0, 34):
            text += textfile.readline()
        text = cls + text.replace('\n', '\r\n')
        clientSocket.send(text.encode('utf-8'))
        time.sleep(5)
        
        text = ''
        textfile = open('./MovieSegments/TitleCard.txt', 'r', encoding='utf-8')
        for line in range(0, 34):
            text += textfile.readline()
        text = cls + text.replace('\n', '\r\n')
        clientSocket.send(text.encode('utf-8'))
        time.sleep(10)

Object = Main()
Object.loop()
#!/usr/bin/python3           # This is server.py file
import os
import socket                                         
import time

clientsocket = socket.socket()

class Main:

    def loop(self):

        # create a socket object
        serversocket = socket.socket(
                    socket.AF_INET, socket.SOCK_STREAM) 

        # get local machine name
        host = socket.gethostname()                        

        port = 2115                                           

        # bind to the port
        serversocket.bind(('', port))

        # queue up to 5 requests
        serversocket.listen(5)                                           

        while True:
            # establish a connection
            clientsocket,addr = serversocket.accept()      
            data = ''
            print("Got a connection from %s" % str(addr))
            #msg = serversocket.recv(1024)
            #print (msg.decode('utf-8'))
            clientsocket.send(data.encode('utf-8'))
            print('welcome')
            Main.welcome()
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
                    data = '\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n' + data.replace('\n', '\r\n')
                    
                    #data = data.replace('█', '#')
                    #data = data.replace('▓', '%')
                    #data = data.replace('▒', '|')
                    #data = data.replace('░', '!')
                    #data = data.replace('╬', '^')
                    #data = data.replace('┼', '=')
                    #data = data.replace('□', '-')
                    #data = data.replace('▫', '.')
                    
                    #for line in range(0, frame * 18):
                        #subfile.readline()
                    #for line in range(0, 18):
                        #byte += subfile.readline()
                    #cls(self)
                    #print('\n\n\n\n\n\n\n\n\n')
                    #print('\n\n\n\n\n\n\n\n\n')
                    #print('\n\n\n\n\n\n\n\n\n')
                    clientsocket.send(data.encode('utf-8'))
                    #byte = textfile.readline()
                    #byte = subfile.readline()
                    clientsocket.send(("Frame: {:9s} ".format(str(8*segment + frame)) + "Time Stamp: {:9s} ".format(str(segment + frame / 8)) + "Overhead Time: {:01.8f} ".format(time.time()-last_time) + "Time: {:6s} ".format(str(time.time()-begin))).encode())
                    #clientsocket.send('\n\n\n\n\n\n\n\n\n'.encode('utf-8'))
                    if time.time() - begin <= segment:
                        time.sleep(4/16)
                    elif time.time() - begin <= segment + 1/4:
                        time.sleep(3/16)
                    elif time.time() - begin <= segment + 1/2:
                        time.sleep(2/16)
                    else:
                        time.sleep(1/16)
            clientsocket.close()

    def welcome():
        text = ''
        textfile = open('./MovieSegments/FrameOutline.txt', 'r', encoding='utf-8')
        while (textfile.readable): 
            text = textfile.readline
        clientsocket.send(text.encode('utf-8'))
        print('test1')
        time.sleep(3)
        print('test1a')
        text = ''
        textfile = open('./MovieSegments/TitleCard.txt', 'r', encoding='utf-8')
        while (textfile.readable): 
            text = textfile.readline
        clientsocket.send(text.encode('utf-8'))
        time.sleep(3)
        print('test2')

#def cls(self):
    #os.system('cls' if os.name == 'nt' else 'clear')

Object = Main()
Object.loop()
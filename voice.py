#-*- coding: utf-8 -*-
import socket
import speech_recognition as sr

ip = '192.168.43.134'
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_address = (ip, 6000)
sock.connect(server_address)
print("connected")

sample_rate = 48000
chunk_size = 2048

while True:
	try:
		r = sr.Recognizer()
		try:
			print("You said: " +r.recognize_google(audio, language = 'ko-KR'))
			x = r.recognize_google(audio, language = 'ko-KR')
			print x # 인식된 말 출력
			if (x.find(u"멈춰")!=-1):
				sock.send(u"멈춰".encode('utf8'))
			if (x.find(u"출발")!=-1):
				sock.send(u"출발".encode('utf8'))

		except sr.UnknownValueError:
	    		print("Google Speech Recognition could not understand audio")
			pass

		except sr.RequestError as e:
	    		print("Could not request results from Google Speech Recognition service; {0}".format(e))
			pass

	except KeyboardInterrupt:
		sock.send("exit".encode())
		break

	except Exception, e:
		print e
		sock.send("exit".encode())
		break
sock.close()


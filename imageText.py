import cv2
import pytesseract
from pytesseract import Output
import base64
import socket
import time
import json
import numpy as np

def server():
    # transform base64 to rgb image
    listen_socket = socket.socket()
    port = 8000
    max_connections = 999
    ip = socket.gethostname()  # Gets Hostname Of Current Macheine
    listen_socket.bind(('', port))

    # Opens Server
    listen_socket.listen(max_connections)
    print("Server started at " + ip + " on port " + str(port))

    return listen_socket

def base64_cv2(base64_str):
    img_string = base64.b64decode(base64_str)
    np_array = np.frombuffer(img_string, np.uint8)
    image = cv2.imdecode(np_array, cv2.IMREAD_COLOR)
    return image
    
def imageToText(img):
    return_json = ""
    d = pytesseract.image_to_data(img, output_type=Output.DICT)
    print(str(d['text']))
    for i in range(len(d['text'])):
        if int(d['conf'][i]) > 30:
            (x, y, w, h) = (d['left'][i], d['top'][i], d['width'][i], d['height'][i])
            img = cv2.rectangle(img, (x, y), (x + w, y + h), (0, 255, 0), 2)
    
    
    
    
    ####Zheer can implement here#####
    m = {"id": 2, "name": "abc"} # a real dict.
    data = json.dumps(m)
    
    
    return data


if __name__ == "__main__":
    listen_socket = server()

    while True:
        print("receiving")
        (client_socket, address) = listen_socket.accept()
        result = ""
        commandFromClient = ""
        print("receive new socket request")
        while True:
            if result[-13:] == "end_of_packet":
                commandFromClient = "end_of_packet"
                break
            result += client_socket.recv(1024).decode()

        if not result == "":
            if len(result) >= 10000:
                result = result[0:-13]
                result = result.encode()
                img = base64_cv2(result)
                print(result)
            else:
                print(result)
                continue
                
            if commandFromClient == "end_of_packet":
                print("doing object detection......")
                output = imageToText(img)
#                output = "qweqwe"
                client_socket.sendall(bytes(output,encoding="utf-8"))
            
            
        client_socket.close()
    print("end")

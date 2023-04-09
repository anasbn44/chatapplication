import socket
import threading
listen = True;


def listen_to_response(socket):
    while listen:
        data = socket.recv(1024).decode()
        print('Received from server: ' + data)


def client_program():
    host = "127.0.0.1"
    port = 1997
    client_socket = socket.socket()
    client_socket.connect((host, port))
    thread = threading.Thread(target = listen_to_response, args = (client_socket,))
    thread.start()
    request = ""
    while request.lower().strip() != 'exit':
        request = input(" -> ")
        client_socket.send(request.encode())
    client_socket.close()
    listen = False

if __name__ == '__main__':
    client_program()
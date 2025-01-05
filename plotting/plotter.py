import socket
import json
import matplotlib.pyplot as plt
import threading
import time

# Data storage for plotting
server_data = {1: [], 2: [], 3: []}
server_timestamps = {1: [], 2: [], 3: []}
colors = {1: 'red', 2: 'blue', 3: 'green'}

HOST = 'localhost'
PORT = 8000

# Function to update and plot the graph
def plot_graph():
    plt.ion()
    while True:
        plt.clf()
        for server_id, statuses in server_data.items():
            timestamps = server_timestamps[server_id]
            if statuses and timestamps:
                plt.plot(timestamps, statuses, label=f"Server {server_id}", color=colors[server_id])

        plt.xlabel('Timestamp (s)')
        plt.ylabel('Server Status (Number of Subscribers)')
        plt.title('Server Capacity Over Time')
        plt.legend()
        plt.pause(1)

# Function to handle incoming data from servers
def handle_server_data():
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as server_socket:
        server_socket.bind((HOST, PORT))
        server_socket.listen()
        print(f"Plotter server is listening on {HOST}:{PORT}")

        while True:
            conn, addr = server_socket.accept()
            with conn:
                print(f"Connected by {addr}")
                data = conn.recv(1024)
                if data:
                    try:
                        message = json.loads(data.decode('utf-8'))
                        server_id = message['server_id']
                        server_status = message['server_status']
                        timestamp = message['timestamp']

                        server_data[server_id].append(server_status)
                        server_timestamps[server_id].append(timestamp)

                        # Keep only the last 50 data points for better visualization
                        if len(server_data[server_id]) > 50:
                            server_data[server_id].pop(0)
                            server_timestamps[server_id].pop(0)

                        print(f"Updated data for Server {server_id}: {server_status} at {timestamp}")
                    except Exception as e:
                        print(f"Error processing data: {e}")

# Start the plotter and data handler threads
if __name__ == "__main__":
    threading.Thread(target=plot_graph, daemon=True).start()
    handle_server_data()

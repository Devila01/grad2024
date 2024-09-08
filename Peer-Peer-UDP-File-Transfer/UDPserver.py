import socket  
import random  
  
  
SERVER_DATA_PATH = "./server_data"  
  
ADDR = "localhost"    
PORT = 10000  
ENCODING = "utf-8"  
# Sender Format = SEQ_NUM FILE_NAME SIZE    
HEADER = "\x01{0}\x1D{1}\x1D{2}\x19"  
  
# Format is \x01 SEQ_NUM \x19  
ACK = "\x01{0}\x19" # ACK is just sending packet seq_num  
  
WINDOW_SIZE = 10  
FRAME_SIZE = 1024 # 80 Bytes  
# 255 for max size of unix filename, 4 bytes for control chars, 4 bytes for seq_num, 4 bytes for file size.  
BUF_SIZE = FRAME_SIZE+255+4+4+4  
  
window_start = 0 # Start of window counter  
pkts = [] # Consists of (packet_num, meta) tuples  
pkts_keys = [] # Consists of packet number to prevent duplicates  
file_size = 0 # total file size  
recv_size = 0 # total bytes written  
written_packet = -1 # Packet number of most recently written packet  
next_expected_seq_num = 0  
accepting = False  
  
def get_info(packet):  
    # Function to extract meta data from packet.  
    trunc = packet[packet.find("\x01")+1:]  
  
    # retrieve sequence number  
    seq_num = int(trunc[:trunc.find("\x1d")])  
  
    #retrieve file name  
    fname = packet[packet.find("\x1D") + 1: packet.find("\x1D", packet.find("\x1D") + 1)]  
  
  
    start = packet.find("\x1D") + len(fname) + 2  
    end = packet.find("\x19")  
    # retrieve file size  
    fsize = int(packet[start:end])  
  
    return (seq_num, fname, fsize)    
  
  
def packet_lost():  
    # Function to determine whether packet is lost or not. Uses Python random module to  
    # get random integer from 0 to 10  
    is_lost = random.randint(1, 10)  
    if is_lost < 4:  
  
        # True means packet is lost  
        return True  
      
    # False means packet is not lost  
    return False  
  
def get_packet_number(seq_num):  
    # Function to get packet number given the sequence number  
  
      
    pn = seq_num / FRAME_SIZE  
    pnt = seq_num // FRAME_SIZE  
    ret = pnt+1 if pn > pnt else pn  
    return ret  
  
def receive_packet(buf, sock, client_addr):  
    # Function that deals with receiving of packet, sends ACKs, and writes to file.  
  
    global next_expected_seq_num  
    global pkts  
    global pkts_keys  
    global file_size  
    global recv_size  
    global written_packet  
    global window_start  
    global accepting  
  
    received_packet_num = -1  
  
    # Retrieve the header from the packet, decode it, and seperate meta data from header.  
    start_index = buf.find(b"\x01")  
    end_index = buf.find(b"\x19") + 1  
    header = buf[start_index:end_index]  
    seq_num, fname, fsize = get_info(header.decode(ENCODING))  
  
    # Data portion of packet is spliced from buf  
    data = buf[end_index:end_index+fsize]  
  
    # Open server_log.txt  
    f = open("server_log.txt", "a")  
  
    # If the sequence number is -1 i.e. meta data packet containing total file size.  
    if seq_num == -1:  
  
        # Create new file with meta data file name and update logs.  
        f.write('Receiver: received {}\n'.format(fname))  
        new_file = open(SERVER_DATA_PATH+"/"+fname, "wb")  
        new_file.close()  
  
        print("Filename successfully received")  
  
        # Update variables indicating total file size and that we are accepting non-meta data packets.  
        received_packet_num = -1  
        file_size = fsize  
        accepting = True  
  
    elif accepting:  
  
        # Get the packet number using the sequence number   
        received_packet_num = get_packet_number(seq_num)  
  
        # Check if the packet is the rcvbase, if it is then write the packet's data to the file immedietly.  
        if received_packet_num == written_packet + 1:  
  
            # Append packet data to the file  
            file = open(SERVER_DATA_PATH+"/"+fname, "ab") # should already exist  
            file.write(data)  
  
            # Increase the variable keeping track of total bytes written  
            recv_size += len(data)  
  
            # Increase the next expected sequence number variable  
            next_expected_seq_num += len(data)  
  
            # Increase start of window since rcvbase has increased.  
            window_start += 1  
  
            written_packet += 1  
  
            # Check if the cached packets can also be written to file. i.e. the first cached packet is the rcvbase.  
            if len(pkts) > 0 and received_packet_num + 1 == pkts[0][0]:  
  
                counter = received_packet_num  
                # If packet at index 0 of the packet cache is the rcvbase/window_start then write it to file.  
                while counter + 1 == pkts[0][0]:  
                    file.write(pkts[0][1])  
  
                    # Update total written bytes.  
                    recv_size += len(pkts[0][1])  
  
                    # Update the expected sequence number variable  
                    next_expected_seq_num += len(pkts[0][1])  
  
                    # delete the index 0 packet  
                    del pkts[0]  
  
                    # Update other necessary counter variables.  
                    counter += 1  
                    written_packet += 1  
                    window_start += 1  
  
                    # If there are no more packets to write then break.  
                    if len(pkts) == 0:  
                        break  
            file.close()  
        else:  
            # If the received packet was already written then ignore.  
            if received_packet_num <= written_packet:  
                pass  
            else:  
                # Check if the received packet is not a duplicate packet already cached and that it is within the window  
                if received_packet_num in range(window_start, window_start + WINDOW_SIZE) and \
                received_packet_num not in pkts_keys:  
                    # If the packet is in the window and not duplicate then cache it.  
                    pkts.append((received_packet_num, data))  
                    pkts = sorted(pkts)  
                    pkts_keys.append(received_packet_num)  
  
    # Remove the processed packet from the buffer  
    buf = buf[end_index+fsize:]  
      
    # For non-meta data packets, print to console and appropriately write to log  
    if received_packet_num != -1:  
        print("Server: PKT{}, INCOMING SEQ NUM: {}, EXPECTED SEQ NUM: {}\n".format(received_packet_num, seq_num, next_expected_seq_num))  
        f.write('Receiver: received PKT{}\n'.format(received_packet_num))  
    #ACK packets in [rcv_base, rcv_base+N-1] union [rcv_base-N, rcv_base-1] only  
    if received_packet_num in range(window_start - WINDOW_SIZE, window_start + WINDOW_SIZE):  
        send_ack(sock, seq_num, client_addr)  
        f.write('Receiver: sent an ACK{}\n'.format(received_packet_num))  
  
  
    # Check if the whole file has been received indicating file transfer complete.  
    if accepting and recv_size >= file_size:  
  
        # Update log appropriately  
        f.write('Receiver: file transfer completed\n')  
        f.write('Receiver: number of bytes received: {} bytes\n'.format(file_size))  
  
        # Reset global and local variables.  
        window_start = 0  
        pkts = []   
        pkts_keys = []  
        file_size = 0  
        recv_size = 0  
        written_packet = -1  
        next_expected_seq_num = 0  
        accepting = False  
  
        f.close()  
        # Return an empty buffer in preperation of next file transfer.  
        return b""  
  
    f.close()  
    # Return updated buffer without received packet.  
    return buf  
  
  
def send_ack(sock, seq_num, client_addr):  
    # Send an encoded ACK with the seq_num  
    sock.sendto(ACK.format(seq_num).encode(ENCODING), client_addr)  
  
  
if __name__ == "__main__":  
  
    # Setup socket with client address and port number then bind it  
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)  
    server_address = (ADDR, PORT)  
    print('Starting up on {} port {}'.format(*server_address))  
    sock.bind(server_address)  
  
    # initialize buffer  
    buf = b""  
  
    while True:  
        print('\nWaiting to receive message')  
        # Wait until client sends a packet at which point buf contains the packet and client_addr  
        # contains the address for server to sends ACKS to.  
        buf, client_addr = sock.recvfrom(BUF_SIZE)  
  
        # Runs the artificial packet loss function, if true packet is lost  
        if packet_lost():  
            # Find start and end of the "to-be-lost" packet and remove it from buffer.  
            start_index = buf.find(b"\x01")  
            end_index = buf.find(b"\x19") + 1  
            lost_meta = buf[start_index:end_index].decode(ENCODING)  
            fsize = get_info(lost_meta)[2]  
  
            # update buffer  
            buf = buf[end_index+fsize:]  
  
        else: # packet is not lost and is received by server  
            buf = receive_packet(buf, sock, client_addr)  
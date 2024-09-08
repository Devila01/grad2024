import socket  
import time  
  
# DEFINITIONS  
  
LOG_PATH = "./client_log.txt"  
log = None  
  
INT_MAX = 2147483647  
INT_MIN = -2147483647  
  
bytes_sent = 0  
pkts_sent = 0  
pkts_lost = 0  
pkts_acpt = 0  
  
max_rtt = INT_MIN  
min_rtt = INT_MAX  
avg_rtt = 0  
  
CLIENT_DATA_PATH = "./client_data"  
  
CL_ADDR = 'localhost'  
CL_PORT = 10001  
CLIENT_ADDR = (CL_ADDR, CL_PORT)  
  
SR_ADDR = "localhost"  
SR_PORT = 10000  
SERVER_ADDR = (SR_ADDR, SR_PORT)  
  
FRAME_SIZE = 1024 # y = 80  
WINDOW_SIZE = 10  
ENCODING = "utf-8"  
TIMEOUT = 1  
  
# this is the recv buf for acknowledgements. it is set to 6*WINDOW_SIZE because an acknowlegement is is 2 ctrl bytes and a 4 byte int. There can be at most window_size acks in flight.  
BUF_SIZE = 6*WINDOW_SIZE  
  
# \x01 SEQ_NUM \x1D FILENAME \x1D SIZE \x19  
HEADER = "\x01{0}\x1D{1}\x1D{2}\x19"  
  
# \x01 SEQ_NUM \x19  
# ACK = "\x01{0}\x19"  
  
class Frame:  
    data = None  
    seq_num = None  
    last_sent = None  
    ack = None  
  
    def __init__(self, data: str, seq_num: int, last_sent=time.time()):  
        self.data = data  
        self.seq_num = seq_num  
        self.last_sent = last_sent  
        self.ack = False  
  
  
# END DEFINITIONS  
  
def uavg(rtt: int) -> int:  
    global avg_rtt  
    global pkts_acpt  
    rsum = avg_rtt * pkts_acpt / 100  
    pkts_acpt += 1  
    rsum += rtt*1000  
    return (avg_rtt := (rsum / pkts_acpt * 100))  
  
  
def umax(rtt: int) -> int:  
    global max_rtt  
    return (max_rtt := max(max_rtt, rtt*1000))  
  
  
def umin(rtt: int) -> int:  
    global min_rtt  
    return (min_rtt := min(min_rtt, rtt*1000))  
  
  
def get_packet_num(seq_num: int) -> int:  
    pn = seq_num / FRAME_SIZE  
    pnt = seq_num // FRAME_SIZE  
    return pnt+1 if pn > pnt else pn  
  
  
def get_effective_bytes(fsize: int) -> int:  
    tot_pkts = get_packet_num(fsize)  
    # 6 bytes for metadata pkt. 6*tot_pkts for the bytes of the pkt header, and fsize bytes for the file itself.  
    return 6 + 6*tot_pkts + fsize  
  
  
def get_size(fname: str) -> int:  
    try:  
        fp = open(CLIENT_DATA_PATH+'/'+fname, 'rb')  
    except:  
        return -1  
      
    fp.seek(0, 2)  
    ret = fp.tell()  
    fp.close()  
  
    return ret  
  
  
def push_frame(sock: socket.socket, window: list, fp, fname: str, curr: int) -> int:  
    global bytes_sent  
    global pkts_sent  
  
    while len(window) <= WINDOW_SIZE:  
        if (buf := fp.read(FRAME_SIZE)) == b"":  
            break  
        data = HEADER.format(curr, fname, len(buf)).encode(ENCODING) + buf  
        window.append(Frame(data, curr))  
  
        sock.sendto(data, SERVER_ADDR)  
        bytes_sent += len(data)  
        pkts_sent += 1  
  
        log.write("Sender: sent PKT{0}\n".format(get_packet_num(curr)))  
        curr += FRAME_SIZE  
  
    return curr  
  
  
def retransmit(sock: socket.socket, window: list) -> None:  
    global bytes_sent  
    global pkts_sent  
    global pkts_lost  
  
    now = time.time()  
  
    for frame in window:  
        if frame.ack == False and now - frame.last_sent > TIMEOUT:  
            print("PKT{0} Request Time Out".format(get_packet_num(frame.seq_num)))  
  
            sock.sendto(frame.data, SERVER_ADDR)  
            bytes_sent += len(frame.data)  
            pkts_sent += 1  
            pkts_lost += 1  
  
            frame.last_sent = now  
  
  
def read_acks(sock: socket.socket) -> list:  
    ret = []  
    try:  
        buf, addr = sock.recvfrom(BUF_SIZE)  
    except:  
        return ret  
    buf = buf.decode(ENCODING)  
      
    while ((s := buf.find("\x01")) != -1 and (e := buf.find("\x19")) != -1):  
        ack = int(buf[s+1:e])  
        ret.append(ack)  
        log.write("Sender: received ACK{0}\n".format(get_packet_num(ack)))  
        buf = buf[e+1:]  
  
    return ret  
  
  
def mark_acks(acks: list, window: list, base: int) -> int:  
    for frame in window:  
        if frame.seq_num in acks:  
            now = time.time()  
            rtt = now - frame.last_sent  
            umax(rtt)  
            umin(rtt)  
            uavg(rtt)  
            frame.ack = True  
  
            print("ACK{0} received".format(get_packet_num(frame.seq_num)))  
            print("Start Time: {0} sec".format(frame.last_sent))  
            print("End Time: {0} sec".format(now))  
            print("RTT: {0} sec".format(rtt))  
  
        # slide the window base forward if the acked frame is the base frame  
        if frame.seq_num == base and frame.ack == True:  
            window.remove(frame)  
            base += FRAME_SIZE  
  
    return base  
  
  
def send_meta(sock: socket.socket, fname: str, fsize: int) -> None:  
    global bytes_sent  
    global pkts_sent  
    global pkts_lost  
  
    last_sent = time.time()  
  
    meta = HEADER.format(-1, fname, fsize).encode(ENCODING)  
    sock.sendto(meta, SERVER_ADDR)  
    bytes_sent += len(meta)  
    pkts_sent += 1  
  
    while -1 not in read_acks(sock):  
        now = time.time()  
        if now - last_sent > TIMEOUT:  
            print("PKT-1 Request Time Out")  
            sock.sendto(meta, SERVER_ADDR)  
            bytes_sent += len(meta)  
            pkts_sent += 1  
            pkts_lost += 1  
  
            last_sent = now  
  
  
def send_file(sock: socket.socket, fname: str) -> None:  
    global bytes_sent  
    global pkts_sent  
  
    # open the file. If it does not exist, do nothing.  
    try:  
        fp = open(CLIENT_DATA_PATH+'/'+fname, 'rb')  
    except:  
        print("Sender: File not found.")  
        return  
  
    base = 0 # window base sequence number  
    curr = 0 # sequence number of packet after the last one in window  
    fsize = get_size(fname)  
    window = []  
  
    # send file metadata and await ack  
    send_meta(sock, fname, fsize)  
  
    # send file  
    while True:  
        # get and set acknowledged acks from the reciever  
        base = mark_acks(read_acks(sock), window, base)  
        # retransmit timed-out packets  
        retransmit(sock, window)  
        # if the window moved forward (because send_base pkt was acked), push and transmit next pkts  
        curr = push_frame(sock, window, fp, fname, curr)  
  
        if curr >= fsize and len(window) == 0:  
            break  
  
    log.write("Sender: file transfer completed\n")  
    log.write("Sender: number of effective bytes sent: {0} bytes\n".format(get_effective_bytes(fsize)))  
    log.write("Sender: number of packets sent: {0} packets\n".format(pkts_sent))  
    log.write("Sender: number of bytes sent: {0} bytes\n".format(bytes_sent))  
    fp.close()  
  
  
if __name__ == '__main__':  
    log = open(LOG_PATH, 'a')  
    log.write("Sender: starting on host {0}\n".format(CLIENT_ADDR))  
    try:  
        sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)  
        sock.settimeout(0.01)  
        sock.bind(CLIENT_ADDR)  
    except:  
        log.write("Sender: failed to start\n")  
        log.close()  
        exit(-1)  
  
    while True:  
        stdin = input(">")  
        if stdin == 'exit':  
            sock.close()  
            log.write("Sender: shutting down\n")  
            log.close()  
            exit(0)  
  
        bytes_sent = 0  
        pkts_sent = 0  
        pkts_lost = 0  
        pkts_acpt = 0  
  
        max_rtt = INT_MIN  
        min_rtt = INT_MAX  
        avg_rtt = 0  
  
        send_file(sock, stdin)  
  
        if pkts_sent != 0:  
            print("{0} successfully sent".format(stdin))  
            print("Maximum RTT: {0} msec".format(max_rtt))  
            print("Minimum RTT: {0} msec".format(min_rtt))  
            print("Average RTT: {0} msec".format(avg_rtt))  
            print("Packet loss rate: {0}%".format(pkts_lost/pkts_sent*100))  
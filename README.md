# About
This repository contains the data traces and source codes for multi-path TCP (MPTCP) measurement on high-speed rails. It also contains the scripts for data analysis and plotting.

Please cite the paper as follows (or use this [bibtex record](./bibtex.txt)).

- Li Li, Ke Xu, Tong Li, Kai Zheng, Chunyi Peng, Dan Wang, Xiangxiang Wang, Meng Shen, Rashid Mijumbi. [A Measurement Study on Multi-path TCP with Multiple Cellular Carriers on High-speed Rails](./paper.pdf). Annual conference of the ACM Special Interest Group on Data Communication on the applications, technologies, architectures, and protocols for computer communication (ACM SIGCOMM), pp. 161-175, 2018. 

## Data Traces

`data_traces` includes the traces for elephant and mice flows collected.

## Tools and Scripts

The directory structure in `src` is as follows:

``` text
HSR Measurement
|-- android            # Code of Mobinet for measurement
|   \-- ...
|-- bash            # Code for measurement
|   \-- ...
|-- c               # Code for create txt files
|   \-- ...
|-- matlab          # Code for evaluating the performance
|   \-- ...
\-- README.md
```

## How to Use

### Measurement

First of all, the Android source code of MobiNet output an APK, which can be installed on the test smartphones.

For measurement, you could copy the `bash` code into Linux environment and just run `run.sh`. Notice here, in our experiments, we requires subflows using different WLAN environment. That is, for each subflow, the corresponding WLAN should have a different SSID.

`loop.sh` is used for countint the bytes in/out from each port

`monitor.sh` is used for monitoring if MPTCP works well

Our experiments will collect data on both ends, for each experiment, data is stored in `./<HSR-ID>/<EXPR-ID>/` and organized as following:

``` text
.
|-- HSR-ID                          # HSR ID, like C2006
|   |-- EXPR-ID                     # Experiment ID, starts from 1
|   |   |-- ifaceX                  # Port name, like eth0, wlan1, etc.
|   |   |   |-- THP-File            # .thp file collected by loop.sh
|   |   |   |-- PCAP-FILE           # .pcap file collected by tcpdump
|   |   |   |-- carrier             # the carrier of ifaceX
|   |   |   |-- interface           # the name of ifaceX
|   |   |   \-- ip_address          # the IP address of ifaceX
|   |   |-- buffer_size             # experiment buffer size
|   |   |-- congestion_control      # experiment algorithm like reno, lia, etc.
|   |   |-- download_size           # download file size
|   |   |-- end_time                # experiment end time
|   |   |-- expr_type               # experiment type (Obsolete)
|   |   |-- scheduler               # MPTCP scheduler
|   |   \-- start_time              # experiment start time
|   \-- ...
\-- ...
```

We also record the basic experiment information in `global.log` file with following format:
``` text
EXPR-ID CARRIER-TYPE EXPR-TYPE CONG RMEM  WMEM  SCH     FILE    SV-IP   START-TIME END-TIME  
1       MOBILE       0         Reno 16384 16384 Default 1GB.dat 1.2.3.4 12345.123  12465.234 

(continue with the last line)

CL-PCAP-PATH               CL-THP-PATH               SV-PCAP-PATH         CARRIER FILE-SIZE
~/C2008/1/wlan2/wlan2.pcap ~/C2008/1/wlan2/wlan2.thp ~/C2008.1.wlan2.pcap Mobile  1GB
```

### Analyzing
For analyzing the performance, first you should create txt file from pcap file with our tool `pcap2txt`. You can build and use `pcap2txt` as following:
``` bash
g++ pcap2txt.cpp -o pcap2txt
find . -name "*.pcap" -exec ./pcap2txt {} {}.txt \;
```

After txt file processing, you can use Matlab to analyze its performance. Here we provide several kinds of performance evaluation, like throughput, RTT, bytes-in-flight, loss-packet-rate, packet retransmitting, and reordering. They are all included in `Matalb` directory. We also add a example.m to indicate how to use them.

``` text
Matlab
|-- cwnd.m              # Code for calculating bytes-in-flight
|-- disorder.m          # Code for calculating reordering
|-- example.m           # Example code for use
|-- retrans.m           # Code for calculating retransmitted packets
|-- rtt.m               # Code for calculating RTT
|-- throughput.m        # Code for calculating throughput
\-- txt2stream.m        # Read txt file into matrix format
```

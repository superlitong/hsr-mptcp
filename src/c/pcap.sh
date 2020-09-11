#ifndef DEFINEPCAP_H
#define DEFINEPCAP_H

/* pacp文件构成：

  1： pacp = pcap_head + pkt_head + pket_data +  next->pkt_head  +  next->pkt_data :  ……
  2:  pkt_data=frame_head + ip_head + tcp_head +tcp_data

  //其中 pacp_head 中的 linktype 又决定了 frame_head 的类型


*/

typedef unsigned int  u_int32;
typedef unsigned short  u_int16;
typedef unsigned char u_int8;
typedef int int32;
/*
 Pcap文件头24B各字段说明：
 Magic：4B：0x1A 2B 3C 4D:用来标示文件的开始
 Major：2B，0x02 00:当前文件主要的版本号
 Minor：2B，0x04 00当前文件次要的版本号
 ThisZone：4B当地的标准时间；全零
 SigFigs：4B时间戳的精度；全零
 SnapLen：4B最大的存储长度
 LinkType：4B链路类型
 常用类型：
 　0            BSD loopback devices, except for later OpenBSD
 1            Ethernet, and Linux loopback devices
 6            802.5 Token Ring
 7            ARCnet
 8            SLIP
 9            PPP
 */
typedef struct pcap_header {
    u_int32 magic;
    u_int16 version_major;
    u_int16 version_minor;
    int32 thiszone;
    u_int32 sigfigs;
    u_int32 snaplen;
    u_int32 linktype;
}pcap_header;

/*
 Packet 包头和Packet数据组成
 字段说明：
 Timestamp：时间戳高位，精确到seconds
 Timestamp：时间戳低位，精确到microseconds
 Caplen：当前数据区的长度，即抓取到的数据帧长度，由此可以得到下一个数据帧的位置。
 Len：离线数据长度：网络中实际数据帧的长度，一般不大于caplen，多数情况下和Caplen数值相等。
 Packet 数据：即 Packet（通常就是链路层的数据帧）具体内容，长度就是Caplen，这个长度的后面，就是当前PCAP文件中存放的下一个Packet数据包，也就 是说：PCAP文件里面并没有规定捕获的Packet数据包之间有什么间隔字符串，下一组数据在文件中的起始位置。我们需要靠第一个Packet包确定。
 */

typedef struct  timestamp{
    u_int32 timestamp_s;
    u_int32 timestamp_ns;
}timestamp;

typedef struct pkt_header{
    timestamp ts;
    u_int32 capture_len;
    u_int32 len;

}pkt_header;


typedef struct Ethernet
{
    u_int8  DstMAC[6];   //目的MAC地址
    u_int8  SrcMAC[6];   //源MAC地址
    u_int16 FrameType;    //帧类型
} Ethernet;


typedef struct Linux_cooked_capture
{
    u_int16 package_type;
    u_int16 address_type;
    u_int16 address_length;
    u_int16 un_used[4];
    u_int16 FrameType; //帧类型
}Linux_cooked_capture;


typedef struct ip_header
{    //IP数据报头
    u_int8   Ver_HLen;       //版本+报头长度
    u_int8   TOS;            //服务类型
    u_int16  TotalLen;       //总长度
    u_int16  ID;     //标识
    u_int16  Flag_Segment;   //标志+片偏移
    u_int8   TTL;            //生存周期
    u_int8   Protocol;       //协议类型
    u_int16  Checksum;       //头部校验和
    u_int32  SrcIP;  //源IP地址
    u_int32  DstIP;  //目的IP地址
}ip_header ;


typedef struct tcp_header
{    //TCP数据报头
u_int16  SrcPort;    //源端口
u_int16  DstPort;    //目的端口
u_int32  SeqNO;  //序号
u_int32  AckNO;  //确认号
u_int8   HeaderLen;  //数据报头的长度(4 bit) + 保留(4 bit)
u_int8   Flags;  //标识TCP不同的控制消息
u_int16  Window;     //窗口大小
u_int16  Checksum;   //校验和
u_int16  UrgentPointer;  //紧急指针
}tcp_header ;

typedef struct udp_header
{    //TCP数据报头
u_int16  SrcPort;    //源端口
u_int16  DstPort;    //目的端口
u_int16  Len;  //数据报头的长度(4 bit) + 保留(4 bit)
u_int16  Checksum;   //校验和
}udp_header ;

typedef struct udp_data_header
{    //TCP数据报头
char data[32];
}udp_data_header ;

typedef struct option12
{//长度为12字节的选项 
        u_int16 nop;
        u_int16 nop2;
        u_int32 TSV;
        u_int32 TSER;
}option12;

typedef struct option24
{//长度为20 字节的选项 
        u_int16 nop;
        u_int16 nop2;
        u_int32 TSV;
        u_int32 TSER;
        u_int16 nop3;
        u_int16 nop4;
        u_int32 LeftEdge;//SACK
        u_int32 RightEdge;//SACK
}option24;

typedef struct option20
{//长度为12字节的选项 
        u_int32 MSS;
        u_int8 nop;
        u_int16 nop2;
        u_int8 WindowsScale;
        u_int16 SACKPermit;
        u_int16 nop3;
        u_int32 TSV;
        u_int32 TSER;
}option20;
#endif // DEFINEPCAP_H
